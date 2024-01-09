/**
 * Copyright (C) 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.intersmash.tools.provision.openshift.operator;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.assertj.core.util.Strings;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.OperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.OpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.CatalogSource;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.Subscription;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.fabric8.openshift.api.model.operatorhub.lifecyclemanager.v1.PackageChannel;
import io.fabric8.openshift.api.model.operatorhub.lifecyclemanager.v1.PackageManifest;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.CRDDescription;
import lombok.extern.slf4j.Slf4j;

/**
 * Provisioner for operator service subscription.
 * <p>
 * Use the following oc commands to track the progress of services.
 * oc get packagemanifest -n openshift-marketplace
 * oc get operatorgroups
 * oc get subscriptions
 * oc get installplans
 * oc get clusterserviceversion
 */
@Slf4j
public abstract class OperatorProvisioner<T extends OperatorApplication> implements OpenShiftProvisioner<T> {
	// cache the current csv and list of provided custom resource definitions
	static String currentCSV;
	final String packageManifestName;
	private CatalogSource catalogSource;
	private final T operatorApplication;
	private PackageManifest packageManifest;
	private String operatorChannel;
	protected FailFastCheck ffCheck = () -> false;
	private OpenShift adminShift;
	private OpenShiftBinary adminBinary;
	private Set<String> customResourceDefinitions;
	private static final RetryPolicy<PackageManifest> RETRY_POLICY_LOOKUP_MATCHING_PACKAGE_MANIFEST = RetryPolicy
			.<PackageManifest> builder()
			.handle(IllegalStateException.class)
			.withDelay(Duration.ofSeconds(5))
			.withMaxRetries(3)
			.build();
	public static final String INSTALLPLAN_APPROVAL_MANUAL = "Manual";

	public OperatorProvisioner(T operatorApplication, String packageManifestName) {
		this.operatorApplication = operatorApplication;
		this.packageManifestName = packageManifestName;
	}

	@Override
	public void configure() {
		this.adminShift = OpenShifts.admin();
		this.adminBinary = OpenShifts.adminBinary();

		// custom catalog source initialization
		catalogSource = initCatalogSource();

		// init package manifest used for data parsing
		this.packageManifest = Failsafe.with(RETRY_POLICY_LOOKUP_MATCHING_PACKAGE_MANIFEST).get(() -> initPackageManifest());

		// read operator spec from package manifest
		final String defaultChannel = packageManifest.getStatus().getDefaultChannel();

		// if there is no specific desired channel for Operator installation, let's use the default one.
		final String desiredChannel = this.getOperatorChannel();
		this.operatorChannel = (desiredChannel != null && !desiredChannel.isEmpty()) ? desiredChannel : defaultChannel;

		PackageChannel packageChannel = initPackageChannel(operatorChannel);

		currentCSV = packageChannel.getCurrentCSV();
		customResourceDefinitions = packageChannel.getCurrentCSVDesc().getCustomresourcedefinitions().getOwned().stream()
				.map(CRDDescription::getName).collect(Collectors.toSet());

		// introduce the operator a little, so we can trace the changes in case of a regression
		log.debug("Operator ID: " + packageManifestName);
		log.debug("Operator Default Channel: " + defaultChannel);
		log.debug("Operator Desired Channel: " + desiredChannel);
		log.debug("Operator Channel (used): " + operatorChannel);
		log.debug("Operator CSV: " + currentCSV);
		log.debug("Provided APIs: " + customResourceDefinitions.toString());
	}

	protected abstract String getOperatorCatalogSource();

	protected abstract String getOperatorIndexImage();

	protected abstract String getOperatorChannel();

	/**
	 * The CatalogSource is in the "openshift-marketplace" namespace by default;
	 * When a custom operator image must be used, then a custom CatalogSource will be created in the current namespace;
	 * @return namespace where the custom CatalogSource is located
	 */
	private String getCatalogSourceNamespace() {
		String namespace = IntersmashConfig.defaultOperatorCatalogSourceNamespace(); // default namespace for CatalogSources
		if (!Strings.isNullOrEmpty(getOperatorIndexImage())) {
			namespace = OpenShiftConfig.namespace();
		}
		return namespace;
	}

