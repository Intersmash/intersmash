package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.spec;

public final class KeycloakCredentialBuilder {
	private String type;
	private String value;
	private boolean temporary;

	/**
	* Credential Type.
	 *
	 * @param type The credential type
	 * @return this
	*/
	public KeycloakCredentialBuilder type(String type) {
		this.type = type;
		return this;
	}

	/**
	 * Credential Value.
	 *
	 * @param value The credential value
	 * @return this
	 */
	public KeycloakCredentialBuilder value(String value) {
		this.value = value;
		return this;
	}

	/**
	 * True if this credential object is temporary.
	 *
	 * @param temporary Whether this credential object is temporary
	 * @return this
	 */
	public KeycloakCredentialBuilder temporary(boolean temporary) {
		this.temporary = temporary;
		return this;
	}

	public KeycloakCredential build() {
		KeycloakCredential keycloakCredential = new KeycloakCredential();
		keycloakCredential.setType(type);
		keycloakCredential.setValue(value);
		keycloakCredential.setTemporary(temporary);
		return keycloakCredential;
	}
}
