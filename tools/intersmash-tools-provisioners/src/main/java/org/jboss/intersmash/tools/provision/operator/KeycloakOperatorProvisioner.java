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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Strings;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.operator.KeycloakOperatorApplication;
import org.jboss.intersmash.tools.provision.Provisioner;
import org.jboss.intersmash.tools.provision.openshift.OpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.WaitersUtil;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.backup.KeycloakBackup;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.backup.KeycloakBackupList;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.client.KeycloakClient;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.client.KeycloakClientList;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.Keycloak;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.KeycloakList;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.KeycloakRealm;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.KeycloakRealmList;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.KeycloakUser;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.KeycloakUserList;
import org.slf4j.event.Level;

import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.helpers.ResourceParsers;
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
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;

/**
 * Keycloak operator provisioner
 */
public interface KeycloakOperatorProvisioner extends
		OlmOperatorProvisioner<KeycloakOperatorApplication>, Provisioner<KeycloakOperatorApplication> {
	String KEYCLOAK_RESOURCE = "keycloaks.keycloak.org";

	String KEYCLOAK_REALM_RESOURCE = "keycloakrealms.keycloak.org";

	String KEYCLOAK_BACKUP_RESOURCE = "keycloakbackups.keycloak.org";

	String KEYCLOAK_CLIENT_RESOURCE = "keycloakclients.keycloak.org";

	String KEYCLOAK_USER_RESOURCE = "keycloakusers.keycloak.org";

	String STATEFUL_SET_NAME = "keycloak";

	// this is the packagemanifest for the hyperfoil operator;
	// you can get it with command:
	// oc get packagemanifest hyperfoil-bundle -o template --template='{{ .metadata.name }}'
	static String operatorId() {
		return IntersmashConfig.keycloakOperatorPackageManifest();
	}

	default String getOperatorCatalogSource() {
		return IntersmashConfig.keycloakOperatorCatalogSource();
	}

	default String getOperatorIndexImage() {
		return IntersmashConfig.keycloakOperatorIndexImage();
	}

	default String getOperatorChannel() {
		return IntersmashConfig.keycloakOperatorChannel();
	}

	default void deploy() {
		FailFastCheck ffCheck = () -> false;
		// Keycloak Operator codebase contains the name of the Keycloak image to deploy: user can override Keycloak image to
		// deploy using environment variables in Keycloak Operator Subscription
		subscribe();

		// create custom resources
		keycloaksClient().createOrReplace(getApplication().getKeycloak());
		if (getApplication().getKeycloakRealms().size() > 0)
			keycloakRealmsClient().createOrReplace(getApplication().getKeycloakRealms().stream().toArray(KeycloakRealm[]::new));
		if (getApplication().getKeycloakClients().size() > 0)
			keycloakClientsClient()
					.createOrReplace(getApplication().getKeycloakClients().stream().toArray(KeycloakClient[]::new));
		if (getApplication().getKeycloakUsers().size() > 0)
			keycloakUsersClient().createOrReplace(getApplication().getKeycloakUsers().stream().toArray(KeycloakUser[]::new));
		if (getApplication().getKeycloakBackups().size() > 0)
			keycloakBackupsClient()
					.createOrReplace(getApplication().getKeycloakBackups().stream().toArray(KeycloakBackup[]::new));

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

	/**
	 * Wait for Keycloak and PostgreSQL (in case no external database is used) to be ready
	 *
	 * @param keycloak Concrete {@link Keycloak} instance which the method should be wait for
	 */
	default void waitFor(Keycloak keycloak) {
		FailFastCheck ffCheck = () -> false;
		int replicas = keycloak.getSpec().getInstances();
		if (replicas > 0) {
			// 1. check externalDatabase
			if (keycloak.getSpec().getExternalDatabase() == null || !keycloak.getSpec().getExternalDatabase().isEnabled()) {
				// 2. wait for PostgreSQL to be ready (Service "keycloak-postgresql" is guaranteed to exist by documentation)
				new SimpleWaiter(() -> OpenShiftProvisioner.openShift.getPods()
						.stream()
						.filter(
								pod -> OpenShiftProvisioner.openShift.getService("keycloak-postgresql") != null
										&& pod.getMetadata().getLabels().entrySet().containsAll(
												OpenShiftProvisioner.openShift.getService("keycloak-postgresql").getSpec()
														.getSelector().entrySet())
										&& ResourceParsers.isPodReady(pod))
						.count() > 0).level(Level.DEBUG).waitFor();
			}
			// 4. wait for >= 1 pods with label controller-revision-hash=keycloak-d86bb6ddc
			String controllerRevisionHash = getStatefulSet().getStatus().getUpdateRevision();
			OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
					.areExactlyNPodsReady(replicas, "controller-revision-hash",
							controllerRevisionHash)
					.waitFor();
		}
	}

	private void waitForKeycloakResourceReadiness() {
		new SimpleWaiter(() -> keycloak().get().getStatus().isReady())
				.reason("Wait for keycloak resource to be ready").level(Level.DEBUG).waitFor();
		if (getApplication().getKeycloakRealms().size() > 0)
			new SimpleWaiter(() -> keycloakRealms().stream().map(realm -> realm.get().getStatus().isReady())
					.reduce(Boolean::logicalAnd).get())
					.reason("Wait for keycloakrealms to be ready.").level(Level.DEBUG).waitFor();
		if (getApplication().getKeycloakClients().size() > 0)
			new SimpleWaiter(() -> keycloakClients().stream().map(realm -> realm.get().getStatus().isReady())
					.reduce(Boolean::logicalAnd).get())
					.reason("Wait for keycloakclients to be ready.").level(Level.DEBUG).waitFor();
		if (getApplication().getKeycloakUsers().size() > 0)
			new SimpleWaiter(() -> keycloakUsersClient().list().getItems().size() == getApplication().getKeycloakUsers().size())
					.reason("Wait for keycloakusers to be ready.").level(Level.DEBUG).waitFor(); // no isReady() for users
		if (getApplication().getKeycloakBackups().size() > 0)
			new SimpleWaiter(() -> keycloakBackups().stream().map(realm -> realm.get().getStatus().isReady())
					.reduce(Boolean::logicalAnd).get())
					.reason("Wait for keycloakbackups to be ready.").level(Level.DEBUG).waitFor();
	}

	default void undeploy() {
		// delete custom resources
		keycloakBackups()
				.forEach(keycloakBackup -> keycloakBackup.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		new SimpleWaiter(() -> keycloakBackupsClient().list().getItems().size() == 0)
				.reason("Wait for all keycloakbackups instances to be deleted.").level(Level.DEBUG).waitFor();
		keycloakUsers().forEach(keycloakUser -> keycloakUser.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		new SimpleWaiter(() -> keycloakUsersClient().list().getItems().size() == 0)
				.reason("Wait for all keycloakusers instances to be deleted.").level(Level.DEBUG).waitFor();
		keycloakClients()
				.forEach(keycloakClient -> keycloakClient.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		keycloakUsers().forEach(keycloakUser -> keycloakUser.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		new SimpleWaiter(() -> keycloakClientsClient().list().getItems().size() == 0)
				.reason("Wait for all keycloakclients instances to be deleted.").level(Level.DEBUG).waitFor();
		keycloakRealms().forEach(keycloakRealm -> keycloakRealm.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		new SimpleWaiter(() -> keycloakRealmsClient().list().getItems().size() == 0)
				.reason("Wait for all keycloakrealms instances to be deleted.").level(Level.DEBUG).waitFor();
		keycloak().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		new SimpleWaiter(() -> keycloaksClient().list().getItems().size() == 0)
				.reason("Wait for all keycloakrealms instances to be deleted.").level(Level.DEBUG).waitFor();

		// wait for 0 pods
		OpenShiftWaiters.get(OpenShiftProvisioner.openShift, () -> false)
				.areExactlyNPodsReady(0, "app", getApplication().getKeycloak().getKind().toLowerCase()).level(Level.DEBUG)
				.waitFor();
		unsubscribe();
	}

	default void scale(int replicas, boolean wait) {
		FailFastCheck ffCheck = () -> false;
		String controllerRevisionHash = getStatefulSet().getStatus().getUpdateRevision();
		Keycloak tmpKeycloak = keycloak().get();
		int originalReplicas = tmpKeycloak.getSpec().getInstances();
		tmpKeycloak.getSpec().setInstances(replicas);
		keycloak().replace(tmpKeycloak);
		if (wait) {
			OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
					.areExactlyNPodsReady(replicas, "controller-revision-hash", controllerRevisionHash)
					.level(Level.DEBUG)
					.waitFor();
		}
		new SimpleWaiter(() -> keycloak().get().getStatus().isReady())
				.reason("Wait for keycloak resource to be ready").level(Level.DEBUG).waitFor();
		// check that route is up
		if (originalReplicas == 0 && replicas > 0) {
			WaitersUtil.routeIsUp(getURL().toExternalForm())
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	default List<Pod> getPods() {
		StatefulSet statefulSet = retrieveNamedStatefulSet(STATEFUL_SET_NAME);
		return Objects.nonNull(statefulSet)
				? OpenShiftProvisioner.openShift.getLabeledPods("controller-revision-hash",
						statefulSet.getStatus().getUpdateRevision())
				: Lists.emptyList();
	}

	default URL getURL() {
		// https://github.com/keycloak/keycloak-operator/blob/15.0.2/pkg/apis/keycloak/v1alpha1/keycloak_types.go#L232
		String externalUrl = keycloak().get().getStatus().getExternalURL();
		try {
			return Strings.isNullOrEmpty(externalUrl) ? null : new URL(externalUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(String.format("Keycloak operator External URL \"%s\" is malformed.", externalUrl), e);
		}
	}

	// keycloakrealms.keycloak.org

	HasMetadataOperationsImpl<Keycloak, KeycloakList> keycloaksCustomResourcesClient(CustomResourceDefinitionContext crdc);

	NonNamespaceOperation<Keycloak, KeycloakList, Resource<Keycloak>> keycloaksClient();

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_RESOURCE} custom resource
	 */
	default MixedOperation<Keycloak, KeycloakList, Resource<Keycloak>> buildKeycloaksClient() {
		CustomResourceDefinition crd = retrieveCustomResourceDefinitions().withName(KEYCLOAK_RESOURCE).get();
		CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
		if (!retrieveCustomResourceDefinitions().list().getItems().contains(KEYCLOAK_RESOURCE)) {
			throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
					KEYCLOAK_RESOURCE, KeycloakOperatorProvisioner.operatorId()));
		}
		return keycloaksCustomResourcesClient(crdc);
	}

	/**
	 * Get a reference to keycloak object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link Keycloak} resource definition
	 */
	default Resource<Keycloak> keycloak() {
		return keycloaksClient().withName(getApplication().getKeycloak().getMetadata().getName());
	}

	// keycloakrealms.keycloak.org
	HasMetadataOperationsImpl<KeycloakRealm, KeycloakRealmList> keycloakRealmsCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	NonNamespaceOperation<KeycloakRealm, KeycloakRealmList, Resource<KeycloakRealm>> keycloakRealmsClient();

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_REALM_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_REALM_RESOURCE} custom resource
	 */
	default MixedOperation<KeycloakRealm, KeycloakRealmList, Resource<KeycloakRealm>> buildKeycloakRealmsClient() {
		CustomResourceDefinition crd = retrieveCustomResourceDefinitions()
				.withName(KEYCLOAK_REALM_RESOURCE).get();
		CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
		if (!retrieveCustomResourceDefinitions().list().getItems().contains(KEYCLOAK_REALM_RESOURCE)) {
			throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
					KEYCLOAK_REALM_RESOURCE, KeycloakOperatorProvisioner.operatorId()));
		}
		return keycloakRealmsCustomResourcesClient(crdc);
	}

	/**
	 * Get a reference to keycloakrealms object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the keycloakrealm custom resource
	 * @return A concrete {@link Resource} instance representing the {@link KeycloakRealm} resource definition
	 */
	default Resource<KeycloakRealm> keycloakRealm(String name) {
		return keycloakRealmsClient().withName(name);
	}

	/**
	 * Get all keycloakrealms maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link KeycloakRealm} resource definitions
	 */
	default List<Resource<KeycloakRealm>> keycloakRealms() {
		KeycloakOperatorApplication keycloakOperatorApplication = getApplication();
		return keycloakOperatorApplication.getKeycloakRealms().stream()
				.map(keycloakRealm -> keycloakRealm.getMetadata().getName())
				.map(this::keycloakRealm)
				.collect(Collectors.toList());
	}

	// keycloakbackups.keycloak.org
	HasMetadataOperationsImpl<KeycloakBackup, KeycloakBackupList> keycloakBackupsCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	NonNamespaceOperation<KeycloakBackup, KeycloakBackupList, Resource<KeycloakBackup>> keycloakBackupsClient();

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_BACKUP_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_BACKUP_RESOURCE} custom resource
	 */
	default MixedOperation<KeycloakBackup, KeycloakBackupList, Resource<KeycloakBackup>> buildKeycloakBackupsClient() {
		CustomResourceDefinition crd = retrieveCustomResourceDefinitions()
				.withName(KEYCLOAK_BACKUP_RESOURCE).get();
		CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
		if (!retrieveCustomResourceDefinitions().list().getItems().contains(KEYCLOAK_BACKUP_RESOURCE)) {
			throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
					KEYCLOAK_BACKUP_RESOURCE, KeycloakOperatorProvisioner.operatorId()));
		}

		return keycloakBackupsCustomResourcesClient(crdc);

	}

	/**
	 * Get a reference to keycloakbackup object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the {@link KeycloakBackup} custom resource
	 * @return A concrete {@link Resource} instance representing the {@link KeycloakBackup} resource definition
	 */
	default Resource<KeycloakBackup> keycloakBackup(String name) {
		return keycloakBackupsClient().withName(name);
	}

	/**
	 * Get all keycloakbackups maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link KeycloakBackup} resource definitions
	 */
	default List<Resource<KeycloakBackup>> keycloakBackups() {
		KeycloakOperatorApplication keycloakOperatorApplication = getApplication();
		return keycloakOperatorApplication.getKeycloakBackups().stream()
				.map(keycloakBackup -> keycloakBackup.getMetadata().getName())
				.map(this::keycloakBackup)
				.collect(Collectors.toList());
	}

	// keycloakclients.keycloak.org
	HasMetadataOperationsImpl<KeycloakClient, KeycloakClientList> keycloakClientsCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	NonNamespaceOperation<KeycloakClient, KeycloakClientList, Resource<KeycloakClient>> keycloakClientsClient();

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_CLIENT_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_CLIENT_RESOURCE} custom resource
	 */
	default MixedOperation<KeycloakClient, KeycloakClientList, Resource<KeycloakClient>> buildKeycloakClientsClient() {
		CustomResourceDefinition crd = retrieveCustomResourceDefinitions()
				.withName(KEYCLOAK_CLIENT_RESOURCE).get();
		CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
		if (!retrieveCustomResourceDefinitions().list().getItems().contains(KEYCLOAK_CLIENT_RESOURCE)) {
			throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
					KEYCLOAK_CLIENT_RESOURCE, KeycloakOperatorProvisioner.operatorId()));
		}

		return keycloakClientsCustomResourcesClient(crdc);
	}

	/**
	 * Get a reference to keycloakclient object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the {@link KeycloakClient} custom resource
	 * @return A concrete {@link Resource} instance representing the {@link KeycloakClient} resource definition
	 */
	default Resource<KeycloakClient> keycloakClient(String name) {
		return keycloakClientsClient().withName(name);
	}

	/**
	 * Get all keycloakclients maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link KeycloakClient} resource definitions
	 */
	default List<Resource<KeycloakClient>> keycloakClients() {
		KeycloakOperatorApplication keycloakOperatorApplication = getApplication();
		return keycloakOperatorApplication.getKeycloakClients().stream()
				.map(keycloakClient -> keycloakClient.getMetadata().getName())
				.map(this::keycloakClient)
				.collect(Collectors.toList());
	}

	// keycloakusers.keycloak.org
	HasMetadataOperationsImpl<KeycloakUser, KeycloakUserList> keycloakUsersCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	NonNamespaceOperation<KeycloakUser, KeycloakUserList, Resource<KeycloakUser>> keycloakUsersClient();

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_USER_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_USER_RESOURCE} custom resource
	 */
	default MixedOperation<KeycloakUser, KeycloakUserList, Resource<KeycloakUser>> buildKeycloakUsersClient() {
		CustomResourceDefinition crd = retrieveCustomResourceDefinitions()
				.withName(KEYCLOAK_USER_RESOURCE).get();
		CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
		if (!retrieveCustomResourceDefinitions().list().getItems().contains(KEYCLOAK_USER_RESOURCE)) {
			throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
					KEYCLOAK_USER_RESOURCE, KeycloakOperatorProvisioner.operatorId()));
		}
		return keycloakUsersCustomResourcesClient(crdc);
	}

	/**
	 * Get a reference to keycloakuser object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the keycloakuser custom resource
	 * @return A concrete {@link Resource} instance representing the {@link KeycloakUser} resource definition
	 */
	default Resource<KeycloakUser> keycloakUser(String name) {
		return keycloakUsersClient().withName(name);
	}

	/**
	 * Get all keycloakusers maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link KeycloakUser} resource definitions
	 */
	default List<Resource<KeycloakUser>> keycloakUsers() {
		KeycloakOperatorApplication keycloakOperatorApplication = getApplication();
		return keycloakOperatorApplication.getKeycloakUsers().stream()
				.map(keycloakUser -> keycloakUser.getMetadata().getName())
				.map(this::keycloakUser)
				.collect(Collectors.toList());
	}

	StatefulSet retrieveNamedStatefulSet(final String statefulSetName);

	/**
	 * @return the underlying StatefulSet which provisions the cluster
	 */
	private StatefulSet getStatefulSet() {
		StatefulSet statefulSet = retrieveNamedStatefulSet(STATEFUL_SET_NAME);
		if (Objects.isNull(statefulSet)) {
			throw new IllegalStateException(String.format(
					"Impossible to find StatefulSet with name=\"%s\"!",
					STATEFUL_SET_NAME));
		}
		return statefulSet;
	}
}
