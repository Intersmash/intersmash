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
package org.jboss.intersmash.provision.operator.model.keycloak.client.spec;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.v1alpha1.keycloakclientspec.client.ProtocolMappers;

public final class KeycloakProtocolMapperBuilder {
	private String id;
	private String name;
	private String protocol;
	private String protocolMapper;
	private boolean consentRequired;
	private String consentText;
	private Map<String, String> config;

	/**
	 * Set the protocol Mapper ID.
	 *
	 * @param id Desired protocol Mapper ID
	 * @return this
	 */
	public KeycloakProtocolMapperBuilder id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Set the protocol Mapper Name.
	 *
	 * @param name Desired protocol Mapper Name
	 * @return this
	 */
	public KeycloakProtocolMapperBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Set the protocol to use.
	 *
	 * @param protocol Protocol that should be used
	 * @return this
	 */
	public KeycloakProtocolMapperBuilder protocol(String protocol) {
		this.protocol = protocol;
		return this;
	}

	/**
	 * Set the protocol Mapper to use.
	 *
	 * @param protocolMapper Protocol Mapper that should be used
	 * @return this
	 */
	public KeycloakProtocolMapperBuilder protocolMapper(String protocolMapper) {
		this.protocolMapper = protocolMapper;
		return this;
	}

	/**
	 * True if Consent Screen is required.
	 *
	 * @param consentRequired Whether Consent Screen is required
	 * @return this
	 */
	public KeycloakProtocolMapperBuilder consentRequired(boolean consentRequired) {
		this.consentRequired = consentRequired;
		return this;
	}

	/**
	 * Set the text to use for displaying Consent Screen.
	 *
	 * @param consentText Text to use for displaying Consent Screen.
	 * @return this
	 */
	public KeycloakProtocolMapperBuilder consentText(String consentText) {
		this.consentText = consentText;
		return this;
	}

	/**
	 * Config options.
	 *
	 * @param config Map of options for configuring the protocol
	 * @return this
	 */
	public KeycloakProtocolMapperBuilder config(Map<String, String> config) {
		this.config = config;
		return this;
	}

	/**
	 * Add Config option.
	 *
	 * @param key Name of the configuration option
	 * @param value Value for the configuration option
	 * @return this
	 */
	public KeycloakProtocolMapperBuilder config(String key, String value) {
		if (config == null) {
			config = new HashMap<>();
		}
		config.put(key, value);
		return this;
	}

	public ProtocolMappers build() {
		ProtocolMappers keycloakProtocolMapper = new ProtocolMappers();
		keycloakProtocolMapper.setId(id);
		keycloakProtocolMapper.setName(name);
		keycloakProtocolMapper.setProtocol(protocol);
		keycloakProtocolMapper.setProtocolMapper(protocolMapper);
		keycloakProtocolMapper.setConsentRequired(consentRequired);
		keycloakProtocolMapper.setConsentText(consentText);
		keycloakProtocolMapper.setConfig(config);
		return keycloakProtocolMapper;
	}
}
