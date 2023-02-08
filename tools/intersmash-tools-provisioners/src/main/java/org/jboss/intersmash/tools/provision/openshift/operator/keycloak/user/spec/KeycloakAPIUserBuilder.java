package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.spec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class KeycloakAPIUserBuilder {
	private String id;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private boolean emailVerified;
	private boolean enabled;
	private Set<String> realmRoles;
	private Map<String, List<String>> clientRoles;
	private Set<String> requiredActions;
	private Set<String> groups;
	private Set<FederatedIdentity> federatedIdentities;
	private Set<KeycloakCredential> credentials;
	//	private Map<String, List<String>> attributes;

	/**
	 * Set the User ID.
	 *
	 * @param id The user ID
	 * @return this
	 */
	public KeycloakAPIUserBuilder id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Set the User Name.
	 *
	 * @param username The user name
	 * @return this
	 */
	public KeycloakAPIUserBuilder username(String username) {
		this.username = username;
		return this;
	}

	/**
	 * Set the First Name.
	 *
	 * @param firstName The user first name
	 * @return this
	 */
	public KeycloakAPIUserBuilder firstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	/**
	 * Set the Last Name.
	 *
	 * @param lastName The user last name
	 * @return this
	 */
	public KeycloakAPIUserBuilder lastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	/**
	 * Set the Email.
	 *
	 * @param email The user email
	 * @return this
	 */
	public KeycloakAPIUserBuilder email(String email) {
		this.email = email;
		return this;
	}

	/**
	 * True if email has already been verified.
	 *
	 * @param emailVerified Whether the user email is verified
	 * @return this
	 */
	public KeycloakAPIUserBuilder emailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
		return this;
	}

	/**
	 * Set the User enabled flag.
	 *
	 * @param enabled Whether the user is enabled
	 * @return this
	 */
	public KeycloakAPIUserBuilder enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/**
	 * A set of Realm Roles.
	 *
	 * @param realmRoles A set of Realm Roles that should be added the user
	 * @return this
	 */
	public KeycloakAPIUserBuilder realmRoles(Set<String> realmRoles) {
		this.realmRoles = realmRoles;
		return this;
	}

	/**
	 * Add one Realm Role.
	 *
	 * @param realmRole The Realm Role that should be added to the user ones
	 * @return this
	 */
	public KeycloakAPIUserBuilder realmRoles(String realmRole) {
		if (realmRoles == null) {
			realmRoles = new HashSet<>();
		}
		realmRoles.add(realmRole);
		return this;
	}

	/**
	 * A set of Client Roles.
	 *
	 * @param clientRoles A set of Client Roles that should be added the user
	 * @return this
	 */
	public KeycloakAPIUserBuilder clientRoles(Map<String, List<String>> clientRoles) {
		this.clientRoles = clientRoles;
		return this;
	}

	/**
	 * Add one Client Role
	 *
	 * @param clientRole The Client Role that should be added to the user ones
	 * @param additionalProperties The Client Role properties
	 * @return this
	 */
	public KeycloakAPIUserBuilder clientRole(String clientRole, List<String> additionalProperties) {
		if (clientRoles == null) {
			clientRoles = new HashMap<>();
		}
		clientRoles.put(clientRole, additionalProperties);
		return this;
	}

	/**
	 * A set of Required Actions
	 *
	 * @param requiredActions A set of Required Actions that should be added the user
	 * @return this
	 */
	public KeycloakAPIUserBuilder requiredActions(Set<String> requiredActions) {
		this.requiredActions = requiredActions;
		return this;
	}

	/**
	 * Add one Required Action
	 *
	 * @param requiredAction The Required Action that should be added to the user ones
	 * @return this
	 */
	public KeycloakAPIUserBuilder requiredActions(String requiredAction) {
		if (requiredActions == null) {
			requiredActions = new HashSet<>();
		}
		requiredActions.add(requiredAction);
		return this;
	}

	/**
	 * A set of Groups.
	 *
	 * @param groups A set of Groups that should be added the user
	 * @return this
	 */
	public KeycloakAPIUserBuilder groups(Set<String> groups) {
		this.groups = groups;
		return this;
	}

	/**
	 * Add one Group.
	 *
	 * @param group The group that should be added to the user ones
	 * @return this
	 */
	public KeycloakAPIUserBuilder groups(String group) {
		if (groups == null) {
			groups = new HashSet<>();
		}
		groups.add(group);
		return this;
	}

	/**
	 * A set of Federated Identities.
	 *
	 * @param federatedIdentities A set of Federated Identities that should be added the user
	 * @return this
	 */
	public KeycloakAPIUserBuilder federatedIdentities(Set<FederatedIdentity> federatedIdentities) {
		this.federatedIdentities = federatedIdentities;
		return this;
	}

	/**
	 * Add one Federated Identity.
	 *
	 * @param federatedIdentity The Federated Identity that should be added to the user ones
	 * @return this
	 */
	public KeycloakAPIUserBuilder federatedIdentities(FederatedIdentity federatedIdentity) {
		if (federatedIdentities == null) {
			federatedIdentities = new HashSet<>();
		}
		federatedIdentities.add(federatedIdentity);
		return this;
	}

	/**
	 * A set of Credentials.
	 *
	 * @param credentials A set of credential that should be added the user
	 * @return this
	 */
	public KeycloakAPIUserBuilder credentials(Set<KeycloakCredential> credentials) {
		this.credentials = credentials;
		return this;
	}

	/**
	 * Add one Credential.
	 *
	 * @param credential The credential that should be added to the user ones
	 * @return this
	 */
	public KeycloakAPIUserBuilder credentials(KeycloakCredential credential) {
		if (credentials == null) {
			credentials = new HashSet<>();
		}
		credentials.add(credential);
		return this;
	}

	//	/**
	//	 * A set of Attributes.
	//	 */
	//	public KeycloakAPIUserBuilder attributes(Map<String, List<String>> attributes) {
	//		this.attributes = attributes;
	//		return this;
	//	}
	//
	//	/**
	//	 * Add Attribute.
	//	 */
	//	public KeycloakAPIUserBuilder attributes(String attribute, List<String> additionalProperties) {
	//		if (attributes == null) {
	//			attributes = new HashMap<>();
	//		}
	//		attributes.put(attribute, additionalProperties);
	//		return this;
	//	}

	public KeycloakAPIUser build() {
		KeycloakAPIUser keycloakAPIUser = new KeycloakAPIUser();
		keycloakAPIUser.setId(id);
		keycloakAPIUser.setUsername(username);
		keycloakAPIUser.setFirstName(firstName);
		keycloakAPIUser.setLastName(lastName);
		keycloakAPIUser.setEmail(email);
		keycloakAPIUser.setEmailVerified(emailVerified);
		keycloakAPIUser.setEnabled(enabled);
		keycloakAPIUser.setRealmRoles(realmRoles);
		keycloakAPIUser.setClientRoles(clientRoles);
		keycloakAPIUser.setRequiredActions(requiredActions);
		keycloakAPIUser.setGroups(groups);
		keycloakAPIUser.setFederatedIdentities(federatedIdentities);
		keycloakAPIUser.setCredentials(credentials);
		//		keycloakAPIUser.setAttributes(attributes);
		return keycloakAPIUser;
	}
}
