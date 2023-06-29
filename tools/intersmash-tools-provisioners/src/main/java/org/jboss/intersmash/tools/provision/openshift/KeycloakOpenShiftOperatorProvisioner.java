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

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Strings;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.operator.KeycloakOperatorApplication;
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
import org.jboss.intersmash.tools.provision.operator.KeycloakOperatorProvisioner;
import org.jboss.intersmash.tools.provision.operator.OperatorProvisioner;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import lombok.NonNull;

/**
 * Keycloak operator provisioner
 */
public class KeycloakOpenShiftOperatorProvisioner
		// default Operator based provisioner behavior, which implements OLM workflow contract and leverage lifecycle as well
		extends OperatorProvisioner<KeycloakOperatorApplication>
		// leverage Wildfly common Operator based provisioner behavior
		implements KeycloakOperatorProvisioner,
		// leverage common OpenShift provisioning logic
		OpenShiftProvisioner<KeycloakOperatorApplication> {
	private static NonNamespaceOperation<Keycloak, KeycloakList, Resource<Keycloak>> KEYCLOAKS_CLIENT;
	private static NonNamespaceOperation<KeycloakRealm, KeycloakRealmList, Resource<KeycloakRealm>> KEYCLOAK_REALMS_CLIENT;
	private static NonNamespaceOperation<KeycloakBackup, KeycloakBackupList, Resource<KeycloakBackup>> KEYCLOAK_BACKUPS_CLIENT;
	private static NonNamespaceOperation<KeycloakClient, KeycloakClientList, Resource<KeycloakClient>> KEYCLOAK_CLIENTS_CLIENT;
	private static NonNamespaceOperation<KeycloakUser, KeycloakUserList, Resource<KeycloakUser>> KEYCLOAK_USERS_CLIENT;

	public KeycloakOpenShiftOperatorProvisioner(@NonNull KeycloakOperatorApplication keycloakOperatorApplication) {
		super(keycloakOperatorApplication, KeycloakOperatorProvisioner.operatorId());
	}

	@Override
	public String execute(String... args) {
		return OpenShifts.adminBinary().execute(args);
	}

	@Override
	public URL getURL() {
		return KeycloakOperatorProvisioner.super.getURL();
	}

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
	public void scale(int replicas, boolean wait) {
		KeycloakOperatorProvisioner.super.scale(replicas, wait);
	}

	@Override
	public List<Pod> getPods() {
		return KeycloakOperatorProvisioner.super.getPods();
	}

	@Override
	public HasMetadataOperationsImpl<Keycloak, KeycloakList> keycloaksCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().customResources(crdc, Keycloak.class, KeycloakList.class);
	}

	@Override
	protected String getOperatorNamespace() {
		return "openshift-marketplace";
	}

	@Override
	public List<Pod> retrievePods() {
		return OpenShiftProvisioner.openShift.getPods();
	}

	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> retrieveCustomResourceDefinitions() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
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
	protected String getTargetNamespace() {
		return OpenShiftConfig.namespace();
	}

	// keycloaks.keycloak.org

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<Keycloak, KeycloakList, Resource<Keycloak>> keycloaksClient() {
		if (KEYCLOAKS_CLIENT == null) {
			KEYCLOAKS_CLIENT = buildKeycloaksClient().inNamespace(OpenShiftConfig.namespace());
		}
		return KEYCLOAKS_CLIENT;
	}

	@Override
	public HasMetadataOperationsImpl<KeycloakRealm, KeycloakRealmList> keycloakRealmsCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().customResources(crdc, KeycloakRealm.class, KeycloakRealmList.class);
	}

	// keycloakrealms.keycloak.org

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_REALM_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_REALM_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<KeycloakRealm, KeycloakRealmList, Resource<KeycloakRealm>> keycloakRealmsClient() {
		if (KEYCLOAK_REALMS_CLIENT == null) {
			KEYCLOAK_REALMS_CLIENT = buildKeycloakRealmsClient().inNamespace(OpenShiftConfig.namespace());
		}
		return KEYCLOAK_REALMS_CLIENT;
	}

	@Override
	public HasMetadataOperationsImpl<KeycloakBackup, KeycloakBackupList> keycloakBackupsCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().customResources(crdc, KeycloakBackup.class, KeycloakBackupList.class);
	}

	// keycloakbackups.keycloak.org

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_BACKUP_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_BACKUP_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<KeycloakBackup, KeycloakBackupList, Resource<KeycloakBackup>> keycloakBackupsClient() {
		if (KEYCLOAK_BACKUPS_CLIENT == null) {
			KEYCLOAK_BACKUPS_CLIENT = buildKeycloakBackupsClient().inNamespace(OpenShiftConfig.namespace());
		}
		return KEYCLOAK_BACKUPS_CLIENT;
	}

	@Override
	public HasMetadataOperationsImpl<KeycloakClient, KeycloakClientList> keycloakClientsCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().customResources(crdc, KeycloakClient.class, KeycloakClientList.class);
	}

	// keycloakclients.keycloak.org

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_CLIENT_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_CLIENT_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<KeycloakClient, KeycloakClientList, Resource<KeycloakClient>> keycloakClientsClient() {
		if (KEYCLOAK_CLIENTS_CLIENT == null) {
			KEYCLOAK_CLIENTS_CLIENT = buildKeycloakClientsClient().inNamespace(OpenShiftConfig.namespace());
		}
		return KEYCLOAK_CLIENTS_CLIENT;
	}

	@Override
	public HasMetadataOperationsImpl<KeycloakUser, KeycloakUserList> keycloakUsersCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().customResources(crdc, KeycloakUser.class, KeycloakUserList.class);
	}

	// keycloakusers.keycloak.org

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_USER_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_USER_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<KeycloakUser, KeycloakUserList, Resource<KeycloakUser>> keycloakUsersClient() {
		if (KEYCLOAK_USERS_CLIENT == null) {
			KEYCLOAK_USERS_CLIENT = buildKeycloakUsersClient().inNamespace(OpenShiftConfig.namespace());
		}
		return KEYCLOAK_USERS_CLIENT;
	}

	@Override
	public StatefulSet retrieveNamedStatefulSet(String statefulSetName) {
		return OpenShiftProvisioner.openShift.getStatefulSet(statefulSetName);
	}
}
