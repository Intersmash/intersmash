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
package org.jboss.intersmash.provision.openshift;

import java.util.Map;

import org.assertj.core.util.Strings;
import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.operator.RhSsoOperatorApplication;
import org.jboss.intersmash.provision.operator.RhSsoOperatorProvisioner;
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

import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClientAdapter;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import lombok.NonNull;

/**
 * Keycloak operator provisioner
 */
@Deprecated(since = "0.0.2")
public class RhSsoOpenShiftOperatorProvisioner
		// leverage Red Hat SSO common Operator based provisioner behavior
		extends RhSsoOperatorProvisioner<NamespacedOpenShiftClient>
		// ... and common OpenShift provisioning logic, too
		implements OpenShiftProvisioner<RhSsoOperatorApplication> {

	public RhSsoOpenShiftOperatorProvisioner(@NonNull RhSsoOperatorApplication application) {
		super(application);
	}

	@Override
	public NamespacedKubernetesClientAdapter<NamespacedOpenShiftClient> client() {
		return OpenShiftProvisioner.super.client();
	}

	@Override
	public String execute(String... args) {
		return OpenShiftProvisioner.super.execute(args);
	}

	// =================================================================================================================
	// Operator based provisioning concrete implementation, see OperatorProvisioner
	// =================================================================================================================
	@Override
	public void subscribe() {
		if (Strings.isNullOrEmpty(IntersmashConfig.rhSsoImageURL())) {
			super.subscribe();
		} else {
			// RELATED_IMAGE_RHSSO_OPENJ9 and RELATED_IMAGE_RHSSO_OPENJDK, determine the final value for RELATED_IMAGE_RHSSO
			subscribe(
					INSTALLPLAN_APPROVAL_MANUAL,
					Map.of(
							"RELATED_IMAGE_RHSSO", IntersmashConfig.rhSsoImageURL(),
							"PROFILE", "RHSSO"));
		}
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}

	@Override
	public HasMetadataOperationsImpl<Keycloak, KeycloakList> keycloakCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, Keycloak.class, KeycloakList.class);
	}

	@Override
	public HasMetadataOperationsImpl<KeycloakRealm, KeycloakRealmList> keycloakRealmCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, KeycloakRealm.class, KeycloakRealmList.class);
	}

	@Override
	public HasMetadataOperationsImpl<KeycloakClient, KeycloakClientList> keycloakClientCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, KeycloakClient.class, KeycloakClientList.class);
	}

	@Override
	public HasMetadataOperationsImpl<KeycloakBackup, KeycloakBackupList> keycloakBackupCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, KeycloakBackup.class, KeycloakBackupList.class);
	}

	@Override
	public HasMetadataOperationsImpl<KeycloakUser, KeycloakUserList> keycloakUserCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, KeycloakUser.class, KeycloakUserList.class);
	}
}
