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
package org.jboss.intersmash.provision.openshift.operator.keycloak.realm.spec;

import org.keycloak.v1alpha1.keycloakrealmspec.realm.authenticationflows.AuthenticationExecutions;

public final class KeycloakAPIAuthenticationExecutionBuilder {
	private String authenticator;
	private String authenticatorConfig;
	private boolean authenticatorFlow;
	private String flowAlias;
	private int priority;
	private String requirement;
	private boolean userSetupAllowed;

	/**
	 * Set an authenticator.
	 *
	 * @param authenticator the authenticator that should be used
	 * @return this
	 */
	public KeycloakAPIAuthenticationExecutionBuilder authenticator(String authenticator) {
		this.authenticator = authenticator;
		return this;
	}

	/**
	 * Set Authenticator Config.
	 *
	 * @param authenticatorConfig String representing a valid authenticator configuration option
	 * @return this
	 */
	public KeycloakAPIAuthenticationExecutionBuilder authenticatorConfig(String authenticatorConfig) {
		this.authenticatorConfig = authenticatorConfig;
		return this;
	}

	/**
	 * Authenticator flow.
	 *
	 * @param authenticatorFlow String representing a valid authenticator flow
	 * @return this
	 */
	public KeycloakAPIAuthenticationExecutionBuilder authenticatorFlow(boolean authenticatorFlow) {
		this.authenticatorFlow = authenticatorFlow;
		return this;
	}

	/**
	 * Flow Alias.
	 *
	 * @param flowAlias String representing a valid authenticator flow alias
	 * @return this
	 */
	public KeycloakAPIAuthenticationExecutionBuilder flowAlias(String flowAlias) {
		this.flowAlias = flowAlias;
		return this;
	}

	/**
	 * Priority.
	 *
	 * @param priority Integer representing a valid priority
	 * @return this
	 */
	public KeycloakAPIAuthenticationExecutionBuilder priority(int priority) {
		this.priority = priority;
		return this;
	}

	/**
	 * Requirement {@code[REQUIRED, OPTIONAL, ALTERNATIVE, DISABLED]}.
	 *
	 * @param requirement String representing a valid requirement
	 * @return this
	 */
	public KeycloakAPIAuthenticationExecutionBuilder requirement(String requirement) {
		this.requirement = requirement;
		return this;
	}

	/**
	 * User setup allowed.
	 *
	 * @param userSetupAllowed Whether user setup is allowed
	 * @return this
	 */
	public KeycloakAPIAuthenticationExecutionBuilder userSetupAllowed(boolean userSetupAllowed) {
		this.userSetupAllowed = userSetupAllowed;
		return this;
	}

	public AuthenticationExecutions build() {
		AuthenticationExecutions keycloakAPIAuthenticationExecution = new AuthenticationExecutions();
		keycloakAPIAuthenticationExecution.setAuthenticator(authenticator);
		keycloakAPIAuthenticationExecution.setAuthenticatorConfig(authenticatorConfig);
		keycloakAPIAuthenticationExecution.setAuthenticatorFlow(authenticatorFlow);
		keycloakAPIAuthenticationExecution.setFlowAlias(flowAlias);
		keycloakAPIAuthenticationExecution.setPriority(priority);
		keycloakAPIAuthenticationExecution.setRequirement(requirement);
		keycloakAPIAuthenticationExecution.setUserSetupAllowed(userSetupAllowed);
		return keycloakAPIAuthenticationExecution;
	}
}
