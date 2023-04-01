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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakIdentityProvider {

	/**
	 * Identity Provider Alias.
	 */
	private String alias;

	/**
	 * Identity Provider Display Name.
	 */
	private String displayName;

	/**
	 * Identity Provider Internal ID.
	 */
	private String internalId;

	/**
	 * Identity Provider ID.
	 */
	private String providerId;

	/**
	 * Identity Provider enabled flag.
	 */
	private boolean enabled;

	/**
	 * Identity Provider Trust Email.
	 */
	private boolean trustEmail;

	/**
	 * Identity Provider Store to Token.
	 */
	private boolean storeToken;

	/**
	 * Adds Read Token role when creating this Identity Provider.
	 */
	private boolean addReadTokenRoleOnCreate;

	/**
	 * Identity Provider First Broker Login Flow Alias.
	 */
	private String firstBrokerLoginFlowAlias;

	/**
	 * Identity Provider Post Broker Login Flow Alias.
	 */
	private String postBrokerLoginFlowAlias;

	/**
	 * Identity Provider Link Only setting.
	 */
	private boolean linkOnly;

	/**
	 * Identity Provider config.
	 */
	private Map<String, String> omitempty;
}
