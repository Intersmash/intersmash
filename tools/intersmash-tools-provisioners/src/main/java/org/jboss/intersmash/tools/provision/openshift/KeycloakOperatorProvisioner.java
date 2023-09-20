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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Strings;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.KeycloakOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.operator.OperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.client.KeycloakClientList;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.external.ExternalKeycloakList;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.KeycloakRealmList;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.KeycloakUserList;
import org.keycloak.k8s.legacy.v1alpha1.ExternalKeycloak;
import org.keycloak.k8s.legacy.v1alpha1.KeycloakClient;
import org.keycloak.k8s.legacy.v1alpha1.KeycloakRealm;
import org.keycloak.k8s.legacy.v1alpha1.KeycloakUser;
import org.keycloak.k8s.v2alpha1.Keycloak;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
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
	//	private static final String KEYCLOAK_RESOURCE = "keycloaks.keycloak.org";
	//	private static NonNamespaceOperation<Keycloak, KeycloakList, Resource<Keycloak>> KEYCLOAKS_CLIENT;

	private static final String KEYCLOAK_REALM_RESOURCE = "keycloakrealms.keycloak.org";
	private static NonNamespaceOperation<KeycloakRealm, KeycloakRealmList, Resource<KeycloakRealm>> KEYCLOAK_REALMS_CLIENT;

	private static final String KEYCLOAK_EXTERNAL_KEYCLOAK_RESOURCE = "externalkeycloaks.legacy.k8s.keycloak.org";
	private static NonNamespaceOperation<ExternalKeycloak, ExternalKeycloakList, Resource<ExternalKeycloak>> KEYCLOAK_EXTERNAL_KEYCLOAK_CLIENT;

	private static final String KEYCLOAK_CLIENT_RESOURCE = "keycloakclients.keycloak.org";
	private static NonNamespaceOperation<KeycloakClient, KeycloakClientList, Resource<KeycloakClient>> KEYCLOAK_CLIENTS_CLIENT;

	private static final String KEYCLOAK_USER_RESOURCE = "keycloakusers.keycloak.org";
	private static NonNamespaceOperation<KeycloakUser, KeycloakUserList, Resource<KeycloakUser>> KEYCLOAK_USERS_CLIENT;

	// oc get packagemanifest rhsso-operator -n openshift-marketplace
	private static final String OPERATOR_ID = IntersmashConfig.keycloakOperatorPackageManifest();
	private static final String STATEFUL_SET_NAME = "keycloak";

	public KeycloakOperatorProvisioner(@NonNull KeycloakOperatorApplication keycloakOperatorApplication) {
		super(keycloakOperatorApplication, OPERATOR_ID);
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
			// RELATED_IMAGE_RHSSO_OPENJ9 and RELATED_IMAGE_RHSSO_OPENJDK, determine the final value for RELATED_IMAGE_RHSSO
			subscribe(
					INSTALLPLAN_APPROVAL_MANUAL,
					Map.of(
							"RELATED_IMAGE_RHSSO", IntersmashConfig.keycloakImageURL(),
							"PROFILE", "RHSSO"));
		}
	}

	@Override
	public void deploy() {
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				getApplication().getName());
		//		 Keycloak Operator codebase contains the name of the Keycloak image to deploy: user can override Keycloak image to
		//		 deploy using environment variables in Keycloak Operator Subscription
		subscribe();

		//		// create custom resources

		externalKeycloaksClient().resource(getApplication().getExternalKeycloak()).create();

		if (getApplication().getKeycloakRealms().size() > 0) {
			getApplication().getKeycloakRealms().stream().forEach((i) -> keycloakRealmsClient().resource(i).create());
		}
		if (getApplication().getKeycloakClients().size() > 0) {
			getApplication().getKeycloakClients().stream().forEach((i) -> keycloakClientsClient().resource(i).create());
		}
		if (getApplication().getKeycloakUsers().size() > 0) {
			getApplication().getKeycloakUsers().stream().forEach((i) -> keycloakUsersClient().resource(i).create());
		}

		//		// Wait for Keycloak (and PostgreSQL) to be ready
		//		waitFor(getApplication().getKeycloak());
		// wait for all resources to be ready
		waitForKeycloakResourceReadiness();
		//		// check that route is up, only if there's a valid external URL available
		//		URL externalUrl = getURL();
		//		if ((getApplication().getKeycloak().getSpec().getInstances() > 0) && (externalUrl != null)) {
		//			WaitersUtil.routeIsUp(externalUrl.toExternalForm())
		//					.level(Level.DEBUG)
		//					.waitFor();
		//		}
	}

	//	/**
	//	 * Wait for Keycloak and PostgreSQL (in case no external database is used) to be ready
	//	 *
	//	 * @param keycloak Concrete {@link Keycloak} instance which the method should be wait for
	//	 */
	//	public void waitFor(Keycloak keycloak) {
	//		Long replicas = keycloak.getSpec().getInstances();
	//		if (replicas > 0) {
	//			// wait for >= 1 pods with label controller-revision-hash=keycloak-d86bb6ddc
	//			String controllerRevisionHash = getStatefulSet().getStatus().getUpdateRevision();
	//			OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
	//					.areExactlyNPodsReady(replicas.intValue(), "controller-revision-hash",
	//							controllerRevisionHash)
	//					.waitFor();
	//		}
	//	}

	private void waitForKeycloakResourceReadiness() {
		//		new SimpleWaiter(
		//				() -> keycloak().get().getStatus().getConditions().stream().anyMatch(
		//						condition -> "Ready".equalsIgnoreCase(condition.getType())
		//								&& condition.getStatus() != null))
		//				.reason("Wait for keycloak resource to be ready").level(Level.DEBUG).waitFor();

		new SimpleWaiter(() -> externalKeycloak(getApplication().getName()).get().getStatus().getReady() == true)
				.reason("Wait for externalkeycloak to be ready.").level(Level.DEBUG).waitFor();
		if (getApplication().getKeycloakRealms().size() > 0)
			new SimpleWaiter(() -> keycloakRealms().stream().map(realm -> realm.get().getStatus().getReady())
					.reduce(Boolean::logicalAnd).get())
					.reason("Wait for keycloakrealms to be ready.").level(Level.DEBUG).waitFor();
		if (getApplication().getKeycloakClients().size() > 0)
			new SimpleWaiter(() -> keycloakClients().stream().map(realm -> realm.get().getStatus().getReady())
					.reduce(Boolean::logicalAnd).get())
					.reason("Wait for keycloakclients to be ready.").level(Level.DEBUG).waitFor();
		if (getApplication().getKeycloakUsers().size() > 0)
			new SimpleWaiter(() -> keycloakUsersClient().list().getItems().size() == getApplication().getKeycloakUsers().size())
					.reason("Wait for keycloakusers to be ready.").level(Level.DEBUG).waitFor(); // no isReady() for users
	}

	@Override
	public void undeploy() {
		// delete custom resources
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
		//		keycloak().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		//		new SimpleWaiter(() -> keycloaksClient().list().getItems().size() == 0)
		//				.reason("Wait for all keycloakrealms instances to be deleted.").level(Level.DEBUG).waitFor();

		//		// wait for 0 pods
		//		OpenShiftWaiters.get(OpenShiftProvisioner.openShift, () -> false)
		//				.areExactlyNPodsReady(0, "app", getApplication().getKeycloak().getKind().toLowerCase()).level(Level.DEBUG)
		//				.waitFor();
		unsubscribe();
	}

	//	@Override
	//	public void scale(int replicas, boolean wait) {
	//		String controllerRevisionHash = getStatefulSet().getStatus().getUpdateRevision();
	//		Keycloak tmpKeycloak = keycloak().get();
	//		Long originalReplicas = tmpKeycloak.getSpec().getInstances();
	//		tmpKeycloak.getSpec().setInstances(Integer.toUnsignedLong(replicas));
	//		keycloak().replace(tmpKeycloak);
	//		if (wait) {
	//			OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
	//					.areExactlyNPodsReady(replicas, "controller-revision-hash", controllerRevisionHash)
	//					.level(Level.DEBUG)
	//					.waitFor();
	//		}
	//		new SimpleWaiter(
	//				() -> keycloak().get().getStatus().getConditions().stream().anyMatch(
	//						condition -> "Ready".equalsIgnoreCase(condition.getType())
	//								&& condition.getStatus() != null))
	//				.reason("Wait for Keycloak resource to be ready").level(Level.DEBUG).waitFor();
	//		// check that route is up
	//		if (originalReplicas == 0 && replicas > 0) {
	//			WaitersUtil.routeIsUp(getURL().toExternalForm())
	//					.level(Level.DEBUG)
	//					.waitFor();
	//		}
	//	}

	@Override
	public List<Pod> getPods() {
		StatefulSet statefulSet = OpenShiftProvisioner.openShift.getStatefulSet(STATEFUL_SET_NAME);
		return Objects.nonNull(statefulSet)
				? OpenShiftProvisioner.openShift.getLabeledPods("controller-revision-hash",
						statefulSet.getStatus().getUpdateRevision())
				: Lists.emptyList();
	}

	//	@Override
	//	public URL getURL() {
	//		String host = OpenShiftProvisioner.openShift.routes().list().getItems()
	//				.stream().filter(
	//						route -> route.getMetadata().getName().startsWith(
	//								keycloak().get().getMetadata().getName())
	//								&&
	//								route.getMetadata().getLabels().entrySet()
	//										.stream().filter(
	//												label -> label.getKey().equalsIgnoreCase("app")
	//														&&
	//														label.getValue().equalsIgnoreCase(
	//																keycloak().get().getMetadata().getLabels().get("app")))
	//										.count() == 1
	//
	//				).findFirst()
	//				.orElseThrow(() -> new RuntimeException(
	//						String.format("No route for Keycloak %s!", keycloak().get().getMetadata().getName())))
	//				.getSpec().getHost();
	//		try {
	//			return Strings.isNullOrEmpty(host) ? null : new URL(String.format("https://%s", host));
	//		} catch (MalformedURLException e) {
	//			throw new RuntimeException(String.format("Keycloak operator External URL \"%s\" is malformed.", host), e);
	//		}
	//	}

	//	// keycloaks.keycloak.org
	//
	//	/**
	//	 * Get a client capable of working with {@link #KEYCLOAK_RESOURCE} custom resource.
	//	 *
	//	 * @return client for operations with {@link #KEYCLOAK_RESOURCE} custom resource
	//	 */
	//	public NonNamespaceOperation<Keycloak, KeycloakList, Resource<Keycloak>> keycloaksClient() {
	//		if (KEYCLOAKS_CLIENT == null) {
	//			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
	//					.withName(KEYCLOAK_RESOURCE).get();
	//			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
	//			if (!getCustomResourceDefinitions().contains(KEYCLOAK_RESOURCE)) {
	//				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
	//						KEYCLOAK_RESOURCE, OPERATOR_ID));
	//			}
	//			MixedOperation<Keycloak, KeycloakList, Resource<Keycloak>> keycloaksClient = OpenShifts
	//					.master()
	//					.newHasMetadataOperation(crdc, Keycloak.class, KeycloakList.class);
	//			KEYCLOAKS_CLIENT = keycloaksClient.inNamespace(OpenShiftConfig.namespace());
	//		}
	//		return KEYCLOAKS_CLIENT;
	//	}

	//	/**
	//	 * Get a reference to keycloak object. Use get() to get the actual object, or null in case it does not
	//	 * exist on tested cluster.
	//	 * @return A concrete {@link Resource} instance representing the {@link Keycloak} resource definition
	//	 */
	//	public Resource<Keycloak> keycloak() {
	//		return keycloaksClient().withName(getApplication().getKeycloak().getMetadata().getName());
	//	}

	// keycloakrealms.keycloak.org

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_REALM_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_REALM_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<KeycloakRealm, KeycloakRealmList, Resource<KeycloakRealm>> keycloakRealmsClient() {
		if (KEYCLOAK_REALMS_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(KEYCLOAK_REALM_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(KEYCLOAK_REALM_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_REALM_RESOURCE, OPERATOR_ID));
			}
			MixedOperation<KeycloakRealm, KeycloakRealmList, Resource<KeycloakRealm>> keycloakRealmsClient = OpenShifts
					.master()
					.newHasMetadataOperation(crdc, KeycloakRealm.class, KeycloakRealmList.class);
			KEYCLOAK_REALMS_CLIENT = keycloakRealmsClient.inNamespace(OpenShiftConfig.namespace());
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
		return keycloakRealmsClient().withName(name);
	}

	/**
	 * Get all keycloakrealms maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link KeycloakRealm} resource definitions
	 */
	public List<Resource<KeycloakRealm>> keycloakRealms() {
		KeycloakOperatorApplication keycloakOperatorApplication = getApplication();
		return keycloakOperatorApplication.getKeycloakRealms().stream()
				.map(keycloakRealm -> keycloakRealm.getMetadata().getName())
				.map(this::keycloakRealm)
				.collect(Collectors.toList());
	}

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_EXTERNAL_KEYCLOAK_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_EXTERNAL_KEYCLOAK_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<ExternalKeycloak, ExternalKeycloakList, Resource<ExternalKeycloak>> externalKeycloaksClient() {
		if (KEYCLOAK_EXTERNAL_KEYCLOAK_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(KEYCLOAK_EXTERNAL_KEYCLOAK_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(KEYCLOAK_EXTERNAL_KEYCLOAK_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_EXTERNAL_KEYCLOAK_RESOURCE, OPERATOR_ID));
			}
			MixedOperation<ExternalKeycloak, ExternalKeycloakList, Resource<ExternalKeycloak>> keycloakBackupsClient = OpenShifts
					.master()
					.newHasMetadataOperation(crdc, ExternalKeycloak.class, ExternalKeycloakList.class);
			KEYCLOAK_EXTERNAL_KEYCLOAK_CLIENT = keycloakBackupsClient.inNamespace(OpenShiftConfig.namespace());
		}
		return KEYCLOAK_EXTERNAL_KEYCLOAK_CLIENT;
	}

	/**
	 * Get a reference to externalkeycloak object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the {@link ExternalKeycloak} custom resource
	 * @return A concrete {@link Resource} instance representing the {@link ExternalKeycloak} resource definition
	 */
	public Resource<ExternalKeycloak> externalKeycloak(String name) {
		return externalKeycloaksClient().withName(name);
	}

	// keycloakclients.keycloak.org

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_CLIENT_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_CLIENT_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<KeycloakClient, KeycloakClientList, Resource<KeycloakClient>> keycloakClientsClient() {
		if (KEYCLOAK_CLIENTS_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(KEYCLOAK_CLIENT_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(KEYCLOAK_CLIENT_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_CLIENT_RESOURCE, OPERATOR_ID));
			}
			MixedOperation<KeycloakClient, KeycloakClientList, Resource<KeycloakClient>> keycloakClientsClient = OpenShifts
					.master()
					.newHasMetadataOperation(crdc, KeycloakClient.class, KeycloakClientList.class);
			KEYCLOAK_CLIENTS_CLIENT = keycloakClientsClient.inNamespace(OpenShiftConfig.namespace());
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
		return keycloakClientsClient().withName(name);
	}

	/**
	 * Get all keycloakclients maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link KeycloakClient} resource definitions
	 */
	public List<Resource<KeycloakClient>> keycloakClients() {
		KeycloakOperatorApplication keycloakOperatorApplication = getApplication();
		return keycloakOperatorApplication.getKeycloakClients().stream()
				.map(keycloakClient -> keycloakClient.getMetadata().getName())
				.map(this::keycloakClient)
				.collect(Collectors.toList());
	}

	// keycloakusers.keycloak.org

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_USER_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_USER_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<KeycloakUser, KeycloakUserList, Resource<KeycloakUser>> keycloakUsersClient() {
		if (KEYCLOAK_USERS_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(KEYCLOAK_USER_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(KEYCLOAK_USER_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_USER_RESOURCE, OPERATOR_ID));
			}
			MixedOperation<KeycloakUser, KeycloakUserList, Resource<KeycloakUser>> keycloakUsersClient = OpenShifts
					.master()
					.newHasMetadataOperation(crdc, KeycloakUser.class, KeycloakUserList.class);
			KEYCLOAK_USERS_CLIENT = keycloakUsersClient.inNamespace(OpenShiftConfig.namespace());
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
		return keycloakUsersClient().withName(name);
	}

	/**
	 * Get all keycloakusers maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link KeycloakUser} resource definitions
	 */
	public List<Resource<KeycloakUser>> keycloakUsers() {
		KeycloakOperatorApplication keycloakOperatorApplication = getApplication();
		return keycloakOperatorApplication.getKeycloakUsers().stream()
				.map(keycloakUser -> keycloakUser.getMetadata().getName())
				.map(this::keycloakUser)
				.collect(Collectors.toList());
	}

//	/**
//	 * @return the underlying StatefulSet which provisions the cluster
//	 */
//	private StatefulSet getStatefulSet() {
//		StatefulSet statefulSet = OpenShiftProvisioner.openShift.getStatefulSet(STATEFUL_SET_NAME);
//		if (Objects.isNull(statefulSet)) {
//			throw new IllegalStateException(String.format(
//					"Impossible to find StatefulSet with name=\"%s\"!",
//					STATEFUL_SET_NAME));
//		}
//		return statefulSet;
//	}

	@Override
	public void scale(int replicas, boolean wait) {
		throw new UnsupportedOperationException(
				"The scale operation is not implemented by this provisioner. The current Keycloak provisioner should be used");
	}
}
