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
package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.client.spec;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Keycloak Client REST object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakAPIClient {
	/**
	 * Client ID. If not specified, automatically generated.
	 */
	private String id;

	/**
	 * Client ID.
	 */
	private String clientId;

	/**
	 * Client name.
	 */
	private String name;

	/**
	 * Surrogate Authentication Required option.
	 */
	private boolean surrogateAuthRequired;

	/**
	 * Client enabled flag.
	 */
	private boolean enabled;

	/**
	 * What Client authentication type to use.
	 */
	private String clientAuthenticatorType;

	/**
	 * Client Secret. The Operator will automatically create a Secret based on this value.
	 */
	private String secret;

	/**
	 * Application base URL.
	 */
	private String baseUrl;

	/**
	 * Application Admin URL.
	 */
	private String adminUrl;

	/**
	 * Application root URL.
	 */
	private String rootUrl;

	/**
	 * Client description.
	 */
	private String description;

	/**
	 * Default Client roles.
	 */
	private List<String> defaultRoles;

	/**
	 * A list of valid Redirection URLs.
	 */
	private List<String> redirectUris;

	/**
	 * A list of valid Web Origins.
	 */
	private List<String> webOrigins;

	/**
	 * Not Before setting.
	 */
	private int notBefore;

	/**
	 * True if a client supports only Bearer Tokens.
	 */
	private boolean bearerOnly;

	/**
	 * True if Consent Screen is required.
	 */
	private boolean consentRequired;

	/**
	 * True if Standard flow is enabled.
	 */
	private boolean standardFlowEnabled;

	/**
	 * True if Implicit flow is enabled.
	 */
	private boolean implicitFlowEnabled;

	/**
	 * True if Direct Grant is enabled.
	 */
	private boolean directAccessGrantsEnabled;

	/**
	 * True if Service Accounts are enabled.
	 */
	private boolean serviceAccountsEnabled;

	/**
	 * True if this is a public Client.
	 */
	private boolean publicClient;

	/**
	 * True if this client supports Front Channel logout.
	 */
	private boolean frontchannelLogout;

	/**
	 * Protocol used for this Client.
	 */
	private String protocol;

	/**
	 * Client Attributes.
	 */
	private Map<String, String> attributes;

	/**
	 * True if Full Scope is allowed.
	 */
	private boolean fullScopeAllowed;

	/**
	 * Node registration timeout.
	 */
	private int nodeReRegistrationTimeout;

	/**
	 * Protocol Mappers.
	 */
	private List<KeycloakProtocolMapper> protocolMappers;

	/**
	 * True to use a Template Config.
	 */
	private boolean useTemplateConfig;

	/**
	 * True to use Template Scope.
	 */
	private boolean useTemplateScope;

	/**
	 * True to use Template Mappers.
	 */
	private boolean useTemplateMappers;

	/**
	 * Access options.
	 */
	private Map<String, Boolean> access;

	//	/**
	//	 * A list of optional client scopes. Optional client scopes are
	//	 * applied when issuing tokens for this client, but only when they
	//	 * are requested by the scope parameter in the OpenID Connect
	//	 * authorization request.
	//	 */
	//	private List<String> optionalClientScopes;
	//
	//	/**
	//	 * A list of default client scopes. Default client scopes are
	//	 * always applied when issuing OpenID Connect tokens or SAML
	//	 * assertions for this client.
	//	 */
	//	private List<String> defaultClientScopes;
}
