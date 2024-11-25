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
package org.jboss.intersmash.provision.operator.model.keycloak.realm.spec;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.v1alpha1.keycloakrealmspec.realm.AuthenticationFlows;
import org.keycloak.v1alpha1.keycloakrealmspec.realm.authenticationflows.AuthenticationExecutions;

public final class KeycloakAPIAuthenticationFlowBuilder {
	private String alias;
	private List<AuthenticationExecutions> authenticationExecutions;
	private boolean builtIn;
	private String description;
	private String id;
	private String providerId;
	private boolean topLevel;

	/**
	 * Set an Alias.
	 *
	 * @param alias The alias that should be used
	 * @return this
	 */
	public KeycloakAPIAuthenticationFlowBuilder alias(String alias) {
		this.alias = alias;
		return this;
	}

	/**
	 * Set Authentication executions.
	 *
	 * @param authenticationExecutions A list of authentication executions
	 * @return this
	 */
	public KeycloakAPIAuthenticationFlowBuilder authenticationExecutions(
			List<AuthenticationExecutions> authenticationExecutions) {
		this.authenticationExecutions = authenticationExecutions;
		return this;
	}

	/**
	 * Add one Authentication execution.
	 *
	 * @param authenticationExecution An authentication execution
	 * @return this
	 */
	public KeycloakAPIAuthenticationFlowBuilder authenticationExecutions(
			AuthenticationExecutions authenticationExecution) {
		if (authenticationExecutions == null) {
			authenticationExecutions = new ArrayList<>();
		}
		authenticationExecutions.add(authenticationExecution);
		return this;
	}

	/**
	 * Set Built in.
	 *
	 * @param builtIn The Built In that should be used
	 * @return this
	 */
	public KeycloakAPIAuthenticationFlowBuilder builtIn(boolean builtIn) {
		this.builtIn = builtIn;
		return this;
	}

	/**
	 * Set Description.
	 *
	 * @param description The description that should be used
	 * @return this
	 */
	public KeycloakAPIAuthenticationFlowBuilder description(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Set ID.
	 *
	 * @param id The ID that should be used
	 * @return this
	 */
	public KeycloakAPIAuthenticationFlowBuilder id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Set Provider ID.
	 *
	 * @param providerId The Provider ID that should be used
	 * @return this
	 */
	public KeycloakAPIAuthenticationFlowBuilder providerId(String providerId) {
		this.providerId = providerId;
		return this;
	}

	/**
	 * Set the Top level flag.
	 *
	 * @param topLevel Whether the Top Level setting is enabled
	 * @return this
	 */
	public KeycloakAPIAuthenticationFlowBuilder topLevel(boolean topLevel) {
		this.topLevel = topLevel;
		return this;
	}

	public AuthenticationFlows build() {
		AuthenticationFlows keycloakAPIAuthenticationFlow = new AuthenticationFlows();
		keycloakAPIAuthenticationFlow.setAlias(alias);
		keycloakAPIAuthenticationFlow.setAuthenticationExecutions(authenticationExecutions);
		keycloakAPIAuthenticationFlow.setBuiltIn(builtIn);
		keycloakAPIAuthenticationFlow.setDescription(description);
		keycloakAPIAuthenticationFlow.setId(id);
		keycloakAPIAuthenticationFlow.setProviderId(providerId);
		keycloakAPIAuthenticationFlow.setTopLevel(topLevel);
		return keycloakAPIAuthenticationFlow;
	}
}
