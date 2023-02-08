package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec;

import java.util.HashSet;
import java.util.Set;

import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.client.spec.KeycloakAPIClient;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.spec.KeycloakAPIUser;

public final class KeycloakAPIRealmBuilder {
	private String id;
	private String realm;
	private boolean enabled;
	private String displayName;
	private Set<KeycloakAPIUser> users;
	private Set<KeycloakAPIClient> clients;
	private Set<KeycloakIdentityProvider> identityProviders;
	private Set<String> eventsListeners;
	private boolean eventsEnabled;
	private boolean adminEventsEnabled;
	private boolean adminEventsDetailsEnabled;
	//	private List<KeycloakClientScope> clientScopes;
	//	private List<KeycloakAPIAuthenticationFlow> authenticationFlows;
	//	private List<KeycloakAPIAuthenticatorConfig> authenticatorConfig;
	//	private List<KeycloakAPIUserFederationProvider> userFederationProviders;
	//	private List<KeycloakAPIUserFederationMapper> userFederationMappers;
	//	private boolean registrationAllowed;
	//	private boolean registrationEmailAsUsername;
	//	private boolean editUsernameAllowed;
	//	private boolean resetPasswordAllowed;
	//	private boolean rememberMe;
	//	private boolean verifyEmail;
	//	private boolean loginWithEmailAllowed;
	//	private boolean duplicateEmailsAllowed;
	//	private String sslRequired;

	public KeycloakAPIRealmBuilder id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Set the realm name.
	 *
	 * @param realm The desired realm name
	 * @return this
	 */
	public KeycloakAPIRealmBuilder realm(String realm) {
		this.realm = realm;
		return this;
	}

	/**
	 * Determine whether the realm is enabled.
	 *
	 * @param enabled Whether the realm is enabled
	 * @return this
	 */
	public KeycloakAPIRealmBuilder enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/**
	 * Set the realm display name.
	 *
	 * @param displayName The desired realm display name
	 * @return this
	 */
	public KeycloakAPIRealmBuilder displayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	/**
	 * Set Keycloak users
	 *
	 * @param users A {@link Set} of {@link KeycloakAPIUser} instances
	 * @return this
	 */
	public KeycloakAPIRealmBuilder users(Set<KeycloakAPIUser> users) {
		this.users = users;
		return this;
	}

	/**
	 * Add one Keycloak User
	 *
	 * @param user The {@link KeycloakAPIUser} instance representing the user that should be added
	 * @return this
	 */
	public KeycloakAPIRealmBuilder users(KeycloakAPIUser user) {
		if (users == null) {
			users = new HashSet<>();
		}
		users.add(user);
		return this;
	}

	/**
	 *  Set Keycloak Clients.
	 *
	 * @param clients A {@link Set} of {@link KeycloakAPIClient} instances
	 * @return this
	 */
	public KeycloakAPIRealmBuilder clients(Set<KeycloakAPIClient> clients) {
		this.clients = clients;
		return this;
	}

	/**
	 *  Add one Keycloak Client.
	 *
	 * @param client The {@link KeycloakAPIClient} instance representing the client that should be added
	 * @return this
	 */
	public KeycloakAPIRealmBuilder clients(KeycloakAPIClient client) {
		if (clients == null) {
			clients = new HashSet<>();
		}
		clients.add(client);
		return this;
	}

	/**
	 * Set Identity Providers.
	 *
	 * @param identityProviders A {@link Set} of {@link KeycloakIdentityProvider} instances
	 * @return this
	 */
	public KeycloakAPIRealmBuilder identityProviders(Set<KeycloakIdentityProvider> identityProviders) {
		this.identityProviders = identityProviders;
		return this;
	}

	/**
	 * Add one Identity Provider.
	 *
	 * @param identityProvider The {@link KeycloakIdentityProvider} instance representing the identity provider that
	 *                         should be added
	 * @return this
	 */
	public KeycloakAPIRealmBuilder identityProviders(KeycloakIdentityProvider identityProvider) {
		if (identityProviders == null) {
			identityProviders = new HashSet<>();
		}
		identityProviders.add(identityProvider);
		return this;
	}

	/**
	 * Set Event Listeners.
	 *
	 * @param eventsListeners A {@link Set} of events listeners
	 * @return this
	 */
	public KeycloakAPIRealmBuilder eventsListeners(Set<String> eventsListeners) {
		this.eventsListeners = eventsListeners;
		return this;
	}

	/**
	 * Add one Event Listener.
	 *
	 * @param eventsListener The string representing the events listener that
	 *                         should be added
	 * @return this
	 */
	public KeycloakAPIRealmBuilder eventsListeners(String eventsListener) {
		if (eventsListeners == null) {
			eventsListeners = new HashSet<>();
		}
		eventsListeners.add(eventsListener);
		return this;
	}

