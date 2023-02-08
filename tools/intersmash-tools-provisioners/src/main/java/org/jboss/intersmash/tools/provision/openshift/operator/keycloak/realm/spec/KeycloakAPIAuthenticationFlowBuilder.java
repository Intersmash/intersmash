package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec;

import java.util.ArrayList;
import java.util.List;

public final class KeycloakAPIAuthenticationFlowBuilder {
	private String alias;
	private List<KeycloakAPIAuthenticationExecution> authenticationExecutions;
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
			List<KeycloakAPIAuthenticationExecution> authenticationExecutions) {
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
			KeycloakAPIAuthenticationExecution authenticationExecution) {
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

	public KeycloakAPIAuthenticationFlow build() {
		KeycloakAPIAuthenticationFlow keycloakAPIAuthenticationFlow = new KeycloakAPIAuthenticationFlow();
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
