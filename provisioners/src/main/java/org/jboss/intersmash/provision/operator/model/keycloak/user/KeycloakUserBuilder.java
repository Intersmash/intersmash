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
package org.jboss.intersmash.provision.operator.model.keycloak.user;

import java.util.Map;

import org.keycloak.v1alpha1.KeycloakUser;
import org.keycloak.v1alpha1.KeycloakUserSpec;
import org.keycloak.v1alpha1.keycloakuserspec.RealmSelector;
import org.keycloak.v1alpha1.keycloakuserspec.User;

import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class KeycloakUserBuilder {
	private String name;
	private Map<String, String> labels;
	private RealmSelector realmSelector;
	private User user;

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
	public KeycloakUserBuilder realmSelector(RealmSelector realmSelector) {
		this.realmSelector = realmSelector;
		return this;
	}

	/**
	 * Set the Keycloak User REST object.
	 *
	 * @param user The Keycloak User REST object.
	 * @return this
	 */
	public KeycloakUserBuilder user(User user) {
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