	/**
	 * Enable events recording.
	 *
	 * @param eventsEnabled Whether events recording is enabled
	 * @return this
	 */
	public KeycloakAPIRealmBuilder eventsEnabled(boolean eventsEnabled) {
		this.eventsEnabled = eventsEnabled;
		return this;
	}

	/**
	 * Enable admin events recording.
	 *
	 * @param adminEventsEnabled Whether admin events recording is enabled
	 * @return this
	 */
	public KeycloakAPIRealmBuilder adminEventsEnabled(boolean adminEventsEnabled) {
		this.adminEventsEnabled = adminEventsEnabled;
		return this;
	}

	/**
	 * Enable admin events details.
	 *
	 * @param adminEventsDetailsEnabled Whether admin events details recording is enabled
	 * @return this
	 */
	public KeycloakAPIRealmBuilder adminEventsDetailsEnabled(boolean adminEventsDetailsEnabled) {
		this.adminEventsDetailsEnabled = adminEventsDetailsEnabled;
		return this;
	}
	//
	//	/**
	//	 * Client scopes.
	//	 */
	//	public KeycloakAPIRealmBuilder clientScopes(List<KeycloakClientScope> clientScopes) {
	//		this.clientScopes = clientScopes;
	//		return this;
	//	}
	//
	//	/**
	//	 * Add Client scopes.
	//	 */
	//	public KeycloakAPIRealmBuilder clientScopes(KeycloakClientScope clientScope) {
	//		if (clientScopes == null) {
	//			clientScopes = new ArrayList<>();
	//		}
	//		clientScopes.add(clientScope);
	//		return this;
	//	}
	//
	//	/**
	//	 * Authentication flows.
	//	 */
	//	public KeycloakAPIRealmBuilder authenticationFlows(List<KeycloakAPIAuthenticationFlow> authenticationFlows) {
	//		this.authenticationFlows = authenticationFlows;
	//		return this;
	//	}
	//
	//	/**
	//	 * Add Authentication flow.
	//	 */
	//	public KeycloakAPIRealmBuilder authenticationFlows(KeycloakAPIAuthenticationFlow authenticationFlow) {
	//		if (authenticationFlows == null) {
	//			authenticationFlows = new ArrayList<>();
	//		}
	//		authenticationFlows.add(authenticationFlow);
	//		return this;
	//	}
	//
	//	/**
	//	 * Authenticator config.
	//	 */
	//	public KeycloakAPIRealmBuilder authenticatorConfig(List<KeycloakAPIAuthenticatorConfig> authenticatorConfig) {
	//		this.authenticatorConfig = authenticatorConfig;
	//		return this;
	//	}
	//
	//	/**
	//	 * Add Authenticator config.
	//	 */
	//	public KeycloakAPIRealmBuilder authenticatorConfig(KeycloakAPIAuthenticatorConfig authenticatorConfig) {
	//		if (this.authenticatorConfig == null) {
	//			this.authenticatorConfig = new ArrayList<>();
	//		}
	//		this.authenticatorConfig.add(authenticatorConfig);
	//		return this;
	//	}
	//
	//	/**
	//	 * Point keycloak to an external user provider to validate credentials or pull in identity information.
	//	 */
	//	public KeycloakAPIRealmBuilder userFederationProviders(List<KeycloakAPIUserFederationProvider> userFederationProviders) {
	//		this.userFederationProviders = userFederationProviders;
	//		return this;
	//	}
	//
	//	/**
	//	 * Add external user provider to validate credentials or pull in identity information.
	//	 */
	//	public KeycloakAPIRealmBuilder userFederationProviders(KeycloakAPIUserFederationProvider userFederationProvider) {
	//		if (userFederationProviders == null) {
	//			userFederationProviders = new ArrayList<>();
	//		}
	//		userFederationProviders.add(userFederationProvider);
	//		return this;
	//	}
	//
	//	/**
	//	 * User federation mappers are extension points triggered by the user federation at various points.
	//	 */
	//	public KeycloakAPIRealmBuilder userFederationMappers(List<KeycloakAPIUserFederationMapper> userFederationMappers) {
	//		this.userFederationMappers = userFederationMappers;
	//		return this;
	//	}
	//
	//	/**
	//	 * Add user federation mapper. User federation mappers are extension points triggered by the user federation at
	//	 * various points.
	//	 */
	//	public KeycloakAPIRealmBuilder userFederationMappers(KeycloakAPIUserFederationMapper userFederationMapper) {
	//		if (userFederationMappers == null) {
	//			userFederationMappers = new ArrayList<>();
	//		}
	//		userFederationMappers.add(userFederationMapper);
	//		return this;
	//	}
	//
	//	/**
	//	 * User registration.
	//	 */
	//	public KeycloakAPIRealmBuilder registrationAllowed(boolean registrationAllowed) {
	//		this.registrationAllowed = registrationAllowed;
	//		return this;
	//	}
	//
	//	/**
	//	 * Email as username.
	//	 */
	//	public KeycloakAPIRealmBuilder registrationEmailAsUsername(boolean registrationEmailAsUsername) {
	//		this.registrationEmailAsUsername = registrationEmailAsUsername;
	//		return this;
	//	}
	//
	//	/**
	//	 * Edit username.
	//	 */
	//	public KeycloakAPIRealmBuilder editUsernameAllowed(boolean editUsernameAllowed) {
	//		this.editUsernameAllowed = editUsernameAllowed;
	//		return this;
	//	}
	//
	//	/**
	//	 * Forgot password.
	//	 */
	//	public KeycloakAPIRealmBuilder resetPasswordAllowed(boolean resetPasswordAllowed) {
	//		this.resetPasswordAllowed = resetPasswordAllowed;
	//		return this;
	//	}
	//
	//	/**
	//	 * Remember me.
	//	 */
	//	public KeycloakAPIRealmBuilder rememberMe(boolean rememberMe) {
	//		this.rememberMe = rememberMe;
	//		return this;
	//	}
	//
	//	/**
	//	 * Verify email.
	//	 */
	//	public KeycloakAPIRealmBuilder verifyEmail(boolean verifyEmail) {
	//		this.verifyEmail = verifyEmail;
	//		return this;
	//	}
	//
	//	/**
	//	 * Login with email.
	//	 */
	//	public KeycloakAPIRealmBuilder loginWithEmailAllowed(boolean loginWithEmailAllowed) {
	//		this.loginWithEmailAllowed = loginWithEmailAllowed;
	//		return this;
	//	}
	//
	//	/**
	//	 * Duplicate emails.
	//	 */
	//	public KeycloakAPIRealmBuilder duplicateEmailsAllowed(boolean duplicateEmailsAllowed) {
	//		this.duplicateEmailsAllowed = duplicateEmailsAllowed;
	//		return this;
	//	}
	//
	//	/**
	//	 * Require SSL.
	//	 */
	//	public KeycloakAPIRealmBuilder sslRequired(String sslRequired) {
	//		this.sslRequired = sslRequired;
	//		return this;
	//	}

