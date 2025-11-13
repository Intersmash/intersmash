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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.assertj.core.util.Strings;
import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.operator.KeycloakOperatorApplication;
import org.jboss.intersmash.provision.operator.KeycloakOperatorProvisioner;
import org.keycloak.k8s.v2alpha1.Keycloak;
import org.keycloak.k8s.v2alpha1.KeycloakOperatorKeycloakList;
import org.keycloak.k8s.v2alpha1.KeycloakOperatorRealmImportList;
import org.keycloak.k8s.v2alpha1.KeycloakRealmImport;

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
public class KeycloakOpenShiftOperatorProvisioner
		// leverage Wildfly common Operator based provisioner behavior
		extends KeycloakOperatorProvisioner<NamespacedOpenShiftClient>
		// ... and common OpenShift provisioning logic
		implements OpenShiftProvisioner<KeycloakOperatorApplication> {

	public KeycloakOpenShiftOperatorProvisioner(@NonNull KeycloakOperatorApplication application) {
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

	@Override
	public String executeInNamespace(String namespace, String... args) {
		return OpenShiftProvisioner.super.executeInNamespace(namespace, args);
	}

	// =================================================================================================================
	// Operator based provisioning concrete implementation, see OperatorProvisioner
	// =================================================================================================================
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
	public URL getURL() {
		String host = OpenShiftProvisioner.openShift.routes().list().getItems()
				.stream().filter(
						route -> route.getMetadata().getName().startsWith(
								keycloak().get().getMetadata().getName())
								&&
								route.getMetadata().getLabels().entrySet()
										.stream().filter(
												label -> label.getKey().equalsIgnoreCase("app.kubernetes.io/instance")
														&&
														label.getValue().equalsIgnoreCase(
																keycloak().get().getMetadata().getLabels()
																		.get("app")))
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

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}

	@Override
	public HasMetadataOperationsImpl<Keycloak, KeycloakOperatorKeycloakList> keycloaksCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, Keycloak.class, KeycloakOperatorKeycloakList.class);
	}

	@Override
	public HasMetadataOperationsImpl<KeycloakRealmImport, KeycloakOperatorRealmImportList> keycloakRealmImportsCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, KeycloakRealmImport.class, KeycloakOperatorRealmImportList.class);
	}
}