	/**
	 * Initialize a reference to a proper catalog source which will be used to pull the required operator image.
	 *
	 * The implementation covers the following scenarios:
	 * <ul>
	 *     <li>
	 *         neither {@code intersmash.*.operators.catalog_source} nor {@code intersmash.*.operators.index.image} are set -
	 *         in such case the the default <b>existing</b> catalog source for a given operator will be used, which is
	 *         located in the default namespace, i.e.: "openshift-marketplace".
	 *         <br>This is to cover the most common use case of leveraging default resources.
	 *     </li>
	 *
	 *     <li>
	 *         Only {@code intersmash.*.operators.catalog_source} is set - in this case as well the {@code CatalogSource}
	 *         is expected to <b>exist</b> already in the default namespace, i.e.: "openshift-marketplace".
	 *         <br>This is to cover the use case of a custom catalog source which must ne used but NOT be managed by
	 *         Intersmash.
	 *     </li>
	 *
	 *     <li>
	 *         Both {@code intersmash.*.operators.catalog_source} and {@code intersmash.*.operators.index.image} are defined
	 *         or just {@code intersmash.*.operators.index.image} is (i.e.: ".operators.catalog_source" might fallback to
	 *         the default "openshift-marketplace" namespace) - in this case a custom {@code CatalogSource} will be
	 *         created and managed by Intersmash in the working namespace and it will point to the custom operator image.
	 *         <br>This is to cover the use case of testing a development version of a given Operator</li>
	 * </ul>
	 *
	 * @return Existing or newly created {@link CatalogSource} instance.
	 */
	private CatalogSource initCatalogSource() {
		CatalogSource catalogSource;
		final String operatorCatalogSource = getOperatorCatalogSource();
		final String operatorCatalogSourceNamespace = getCatalogSourceNamespace();
		final String operatorIndexImage = getOperatorIndexImage();
		// if a custom index-image has been specified, then a custom CatalogSource has to be created
		if (!Strings.isNullOrEmpty(operatorIndexImage)) {
			String catalogSourceName;
			// default CatalogSource name must be differentiated per product adding a suffix to avoid conflicts
			if (IntersmashConfig.defaultOperatorCatalogSourceName().equalsIgnoreCase(operatorCatalogSource)) {
				catalogSourceName = String.format("%s-%s",
						operatorCatalogSource,
						getApplication().getName() == null ? // happens when Application is mocked
								getApplication().getClass().getSimpleName().substring(0, 4)
								: getApplication().getName())
						.toLowerCase();
			} else {
				catalogSourceName = operatorCatalogSource;
			}
			// create CatalogSource pointing to our custom IndexImage
			catalogSource = new CatalogSource(
					// a composite name is needed in order to avoid conflicts in case of multiple custom CatalogSources
					catalogSourceName,
					operatorCatalogSourceNamespace,
					"grpc",
					operatorIndexImage,
					catalogSourceName,
					"jboss-tests@redhat.com");
			try {
				adminBinary.execute("apply", "-f", catalogSource.save().getAbsolutePath());
				AtomicReference<String> catalogSourceStatus = new AtomicReference<>();
				new SimpleWaiter(() -> {
					// oc get CatalogSource redhat-operators -n openshift-marketplace -o template --template {{.status.connectionState.lastObservedState}}
					catalogSourceStatus.set(adminBinary.execute("get", "CatalogSource", catalogSource.getMetadata().getName(),
							"-n", operatorCatalogSourceNamespace,
							"-o", "template", "--template",
							"{{.status.connectionState.lastObservedState}}",
							"--ignore-not-found"));
					if (!Strings.isNullOrEmpty(catalogSourceStatus.get())) {
						log.info("CatalogSource {} status {}", catalogSource.getMetadata().getName(),
								catalogSourceStatus.get());
					}
					return !Strings.isNullOrEmpty(catalogSourceStatus.get())
							&& "READY".equalsIgnoreCase(catalogSourceStatus.get());
				}).reason(String.format("CatalogSource [%s] not found in namespace [%s]",
						catalogSource.getMetadata().getName(), operatorCatalogSourceNamespace))
						.level(Level.DEBUG)
						.failFast(getFailFastCheck())
						.waitFor();
			} catch (IOException e) {
				throw new RuntimeException(String.format("Failed to serialize the %s CatalogSource object into a yaml file.",
						catalogSource.getMetadata().getName()), e);
			}
		} else {
			// load CatalogSource by name from OpenShift cluster
			io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSource existing = OpenShifts
					.admin(IntersmashConfig.defaultOperatorCatalogSourceNamespace()).operatorHub()
					.catalogSources().list().getItems()
					.stream().filter(cs -> cs.getMetadata().getName().equalsIgnoreCase(operatorCatalogSource))
					.findFirst().orElseThrow(
							() -> new IllegalStateException(
									"Unable to retrieve CatalogSource " + operatorCatalogSource));
			catalogSource = new CatalogSource();
			catalogSource.load(existing);
		}
		return catalogSource;
	}

