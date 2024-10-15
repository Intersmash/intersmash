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
package org.jboss.intersmash.provision.operator;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.assertj.core.util.Strings;
import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.operator.KeycloakOperatorApplication;
import org.jboss.intersmash.provision.Provisioner;
import org.jboss.intersmash.util.tls.CertificatesUtils;
import org.keycloak.k8s.v2alpha1.Keycloak;
import org.keycloak.k8s.v2alpha1.KeycloakOperatorKeycloakList;
import org.keycloak.k8s.v2alpha1.KeycloakOperatorRealmImportList;
import org.keycloak.k8s.v2alpha1.KeycloakRealmImport;
import org.keycloak.k8s.v2alpha1.keycloakspec.Http;
import org.slf4j.event.Level;

import cz.xtf.core.http.Https;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;

/**
 * Keycloak operator provisioner
 */
public abstract class KeycloakOperatorProvisioner<C extends NamespacedKubernetesClient> extends
		OperatorProvisioner<KeycloakOperatorApplication, C> implements Provisioner<KeycloakOperatorApplication> {

	public KeycloakOperatorProvisioner(KeycloakOperatorApplication application) {
		super(application, KeycloakOperatorProvisioner.OPERATOR_ID);
	}

	// =================================================================================================================
	// Related to generic provisioning behavior
	// =================================================================================================================
	@Override
	public String getOperatorCatalogSource() {
		return IntersmashConfig.keycloakOperatorCatalogSource();
	}

	@Override
	public String getOperatorIndexImage() {
		return IntersmashConfig.keycloakOperatorIndexImage();
	}

	@Override
	public String getOperatorChannel() {
		return IntersmashConfig.keycloakOperatorChannel();
	}

	@Override
	public void deploy() {
		FailFastCheck ffCheck = () -> false;
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
			if (getApplication().getKeycloak().getSpec().getHostname() == null ||
					com.google.common.base.Strings
							.isNullOrEmpty(getApplication().getKeycloak().getSpec().getHostname().getHostname())) {
				throw new IllegalStateException(
						"A .spec.hostname.hostname must be set when configuring a Keycloak resource .spec.http");
			}
			// create key, certificate and tls secret
			String tlsSecretName = getApplication().getKeycloak().getMetadata().getName() + "-tls-secret";
			CertificatesUtils.CertificateAndKey certificateAndKey = CertificatesUtils
					.generateSelfSignedCertificateAndKey(
							getApplication().getKeycloak().getSpec().getHostname().getHostname().replaceFirst("[.].*$", ""),
							tlsSecretName, this.client(), this.client().getNamespace());
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
			new SimpleWaiter(() -> this.client().services().withName(getApplication().getKeycloak().getSpec().getDb().getHost())
					.get() != null)
					.level(Level.DEBUG).waitFor();
		}

		// create custom resources
		keycloakClient().createOrReplace(getApplication().getKeycloak());
		getApplication().getKeycloakRealmImports().stream()
				.forEach(ri -> keycloakRealmImportClient().createOrReplace(ri));

		// Wait for Keycloak (and PostgreSQL) to be ready
		waitFor(getApplication().getKeycloak());
		// wait for all resources to be ready
		waitForKeycloakResourceReadiness();
		// check that route is up, only if there's a valid external URL available
		URL externalUrl = getURL();
		if ((getApplication().getKeycloak().getSpec().getInstances() > 0) && (externalUrl != null)) {
			new SimpleWaiter(
					() -> Https.getCode(getURL().toExternalForm()) != 503)
					.reason("Wait until the route is ready to serve.");
		}
	}

	public void waitFor(Keycloak keycloak) {
		Long replicas = keycloak.getSpec().getInstances();
		if (replicas > 0) {
			// wait for >= 1 pods with label controller-revision-hash=keycloak-d86bb6ddc
			String controllerRevisionHash = getStatefulSet().getStatus().getUpdateRevision();
			BooleanSupplier bs = () -> new ArrayList<>(getLabeledPods("controller-revision-hash", controllerRevisionHash))
					.size() == replicas.intValue();
			new SimpleWaiter(bs, TimeUnit.MINUTES, 2,
					"Waiting for pods with label \"controller-revision-hash\"=" + controllerRevisionHash + " to be scaled")
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
				() -> keycloak().get().getStatus() != null &&
						keycloak().get().getStatus().getConditions().stream().anyMatch(
								condition -> "Ready".equalsIgnoreCase(condition.getType()) && condition.getStatus() != null))
				.reason("Wait for Keycloak resource to be ready").level(Level.DEBUG).waitFor();
		if (!getApplication().getKeycloakRealmImports().isEmpty()) {
			new SimpleWaiter(() -> keycloakRealmImports().stream().allMatch(
					realmImport -> realmImport.getStatus().getConditions().stream().anyMatch(
							condition -> "Done".equalsIgnoreCase(condition.getType())
									&& condition.getStatus() != null)))
					.reason("Wait for KeycloakRealmImports to be done.").level(Level.DEBUG).waitFor();
		}
	}

	/**
	 * @return the underlying StatefulSet which provisions the cluster
	 */
	public StatefulSet getStatefulSet() {
		final String statefulSetName = getApplication().getKeycloak().getMetadata().getName();
		new SimpleWaiter(
				() -> Objects.nonNull(this.client().apps().statefulSets().withName(statefulSetName).get()))
				.reason(
						MessageFormat.format(
								"Waiting for StatefulSet \"{0}\" to be created for Keycloak \"{1}\".",
								statefulSetName,
								getApplication().getKeycloak().getMetadata().getName()))
				.level(Level.DEBUG).failFast(getFailFastCheck()).waitFor();
		return this.client().apps().statefulSets().withName(statefulSetName).get();
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
		BooleanSupplier bs = () -> this.client().pods().inNamespace(this.client().getNamespace()).list().getItems().stream()
				.filter(p -> !com.google.common.base.Strings.isNullOrEmpty(p.getMetadata().getLabels().get("app"))
						&& p.getMetadata().getLabels().get("app")
								.equals(getApplication().getKeycloak().getKind().toLowerCase()))
				.collect(Collectors.toList()).isEmpty();
		String reason = "Waiting for exactly 0 pods with label \"app\"="
				+ getApplication().getKeycloak().getKind().toLowerCase() + " to be ready.";
		new SimpleWaiter(bs, TimeUnit.MINUTES, 2, reason)
				.level(Level.DEBUG)
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
			BooleanSupplier bs = () -> getPods().stream()
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
								&& condition.getStatus() != null))
				.reason("Wait for Keycloak resource to be ready").level(Level.DEBUG).waitFor();
		// check that route is up
		if (originalReplicas == 0 && replicas > 0) {
			new SimpleWaiter(
					() -> Https.getCode(getURL().toExternalForm()) != 503)
					.reason("Wait until the route is ready to serve.");
		}
	}

	/**
	 * Keycloak operator based provisioner implementation is designed to return just the Keycloak instance pods
	 * @return a list of {@link Pod} that represent the replicas of Keycloak instances
	 */
	public List<Pod> getPods() {
		final String statefulSetName = getApplication().getKeycloak().getMetadata().getName();
		StatefulSet statefulSet = this.client().apps().statefulSets().withName(statefulSetName).get();
		return Objects.nonNull(statefulSet)
				? this.client().pods().inNamespace(this.client().getNamespace()).list().getItems().stream()
						.filter(p -> p.getMetadata().getLabels().get("controller-revision-hash") != null
								&& p.getMetadata().getLabels().get("controller-revision-hash")
										.equals(statefulSet.getStatus().getUpdateRevision()))
						.collect(Collectors.toList())
				: List.of();
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	// this is the packagemanifest for the operator;
	// you can get it with command:
	// oc get packagemanifest <bundle> -o template --template='{{ .metadata.name }}'
	public static String OPERATOR_ID = IntersmashConfig.keycloakOperatorPackageManifest();
	// this is the name of the CustomResourceDefinition(s)
	// you can get it with command:
	// oc get crd <group>> -o template --template='{{ .metadata.name }}'
	public String KEYCLOACK_CRD_NAME = "keycloaks.k8s.keycloak.org";

	public String KEYCLOACK_REALM_IMPORT_CRD_NAME = "keycloakrealmimports.k8s.keycloak.org";

	/**
	 * Generic CRD client which is used by client builders default implementation to build the CRDs client
	 *
	 * @return A {@link NonNamespaceOperation} instance that represents a
	 */
	public abstract NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient();

	private static NonNamespaceOperation<Keycloak, KeycloakOperatorKeycloakList, Resource<Keycloak>> KEYCLOAKS_CLIENT;
	private static NonNamespaceOperation<KeycloakRealmImport, KeycloakOperatorRealmImportList, Resource<KeycloakRealmImport>> KEYCLOAK_REALM_IMPORTS_CLIENT;

	// keycloaks.k8s.keycloak.org
	protected abstract HasMetadataOperationsImpl<Keycloak, KeycloakOperatorKeycloakList> keycloaksCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	// keycloakrealmimports.k8s.keycloak.org
	protected abstract HasMetadataOperationsImpl<KeycloakRealmImport, KeycloakOperatorRealmImportList> keycloakRealmImportsCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	public NonNamespaceOperation<Keycloak, KeycloakOperatorKeycloakList, Resource<Keycloak>> keycloakClient() {
		if (KEYCLOAKS_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(KEYCLOACK_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOACK_CRD_NAME, OPERATOR_ID));
			}
			KEYCLOAKS_CLIENT = keycloaksCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return KEYCLOAKS_CLIENT;
	}

	public NonNamespaceOperation<KeycloakRealmImport, KeycloakOperatorRealmImportList, Resource<KeycloakRealmImport>> keycloakRealmImportClient() {
		if (KEYCLOAK_REALM_IMPORTS_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(KEYCLOACK_REALM_IMPORT_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOACK_REALM_IMPORT_CRD_NAME, OPERATOR_ID));
			}
			KEYCLOAK_REALM_IMPORTS_CLIENT = keycloakRealmImportsCustomResourcesClient(
					CustomResourceDefinitionContext.fromCrd(crd));
		}
		return KEYCLOAK_REALM_IMPORTS_CLIENT;
	}

	/**
	 * Get a reference to keycloak object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link Keycloak} resource definition
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
}
