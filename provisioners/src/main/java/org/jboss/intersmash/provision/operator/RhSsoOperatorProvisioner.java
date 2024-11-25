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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Strings;
import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.operator.RhSsoOperatorApplication;
import org.jboss.intersmash.provision.Provisioner;
import org.jboss.intersmash.provision.operator.model.keycloak.backup.KeycloakBackupList;
import org.jboss.intersmash.provision.operator.model.keycloak.client.KeycloakClientList;
import org.jboss.intersmash.provision.operator.model.keycloak.keycloak.KeycloakList;
import org.jboss.intersmash.provision.operator.model.keycloak.realm.KeycloakRealmList;
import org.jboss.intersmash.provision.operator.model.keycloak.user.KeycloakUserList;
import org.keycloak.v1alpha1.Keycloak;
import org.keycloak.v1alpha1.KeycloakBackup;
import org.keycloak.v1alpha1.KeycloakClient;
import org.keycloak.v1alpha1.KeycloakRealm;
import org.keycloak.v1alpha1.KeycloakUser;
import org.slf4j.event.Level;

import cz.xtf.core.http.Https;
import cz.xtf.core.openshift.helpers.ResourceParsers;
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
 * Red Hat SSO (7.6, based on legacy Keycloak v20) operator provisioner
 */
