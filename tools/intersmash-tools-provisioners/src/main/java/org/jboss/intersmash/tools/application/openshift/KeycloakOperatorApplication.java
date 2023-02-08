package org.jboss.intersmash.tools.application.openshift;

import java.util.Collections;
import java.util.List;

import org.jboss.intersmash.tools.provision.openshift.KeycloakOperatorProvisioner;
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
 *     <li>{@link KeycloakOperatorProvisioner}</li>
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
