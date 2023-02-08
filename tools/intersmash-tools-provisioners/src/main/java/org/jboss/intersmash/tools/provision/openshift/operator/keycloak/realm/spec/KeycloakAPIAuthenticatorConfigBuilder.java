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