	public KeycloakAPIRealm build() {
		KeycloakAPIRealm keycloakAPIRealm = new KeycloakAPIRealm();
		keycloakAPIRealm.setId(id);
		keycloakAPIRealm.setRealm(realm);
		keycloakAPIRealm.setEnabled(enabled);
		keycloakAPIRealm.setDisplayName(displayName);
		keycloakAPIRealm.setUsers(users);
		keycloakAPIRealm.setClients(clients);
		keycloakAPIRealm.setIdentityProviders(identityProviders);
		keycloakAPIRealm.setEventsListeners(eventsListeners);
		keycloakAPIRealm.setEventsEnabled(eventsEnabled);
		keycloakAPIRealm.setAdminEventsEnabled(adminEventsEnabled);
		keycloakAPIRealm.setAdminEventsDetailsEnabled(adminEventsDetailsEnabled);
		//		keycloakAPIRealm.setClientScopes(clientScopes);
		//		keycloakAPIRealm.setAuthenticationFlows(authenticationFlows);
		//		keycloakAPIRealm.setAuthenticatorConfig(authenticatorConfig);
		//		keycloakAPIRealm.setUserFederationProviders(userFederationProviders);
		//		keycloakAPIRealm.setUserFederationMappers(userFederationMappers);
		//		keycloakAPIRealm.setRegistrationAllowed(registrationAllowed);
		//		keycloakAPIRealm.setRegistrationEmailAsUsername(registrationEmailAsUsername);
		//		keycloakAPIRealm.setEditUsernameAllowed(editUsernameAllowed);
		//		keycloakAPIRealm.setResetPasswordAllowed(resetPasswordAllowed);
		//		keycloakAPIRealm.setRememberMe(rememberMe);
		//		keycloakAPIRealm.setVerifyEmail(verifyEmail);
		//		keycloakAPIRealm.setLoginWithEmailAllowed(loginWithEmailAllowed);
		//		keycloakAPIRealm.setDuplicateEmailsAllowed(duplicateEmailsAllowed);
		//		keycloakAPIRealm.setSslRequired(sslRequired);
		return keycloakAPIRealm;
	}
}
