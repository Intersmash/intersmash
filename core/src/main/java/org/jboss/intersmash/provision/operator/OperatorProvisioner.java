/*
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
package org.jboss.intersmash.provision.operator;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.assertj.core.util.Strings;
import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.operator.OperatorApplication;
import org.jboss.intersmash.provision.Provisioner;
import org.jboss.intersmash.provision.k8s.Scalable;
import org.jboss.intersmash.provision.olm.Subscription;
import org.slf4j.event.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.NamespacedKubernetesClientAdapter;
import io.fabric8.openshift.api.model.operatorhub.lifecyclemanager.v1.PackageChannel;
import io.fabric8.openshift.api.model.operatorhub.lifecyclemanager.v1.PackageManifest;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.CRDDescription;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSource;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSourceBuilder;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSourceList;
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
public abstract class OperatorProvisioner<A extends OperatorApplication, C extends NamespacedKubernetesClient>
		implements Provisioner<A>, Scalable {
	// cache the current csv and list of provided custom resource definitions
	protected static String currentCSV;
	protected final String packageManifestName;
	private CatalogSource catalogSource;
	private final A operatorApplication;
	private PackageManifest packageManifest;
	private String operatorChannel;
	private Set<String> customResourceDefinitions;
	private static final RetryPolicy<PackageManifest> RETRY_POLICY_LOOKUP_MATCHING_PACKAGE_MANIFEST = RetryPolicy
			.<PackageManifest> builder()
			.handle(IllegalStateException.class)
			.withDelay(Duration.ofSeconds(5))
			.withMaxRetries(3)
			.build();
	public static final String INSTALLPLAN_APPROVAL_MANUAL = "Manual";

	public OperatorProvisioner(A operatorApplication, String packageManifestName) {
		this.operatorApplication = operatorApplication;
		this.packageManifestName = packageManifestName;
	}

	protected abstract NamespacedKubernetesClientAdapter<C> client();

	public List<Pod> getPods() {
		return this.client().pods().inNamespace(this.client().getNamespace()).list().getItems();
	}

	protected List<Pod> getLabeledPods(final String labelName, final String labelValue) {
		return this.client().pods().inNamespace(this.client().getNamespace()).withLabel(labelName, labelValue).list()
				.getItems();
	}

	protected abstract String execute(String... args);

	public PackageManifest getPackageManifest(String operatorName, String operatorNamespace) {
		try {
			return new ObjectMapper()
					.readValue(this.execute("get", "packagemanifest", operatorName, "-n", operatorNamespace, "-o", "json"),
							PackageManifest.class);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Couldn't deserialize package manifest data: " + operatorName, e);
		}
	}

	public List<CatalogSource> getCatalogSources(final String catalogSourceNamespace) {
		try {
			return new ObjectMapper().readValue(this.execute("get", "catsrc", "-n", catalogSourceNamespace, "-o", "json"),
					CatalogSourceList.class).getItems();
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Couldn't deserialize catalog source data: " + catalogSourceNamespace, e);
		}
	}

	public CatalogSource getCatalogSource(final String catalogSourceNamespace, final String catalogSourceName) {
		io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSource loaded = getCatalogSources(catalogSourceNamespace)
				.stream()
				.filter(cs -> cs.getMetadata().getName().equalsIgnoreCase(catalogSourceName))
				.findFirst().orElseThrow(
						() -> new IllegalStateException(
								"Unable to retrieve CatalogSource " + catalogSourceName));
		CatalogSource catalogSource = new CatalogSource();
		catalogSource.setMetadata(loaded.getMetadata());
		catalogSource.setSpec(loaded.getSpec());
		return catalogSource;
	}

	@Override
	public void configure() {

		// custom catalog source initialization
		catalogSource = initCatalogSource();

		// init package manifest used for data parsing
		this.packageManifest = Failsafe.with(RETRY_POLICY_LOOKUP_MATCHING_PACKAGE_MANIFEST).get(this::initPackageManifest);

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
	 * The CatalogSource is in the "openshift-marketplace" namespace by default on OpenShift, in the "olm" one on K8s.
	 * When a custom operator image must be used, then a custom CatalogSource will be created in the current namespace.
	 *
	 * @return namespace where the custom CatalogSource is located
	 */
	private String getCatalogSourceNamespace() {
		String namespace = IntersmashConfig.defaultOperatorCatalogSourceNamespace(); // default namespace for CatalogSources
		if (!Strings.isNullOrEmpty(getOperatorIndexImage())) {
			namespace = this.client().getNamespace();
		}
		return namespace;
	}

	/**
	 * The CatalogSource is in the "openshift-marketplace" namespace by default on OpenShift, in the "olm" one on K8s.
	 * When a custom operator image must be used, then a custom CatalogSource will be created in the current namespace.
	 *
	 * @return namespace where the custom CatalogSource is located
	 */
	protected String getTargetNamespace() {
		return this.client().getNamespace();
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
						getApplication().getName())
						.toLowerCase();
			} else {
				catalogSourceName = operatorCatalogSource;
			}
			// create CatalogSource pointing to our custom IndexImage
			catalogSource = new CatalogSourceBuilder()
					.withNewMetadata()
					.withName(catalogSourceName)
					.withNamespace(operatorCatalogSourceNamespace)
					.endMetadata()
					.withNewSpec()
					.withSourceType("grpc")
					.withImage(operatorIndexImage)
					.withDisplayName(catalogSourceName)
					.withPublisher("intersmash@intersmash.org")
					.endSpec()
					.build();
			try {
				this.execute("apply", "-f",
						new org.jboss.intersmash.provision.olm.CatalogSource()
								.load(catalogSource)
								.save()
								.getAbsolutePath());
				AtomicReference<String> catalogSourceStatus = new AtomicReference<>();
				new SimpleWaiter(() -> {
					// oc get CatalogSource redhat-operators -n openshift-marketplace -o template --template {{.status.connectionState.lastObservedState}}
					catalogSourceStatus.set(this.execute("get", "CatalogSource", catalogSource.getMetadata().getName(),
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
			// load CatalogSource by name from cluster
			catalogSource = getCatalogSource(IntersmashConfig.defaultOperatorCatalogSourceNamespace(), operatorCatalogSource);
		}
		return catalogSource;
	}

	private PackageManifest initPackageManifest() {
		log.debug("Listing package manifests belonging to: " + this.catalogSource.getMetadata().getName());
		PackageManifest catalogSourcePackageManifest = this.getPackageManifest(this.packageManifestName,
				this.getCatalogSourceNamespace());
		if (catalogSourcePackageManifest == null) {
			throw new IllegalStateException(
					"Unable to retrieve PackageManifest " + this.packageManifestName + " in CatalogSource "
							+ this.catalogSource.getMetadata().getName());
		}
		log.debug("---> " + catalogSourcePackageManifest.getMetadata().getName());
		return catalogSourcePackageManifest;
	}

	private PackageChannel initPackageChannel(String channelName) {
		return packageManifest.getStatus().getChannels().stream()
				.filter(ch -> ch.getName().equals(channelName)).findFirst()
				.orElseThrow(
						() -> new IllegalStateException("Unable retrieve currentCSV for " + channelName + " in "
								+ packageManifest.getMetadata().getName()));
	}

	@Override
	public A getApplication() {
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
		final String catalogSourceNamespace = getCatalogSourceNamespace();
		final String targetNamespace = getTargetNamespace();
		Subscription operatorSubscription = (envVariables == null || envVariables.isEmpty())
				? new Subscription(catalogSourceNamespace, targetNamespace, getOperatorCatalogSource(),
						packageManifestName,
						operatorChannel, installPlanApproval)
				: new Subscription(catalogSourceNamespace, targetNamespace, getOperatorCatalogSource(),
						packageManifestName,
						operatorChannel, installPlanApproval, envVariables);
		try {
			this.execute("apply", "-f", operatorSubscription.save().getAbsolutePath());
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
				installPlan.set(this.execute("get", "subscription", operatorSubscription.getMetadata().getName(),
						"-o", "template", "--template",
						"{{ if .status.installPlanRef.name }}{{.status.installPlanRef.name}}{{ end }}",
						"--ignore-not-found"));
				if (!Strings.isNullOrEmpty(installPlan.get())) {
					log.info("Pending approval on InstallPlan {} for Subscription {}", installPlan.get(),
							operatorSubscription.getMetadata().getName());
				}
				return !Strings.isNullOrEmpty(installPlan.get());
			}).reason(
					String.format("InstallPlan not found for subscription [%s]", operatorSubscription.getMetadata().getName()))
					.level(Level.DEBUG)
					.failFast(getFailFastCheck())
					.waitFor();
			String outcome = this.execute("patch", "InstallPlan", installPlan.get(),
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
			String clusterServicePhase = this.execute("get", "csvs", currentCSV, "-o", "template", "--template",
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
		String[] operatorSpecs = this.execute(
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
			BooleanSupplier bs = () -> this.client().inNamespace(this.getTargetNamespace()).pods().list().getItems().stream()
					.filter(p -> !com.google.common.base.Strings.isNullOrEmpty(p.getMetadata().getLabels().get(operatorSpec[1]))
							&& p.getMetadata().getLabels().get(operatorSpec[1]).equals(operatorSpec[2])
							&& p.getStatus().getContainerStatuses().size() > 0
							&& p.getStatus().getContainerStatuses().stream().allMatch(ContainerStatus::getReady))
					.collect(Collectors.toList()).size() == Integer.valueOf(operatorSpec[0]);
			String reason = "Waiting for exactly " + Integer.valueOf(operatorSpec[0]) + " pods with label \"" + operatorSpec[1]
					+ "\"="
					+ operatorSpec[2] + " to be ready.";
			new SimpleWaiter(bs, TimeUnit.MINUTES, 2, reason)
					.level(Level.DEBUG)
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
		removeSubscription();
		removeClusterServiceVersion();
		for (String customResource : getCustomResourceDefinitions()) {
			final String crds = this.execute("get", "crd", customResource, "--ignore-not-found");
			if (crds != null && !crds.isEmpty()) {
				log.info("CRD: {} is still defined on the cluster", customResource);
			}
		}
	}

	protected void removeClusterServiceVersion() {
		this.execute("delete", "csvs", currentCSV, "--ignore-not-found");
	}

	protected void removeSubscription() {
		this.execute("delete", "subscription", packageManifestName, "--ignore-not-found");
	}

	public String getCurrentCSV() {
		return currentCSV;
	}

	@Override
	public void dismiss() {
		// let's remove any custom catalog source
		if (Arrays.stream(IntersmashConfig.getKnownCatalogSources())
				.noneMatch(cs -> this.catalogSource.getMetadata().getName().equals(cs))) {
			this.execute("delete", "catalogsource", catalogSource.getMetadata().getName(), "--ignore-not-found");
		}
	}
}
