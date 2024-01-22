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
package org.jboss.intersmash.provision.openshift.operator.keycloak.keycloak.spec;

import org.keycloak.v1alpha1.keycloakspec.External;

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

	public External build() {
		External keycloakExternal = new External();
		keycloakExternal.setEnabled(enabled);
		keycloakExternal.setUrl(url);
		return keycloakExternal;
	}
}
