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
package org.jboss.intersmash.provision.openshift;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Strings;
import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.openshift.KeycloakOperatorApplication;
import org.jboss.intersmash.provision.openshift.operator.OperatorProvisioner;
import org.jboss.intersmash.util.tls.CertificatesUtils;
import org.keycloak.k8s.v2alpha1.Keycloak;
import org.keycloak.k8s.v2alpha1.KeycloakOperatorKeycloakList;
import org.keycloak.k8s.v2alpha1.KeycloakOperatorRealmImportList;
import org.keycloak.k8s.v2alpha1.KeycloakRealmImport;
import org.keycloak.k8s.v2alpha1.keycloakspec.Http;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import lombok.NonNull;

/**
 * Keycloak operator provisioner
 */
public class KeycloakOperatorProvisioner extends OperatorProvisioner<KeycloakOperatorApplication> {
	private static final String KEYCLOAK_RESOURCE = "keycloaks.k8s.keycloak.org";
	private static final String KEYCLOAK_REALM_IMPORT_RESOURCE = "keycloakrealmimports.k8s.keycloak.org";
	private static NonNamespaceOperation<Keycloak, KeycloakOperatorKeycloakList, Resource<Keycloak>> KEYCLOAK_CUSTOM_RESOURCE_CLIENT;
	private static NonNamespaceOperation<KeycloakRealmImport, KeycloakOperatorRealmImportList, Resource<KeycloakRealmImport>> KEYCLOAK_REALM_IMPORT_CUSTOM_RESOURCE_CLIENT;