@Deprecated(since = "0.0.2")
public abstract class RhSsoOperatorProvisioner<C extends NamespacedKubernetesClient> extends
		OperatorProvisioner<RhSsoOperatorApplication, C> implements Provisioner<RhSsoOperatorApplication> {

	public RhSsoOperatorProvisioner(RhSsoOperatorApplication application) {
		super(application, RhSsoOperatorProvisioner.OPERATOR_ID);
	}

	/**
	 * @return the underlying StatefulSet which provisions the cluster
	 */
	protected StatefulSet getStatefulSet() {
		final String name = "keycloak";
		StatefulSet statefulSet = this.client().apps().statefulSets().withName(name).get();
		if (Objects.isNull(statefulSet)) {
			throw new IllegalStateException(String.format(
					"StatefulSet with name=\"%s\" not found",
					name));
		}
		return statefulSet;
	}

	// =================================================================================================================
	// Related to generic provisioning behavior
	// =================================================================================================================
	@Override
	protected String getOperatorCatalogSource() {
		return IntersmashConfig.rhSsoOperatorCatalogSource();
	}

	@Override
	protected String getOperatorIndexImage() {
		return IntersmashConfig.rhSsoOperatorIndexImage();
	}

	@Override
	protected String getOperatorChannel() {
		return IntersmashConfig.rhSsoOperatorChannel();
	}

	@Override
	public void deploy() {
		FailFastCheck ffCheck = () -> false;
		// Keycloak Operator codebase contains the name of the Keycloak image to deploy: user can override Keycloak image to
		// deploy using environment variables in Keycloak Operator Subscription
		subscribe();

		// create custom resources
		keycloakClient().createOrReplace(getApplication().getKeycloak());
		if (getApplication().getKeycloakRealms().size() > 0) {
			getApplication().getKeycloakRealms().stream().forEach((i) -> keycloakRealmClient().resource(i).create());
		}
		if (getApplication().getKeycloakClients().size() > 0) {
			getApplication().getKeycloakClients().stream().forEach((i) -> keycloakClientClient().resource(i).create());
		}
		if (getApplication().getKeycloakUsers().size() > 0) {
			getApplication().getKeycloakUsers().stream().forEach((i) -> keycloakUserClient().resource(i).create());
		}
		if (getApplication().getKeycloakBackups().size() > 0) {
			getApplication().getKeycloakBackups().stream().forEach((i) -> keycloakBackupClient().resource(i).create());
		}

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

	/**
	 * Wait for Keycloak and PostgreSQL (in case no external database is used) to be ready
	 *
	 * @param keycloak Concrete {@link Keycloak} instance which the method should be wait for
	 */
	public void waitFor(Keycloak keycloak) {
		int replicas = keycloak.getSpec().getInstances().intValue();
		if (replicas > 0) {
			// 1. check externalDatabase
			if (keycloak.getSpec().getExternalDatabase() == null || !keycloak.getSpec().getExternalDatabase().getEnabled()) {
				// 2. wait for PostgreSQL to be ready (Service "keycloak-postgresql" is guaranteed to exist by documentation)
				new SimpleWaiter(() -> client().pods().inNamespace(client().getNamespace()).list().getItems()
						.stream()
						.filter(
								pod -> this.client().services().withName("keycloak-postgresql").get() != null
										&& pod.getMetadata().getLabels().entrySet().containsAll(
												this.client().services().withName("keycloak-postgresql").get().getSpec()
														.getSelector().entrySet())
										&& ResourceParsers.isPodReady(pod))
						.count() > 0).level(Level.DEBUG).waitFor();
			}
			// 4. wait for >= 1 pods with label controller-revision-hash=keycloak-d86bb6ddc
			final String controllerRevisionHash = getStatefulSet().getStatus().getUpdateRevision();
			waitForExactNumberOfLabeledPodsToBeReady("controller-revision-hash", controllerRevisionHash, replicas);
		}
	}

	private void waitForExactNumberOfLabeledPodsToBeReady(final String labelName, final String labelValue, int replicas) {
		BooleanSupplier bs = () -> getLabeledPods(labelName, labelValue).size() == replicas;
		new SimpleWaiter(bs, TimeUnit.MINUTES, 2,
				"Waiting for pods with label \"" + labelName + "\"=" + labelValue + " to be ready")
				.waitFor();
	}

	private void waitForKeycloakResourceReadiness() {
		new SimpleWaiter(() -> keycloak().get().getStatus().getReady())
				.reason("Wait for keycloak resource to be ready").level(Level.DEBUG).waitFor();
		if (getApplication().getKeycloakRealms().size() > 0)
			new SimpleWaiter(() -> keycloakRealms().stream().map(realm -> realm.get().getStatus().getReady())
					.reduce(Boolean::logicalAnd).get())
					.reason("Wait for keycloakrealms to be ready.").level(Level.DEBUG).waitFor();
		if (getApplication().getKeycloakClients().size() > 0)
			new SimpleWaiter(() -> keycloakClients().stream().map(realm -> realm.get().getStatus().getReady())
					.reduce(Boolean::logicalAnd).get())
					.reason("Wait for keycloakclients to be ready.").level(Level.DEBUG).waitFor();
		if (getApplication().getKeycloakUsers().size() > 0)
			new SimpleWaiter(() -> keycloakUserClient().list().getItems().size() == getApplication().getKeycloakUsers().size())
					.reason("Wait for keycloakusers to be ready.").level(Level.DEBUG).waitFor(); // no isReady() for users
		if (getApplication().getKeycloakBackups().size() > 0)
			new SimpleWaiter(() -> keycloakBackups().stream().map(realm -> realm.get().getStatus().getReady())
					.reduce(Boolean::logicalAnd).get())
					.reason("Wait for keycloakbackups to be ready.").level(Level.DEBUG).waitFor();
	}

	@Override
	public void undeploy() {
		// delete custom resources
		keycloakBackups()
				.forEach(keycloakBackup -> keycloakBackup.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		new SimpleWaiter(() -> keycloakBackupClient().list().getItems().size() == 0)
				.reason("Wait for all keycloakbackups instances to be deleted.").level(Level.DEBUG).waitFor();
		keycloakUsers().forEach(keycloakUser -> keycloakUser.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		new SimpleWaiter(() -> keycloakUserClient().list().getItems().size() == 0)
				.reason("Wait for all keycloakusers instances to be deleted.").level(Level.DEBUG).waitFor();
		keycloakClients()
				.forEach(keycloakClient -> keycloakClient.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		keycloakUsers().forEach(keycloakUser -> keycloakUser.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		new SimpleWaiter(() -> keycloakClientClient().list().getItems().size() == 0)
				.reason("Wait for all keycloakclients instances to be deleted.").level(Level.DEBUG).waitFor();
		keycloakRealms().forEach(keycloakRealm -> keycloakRealm.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		new SimpleWaiter(() -> keycloakRealmClient().list().getItems().size() == 0)
				.reason("Wait for all keycloakrealms instances to be deleted.").level(Level.DEBUG).waitFor();
		keycloak().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		new SimpleWaiter(() -> keycloakClient().list().getItems().size() == 0)
				.reason("Wait for all keycloakrealms instances to be deleted.").level(Level.DEBUG).waitFor();

		// wait for 0 pods
		waitForExactNumberOfLabeledPodsToBeReady("app", getApplication().getKeycloak().getKind().toLowerCase(), 0);
		unsubscribe();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		FailFastCheck ffCheck = () -> false;
		final String controllerRevisionHash = getStatefulSet().getStatus().getUpdateRevision();
		Keycloak tmpKeycloak = keycloak().get();
		final int originalReplicas = tmpKeycloak.getSpec().getInstances().intValue();
		tmpKeycloak.getSpec().setInstances(Long.valueOf(replicas));
		keycloak().replace(tmpKeycloak);
		if (wait) {
			waitForExactNumberOfLabeledPodsToBeReady("controller-revision-hash", controllerRevisionHash, replicas);
		}
		new SimpleWaiter(() -> keycloak().get().getStatus().getReady())
				.reason("Wait for keycloak resource to be ready").level(Level.DEBUG).waitFor();
		// check that route is up
		if (originalReplicas == 0 && replicas > 0) {
			new SimpleWaiter(
					() -> Https.getCode(getURL().toExternalForm()) != 503)
					.reason("Wait until the route is ready to serve.");
		}
	}

	/**
	 * RH-SSO Operator based provisioner implementation is designed to return just the RH-SSO instance pods
	 * @return a list of {@link Pod} that represent the replicas of RH-SSO instances
	 */
	@Override
	public List<Pod> getPods() {
		StatefulSet statefulSet = getStatefulSet();
		return Objects.nonNull(statefulSet)
				? getLabeledPods("controller-revision-hash",
						statefulSet.getStatus().getUpdateRevision())
				: Lists.emptyList();
	}

	@Override
	public URL getURL() {
		// https://github.com/keycloak/keycloak-operator/blob/15.0.2/pkg/apis/keycloak/v1alpha1/keycloak_types.go#L232
		String externalUrl = keycloak().get().getStatus().getExternalURL();
		try {
			return Strings.isNullOrEmpty(externalUrl) ? null : new URL(externalUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(String.format("Keycloak operator External URL \"%s\" is malformed.", externalUrl), e);
		}
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	// this is the packagemanifest for the operator;
	// you can get it with command:
	// oc get packagemanifest <bundle> -o template --template='{{ .metadata.name }}'
	public static String OPERATOR_ID = IntersmashConfig.rhSsoOperatorPackageManifest();

	// this is the name of the CustomResourceDefinition(s)
	// you can get it with command:
	// oc get crd <group>> -o template --template='{{ .metadata.name }}'
	private static final String KEYCLOAK_CRD_NAME = "keycloaks.keycloak.org";
	private static NonNamespaceOperation<Keycloak, KeycloakList, Resource<Keycloak>> KEYCLOAKS_CLIENT;

	private static final String KEYCLOAK_REALM_CRD_NAME = "keycloakrealms.keycloak.org";
	private static NonNamespaceOperation<KeycloakRealm, KeycloakRealmList, Resource<KeycloakRealm>> KEYCLOAK_REALMS_CLIENT;

	private static final String KEYCLOAK_BACKUP_CRD_NAME = "keycloakbackups.keycloak.org";
	private static NonNamespaceOperation<KeycloakBackup, KeycloakBackupList, Resource<KeycloakBackup>> KEYCLOAK_BACKUPS_CLIENT;

	private static final String KEYCLOAK_CLIENT_CRD_NAME = "keycloakclients.keycloak.org";
	private static NonNamespaceOperation<KeycloakClient, KeycloakClientList, Resource<KeycloakClient>> KEYCLOAK_CLIENTS_CLIENT;

	private static final String KEYCLOAK_USER_CRD_NAME = "keycloakusers.keycloak.org";
	private static NonNamespaceOperation<KeycloakUser, KeycloakUserList, Resource<KeycloakUser>> KEYCLOAK_USERS_CLIENT;

	/**
	 * Generic CRD client which is used by client builders default implementation to build the CRDs client
	 *
	 * @return A {@link NonNamespaceOperation} instance that represents a
	 */
	public abstract NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient();

	// keycloaks.keycloak.org
	protected abstract HasMetadataOperationsImpl<Keycloak, KeycloakList> keycloakCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	/**
	 * Get a client capable of working with {@link RhSsoOperatorProvisioner#KEYCLOAK_CRD_NAME} custom resource.
	 *
	 * @return client for operations with {@link RhSsoOperatorProvisioner#KEYCLOAK_CRD_NAME} custom resource
	 */
	public NonNamespaceOperation<Keycloak, KeycloakList, Resource<Keycloak>> keycloakClient() {
		if (KEYCLOAKS_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(KEYCLOAK_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_CRD_NAME, OPERATOR_ID));
			}
			KEYCLOAKS_CLIENT = keycloakCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return KEYCLOAKS_CLIENT;
	}

	/**
	 * Get a reference to keycloak object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link Keycloak} resource definition
	 */
	public Resource<Keycloak> keycloak() {
		return keycloakClient().withName(getApplication().getKeycloak().getMetadata().getName());
	}

	// keycloakrealms.keycloak.org
	protected abstract HasMetadataOperationsImpl<KeycloakRealm, KeycloakRealmList> keycloakRealmCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_REALM_CRD_NAME} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_REALM_CRD_NAME} custom resource
	 */
	public NonNamespaceOperation<KeycloakRealm, KeycloakRealmList, Resource<KeycloakRealm>> keycloakRealmClient() {
		if (KEYCLOAK_REALMS_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(KEYCLOAK_REALM_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_REALM_CRD_NAME, OPERATOR_ID));
			}
			KEYCLOAK_REALMS_CLIENT = keycloakRealmCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return KEYCLOAK_REALMS_CLIENT;
	}

	/**
	 * Get a reference to keycloakrealms object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the keycloakrealm custom resource
	 * @return A concrete {@link Resource} instance representing the {@link KeycloakRealm} resource definition
	 */
	public Resource<KeycloakRealm> keycloakRealm(String name) {
		return keycloakRealmClient().withName(name);
	}

	/**
	 * Get all keycloakrealms maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link KeycloakRealm} resource definitions
	 */
	public List<Resource<KeycloakRealm>> keycloakRealms() {
		RhSsoOperatorApplication rhSsoOperatorApplication = getApplication();
		return rhSsoOperatorApplication.getKeycloakRealms().stream()
				.map(keycloakRealm -> keycloakRealm.getMetadata().getName())
				.map(this::keycloakRealm)
				.collect(Collectors.toList());
	}

	// keycloakbackups.keycloak.org
	protected abstract HasMetadataOperationsImpl<KeycloakBackup, KeycloakBackupList> keycloakBackupCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_BACKUP_CRD_NAME} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_BACKUP_CRD_NAME} custom resource
	 */
	public NonNamespaceOperation<KeycloakBackup, KeycloakBackupList, Resource<KeycloakBackup>> keycloakBackupClient() {
		if (KEYCLOAK_BACKUPS_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(KEYCLOAK_BACKUP_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_BACKUP_CRD_NAME, OPERATOR_ID));
			}
			KEYCLOAK_BACKUPS_CLIENT = keycloakBackupCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return KEYCLOAK_BACKUPS_CLIENT;
	}

	/**
	 * Get a reference to keycloakbackup object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the {@link KeycloakBackup} custom resource
	 * @return A concrete {@link Resource} instance representing the {@link KeycloakBackup} resource definition
	 */
	public Resource<KeycloakBackup> keycloakBackup(String name) {
		return keycloakBackupClient().withName(name);
	}

	/**
	 * Get all keycloakbackups maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link KeycloakBackup} resource definitions
	 */
	public List<Resource<KeycloakBackup>> keycloakBackups() {
		RhSsoOperatorApplication rhSsoOperatorApplication = getApplication();
		return rhSsoOperatorApplication.getKeycloakBackups().stream()
				.map(keycloakBackup -> keycloakBackup.getMetadata().getName())
				.map(this::keycloakBackup)
				.collect(Collectors.toList());
	}

	// keycloakclients.keycloak.org
	protected abstract HasMetadataOperationsImpl<KeycloakClient, KeycloakClientList> keycloakClientCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_CLIENT_CRD_NAME} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_CLIENT_CRD_NAME} custom resource
	 */
	public NonNamespaceOperation<KeycloakClient, KeycloakClientList, Resource<KeycloakClient>> keycloakClientClient() {
		if (KEYCLOAK_CLIENTS_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(KEYCLOAK_CLIENT_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_CLIENT_CRD_NAME, OPERATOR_ID));
			}
			KEYCLOAK_CLIENTS_CLIENT = keycloakClientCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return KEYCLOAK_CLIENTS_CLIENT;
	}

	/**
	 * Get a reference to keycloakclient object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the {@link KeycloakClient} custom resource
	 * @return A concrete {@link Resource} instance representing the {@link KeycloakClient} resource definition
	 */
	public Resource<KeycloakClient> keycloakClient(String name) {
		return keycloakClientClient().withName(name);
	}

	/**
	 * Get all keycloakclients maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link KeycloakClient} resource definitions
	 */
	public List<Resource<KeycloakClient>> keycloakClients() {
		RhSsoOperatorApplication rhSsoOperatorApplication = getApplication();
		return rhSsoOperatorApplication.getKeycloakClients().stream()
				.map(keycloakClient -> keycloakClient.getMetadata().getName())
				.map(this::keycloakClient)
				.collect(Collectors.toList());
	}

	// keycloakusers.keycloak.org
	protected abstract HasMetadataOperationsImpl<KeycloakUser, KeycloakUserList> keycloakUserCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_USER_CRD_NAME} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_USER_CRD_NAME} custom resource
	 */
	public NonNamespaceOperation<KeycloakUser, KeycloakUserList, Resource<KeycloakUser>> keycloakUserClient() {
		if (KEYCLOAK_USERS_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(KEYCLOAK_USER_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_USER_CRD_NAME, OPERATOR_ID));
			}
			KEYCLOAK_USERS_CLIENT = keycloakUserCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return KEYCLOAK_USERS_CLIENT;
	}

	/**
	 * Get a reference to keycloakuser object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the keycloakuser custom resource
	 * @return A concrete {@link Resource} instance representing the {@link KeycloakUser} resource definition
	 */
	public Resource<KeycloakUser> keycloakUser(String name) {
		return keycloakUserClient().withName(name);
	}

	/**
	 * Get all keycloakusers maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link KeycloakUser} resource definitions
	 */
	public List<Resource<KeycloakUser>> keycloakUsers() {
		RhSsoOperatorApplication rhSsoOperatorApplication = getApplication();
		return rhSsoOperatorApplication.getKeycloakUsers().stream()
				.map(keycloakUser -> keycloakUser.getMetadata().getName())
				.map(this::keycloakUser)
				.collect(Collectors.toList());
	}
}
