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
package org.jboss.intersmash.provision.operator.model.keycloak.realm.spec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.v1alpha1.keycloakrealmspec.realm.ClientScopes;
import org.keycloak.v1alpha1.keycloakrealmspec.realm.clientscopes.ProtocolMappers;

public final class KeycloakClientScopeBuilder {
	private Map<String, String> attributes;
	private String description;
	private String id;
	private String name;
	private String protocol;
	private List<ProtocolMappers> protocolMappers;

	/**
	 * Add a list of attributes.
	 *
	 * @param attributes A Map holding the attributes
	 * @return this
	 */
	public KeycloakClientScopeBuilder attributes(Map<String, String> attributes) {
		this.attributes = attributes;
		return this;
	}

	/**
	 * Set the Config.
	 *
	 * @param key A key for the attribute that should be added
	 * @param value A value for the attribute that should be added
	 * @return this
	 */
	public KeycloakClientScopeBuilder attributes(String key, String value) {
		if (attributes == null) {
			attributes = new HashMap<>();
		}
		attributes.put(key, value);
		return this;
	}

	/**
	 * Set the Config.
	 *
	 * @param description The Description that should be used
	 * @return this
	 */
	public KeycloakClientScopeBuilder description(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Set the ID.
	 *
	 * @param id The ID that should be used
	 * @return this
	 */
	public KeycloakClientScopeBuilder id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Set the Name.
	 *
	 * @param name The Name that should be used
	 * @return this
	 */
	public KeycloakClientScopeBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Set the Protocol.
	 *
	 * @param protocol The Protocol that should be used
	 * @return this
	 */
	public KeycloakClientScopeBuilder protocol(String protocol) {
		this.protocol = protocol;
		return this;
	}

	/**
	 * Set the Protocol Mappers.
	 *
	 * @param protocolMappers A list of protocol mappers that should be used
	 * @return this
	 */
	public KeycloakClientScopeBuilder protocolMappers(List<ProtocolMappers> protocolMappers) {
		this.protocolMappers = protocolMappers;
		return this;
	}

	public ClientScopes build() {
		ClientScopes keycloakClientScope = new ClientScopes();
		keycloakClientScope.setAttributes(attributes);
		keycloakClientScope.setDescription(description);
		keycloakClientScope.setId(id);
		keycloakClientScope.setName(name);
		keycloakClientScope.setProtocol(protocol);
		keycloakClientScope.setProtocolMappers(protocolMappers);
		return keycloakClientScope;
	}
}