	private PackageManifest initPackageManifest() {
		log.debug("Listing package manifests belonging to: " + this.catalogSource.getMetadata().getName());
		List<PackageManifest> catalogSourcePackageManifests = adminShift.operatorHub().packageManifests().list().getItems()
				.stream()
				.filter(pm -> this.catalogSource.getMetadata().getName().equals(pm.getStatus().getCatalogSource()))
				.collect(Collectors.toList());
		catalogSourcePackageManifests.stream()
				.forEach(pm -> log.debug("---> " + pm.getMetadata().getName()));
		return catalogSourcePackageManifests.stream()
				.filter(pm -> this.packageManifestName.equals(pm.getMetadata().getName()))
				.findFirst().orElseThrow(
						() -> new IllegalStateException(
								"Unable to retrieve PackageManifest " + this.packageManifestName + " in CatalogSource "
										+ this.catalogSource.getMetadata().getName()));
	}

	private PackageChannel initPackageChannel(String channelName) {
		return packageManifest.getStatus().getChannels().stream()
				.filter(ch -> ch.getName().equals(channelName)).findFirst()
				.orElseThrow(
						() -> new IllegalStateException("Unable retrieve currentCSV for " + channelName + " in "
								+ packageManifest.getMetadata().getName()));
	}

	@Override
	public T getApplication() {
		return operatorApplication;
	}

	public String getPackageManifestName() {
		return packageManifestName;
	}

	/**
	 * List of custom resources definition names provided by operator. It is read from a cluster, so operator has to be
	 * subscribed in order to be able to retrieve this value.
	 *
	 * @return List of custom resources definition names provided by operator
	 */
	public Set<String> getCustomResourceDefinitions() {
		return Collections.unmodifiableSet(customResourceDefinitions);
	}

	/**
	 * Use OLM to subscribe to operator service from Operator Hub.
	 * Documentation:
	 * https://docs.openshift.com/container-platform/4.4/operators/olm-adding-operators-to-cluster.html#olm-installing-operator-from-operatorhub-using-cli_olm-adding-operators-to-a-cluster
	 */
	public void subscribe() {
		subscribe(null, null);
	}

