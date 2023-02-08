package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

/**
 * Contains configuration for external Keycloak instances. Unmanaged needs to be set to true to use this.
 */
public final class KeycloakExternalBuilder {
	private boolean enabled;
	private String url;

	/**
	 * If set to true, this Keycloak will be treated as an external instance.
	 * The unmanaged field also needs to be set to true if this field is true.
	 *
	 * @param enabled Whether this Keycloak will be treated as an external instance
	 * @return this
	 */
	public KeycloakExternalBuilder enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/**
	 * Set the URL to use for the keycloak admin API. Needs to be set if external is true.
	 *
	 * @param url The URL to use for the keycloak admin API
	 * @return this
	 */
	public KeycloakExternalBuilder url(String url) {
		this.url = url;
		return this;
	}

	public KeycloakExternal build() {
		KeycloakExternal keycloakExternal = new KeycloakExternal();
		keycloakExternal.setEnabled(enabled);
		keycloakExternal.setUrl(url);
		return keycloakExternal;
	}
}
