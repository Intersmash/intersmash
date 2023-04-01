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
