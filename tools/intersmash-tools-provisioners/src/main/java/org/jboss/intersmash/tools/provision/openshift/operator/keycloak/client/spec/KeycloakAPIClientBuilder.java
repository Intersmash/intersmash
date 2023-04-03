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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keycloak Client REST object.
 */
public final class KeycloakAPIClientBuilder {
	private String id;
	private String clientId;
	private String name;
	private boolean surrogateAuthRequired;
	private boolean enabled;
	private String clientAuthenticatorType;
	private String secret;
	private String baseUrl;
	private String adminUrl;
	private String rootUrl;
	private String description;
	private List<String> defaultRoles;
	private List<String> redirectUris;
	private List<String> webOrigins;
	private int notBefore;
	private boolean bearerOnly;
	private boolean consentRequired;
	private boolean standardFlowEnabled;
	private boolean implicitFlowEnabled;
	private boolean directAccessGrantsEnabled;
	private boolean serviceAccountsEnabled;
	private boolean publicClient;
	private boolean frontchannelLogout;
	private String protocol;
	private Map<String, String> attributes;
	private boolean fullScopeAllowed;
	private int nodeReRegistrationTimeout;
	private List<KeycloakProtocolMapper> protocolMappers;
	private boolean useTemplateConfig;
	private boolean useTemplateScope;
	private boolean useTemplateMappers;
	private Map<String, Boolean> access;
	//	private List<String> optionalClientScopes;
	//	private List<String> defaultClientScopes;

	/**
	 * If no client ID is specified, it is automatically generated.
	 *
	 * @param id Client ID
	 * @return this
	 */
	public KeycloakAPIClientBuilder id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Set the Client ID.
	 *
	 * @param clientId Desired client ID
	 * @return this
	 */
	public KeycloakAPIClientBuilder clientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	/**
	 * Set the client name.
	 *
	 * @param name Desired client name
	 * @return this
	 */
	public KeycloakAPIClientBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Surrogate Authentication Required option.
	 *
	 * @param surrogateAuthRequired Whether Surrogate Authentication is required
	 * @return this
	 */
	public KeycloakAPIClientBuilder surrogateAuthRequired(boolean surrogateAuthRequired) {
		this.surrogateAuthRequired = surrogateAuthRequired;
		return this;
	}

	/**
	 * Client enabled flag.
	 *
	 * @param enabled Whether the client is enabled
	 * @return this
	 */
	public KeycloakAPIClientBuilder enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/**
	 * What Client authentication type to use.
	 *
	 * @param clientAuthenticatorType The desired Client authentication type.
	 * @return this
	 */
	public KeycloakAPIClientBuilder clientAuthenticatorType(String clientAuthenticatorType) {
		this.clientAuthenticatorType = clientAuthenticatorType;
		return this;
	}

	/**
	 * Client Secret. The Operator will automatically create a Secret based on this value.
	 *
	 * @param secret The desired Client secret
	 * @return this
	 */
	public KeycloakAPIClientBuilder secret(String secret) {
		this.secret = secret;
		return this;
	}

