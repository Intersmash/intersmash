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
package org.jboss.intersmash.application.operator;

import java.util.Collections;
import java.util.List;

import org.keycloak.v1alpha1.Keycloak;
import org.keycloak.v1alpha1.KeycloakBackup;
import org.keycloak.v1alpha1.KeycloakClient;
import org.keycloak.v1alpha1.KeycloakRealm;
import org.keycloak.v1alpha1.KeycloakUser;

/**
 * End user Application interface which presents Keycloak operator application on OpenShift Container Platform.
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link org.jboss.intersmash.provision.operator.RhSsoOperatorProvisioner}</li>
 * </ul>
 */
@Deprecated(since = "0.0.2")
public interface RhSsoOperatorApplication extends OperatorApplication {

	Keycloak getKeycloak();

	default List<KeycloakBackup> getKeycloakBackups() {
		return Collections.emptyList();
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
