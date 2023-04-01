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
package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec;

import java.util.HashMap;
import java.util.Map;

public final class KeycloakAPIUserFederationMapperBuilder {
	private Map<String, String> config;
	private String name;
	private String id;
	private String federationMapperType;
	private String federationProviderDisplayName;

	/**
	 * User federation mapper config.
	 *
	 * @param config A {@link Map} storing the user federation mapper config
	 * @return this
	 */
	public KeycloakAPIUserFederationMapperBuilder config(Map<String, String> config) {
		this.config = config;
		return this;
	}

	/**
	 * Add one user federation mapper config.
	 *
	 * @param key The name of one property of the user federation mapper config
	 * @param value A value for one property of the user federation mapper config
	 * @return this
	 */
	public KeycloakAPIUserFederationMapperBuilder config(String key, String value) {
		if (config == null) {
			config = new HashMap<>();
		}
		config.put(key, value);
		return this;
	}

	/**
	 * Set the name of this mapper instance.
	 *
	 * @param name The name of this mapper instance
	 * @return this
	 */
	public KeycloakAPIUserFederationMapperBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Set the ID of this mapper instance.
	 *
	 * @param id The ID of this mapper instance
	 * @return this
	 */
	public KeycloakAPIUserFederationMapperBuilder id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Set the type of this mapper instance.
	 *
	 * @param federationMapperType The type of this mapper instance
	 * @return this
	 */
	public KeycloakAPIUserFederationMapperBuilder federationMapperType(String federationMapperType) {
		this.federationMapperType = federationMapperType;
		return this;
	}

	/**
	 * The displayName for the user federation provider this mapper applies to.
	 *
	 * @param federationProviderDisplayName The display name of the provider instance
	 * @return this
	 */
	public KeycloakAPIUserFederationMapperBuilder federationProviderDisplayName(String federationProviderDisplayName) {
		this.federationProviderDisplayName = federationProviderDisplayName;
		return this;
	}

	public KeycloakAPIUserFederationMapper build() {
		KeycloakAPIUserFederationMapper keycloakAPIUserFederationMapper = new KeycloakAPIUserFederationMapper();
		keycloakAPIUserFederationMapper.setConfig(config);
		keycloakAPIUserFederationMapper.setName(name);
		keycloakAPIUserFederationMapper.setId(id);
		keycloakAPIUserFederationMapper.setFederationMapperType(federationMapperType);
		keycloakAPIUserFederationMapper.setFederationProviderDisplayName(federationProviderDisplayName);
		return keycloakAPIUserFederationMapper;
	}
}
