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
import java.util.Map;

import org.keycloak.v1alpha1.keycloakrealmspec.realm.UserFederationProviders;

public final class KeycloakAPIUserFederationProviderBuilder {
	private Map<String, String> config;
	private String displayName;
	private int fullSyncPeriod;
	private String id;
	private int priority;
	private String providerName;

	/**
	 * Set the user federation provider config.
	 *
	 * @param config A {@link Map} storing the user federation provider config
	 * @return this
	 */
	public KeycloakAPIUserFederationProviderBuilder config(Map<String, String> config) {
		this.config = config;
		return this;
	}

	/**
	 * Add one user federation provider config.
	 *
	 * @param key The name of one property of the user federation provider config
	 * @param value A value for one property of the user federation provider config
	 * @return this
	 */
	public KeycloakAPIUserFederationProviderBuilder config(String key, String value) {
		if (config == null) {
			config = new HashMap<>();
		}
		config.put(key, value);
		return this;
	}

	/**
	 * Set the display name of this provider instance.
	 *
	 * @param displayName The display name of this provider instance
	 * @return this
	 */
	public KeycloakAPIUserFederationProviderBuilder displayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	public KeycloakAPIUserFederationProviderBuilder fullSyncPeriod(int fullSyncPeriod) {
		this.fullSyncPeriod = fullSyncPeriod;
		return this;
	}

	/**
	 * Set the ID of this provider.
	 *
	 * @param id The ID of this provider
	 * @return this
	 */
	public KeycloakAPIUserFederationProviderBuilder id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Set the priority of this provider when looking up users or adding a user.
	 *
	 * @param priority The desired priority of this provider
	 * @return this
	 */
	public KeycloakAPIUserFederationProviderBuilder priority(int priority) {
		this.priority = priority;
		return this;
	}

	/**
	 * Set the name of the user provider, such as "ldap", "kerberos" or a custom SPI.
	 *
	 * @param providerName The name of the user provider
	 * @return this
	 */
	public KeycloakAPIUserFederationProviderBuilder providerName(String providerName) {
		this.providerName = providerName;
		return this;
	}

	public UserFederationProviders build() {
		UserFederationProviders keycloakAPIUserFederationProvider = new UserFederationProviders();
		keycloakAPIUserFederationProvider.setConfig(config);
		keycloakAPIUserFederationProvider.setDisplayName(displayName);
		keycloakAPIUserFederationProvider.setFullSyncPeriod(fullSyncPeriod);
		keycloakAPIUserFederationProvider.setId(id);
		keycloakAPIUserFederationProvider.setPriority(priority);
		keycloakAPIUserFederationProvider.setProviderName(providerName);
		return keycloakAPIUserFederationProvider;
	}
}
