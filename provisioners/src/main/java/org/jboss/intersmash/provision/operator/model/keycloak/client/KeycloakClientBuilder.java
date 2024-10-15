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
package org.jboss.intersmash.provision.operator.model.keycloak.client;

import java.util.Map;

import org.keycloak.v1alpha1.KeycloakClient;
import org.keycloak.v1alpha1.KeycloakClientSpec;
import org.keycloak.v1alpha1.keycloakclientspec.Client;
import org.keycloak.v1alpha1.keycloakclientspec.RealmSelector;

import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class KeycloakClientBuilder {
	private String name;
	private Map<String, String> labels;
	private RealmSelector realmSelector;
	private Client client;

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
	public KeycloakClientBuilder realmSelector(RealmSelector realmSelector) {
		this.realmSelector = realmSelector;
		return this;
	}

	/**
	 * Set the Keycloak REST API client.
	 *
	 * @param client Keycloak REST API client.
	 * @return this
	 */
	public KeycloakClientBuilder client(Client client) {
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
