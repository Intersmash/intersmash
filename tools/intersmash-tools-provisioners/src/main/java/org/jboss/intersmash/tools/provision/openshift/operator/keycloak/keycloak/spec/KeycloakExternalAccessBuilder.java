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
package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

/**
 * Controls external Ingress/Route settings.
 */
public final class KeycloakExternalAccessBuilder {
	private boolean enabled;
	//	private String tlsTermination;
	//	private String host;

	/**
	 * If set to true, the Operator will create an Ingress or a Route pointing to Keycloak.
	 *
	 * @param enabled Whether the Operator will create an Ingress or a Route pointing to Keycloak
	 * @return this
	 */
	public KeycloakExternalAccessBuilder enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	//	/**
	//	 * TLS Termination type for the external access. Setting this field to "reencrypt" will
	//	 * terminate TLS on the Ingress/Route level. Setting this field to "passthrough" will
	//	 * send encrypted traffic to the Pod. If unspecified, defaults to "reencrypt".
	//	 * Note, that this setting has no effect on Ingress
	//	 * as Ingress TLS settings are not reconciled by this operator. In other words,
	//	 * Ingress TLS configuration is the same in both cases and it is up to the user
	//	 * to configure TLS section of the Ingress.
	//	 */
	//	public KeycloakExternalAccessBuilder tlsTermination(TLSTermination tlsTermination) {
	//		if (tlsTermination != TLSTermination.DEFAULT_TLS_TERMINTATION) {
	//			this.tlsTermination = tlsTermination.getValue();
	//		}
	//		return this;
	//	}
	//
	//	/**
	//	 * If set, the Operator will use value of host for Ingress/Route host instead of default value keycloak.local for
	//	 * ingress and automatically chosen name for Route.
	//	 */
	//	public KeycloakExternalAccessBuilder host(String host) {
	//		this.host = host;
	//		return this;
	//	}

	public KeycloakExternalAccess build() {
		KeycloakExternalAccess keycloakExternalAccess = new KeycloakExternalAccess();
		keycloakExternalAccess.setEnabled(enabled);
		//		keycloakExternalAccess.setTlsTermination(tlsTermination);
		//		keycloakExternalAccess.setHost(host);
		return keycloakExternalAccess;
	}

	public enum TLSTermination {
		DEFAULT_TLS_TERMINTATION(""),
		REENCRYPT_TLS_TERMINATION_TYPE("reencrypt"),
		PASSTHROUGH_TLS_TERMINATION_TYPE("passthrough");

		private String value;

		TLSTermination(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
