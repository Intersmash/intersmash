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
package org.jboss.intersmash.tools.provision.operator;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Strings;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.operator.KeycloakRealmImportOperatorApplication;
import org.jboss.intersmash.tools.provision.Provisioner;
import org.jboss.intersmash.tools.provision.openshift.WaitersUtil;
import org.jboss.intersmash.tools.util.tls.CertificatesUtils;
import org.keycloak.k8s.v2alpha1.Keycloak;
import org.keycloak.k8s.v2alpha1.KeycloakRealmImport;
import org.keycloak.k8s.v2alpha1.keycloakspec.Http;
import org.slf4j.event.Level;

import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.fabric8.openshift.api.model.Route;

/**
 * Keycloak operator provisioner
 */
public interface KeycloakRealmImportOperatorProvisioner extends
		OlmOperatorProvisioner<KeycloakRealmImportOperatorApplication>, Provisioner<KeycloakRealmImportOperatorApplication> {

	String OPERATOR_ID = IntersmashConfig.keycloakRealmImportOperatorPackageManifest();

	default String getOperatorCatalogSource() {
		return IntersmashConfig.keycloakRealmImportOperatorCatalogSource();
	}

	default String getOperatorIndexImage() {
		return IntersmashConfig.keycloakRealmImportOperatorIndexImage();
	}

	default String getOperatorChannel() {
		return IntersmashConfig.keycloakRealmImportOperatorChannel();
	}

	Service retrieveNamedService(final String serviceName);

	default void deploy() {
		FailFastCheck ffCheck = () -> false;
		// Keycloak Operator codebase contains the name of the Keycloak image to deploy: user can override Keycloak image to
		// deploy using environment variables in Keycloak Operator Subscription
		subscribe();

		// Custom Keycloak image to be used: overrides the Keycloak image at the Keycloak level: just this Keycloak
		// instance will be spun out of this image
		if (!Strings.isNullOrEmpty(IntersmashConfig.keycloakRealmImportImageURL())) {
			getApplication().getKeycloak().getSpec().setImage(IntersmashConfig.keycloakRealmImportImageURL());
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
			new SimpleWaiter(() -> retrieveNamedService(getApplication().getKeycloak().getSpec().getDb().getHost()) != null)
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

	default void waitFor(Keycloak keycloak) {
		Long replicas = keycloak.getSpec().getInstances();
		if (replicas > 0) {
			// wait for >= 1 pods with label controller-revision-hash=keycloak-d86bb6ddc
			String controllerRevisionHash = getStatefulSet().getStatus().getUpdateRevision();
			BooleanSupplier bs = () -> retrievePods().stream()
					.filter(p -> p.getMetadata().getLabels().get("controller-revision-hash") != null
							&& p.getMetadata().getLabels().get("controller-revision-hash").equals(controllerRevisionHash))
					.collect(Collectors.toList()).size() == replicas.intValue();
			new SimpleWaiter(bs, TimeUnit.MINUTES, 2,
					"Waiting for pods with label \"controller-revision-hash\"=" + controllerRevisionHash + " to be scaled")
					.waitFor();
		}
	}

	default void waitFor(KeycloakRealmImport realmImport) {
		new SimpleWaiter(() -> {
			Resource<KeycloakRealmImport> res = keycloakRealmImportClient().withName(realmImport.getMetadata().getName());
			if (Objects.nonNull(res)
					&& Objects.nonNull(res.get())
					&& Objects.nonNull(res.get().getStatus())) {
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

	default void waitForKeycloakResourceReadiness() {
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
	default Resource<Keycloak> keycloak() {
		return keycloakClient()
				.withName(getApplication().getKeycloak().getMetadata().getName());
	}

	default List<KeycloakRealmImport> keycloakRealmImports() {
		return keycloakRealmImportClient().list().getItems()
				.stream().filter(
						realm -> getApplication().getKeycloakRealmImports().stream().map(
								ri -> ri.getMetadata().getName())
								.anyMatch(riName -> riName.equalsIgnoreCase(realm.getMetadata().getName())))
				.collect(Collectors.toList());
	}

	StatefulSet retrieveNamedStatefulSet(final String statefulSetName);

	List<Route> retrieveRoutes();

	/**
	 * @return the underlying StatefulSet which provisions the cluster
	 */
	default StatefulSet getStatefulSet() {
		final String STATEFUL_SET_NAME = getApplication().getKeycloak().getMetadata().getName();
		new SimpleWaiter(
				() -> Objects.nonNull(retrieveNamedStatefulSet(STATEFUL_SET_NAME)))
				.reason(
						MessageFormat.format(
								"Waiting for StatefulSet \"{0}\" to be created for Keycloak \"{1}\".",
								STATEFUL_SET_NAME,
								getApplication().getKeycloak().getMetadata().getName()))
				.level(Level.DEBUG).timeout(60000L).waitFor();
		return retrieveNamedStatefulSet(STATEFUL_SET_NAME);
	}

	List<Pod> retrieveNamespacePods();

	default void undeploy() {
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
		BooleanSupplier bs = () -> retrieveNamespacePods().stream()
				.filter(p -> !com.google.common.base.Strings.isNullOrEmpty(p.getMetadata().getLabels().get("app"))
						&& p.getMetadata().getLabels().get("app")
								.equals(getApplication().getKeycloak().getKind().toLowerCase()))
				.collect(Collectors.toList()).size() == 0;
		String reason = "Waiting for exactly 0 pods with label \"app\"="
				+ getApplication().getKeycloak().getKind().toLowerCase() + " to be ready.";
		new SimpleWaiter(bs, TimeUnit.MINUTES, 2, reason)
				.level(Level.DEBUG)
				.waitFor();

		unsubscribe();
	}

	default void scale(int replicas, boolean wait) {
		String controllerRevisionHash = getStatefulSet().getStatus().getUpdateRevision();
		Keycloak tmpKeycloak = keycloak().get();
		Long originalReplicas = tmpKeycloak.getSpec().getInstances();
		tmpKeycloak.getSpec().setInstances(Integer.toUnsignedLong(replicas));
		keycloak().replace(tmpKeycloak);
		if (wait) {
			BooleanSupplier bs = () -> retrievePods().stream()
					.filter(p -> p.getMetadata().getLabels().get("controller-revision-hash") != null
							&& p.getMetadata().getLabels().get("controller-revision-hash").equals(controllerRevisionHash))
					.collect(Collectors.toList()).size() == replicas;
			new SimpleWaiter(bs, TimeUnit.MINUTES, 2,
					"Waiting for pods with label \"controller-revision-hash\"=" + controllerRevisionHash + " to be scaled")
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

	default List<Pod> getPods() {
		String STATEFUL_SET_NAME = getApplication().getKeycloak().getMetadata().getName();
		StatefulSet statefulSet = retrieveNamedStatefulSet(STATEFUL_SET_NAME);
		return Objects.nonNull(statefulSet)
				? retrievePods().stream()
						.filter(p -> p.getMetadata().getLabels().get("controller-revision-hash") != null
								&& p.getMetadata().getLabels().get("controller-revision-hash")
										.equals(statefulSet.getStatus().getUpdateRevision()))
						.collect(Collectors.toList())
				: Lists.emptyList();
	}

	default URL getURL() {
		String host = retrieveRoutes()
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

	String KEYCLOAK_RESOURCE = "keycloaks.k8s.keycloak.org";

	String KEYCLOAK_REALM_RESOURCE = "keycloakrealmimports.k8s.keycloak.org";

	HasMetadataOperationsImpl<Keycloak, KubernetesResourceList<Keycloak>> keycloaksCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	NonNamespaceOperation<Keycloak, KubernetesResourceList<Keycloak>, Resource<Keycloak>> keycloakClient();

	HasMetadataOperationsImpl<KeycloakRealmImport, KubernetesResourceList<KeycloakRealmImport>> keycloakRealmImportsCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	NonNamespaceOperation<KeycloakRealmImport, KubernetesResourceList<KeycloakRealmImport>, Resource<KeycloakRealmImport>> keycloakRealmImportClient();

	default MixedOperation<Keycloak, KubernetesResourceList<Keycloak>, Resource<Keycloak>> buildKeycloakClient() {
		CustomResourceDefinition crd = retrieveCustomResourceDefinitions().withName(KEYCLOAK_RESOURCE).get();
		CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
		if (!retrieveCustomResourceDefinitions().list().getItems().contains(KEYCLOAK_RESOURCE)) {
			throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
					KEYCLOAK_RESOURCE, KeycloakOperatorProvisioner.operatorId()));
		}
		return keycloaksCustomResourcesClient(crdc);
	}

	default MixedOperation<KeycloakRealmImport, KubernetesResourceList<KeycloakRealmImport>, Resource<KeycloakRealmImport>> buildKeycloakRealmImportClient() {
		CustomResourceDefinition crd = retrieveCustomResourceDefinitions().withName(KEYCLOAK_REALM_RESOURCE).get();
		CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
		if (!retrieveCustomResourceDefinitions().list().getItems().contains(KEYCLOAK_REALM_RESOURCE)) {
			throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
					KEYCLOAK_REALM_RESOURCE, KeycloakOperatorProvisioner.operatorId()));
		}
		return keycloakRealmImportsCustomResourcesClient(crdc);
	}
}
