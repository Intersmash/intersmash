package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user;

import java.util.Map;

import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.spec.KeycloakAPIUser;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.spec.KeycloakUserSpec;

import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class KeycloakUserBuilder {
	private String name;
	private Map<String, String> labels;
	private LabelSelector realmSelector;
	private KeycloakAPIUser user;

	/**
	 * Initialize the {@link KeycloakUserBuilder} with given resource name.
	 *
	 * @param name resource object name
	 */
	public KeycloakUserBuilder(String name) {
		this.name = name;
	}

	/**
	 * Initialize the {@link KeycloakUserBuilder} with given resource name and labels.
	 *
	 * @param name resource object name
	 * @param labels key/value pairs that are attached to objects
	 */
	public KeycloakUserBuilder(String name, Map<String, String> labels) {
		this.name = name;
		this.labels = labels;
	}

	/**
	 * Set the selector for looking up KeycloakRealm Custom Resources.
	 *
	 * @param realmSelector The selector for looking up KeycloakRealm Custom Resources
	 * @return this
	 */
	public KeycloakUserBuilder realmSelector(LabelSelector realmSelector) {
		this.realmSelector = realmSelector;
		return this;
	}

	/**
	 * Set the Keycloak User REST object.
	 *
	 * @param user The Keycloak User REST object.
	 * @return this
	 */
	public KeycloakUserBuilder user(KeycloakAPIUser user) {
		this.user = user;
		return this;
	}

	public KeycloakUser build() {
		KeycloakUser keycloakUser = new KeycloakUser();
		keycloakUser.setMetadata(new ObjectMeta());
		keycloakUser.getMetadata().setName(name);
		keycloakUser.getMetadata().setLabels(labels);

		KeycloakUserSpec keycloakUserSpec = new KeycloakUserSpec();
		keycloakUserSpec.setRealmSelector(realmSelector);
		keycloakUserSpec.setUser(user);
		keycloakUser.setSpec(keycloakUserSpec);
		return keycloakUser;
	}
}