	/**
	 * Application base URL.
	 *
	 * @param baseUrl The desired Application base URL.
	 * @return this
	 */
	public KeycloakAPIClientBuilder baseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		return this;
	}

	/**
	 * Application Admin URL.
	 *
	 * @param adminUrl The desired Application Admin URL.
	 * @return this
	 */
	public KeycloakAPIClientBuilder adminUrl(String adminUrl) {
		this.adminUrl = adminUrl;
		return this;
	}

	/**
	 * Application root URL.
	 *
	 * @param rootUrl The desired Application root URL.
	 * @return this
	 */
	public KeycloakAPIClientBuilder rootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
		return this;
	}

	/**
	 * Client description.
	 *
	 * @param description The desired Client description
	 * @return this
	 */
	public KeycloakAPIClientBuilder description(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Default Client roles.
	 *
	 * @param defaultRoles The desired default Client roles
	 * @return this
	 */
	public KeycloakAPIClientBuilder defaultRoles(List<String> defaultRoles) {
		this.defaultRoles = defaultRoles;
		return this;
	}

	/**
	 * Add Default Client role.
	 *
	 * @param defaultRole The default Client role that should be added
	 * @return this
	 */
	public KeycloakAPIClientBuilder defaultRoles(String defaultRole) {
		if (defaultRoles == null) {
			defaultRoles = new ArrayList<>();
		}
		defaultRoles.add(defaultRole);
		return this;
	}

	/**
	 * A list of valid Redirection URLs.
	 *
	 * @param redirectUris List of valid Redirection URLs
	 * @return this
	 */
	public KeycloakAPIClientBuilder redirectUris(List<String> redirectUris) {
		this.redirectUris = redirectUris;
		return this;
	}

	/**
	 * Add valid Redirection URL.
	 *
	 * @param redirectUri A valid Redirection URL
	 * @return this
	 */
	public KeycloakAPIClientBuilder redirectUris(String redirectUri) {
		if (redirectUris == null) {
			redirectUris = new ArrayList<>();
		}
		redirectUris.add(redirectUri);
		return this;
	}

	/**
	 * A list of valid Web Origins.
	 *
	 * @param webOrigins List of valid Web Origins
	 * @return this
	 */
	public KeycloakAPIClientBuilder webOrigins(List<String> webOrigins) {
		this.webOrigins = webOrigins;
		return this;
	}

	/**
	 * Add valid Web Origin.
	 *
	 * @param webOrigin A valid Web Origin
	 * @return this
	 */
	public KeycloakAPIClientBuilder webOrigins(String webOrigin) {
		if (webOrigins == null) {
			webOrigins = new ArrayList<>();
		}
		webOrigins.add(webOrigin);
		return this;
	}

	/**
	 * Not Before setting.
	 *
	 * @param notBefore Not-before desired value
	 * @return this
	 */
	public KeycloakAPIClientBuilder notBefore(int notBefore) {
		this.notBefore = notBefore;
		return this;
	}

	/**
	 * True if a client supports only Bearer Tokens.
	 *
	 * @param bearerOnly Whether a client supports only Bearer Tokens.
	 * @return this
	 */
	public KeycloakAPIClientBuilder bearerOnly(boolean bearerOnly) {
		this.bearerOnly = bearerOnly;
		return this;
	}

	/**
	 * True if Consent Screen is required.
	 *
	 * @param consentRequired Whether Consent Screen is required.
	 * @return this
	 */
	public KeycloakAPIClientBuilder consentRequired(boolean consentRequired) {
		this.consentRequired = consentRequired;
		return this;
	}

	/**
	 * True if Standard flow is enabled.
	 *
	 * @param standardFlowEnabled Whether Standard flow is enabled.
	 * @return this
	 */
	public KeycloakAPIClientBuilder standardFlowEnabled(boolean standardFlowEnabled) {
		this.standardFlowEnabled = standardFlowEnabled;
		return this;
	}

	/**
	 * True if Implicit flow is enabled.
	 *
	 * @param implicitFlowEnabled Whether Implicit flow is enabled.
	 * @return this
	 */
	public KeycloakAPIClientBuilder implicitFlowEnabled(boolean implicitFlowEnabled) {
		this.implicitFlowEnabled = implicitFlowEnabled;
		return this;
	}

	/**
	 * True if Direct Grant is enabled.
	 *
	 * @param directAccessGrantsEnabled Whether Direct Grant is enabled.
	 * @return this
	 */
	public KeycloakAPIClientBuilder directAccessGrantsEnabled(boolean directAccessGrantsEnabled) {
		this.directAccessGrantsEnabled = directAccessGrantsEnabled;
		return this;
	}

	/**
	 * True if Service Accounts are enabled.
	 *
	 * @param serviceAccountsEnabled Whether Service Accounts are enabled.
	 * @return this
	 */
	public KeycloakAPIClientBuilder serviceAccountsEnabled(boolean serviceAccountsEnabled) {
		this.serviceAccountsEnabled = serviceAccountsEnabled;
		return this;
	}

	/**
	 * True if this is a public Client.
	 *
	 * @param publicClient Whether this is a public Client.
	 * @return this
	 */
	public KeycloakAPIClientBuilder publicClient(boolean publicClient) {
		this.publicClient = publicClient;
		return this;
	}

	/**
	 * True if this client supports Front Channel logout.
	 *
	 * @param frontchannelLogout Whether this client supports Front Channel logout.
	 * @return this
	 */
	public KeycloakAPIClientBuilder frontchannelLogout(boolean frontchannelLogout) {
		this.frontchannelLogout = frontchannelLogout;
		return this;
	}

	/**
	 * Set the protocol used for this Client.
	 *
	 * @param protocol Protocol used for this Client.
	 * @return this
	 */
	public KeycloakAPIClientBuilder protocol(String protocol) {
		this.protocol = protocol;
		return this;
	}

	/**
	 * Set Client Attributes.
	 *
	 * @param attributes Attributes used for this Client.
	 * @return this
	 */
	public KeycloakAPIClientBuilder attributes(Map<String, String> attributes) {
		this.attributes = attributes;
		return this;
	}

	/**
	 * Add Client Attribute.
	 *
	 * @param key The client attribute that should be added to this Client
	 * @param value The client attribute value
	 * @return this
	 */
	public KeycloakAPIClientBuilder attributes(String key, String value) {
		if (attributes == null) {
			attributes = new HashMap<>();
		}
		attributes.put(key, value);
		return this;
	}

	/**
	 * True if Full Scope is allowed.
	 *
	 * @param fullScopeAllowed Whether Full Scope is allowed
	 * @return this
	 */
	public KeycloakAPIClientBuilder fullScopeAllowed(boolean fullScopeAllowed) {
		this.fullScopeAllowed = fullScopeAllowed;
		return this;
	}

	/**
	 * Set a node registration timeout.
	 *
	 * @param nodeReRegistrationTimeout Desired node registration timeout
	 * @return this
	 */
	public KeycloakAPIClientBuilder nodeReRegistrationTimeout(int nodeReRegistrationTimeout) {
		this.nodeReRegistrationTimeout = nodeReRegistrationTimeout;
		return this;
	}

	/**
	 * Set the Protocol Mappers.
	 *
	 * @param protocolMappers List of {@link KeycloakProtocolMapper} instances that should be used
	 * @return this
	 */
	public KeycloakAPIClientBuilder protocolMappers(List<KeycloakProtocolMapper> protocolMappers) {
		this.protocolMappers = protocolMappers;
		return this;
	}

	/**
	 * Add a Protocol Mapper.
	 *
	 * @param protocolMapper {@link KeycloakProtocolMapper} instance that should be added
	 * @return this
	 */
	public KeycloakAPIClientBuilder protocolMappers(KeycloakProtocolMapper protocolMapper) {
		if (protocolMappers == null) {
			protocolMappers = new ArrayList<>();
		}
		protocolMappers.add(protocolMapper);
		return this;
	}

	/**
	 * True to use a Template Config.
	 *
	 * @param useTemplateConfig Whether to use a Template Config
	 * @return this
	 */
	public KeycloakAPIClientBuilder useTemplateConfig(boolean useTemplateConfig) {
		this.useTemplateConfig = useTemplateConfig;
		return this;
	}

	/**
	 * True to use Template Scope.
	 *
	 * @param useTemplateScope Whether to use a Template Scope
	 * @return this
	 */
	public KeycloakAPIClientBuilder useTemplateScope(boolean useTemplateScope) {
		this.useTemplateScope = useTemplateScope;
		return this;
	}

	/**
	 * True to use Template Mappers.
	 *
	 * @param useTemplateMappers Whether to use Template Mappers
	 * @return this
	 */
	public KeycloakAPIClientBuilder useTemplateMappers(boolean useTemplateMappers) {
		this.useTemplateMappers = useTemplateMappers;
		return this;
	}

	/**
	 * Set access options.
	 *
	 * @param access Desired access options
	 * @return this
	 */
	public KeycloakAPIClientBuilder access(Map<String, Boolean> access) {
		this.access = access;
		return this;
	}

	/**
	 * Add Access option.
	 *
	 * @param key Access option that should be added
	 * @param value Value for the access option that should be added
	 * @return this
	 */
	public KeycloakAPIClientBuilder access(String key, boolean value) {
		if (access == null) {
			access = new HashMap<>();
		}
		access.put(key, value);
		return this;
	}

	//	/**
	//	 * A list of optional client scopes. Optional client scopes are
	//	 * applied when issuing tokens for this client, but only when they
	//	 * are requested by the scope parameter in the OpenID Connect
	//	 * authorization request.
	//	 */
	//	public KeycloakAPIClientBuilder optionalClientScopes(List<String> optionalClientScopes) {
	//		this.optionalClientScopes = optionalClientScopes;
	//		return this;
	//	}
	//
	//	/**
	//	 * Add optional client scope. Optional client scopes are
	//	 * applied when issuing tokens for this client, but only when they
	//	 * are requested by the scope parameter in the OpenID Connect
	//	 * authorization request.
	//	 */
	//	public KeycloakAPIClientBuilder optionalClientScopes(String optionalClientScope) {
	//		if (optionalClientScopes == null) {
	//			optionalClientScopes = new ArrayList<>();
	//		}
	//		optionalClientScopes.add(optionalClientScope);
	//		return this;
	//	}
	//
	//	/**
	//	 * A list of default client scopes. Default client scopes are
	//	 * always applied when issuing OpenID Connect tokens or SAML
	//	 * assertions for this client.
	//	 */
	//	public KeycloakAPIClientBuilder defaultClientScopes(List<String> defaultClientScopes) {
	//		this.defaultClientScopes = defaultClientScopes;
	//		return this;
	//	}
	//
	//	/**
	//	 * Add default client scope. Default client scopes are
	//	 * always applied when issuing OpenID Connect tokens or SAML
	//	 * assertions for this client.
	//	 */
	//	public KeycloakAPIClientBuilder defaultClientScopes(String defaultClientScope) {
	//		if (defaultClientScopes == null) {
	//			defaultClientScopes = new ArrayList<>();
	//		}
	//		defaultClientScopes.add(defaultClientScope);
	//		return this;
	//	}

	public KeycloakAPIClient build() {
		KeycloakAPIClient keycloakAPIClient = new KeycloakAPIClient();
		keycloakAPIClient.setId(id);
		keycloakAPIClient.setClientId(clientId);
		keycloakAPIClient.setName(name);
		keycloakAPIClient.setSurrogateAuthRequired(surrogateAuthRequired);
		keycloakAPIClient.setEnabled(enabled);
		keycloakAPIClient.setClientAuthenticatorType(clientAuthenticatorType);
		keycloakAPIClient.setSecret(secret);
		keycloakAPIClient.setBaseUrl(baseUrl);
		keycloakAPIClient.setAdminUrl(adminUrl);
		keycloakAPIClient.setRootUrl(rootUrl);
		keycloakAPIClient.setDescription(description);
		keycloakAPIClient.setDefaultRoles(defaultRoles);
		keycloakAPIClient.setRedirectUris(redirectUris);
		keycloakAPIClient.setWebOrigins(webOrigins);
		keycloakAPIClient.setNotBefore(notBefore);
		keycloakAPIClient.setBearerOnly(bearerOnly);
		keycloakAPIClient.setConsentRequired(consentRequired);
		keycloakAPIClient.setStandardFlowEnabled(standardFlowEnabled);
		keycloakAPIClient.setImplicitFlowEnabled(implicitFlowEnabled);
		keycloakAPIClient.setDirectAccessGrantsEnabled(directAccessGrantsEnabled);
		keycloakAPIClient.setServiceAccountsEnabled(serviceAccountsEnabled);
		keycloakAPIClient.setPublicClient(publicClient);
		keycloakAPIClient.setFrontchannelLogout(frontchannelLogout);
		keycloakAPIClient.setProtocol(protocol);
		keycloakAPIClient.setAttributes(attributes);
		keycloakAPIClient.setFullScopeAllowed(fullScopeAllowed);
		keycloakAPIClient.setNodeReRegistrationTimeout(nodeReRegistrationTimeout);
		keycloakAPIClient.setProtocolMappers(protocolMappers);
		keycloakAPIClient.setUseTemplateConfig(useTemplateConfig);
		keycloakAPIClient.setUseTemplateScope(useTemplateScope);
		keycloakAPIClient.setUseTemplateMappers(useTemplateMappers);
		keycloakAPIClient.setAccess(access);
		//		keycloakAPIClient.setOptionalClientScopes(optionalClientScopes);
		//		keycloakAPIClient.setDefaultClientScopes(defaultClientScopes);
		return keycloakAPIClient;
	}
}