	/**
	 * Use OLM to subscribe to operator service from Operator Hub.
	 * <p>
	 * All <code>env</code> entries are added to <code>Subscription</code> as environment variables, e.g.<br>
	 * <pre>{@code
	 *     apiVersion: operators.coreos.com/v1alpha1
	 *     kind: Subscription
	 *     spec:
	 *       channel: alpha
	 *       config:
	 *         env:
	 *         - name: <env-entry.key>
	 *           value: <env-entry.value>
	 *   }</pre>
	 * <p>
	 * Documentation: https://docs.openshift.com/container-platform/4.4/operators/olm-adding-operators-to-cluster.html#olm-installing-operator-from-operatorhub-using-cli_olm-adding-operators-to-a-cluster
	 * </p>
	 *
	 * @param installPlanApproval A value that will define whether the operator should apply an automatic or manual
	 *                            update to the deployed CRs
	 * @param envVariables        A set of environment variables that will be added to the {@link Subscription} definition
	 */
	public void subscribe(String installPlanApproval, Map<String, String> envVariables) {
		log.info("Subscribing the {} operator", packageManifestName);
		// oc get packagemanifest wildfly -o template --template {{.status.defaultChannel}}
		Subscription operatorSubscription = (envVariables == null || envVariables.isEmpty())
				? new Subscription(getCatalogSourceNamespace(), OpenShiftConfig.namespace(), getOperatorCatalogSource(),
						packageManifestName,
						operatorChannel, installPlanApproval)
				: new Subscription(getCatalogSourceNamespace(), OpenShiftConfig.namespace(), getOperatorCatalogSource(),
						packageManifestName,
						operatorChannel, installPlanApproval, envVariables);
		try {
			adminBinary.execute("apply", "-f", operatorSubscription.save().getAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException(String.format("Failed to serialize the %s subscription object into a yaml file.",
					operatorSubscription.getMetadata().getName()), e);
		}

		// if installPlanApproval is "Manual", approve InstallPlan manually
		if (INSTALLPLAN_APPROVAL_MANUAL.equalsIgnoreCase(installPlanApproval)) {
			AtomicReference<String> installPlan = new AtomicReference<>();
			// wait for installPlan to be attached to the subscription
			new SimpleWaiter(() -> {
				// oc get subscription rhsso-operator -o template --template="{{.status.installplan.name}}"
				installPlan.set(adminBinary.execute("get", "subscription", operatorSubscription.getMetadata().getName(),
						"-o", "template", "--template",
						"{{ if .status.installPlanRef.name }}{{.status.installPlanRef.name}}{{ end }}",
						"--ignore-not-found"));
				if (!Strings.isNullOrEmpty(installPlan.get())) {
					log.info("Pending approval on InstallPlan {} for Subscription {}", installPlan.get(),
							operatorSubscription.getMetadata().getName());
				}
				return !Strings.isNullOrEmpty(installPlan.get());
			}).reason(String.format("InstallPlan [%s] not found for subscription [%s]", installPlan.get(),
					operatorSubscription.getMetadata().getName()))
					.level(Level.DEBUG)
					.failFast(getFailFastCheck())
					.waitFor();
			String outcome = adminBinary.execute("patch", "InstallPlan", installPlan.get(),
					"--type", "merge", "--patch", "{\"spec\":{\"approved\":true}}");
			if (!Strings.isNullOrEmpty(outcome) && outcome.contains("patched")) {
				log.info("Approved InstallPlan {} for subscription {}",
						installPlan.get(),
						operatorSubscription.getMetadata().getName());
			} else {
				throw new IllegalStateException(
						"Failed to approve InstallPlan " + installPlan.get() + " for subscription " +
								operatorSubscription.getMetadata().getName() + ": " + outcome);
			}
		}
		// oc get clusterserviceversion wildfly-operator.v1.0.0 -o template --template {{.status.phase}}
		new SimpleWaiter(() -> {
			String clusterServicePhase = adminBinary.execute("get", "csvs", currentCSV, "-o", "template", "--template",
					"{{.status.phase}}", "--ignore-not-found");
			// this is the one where the operator image is pulled
			return clusterServicePhase != null && clusterServicePhase.equals("Succeeded");
		}).reason(String.format("Setup [%s] clusterserviceVersion", currentCSV))
				.level(Level.DEBUG)
				.failFast(getFailFastCheck())
				.waitFor();
		waitForOperatorPod();
	}

	/**
	 * <p>Waits for the operator's POD to be ready;</p>
	 *
	 * <p>The cluster service version resource contains the "app.kubernetes.io/name" label which is propagated to the
	 * operator's POD.
	 * E.g. when CSV contains:
	 * <code>
	 *   spec:
	 *     install:
	 *       spec:
	 *         deployments:
	 *           spec:
	 *             template:
	 *               metadata:
	 *                 labels:
	 *                   app.kubernetes.io/name: infinispan-operator
	 * </code>
	 *
	 * then operator's POD contains the same label:
	 * <code>
	 *   labels:
	 *     app.kubernetes.io/name: infinispan-operator
	 * </code>
	 * </p>
	 *
	 * <p>
	 * The command issued to find the "app.kubernetes.io/name" label is e.g.:
	 * <code>
	 *  $ oc get csvs datagrid-operator.v8.1.0 -o template --template '{{range .spec.install.spec.deployments}}{{printf "%d|%s\n" .spec.replicas .spec.template.metadata.labels.name}}{{end}}'
	 *    1|infinispan-operator-alm-owned
	 * </code>
	 * </p>
	 */
	protected void waitForOperatorPod() {
		final String metadataNameLabelLegacyName = "name";
		final String metadataNameLabelName = "app.kubernetes.io/name";
		String[] operatorSpecs = adminBinary.execute(
				"get",
				"csvs",
				currentCSV,
				"-o",
				"template",
				"--template",
				String.format(
						"{{range .spec.install.spec.deployments}}{{.spec.replicas }}|{{if .spec.template.metadata.labels.name}}%s|{{ .spec.template.metadata.labels.name }}{{else}}%s|{{index .spec.template.metadata.labels \"app.kubernetes.io/name\"}}{{end}}{{end}}",
						metadataNameLabelLegacyName, metadataNameLabelName))
				.split(System.lineSeparator());
		for (String spec : operatorSpecs) {
			String[] operatorSpec = spec.split("\\|");
			if (operatorSpec.length != 3) {
				throw new IllegalStateException("Failed to get operator deployment spec from csvs!");
			}
			OpenShiftWaiters.get(openShift, getFailFastCheck())
					.areExactlyNPodsReady(Integer.valueOf(operatorSpec[0]), operatorSpec[1], operatorSpec[2]).level(Level.DEBUG)
					.waitFor();
		}
	}

	protected FailFastCheck getFailFastCheck() {
		return () -> false;
	}

	/**
	 * Use OLM to un-subscribe to operator service from Operator Hub.
	 * Documentation: https://docs.openshift.com/container-platform/4.4/operators/olm-deleting-operators-from-cluster.html#olm-deleting-operator-from-a-cluster-using-cli_olm-deleting-operators-from-a-cluster
	 */
	public void unsubscribe() {
		adminBinary.execute("delete", "subscription", packageManifestName, "--ignore-not-found");
		adminBinary.execute("delete", "csvs", currentCSV, "--ignore-not-found");
		for (String customResource : getCustomResourceDefinitions()) {
			final String crds = adminBinary.execute("get", "crd", customResource, "--ignore-not-found");
			if (crds != null && !crds.isEmpty()) {
				log.info("CRD: {} is still defined on the cluster", customResource);
			}
		}
	}

	@Override
	public URL getURL() {
		throw new UnsupportedOperationException("To be implemented!");
	}

	/**
	 * @return true is there is an active subscription for the current operator
	 */
	protected boolean isSubscribed() {
		return !Strings.isNullOrEmpty(adminBinary.execute("get", "subscription", packageManifestName,
				"-o", "template", "--template",
				"{{ .status.state }}",
				"--ignore-not-found"));
	}

	protected static String getCurrentCSV() {
		return currentCSV;
	}

	protected OpenShiftBinary getAdminBinary() {
		return adminBinary;
	}

	@Override
	public void dismiss() {
		// let's remove any custom catalog source
		if (Arrays.stream(IntersmashConfig.getKnownCatalogSources())
				.noneMatch(cs -> this.catalogSource.getMetadata().getName().equals(cs))) {
			adminBinary.execute("delete", "catalogsource", catalogSource.getMetadata().getName(), "--ignore-not-found");
		}
	}
}
