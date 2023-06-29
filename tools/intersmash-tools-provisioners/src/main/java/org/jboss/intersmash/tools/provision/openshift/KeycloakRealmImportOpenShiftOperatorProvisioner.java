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
import org.jboss.intersmash.tools.application.operator.KeycloakRealmImportOperatorApplication;
import org.jboss.intersmash.tools.provision.operator.KeycloakRealmImportOperatorProvisioner;
import org.jboss.intersmash.tools.provision.operator.OperatorProvisioner;
import org.keycloak.k8s.v2alpha1.Keycloak;
import org.keycloak.k8s.v2alpha1.KeycloakRealmImport;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.fabric8.openshift.api.model.Route;
import lombok.NonNull;

/**
 * Keycloak operator provisioner
 */
public class KeycloakRealmImportOpenShiftOperatorProvisioner
		// default Operator based provisioner behavior, which implements OLM workflow contract and leverage lifecycle as well
		extends OperatorProvisioner<KeycloakRealmImportOperatorApplication>
		// leverage Wildfly common Operator based provisioner behavior
		implements KeycloakRealmImportOperatorProvisioner,
		// leverage common OpenShift provisioning logic
		OpenShiftProvisioner<KeycloakRealmImportOperatorApplication> {

	public KeycloakRealmImportOpenShiftOperatorProvisioner(@NonNull KeycloakRealmImportOperatorApplication application) {
		super(application, KeycloakRealmImportOperatorProvisioner.OPERATOR_ID);
	}

	@Override
	public String getOperatorCatalogSource() {

		return KeycloakRealmImportOperatorProvisioner.super.getOperatorCatalogSource();
	}

	@Override
	public String getOperatorIndexImage() {

		return KeycloakRealmImportOperatorProvisioner.super.getOperatorIndexImage();
	}

	@Override
	public String getOperatorChannel() {
		return KeycloakRealmImportOperatorProvisioner.super.getOperatorChannel();
	}

	@Override
	public Service retrieveNamedService(String serviceName) {
		return OpenShiftProvisioner.openShift.getService(serviceName);
	}

	@Override
	public StatefulSet retrieveNamedStatefulSet(String statefulSetName) {
		return OpenShiftProvisioner.openShift.getStatefulSet(statefulSetName);
	}

	@Override
	public List<Route> retrieveRoutes() {
		return OpenShiftProvisioner.openShift.getRoutes();
	}

	@Override
	public List<Pod> retrieveNamespacePods() {
		return OpenShiftProvisioner.openShift.getPods();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		KeycloakRealmImportOperatorProvisioner.super.scale(replicas, wait);
	}

	@Override
	public URL getURL() {
		return KeycloakRealmImportOperatorProvisioner.super.getURL();
	}

	@Override
	public HasMetadataOperationsImpl<Keycloak, KubernetesResourceList<Keycloak>> keycloaksCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().customResources(crdc, Keycloak.class, KubernetesResourceList.class);
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
	public String execute(String... args) {
		return OpenShifts.adminBinary().execute(args);
	}

	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> retrieveCustomResourceDefinitions() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}

	@Override
	public void subscribe() {
		if (Strings.isNullOrEmpty(IntersmashConfig.keycloakRealmImportImageURL())) {
			super.subscribe();
		} else {
			subscribe(
					INSTALLPLAN_APPROVAL_MANUAL,
					Map.of(
							// Custom Keycloak image to be used: overrides the Keycloak image at the operator level: all
							// Keycloak instances will be spun out of this image
							// e.g. OPERATOR_KEYCLOAK_IMAGE=quay.io/keycloak/keycloak:21.1.1 --> operator.keycloak.image
							"OPERATOR_KEYCLOAK_IMAGE", IntersmashConfig.keycloakRealmImportImageURL()));
		}
	}

	@Override
	protected String getTargetNamespace() {
		return OpenShiftConfig.namespace();
	}

	private static NonNamespaceOperation<Keycloak, KubernetesResourceList<Keycloak>, Resource<Keycloak>> KEYCLOAKS_CLIENT;
	private static NonNamespaceOperation<KeycloakRealmImport, KubernetesResourceList<KeycloakRealmImport>, Resource<KeycloakRealmImport>> KEYCLOAK_REALM_IMPORTS_CLIENT;

	public NonNamespaceOperation<Keycloak, KubernetesResourceList<Keycloak>, Resource<Keycloak>> keycloakClient() {
		if (KEYCLOAKS_CLIENT == null) {
			KEYCLOAKS_CLIENT = buildKeycloakClient().inNamespace(OpenShiftConfig.namespace());
		}
		return KEYCLOAKS_CLIENT;
	}

	@Override
	public HasMetadataOperationsImpl<KeycloakRealmImport, KubernetesResourceList<KeycloakRealmImport>> keycloakRealmImportsCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return null;
	}

	public NonNamespaceOperation<KeycloakRealmImport, KubernetesResourceList<KeycloakRealmImport>, Resource<KeycloakRealmImport>> keycloakRealmImportClient() {

		if (KEYCLOAK_REALM_IMPORTS_CLIENT == null) {
			KEYCLOAK_REALM_IMPORTS_CLIENT = buildKeycloakRealmImportClient().inNamespace(OpenShiftConfig.namespace());
		}
		return KEYCLOAK_REALM_IMPORTS_CLIENT;
	}
}
