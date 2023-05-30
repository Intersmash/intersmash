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
package org.jboss.intersmash.tools.provision.openshift;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Strings;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.KeycloakQuarkusOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.operator.OperatorProvisioner;
import org.jboss.intersmash.tools.util.tls.CertificatesUtils;
import org.keycloak.k8s.v2alpha1.Keycloak;
import org.keycloak.k8s.v2alpha1.KeycloakRealmImport;
import org.keycloak.k8s.v2alpha1.keycloakspec.Http;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import lombok.NonNull;

/**
 * Keycloak operator provisioner
 */
public class KeycloakQuarkusOperatorProvisioner extends OperatorProvisioner<KeycloakQuarkusOperatorApplication> {

	//private MixedOperation<KeycloakRealmImport, KubernetesResourceList<KeycloakRealmImport>, Resource<KeycloakRealmImport>> keycloakRealmImportClient;
	//private MixedOperation<Keycloak, KubernetesResourceList<Keycloak>, Resource<Keycloak>> keycloakClient;
	private static final String OPERATOR_ID = IntersmashConfig.keycloakQuarkusOperatorPackageManifest();
	protected FailFastCheck ffCheck = () -> false;

	public KeycloakQuarkusOperatorProvisioner(@NonNull KeycloakQuarkusOperatorApplication application) {
		super(application, OPERATOR_ID);
	}

	public static String getOperatorId() {
		return OPERATOR_ID;
	}

	@Override
	protected String getOperatorCatalogSource() {
		return IntersmashConfig.keycloakQuarkusOperatorCatalogSource();
	}

	@Override
	protected String getOperatorIndexImage() {
		return IntersmashConfig.keycloakQuarkusOperatorIndexImage();
	}

	@Override
	protected String getOperatorChannel() {
		return IntersmashConfig.keycloakQuarkusOperatorChannel();
	}

	@Override
	public void subscribe() {
		if (Strings.isNullOrEmpty(IntersmashConfig.keycloakQuarkusImageURL())) {
			super.subscribe();
		} else {
			// RELATED_IMAGE_RHSSO_OPENJ9 and RELATED_IMAGE_RHSSO_OPENJDK, determine the final value for RELATED_IMAGE_RHSSO
			subscribe(
					INSTALLPLAN_APPROVAL_MANUAL,
					// TODO: check if these env variables still make sense in the new quarkus operator
					Map.of(
							// Custom Keycloak image to be used: overrides the Keycloak image at the operator level: all
							// Keycloak instances will be spun out of this image
							// e.g. OPERATOR_KEYCLOAK_IMAGE=quay.io/keycloak/keycloak:21.1.1 --> operator.keycloak.image
							"OPERATOR_KEYCLOAK_IMAGE", IntersmashConfig.keycloakQuarkusImageURL()
					//		"PROFILE", "RHSSO"
					));
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
		if (!Strings.isNullOrEmpty(IntersmashConfig.keycloakQuarkusImageURL())) {
			getApplication().getKeycloak().getSpec().setImage(IntersmashConfig.keycloakQuarkusImageURL());
		}

		// create keys/certificates and add them to the Keycloak resource:
		// TODO: https://www.keycloak.org/operator/basic-deployment or ~/projects/keycloak/docs/guides/operator/basic-deployment.adoc
		if (getApplication().getKeycloak().getSpec().getHttp() == null
				|| getApplication().getKeycloak().getSpec().getHttp().getTlsSecret() == null) {
			// create key, certificate and tls secret
			String tlsSecretName = getApplication().getKeycloak().getMetadata().getName() + "-tls-secret";
			CertificatesUtils.CertificateAndKey certificateAndKey = CertificatesUtils
					.generateSelfSignedCertificateAndKey(getApplication().getKeycloak().getSpec().getHostname().getHostname(),
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
		if (getApplication().getKeycloakRealmImports().size() > 0)
			keycloakRealmImportClient()
					.createOrReplace(getApplication().getKeycloakRealmImports().stream().toArray(KeycloakRealmImport[]::new));

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
			if (res != null && res.get() != null) {
				KeycloakRealmImport imp = res.get();
				return imp.getStatus().getConditions().stream().filter(
						cond -> cond.getStatus()
								&& "Done".equalsIgnoreCase(cond.getType())
								&& com.google.common.base.Strings.isNullOrEmpty(cond.getMessage()))
						.count() == 1
						&&
						imp.getStatus().getConditions().stream().filter(
								cond -> !cond.getStatus()
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
								&& condition.getStatus()))
				.reason("Wait for Keycloak resource to be ready").level(Level.DEBUG).waitFor();
		if (getApplication().getKeycloakRealmImports().size() > 0)
			new SimpleWaiter(() -> keycloakRealmImports().stream().allMatch(
					realmImport -> realmImport.getStatus().getConditions().stream().anyMatch(
							condition -> "Done".equalsIgnoreCase(condition.getType())
									&& condition.getStatus())))
					.reason("Wait for KeycloakRealmImports to be done.").level(Level.DEBUG).waitFor();
	}

	/**
	 * Get a reference to keycloak object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.Keycloak} resource definition
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
		String STATEFUL_SET_NAME = getApplication().getKeycloak().getMetadata().getName();
		StatefulSet statefulSet = OpenShiftProvisioner.openShift.getStatefulSet(STATEFUL_SET_NAME);
		if (Objects.isNull(statefulSet)) {
			throw new IllegalStateException(String.format(
					"Impossible to find StatefulSet with name=\"%s\"!",
					STATEFUL_SET_NAME));
		}
		return statefulSet;
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
								&& condition.getStatus()))
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

	public NonNamespaceOperation<Keycloak, KubernetesResourceList<Keycloak>, Resource<Keycloak>> keycloakClient() {
		try (KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			MixedOperation<Keycloak, KubernetesResourceList<Keycloak>, Resource<Keycloak>> keycloakClient = kubernetesClient
					.resources(Keycloak.class);
			return keycloakClient.inNamespace(OpenShiftConfig.namespace());
		}
	}

	public NonNamespaceOperation<KeycloakRealmImport, KubernetesResourceList<KeycloakRealmImport>, Resource<KeycloakRealmImport>> keycloakRealmImportClient() {
		try (KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
			MixedOperation<KeycloakRealmImport, KubernetesResourceList<KeycloakRealmImport>, Resource<KeycloakRealmImport>> keycloakRealmImportClient = kubernetesClient
					.resources(KeycloakRealmImport.class);
			return keycloakRealmImportClient.inNamespace(OpenShiftConfig.namespace());
		}
	}
}
