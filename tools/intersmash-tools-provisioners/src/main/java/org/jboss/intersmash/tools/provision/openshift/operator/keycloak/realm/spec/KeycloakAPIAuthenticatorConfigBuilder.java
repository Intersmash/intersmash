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

public final class KeycloakAPIAuthenticatorConfigBuilder {
	private String alias;
	private String config;
	private String id;

	/**
	 * Set the Alias.
	 *
	 * @param alias The Alias that should be used
	 * @return this
	 */
	public KeycloakAPIAuthenticatorConfigBuilder alias(String alias) {
		this.alias = alias;
		return this;
	}

	/**
	 * Set the Config.
	 *
	 * @param config The Config that should be used
	 * @return this
	 */
	public KeycloakAPIAuthenticatorConfigBuilder config(String config) {
		this.config = config;
		return this;
	}

	/**
	 * Set the ID.
	 *
	 * @param id The ID that should be used
	 * @return this
	 */
	public KeycloakAPIAuthenticatorConfigBuilder id(String id) {
		this.id = id;
		return this;
	}

	public KeycloakAPIAuthenticatorConfig build() {
		KeycloakAPIAuthenticatorConfig keycloakAPIAuthenticatorConfig = new KeycloakAPIAuthenticatorConfig();
		keycloakAPIAuthenticatorConfig.setAlias(alias);
		keycloakAPIAuthenticatorConfig.setConfig(config);
		keycloakAPIAuthenticatorConfig.setId(id);
		return keycloakAPIAuthenticatorConfig;
	}
}