	public NonNamespaceOperation<Keycloak, KeycloakOperatorKeycloakList, Resource<Keycloak>> keycloakClient() {
		if (KEYCLOAK_CUSTOM_RESOURCE_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(KEYCLOAK_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(KEYCLOAK_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_RESOURCE, OPERATOR_ID));
			}
			MixedOperation<Keycloak, KeycloakOperatorKeycloakList, Resource<Keycloak>> crClient = OpenShifts
					.master().newHasMetadataOperation(crdc, Keycloak.class, KeycloakOperatorKeycloakList.class);
			KEYCLOAK_CUSTOM_RESOURCE_CLIENT = crClient.inNamespace(OpenShiftConfig.namespace());
		}
		return KEYCLOAK_CUSTOM_RESOURCE_CLIENT;
	}

	public NonNamespaceOperation<KeycloakRealmImport, KeycloakOperatorRealmImportList, Resource<KeycloakRealmImport>> keycloakRealmImportClient() {
		if (KEYCLOAK_REALM_IMPORT_CUSTOM_RESOURCE_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(KEYCLOAK_REALM_IMPORT_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(KEYCLOAK_REALM_IMPORT_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_REALM_IMPORT_RESOURCE, OPERATOR_ID));
			}
			MixedOperation<KeycloakRealmImport, KeycloakOperatorRealmImportList, Resource<KeycloakRealmImport>> crClient = OpenShifts
					.master()
					.newHasMetadataOperation(crdc, KeycloakRealmImport.class, KeycloakOperatorRealmImportList.class);
			KEYCLOAK_REALM_IMPORT_CUSTOM_RESOURCE_CLIENT = crClient.inNamespace(OpenShiftConfig.namespace());
		}
		return KEYCLOAK_REALM_IMPORT_CUSTOM_RESOURCE_CLIENT;
	}

	private static final String OPERATOR_ID = IntersmashConfig.keycloakOperatorPackageManifest();
	protected FailFastCheck ffCheck = () -> false;

	public KeycloakOperatorProvisioner(@NonNull KeycloakOperatorApplication application) {
		super(application, OPERATOR_ID);
	}

	public static String getOperatorId() {
		return OPERATOR_ID;
	}

	@Override
	protected String getOperatorCatalogSource() {
		return IntersmashConfig.keycloakOperatorCatalogSource();
	}

	@Override
	protected String getOperatorIndexImage() {
		return IntersmashConfig.keycloakOperatorIndexImage();
	}

	@Override
	protected String getOperatorChannel() {
		return IntersmashConfig.keycloakOperatorChannel();
	}

	@Override
	public void subscribe() {
		if (Strings.isNullOrEmpty(IntersmashConfig.keycloakImageURL())) {
			super.subscribe();
		} else {
			subscribe(
					INSTALLPLAN_APPROVAL_MANUAL,
					Map.of(
							// Custom Keycloak image to be used: overrides the Keycloak image at the operator level: all
							// Keycloak instances will be spun out of this image
							// e.g. OPERATOR_KEYCLOAK_IMAGE=quay.io/keycloak/keycloak:21.1.1 --> operator.keycloak.image
							"OPERATOR_KEYCLOAK_IMAGE", IntersmashConfig.keycloakImageURL()));
		}
	}

	@Override
	public void deploy() {
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				getApplication().getName());
		// Keycloak Operator codebase contains the name of the Keycloak image to deploy: user can override Keycloak image to
		// deploy using environment variables in Keycloak Operator Subscription
		subscribe();

		// Custom Keycloak image to be used: overrides the Keycloak image at the Keycloak level: just this Keycloak
		// instance will be spun out of this image
		if (!Strings.isNullOrEmpty(IntersmashConfig.keycloakImageURL())) {
			getApplication().getKeycloak().getSpec().setImage(IntersmashConfig.keycloakImageURL());
		}

		// create keys/certificates and add them to the Keycloak resource:
		// TODO: https://www.keycloak.org/operator/basic-deployment or ~/projects/keycloak/docs/guides/operator/basic-deployment.adoc
		if (getApplication().getKeycloak().getSpec().getHttp() == null
				|| getApplication().getKeycloak().getSpec().getHttp().getTlsSecret() == null) {
			// create key, certificate and tls secret
			String tlsSecretName = getApplication().getKeycloak().getMetadata().getName() + "-tls-secret";
			CertificatesUtils.CertificateAndKey certificateAndKey = CertificatesUtils
					.generateSelfSignedCertificateAndKey(
							getApplication().getKeycloak().getSpec().getHostname().getHostname().replaceFirst("[.].*$", ""),
							tlsSecretName);
			// add config to keycloak
			if (getApplication().getKeycloak().getSpec().getHttp() == null) {
				Http http = new Http();
				http.setTlsSecret(certificateAndKey.tlsSecret.getMetadata().getName());
				getApplication().getKeycloak().getSpec().setHttp(http);
			} else {
				getApplication().getKeycloak().getSpec().getHttp()
						.setTlsSecret(certificateAndKey.tlsSecret.getMetadata().getName());
			}
		}

		// 1. check externalDatabase exists
		if (getApplication().getKeycloak().getSpec().getDb() != null) {
			// 2. Service "spec.db.host" must be installed beforehand
			new SimpleWaiter(() -> OpenShiftProvisioner.openShift
					.getService(getApplication().getKeycloak().getSpec().getDb().getHost()) != null)
					.level(Level.DEBUG).waitFor();
		}

		// create custom resources
		keycloakClient().createOrReplace(getApplication().getKeycloak());
		if (getApplication().getKeycloakRealmImports().size() > 0) {
			getApplication().getKeycloakRealmImports().stream()
					.forEach((i) -> keycloakRealmImportClient().resource(i).create());
		}

		// Wait for Keycloak (and PostgreSQL) to be ready
		waitFor(getApplication().getKeycloak());
		// wait for all resources to be ready
		waitForKeycloakResourceReadiness();
		// check that route is up, only if there's a valid external URL available
		URL externalUrl = getURL();
		if ((getApplication().getKeycloak().getSpec().getInstances() > 0) && (externalUrl != null)) {
			WaitersUtil.routeIsUp(externalUrl.toExternalForm())
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	public void waitFor(Keycloak keycloak) {
		Long replicas = keycloak.getSpec().getInstances();
		if (replicas > 0) {
			// wait for >= 1 pods with label controller-revision-hash=keycloak-d86bb6ddc
			String controllerRevisionHash = getStatefulSet().getStatus().getUpdateRevision();
			OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
					.areExactlyNPodsReady(replicas.intValue(), "controller-revision-hash",
							controllerRevisionHash)
					.waitFor();
		}
	}

	public void waitFor(KeycloakRealmImport realmImport) {
		new SimpleWaiter(() -> {
			Resource<KeycloakRealmImport> res = keycloakRealmImportClient().withName(realmImport.getMetadata().getName());
			if (Objects.nonNull(res)
					&& Objects.nonNull(res.get())
					&& Objects.nonNull(res.get().getStatus())) {
				KeycloakRealmImport imp = res.get();
				return imp.getStatus().getConditions().stream().filter(
						cond -> cond.getStatus() != null
								&& "Done".equalsIgnoreCase(cond.getType())
								&& com.google.common.base.Strings.isNullOrEmpty(cond.getMessage()))
						.count() == 1
						&&
						imp.getStatus().getConditions().stream().filter(
								cond -> cond.getStatus() == null
										&& "HasErrors".equalsIgnoreCase(cond.getType())
										&& com.google.common.base.Strings.isNullOrEmpty(cond.getMessage()))
								.count() == 1;
			}
			return false;
		}).reason("Wait for KeycloakRealmImport resource to be imported").level(Level.DEBUG).waitFor();
	}

	private void waitForKeycloakResourceReadiness() {
		new SimpleWaiter(
				() -> keycloak().get().getStatus().getConditions().stream().anyMatch(
						condition -> "Ready".equalsIgnoreCase(condition.getType())
								&& condition.getStatus() != null))
				.reason("Wait for Keycloak resource to be ready").level(Level.DEBUG).waitFor();
		if (getApplication().getKeycloakRealmImports().size() > 0)
			new SimpleWaiter(() -> keycloakRealmImports().stream().allMatch(
					realmImport -> realmImport.getStatus().getConditions().stream().anyMatch(
							condition -> "Done".equalsIgnoreCase(condition.getType())
									&& condition.getStatus() != null)))
					.reason("Wait for KeycloakRealmImports to be done.").level(Level.DEBUG).waitFor();
	}

	/**
	 * Get a reference to keycloak object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link org.jboss.intersmash.provision.openshift.operator.keycloak.keycloak.Keycloak} resource definition
	 */
	public Resource<Keycloak> keycloak() {
		return keycloakClient()
				.withName(getApplication().getKeycloak().getMetadata().getName());
	}

	public List<KeycloakRealmImport> keycloakRealmImports() {
		return keycloakRealmImportClient().list().getItems()
				.stream().filter(
						realm -> getApplication().getKeycloakRealmImports().stream().map(
								ri -> ri.getMetadata().getName())
								.anyMatch(riName -> riName.equalsIgnoreCase(realm.getMetadata().getName())))
				.collect(Collectors.toList());
	}

	/**
	 * @return the underlying StatefulSet which provisions the cluster
	 */
	private StatefulSet getStatefulSet() {
		final String STATEFUL_SET_NAME = getApplication().getKeycloak().getMetadata().getName();
		new SimpleWaiter(
				() -> Objects.nonNull(OpenShiftProvisioner.openShift.getStatefulSet(STATEFUL_SET_NAME)))
				.reason(
						MessageFormat.format(
								"Waiting for StatefulSet \"{0}\" to be created for Keycloak \"{1}\".",
								STATEFUL_SET_NAME,
								getApplication().getKeycloak().getMetadata().getName()))
				.level(Level.DEBUG).timeout(60000L).waitFor();
		return OpenShiftProvisioner.openShift.getStatefulSet(STATEFUL_SET_NAME);
	}

	@Override
	public void undeploy() {
		keycloakRealmImports()
				.forEach(
						keycloakRealm -> keycloakRealmImportClient()
								.withName(keycloakRealm.getMetadata().getName())
								.withPropagationPolicy(DeletionPropagation.FOREGROUND)
								.delete());
		new SimpleWaiter(
				() -> keycloakRealmImportClient().list().getItems().size() == 0)
				.reason("Wait for all keycloakRealmImports instances to be deleted.").level(Level.DEBUG).waitFor();
		keycloak().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		new SimpleWaiter(() -> keycloakClient().list().getItems().size() == 0)
				.reason("Wait for Keycloak instances to be deleted.").level(Level.DEBUG).waitFor();

		// wait for 0 pods
		OpenShiftWaiters.get(OpenShiftProvisioner.openShift, () -> false)
				.areExactlyNPodsReady(0, "app", getApplication().getKeycloak().getKind().toLowerCase()).level(Level.DEBUG)
				.waitFor();
		unsubscribe();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		String controllerRevisionHash = getStatefulSet().getStatus().getUpdateRevision();
		Keycloak tmpKeycloak = keycloak().get();
		Long originalReplicas = tmpKeycloak.getSpec().getInstances();
		tmpKeycloak.getSpec().setInstances(Integer.toUnsignedLong(replicas));
		keycloak().replace(tmpKeycloak);
		if (wait) {
			OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
					.areExactlyNPodsReady(replicas, "controller-revision-hash", controllerRevisionHash)
					.level(Level.DEBUG)
					.waitFor();
		}
		new SimpleWaiter(
				() -> keycloak().get().getStatus().getConditions().stream().anyMatch(
						condition -> "Ready".equalsIgnoreCase(condition.getType())
								&& condition.getStatus() != null))
				.reason("Wait for Keycloak resource to be ready").level(Level.DEBUG).waitFor();
		// check that route is up
		if (originalReplicas == 0 && replicas > 0) {
			WaitersUtil.routeIsUp(getURL().toExternalForm())
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	@Override
	public List<Pod> getPods() {
		String STATEFUL_SET_NAME = getApplication().getKeycloak().getMetadata().getName();
		StatefulSet statefulSet = OpenShiftProvisioner.openShift.getStatefulSet(STATEFUL_SET_NAME);
		return Objects.nonNull(statefulSet)
				? OpenShiftProvisioner.openShift.getLabeledPods("controller-revision-hash",
						statefulSet.getStatus().getUpdateRevision())
				: Lists.emptyList();
	}

	@Override
	public URL getURL() {
		String host = OpenShiftProvisioner.openShift.routes().list().getItems()
				.stream().filter(
						route -> route.getMetadata().getName().startsWith(
								keycloak().get().getMetadata().getName())
								&&
								route.getMetadata().getLabels().entrySet()
										.stream().filter(
												label -> label.getKey().equalsIgnoreCase("app")
														&&
														label.getValue().equalsIgnoreCase(
																keycloak().get().getMetadata().getLabels().get("app")))
										.count() == 1

				).findFirst()
				.orElseThrow(() -> new RuntimeException(
						String.format("No route for Keycloak %s!", keycloak().get().getMetadata().getName())))
				.getSpec().getHost();
		try {
			return Strings.isNullOrEmpty(host) ? null : new URL(String.format("https://%s", host));
		} catch (MalformedURLException e) {
			throw new RuntimeException(String.format("Keycloak operator External URL \"%s\" is malformed.", host), e);
		}
	}
}
