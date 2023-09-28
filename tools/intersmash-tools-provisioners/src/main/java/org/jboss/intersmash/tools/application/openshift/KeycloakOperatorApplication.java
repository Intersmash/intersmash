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
package org.jboss.intersmash.tools.application.openshift;

import java.util.Collections;
import java.util.List;

import org.jboss.intersmash.tools.provision.openshift.KeycloakOperatorProvisioner;
import org.keycloak.k8s.legacy.v1alpha1.ExternalKeycloak;
import org.keycloak.k8s.legacy.v1alpha1.ExternalKeycloakSpec;
import org.keycloak.k8s.legacy.v1alpha1.KeycloakClient;
import org.keycloak.k8s.legacy.v1alpha1.KeycloakRealm;
import org.keycloak.k8s.legacy.v1alpha1.KeycloakUser;
import org.keycloak.k8s.v2alpha1.Keycloak;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Service;

/**
 * End user Application interface which presents Keycloak operator application on OpenShift Container Platform.
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link KeycloakOperatorProvisioner}</li>
 * </ul>
 */
public interface KeycloakOperatorApplication extends OperatorApplication {

	//	Keycloak getKeycloak();

	Service getExternalKeycloakService();

	default ExternalKeycloak getExternalKeycloak() {
		ExternalKeycloakSpec externalKeycloakSpec = new ExternalKeycloakSpec();
		externalKeycloakSpec.setContextRoot("/");
		final String internalUrl = "http://" + getExternalKeycloakService().getSpec().getClusterIP() + ":8080";
		externalKeycloakSpec.setUrl(internalUrl);
		ExternalKeycloak externalKeycloak = new ExternalKeycloak();
		externalKeycloak.setSpec(externalKeycloakSpec);
		ObjectMeta metadata = new ObjectMeta();
		metadata.setName(getName());
		externalKeycloak.setMetadata(metadata);
		return externalKeycloak;
	}

	default List<KeycloakClient> getKeycloakClients() {
		return Collections.emptyList();
	}

	default List<KeycloakRealm> getKeycloakRealms() {
		return Collections.emptyList();
	}

	default List<KeycloakUser> getKeycloakUsers() {
		return Collections.emptyList();
	}
}
