package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec;

import java.util.HashMap;
import java.util.Map;

public final class KeycloakIdentityProviderBuilder {
	private String alias;
	private String displayName;
	private String internalId;
	private String providerId;
	private boolean enabled;
	private boolean trustEmail;
	private boolean storeToken;
	private boolean addReadTokenRoleOnCreate;
	private String firstBrokerLoginFlowAlias;
	private String postBrokerLoginFlowAlias;
	private boolean linkOnly;
	private Map<String, String> omitempty;

	/**
	 * Set the identity Provider Alias.
	 *
	 * @param alias The identity Provider Alias
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder alias(String alias) {
		this.alias = alias;
		return this;
	}

	/**
	 * Identity Provider Display Name.
	 *
	 * @param displayName The identity Provider display name
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder displayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	/**
	 * Identity Provider Internal ID.
	 *
	 * @param internalId The Identity Provider internal ID
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder internalId(String internalId) {
		this.internalId = internalId;
		return this;
	}

	/**
	 * Set the Identity Provider ID.
	 *
	 * @param providerId Identity Provider ID
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder providerId(String providerId) {
		this.providerId = providerId;
		return this;
	}

	/**
	 * Identity Provider enabled flag.
	 *
	 * @param enabled Whether the Identity provider is enabled
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/**
	 * Identity Provider Trust Email.
	 *
	 * @param trustEmail Whether the trust-email setting is enabled
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder trustEmail(boolean trustEmail) {
		this.trustEmail = trustEmail;
		return this;
	}

	/**
	 * Identity Provider Store to Token.
	 *
	 * @param storeToken Whether store to token setting is enabled
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder storeToken(boolean storeToken) {
		this.storeToken = storeToken;
		return this;
	}

	/**
	 * Adds Read Token role when creating this Identity Provider.
	 *
	 * @param addReadTokenRoleOnCreate Whether Read Token role should be added when creating this Identity Provider
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder addReadTokenRoleOnCreate(boolean addReadTokenRoleOnCreate) {
		this.addReadTokenRoleOnCreate = addReadTokenRoleOnCreate;
		return this;
	}

	/**
	 * Identity Provider First Broker Login Flow Alias.
	 *
	 * @param firstBrokerLoginFlowAlias The Identity Provider First Broker Login Flow Alias
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder firstBrokerLoginFlowAlias(String firstBrokerLoginFlowAlias) {
		this.firstBrokerLoginFlowAlias = firstBrokerLoginFlowAlias;
		return this;
	}

	/**
	 * Set the Identity Provider Post Broker Login Flow Alias.
	 *
	 * @param postBrokerLoginFlowAlias The Identity Provider Post Broker Login Flow Alias
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder postBrokerLoginFlowAlias(String postBrokerLoginFlowAlias) {
		this.postBrokerLoginFlowAlias = postBrokerLoginFlowAlias;
		return this;
	}

	/**
	 * Identity Provider Link Only setting.
	 *
	 * @param linkOnly Whether the Link-only setting is enabled
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder linkOnly(boolean linkOnly) {
		this.linkOnly = linkOnly;
		return this;
	}

	/**
	 * Set the Identity Provider config.
	 *
	 * @param omitempty The Identity Provider config
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder omitempty(Map<String, String> omitempty) {
		this.omitempty = omitempty;
		return this;
	}

	/**
	 * Ad one Identity Provider config item.
	 *
	 * @param key The key of a Identity Provider config item
	 * @param value The value of a Identity Provider config item
	 * @return this
	 */
	public KeycloakIdentityProviderBuilder omitempty(String key, String value) {
		if (omitempty == null) {
			omitempty = new HashMap<>();
		}
		omitempty.put(key, value);
		return this;
	}

	public KeycloakIdentityProvider build() {
		KeycloakIdentityProvider keycloakIdentityProvider = new KeycloakIdentityProvider();
		keycloakIdentityProvider.setAlias(alias);
		keycloakIdentityProvider.setDisplayName(displayName);
		keycloakIdentityProvider.setInternalId(internalId);
		keycloakIdentityProvider.setProviderId(providerId);
		keycloakIdentityProvider.setEnabled(enabled);
		keycloakIdentityProvider.setTrustEmail(trustEmail);
		keycloakIdentityProvider.setStoreToken(storeToken);
		keycloakIdentityProvider.setAddReadTokenRoleOnCreate(addReadTokenRoleOnCreate);
		keycloakIdentityProvider.setFirstBrokerLoginFlowAlias(firstBrokerLoginFlowAlias);
		keycloakIdentityProvider.setPostBrokerLoginFlowAlias(postBrokerLoginFlowAlias);
		keycloakIdentityProvider.setLinkOnly(linkOnly);
		keycloakIdentityProvider.setOmitempty(omitempty);
		return keycloakIdentityProvider;
	}
}
