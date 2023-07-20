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
package org.jboss.intersmash.tools.application.operator;

import java.util.Collections;
import java.util.List;

import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.backup.KeycloakBackup;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.client.KeycloakClient;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.Keycloak;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.KeycloakRealm;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.KeycloakUser;

/**
 * End user Application interface which presents Keycloak operator application on OpenShift Container Platform.
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link org.jboss.intersmash.tools.provision.operator.KeycloakOperatorProvisioner}</li>
 * </ul>
 */
public interface KeycloakOperatorApplication extends OperatorApplication {

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
