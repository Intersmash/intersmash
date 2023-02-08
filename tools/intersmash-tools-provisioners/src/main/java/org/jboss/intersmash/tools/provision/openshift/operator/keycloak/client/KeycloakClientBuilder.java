package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.client;

import java.util.Map;

import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.client.spec.KeycloakAPIClient;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.client.spec.KeycloakClientSpec;

import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class KeycloakClientBuilder {
	private String name;
	private Map<String, String> labels;
	private LabelSelector realmSelector;
	private KeycloakAPIClient client;

	/**
	 * Initialize the {@link KeycloakClientBuilder} with given resource name.
	 *
	 * @param name resource object name
	 */
	public KeycloakClientBuilder(String name) {
		this.name = name;
	}

	/**
	 * Initialize the {@link KeycloakClientBuilder} with given resource name and labels.
	 *
	 * @param name resource object name
	 * @param labels key/value pairs that are attached to objects
	 */
	public KeycloakClientBuilder(String name, Map<String, String> labels) {
		this.name = name;
		this.labels = labels;
	}

	/**
	 * Set the selector for looking up KeycloakRealm Custom Resources.
	 *
	 * @param realmSelector Label selector for looking up KeycloakRealm Custom Resources.
	 * @return this
	 */
	public KeycloakClientBuilder realmSelector(LabelSelector realmSelector) {
		this.realmSelector = realmSelector;
		return this;
	}

	/**
	 * Set the Keycloak REST API client.
	 *
	 * @param client Keycloak REST API client.
	 * @return this
	 */
	public KeycloakClientBuilder client(KeycloakAPIClient client) {
		this.client = client;
		return this;
	}

	public KeycloakClient build() {
		KeycloakClient keycloakClient = new KeycloakClient();
		keycloakClient.setMetadata(new ObjectMeta());
		keycloakClient.getMetadata().setName(name);
		keycloakClient.getMetadata().setLabels(labels);

		KeycloakClientSpec keycloakClientSpec = new KeycloakClientSpec();
		keycloakClientSpec.setRealmSelector(realmSelector);
		keycloakClientSpec.setClient(client);
		keycloakClient.setSpec(keycloakClientSpec);
		return keycloakClient;
	}
}
