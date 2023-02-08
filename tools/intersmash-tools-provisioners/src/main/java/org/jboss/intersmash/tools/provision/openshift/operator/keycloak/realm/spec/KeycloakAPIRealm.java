package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec;

import java.util.Set;

import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.client.spec.KeycloakAPIClient;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.spec.KeycloakAPIUser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakAPIRealm {

	private String id;

	/**
	 * Realm name.
	 */
	private String realm;

	/**
	 * Realm enabled flag.
	 */
	private boolean enabled;

	/**
	 * Realm display name.
	 */
	private String displayName;

	/**
	 * A set of Keycloak Users.
	 */
	private Set<KeycloakAPIUser> users;

	/**
	 *  A set of Keycloak Clients.
	 */
	private Set<KeycloakAPIClient> clients;

	/**
	 * A set of Identity Providers.
	 */
	private Set<KeycloakIdentityProvider> identityProviders;

	/**
	 * A set of Event Listeners.
	 */
	private Set<String> eventsListeners;

	/**
	 * Enable events recording.
	 */
	private boolean eventsEnabled;

	/**
	 * Enable events recording.
	 */
	private boolean adminEventsEnabled;

	/**
	 * Enable admin events details.
	 */
	private boolean adminEventsDetailsEnabled;
	//
	//	/**
	//	 * Client scopes.
	//	 */
	//	private List<KeycloakClientScope> clientScopes;
	//
	//	/**
	//	 * Authentication flows.
	//	 */
	//	private List<KeycloakAPIAuthenticationFlow> authenticationFlows;
	//
	//	/**
	//	 * Authenticator config.
	//	 */
	//	private List<KeycloakAPIAuthenticatorConfig> authenticatorConfig;
	//
	//	/**
	//	 * Point keycloak to an external user provider to validate credentials or pull in identity information.
	//	 */
	//	private List<KeycloakAPIUserFederationProvider> userFederationProviders;
	//
	//	/**
	//	 * User federation mappers are extension points triggered by the user federation at various points.
	//	 */
	//	private List<KeycloakAPIUserFederationMapper> userFederationMappers;
	//
	//	/**
	//	 * User registration.
	//	 */
	//	private boolean registrationAllowed;
	//
	//	/**
	//	 * Email as username.
	//	 */
	//	private boolean registrationEmailAsUsername;
	//
	//	/**
	//	 * Edit username.
	//	 */
	//	private boolean editUsernameAllowed;
	//
	//	/**
	//	 * Forgot password.
	//	 */
	//	private boolean resetPasswordAllowed;
	//
	//	/**
	//	 * Remember me.
	//	 */
	//	private boolean rememberMe;
	//
	//	/**
	//	 * Verify email.
	//	 */
	//	private boolean verifyEmail;
	//
	//	/**
	//	 * Login with email.
	//	 */
	//	private boolean loginWithEmailAllowed;
	//
	//	/**
	//	 * Duplicate emails.
	//	 */
	//	private boolean duplicateEmailsAllowed;
	//
	//	/**
	//	 * Require SSL.
	//	 */
	//	private String sslRequired;
	//
}
