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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.k8s.legacy.v1alpha1.keycloakrealmspec.realm.Users;
import org.keycloak.k8s.legacy.v1alpha1.keycloakrealmspec.realm.users.Credentials;
import org.keycloak.k8s.legacy.v1alpha1.keycloakrealmspec.realm.users.FederatedIdentities;
import org.keycloak.k8s.legacy.v1alpha1.keycloakuserspec.User;

public final class KeycloakAPIUserBuilder {
	private String id;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private boolean emailVerified;
	private boolean enabled;
	private List<String> realmRoles;
	private Map<String, List<String>> clientRoles;
	private List<String> requiredActions;
	private List<String> groups;
	private List<FederatedIdentities> federatedIdentities;
	private List<Credentials> credentials;
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
	public KeycloakAPIUserBuilder realmRoles(List<String> realmRoles) {
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
			realmRoles = new ArrayList<>();
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
	public KeycloakAPIUserBuilder requiredActions(List<String> requiredActions) {
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
			requiredActions = new ArrayList<>();
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
	public KeycloakAPIUserBuilder groups(List<String> groups) {
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
			groups = new ArrayList<>();
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
	public KeycloakAPIUserBuilder federatedIdentities(List<FederatedIdentities> federatedIdentities) {
		this.federatedIdentities = federatedIdentities;
		return this;
	}

	/**
	 * Add one Federated Identity.
	 *
	 * @param federatedIdentity The Federated Identity that should be added to the user ones
	 * @return this
	 */
	public KeycloakAPIUserBuilder federatedIdentities(FederatedIdentities federatedIdentity) {
		if (federatedIdentities == null) {
			federatedIdentities = new ArrayList<>();
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
	public KeycloakAPIUserBuilder credentials(List<Credentials> credentials) {
		this.credentials = credentials;
		return this;
	}

	/**
	 * Add one Credential.
	 *
	 * @param credential The credential that should be added to the user ones
	 * @return this
	 */
	public KeycloakAPIUserBuilder credentials(Credentials credential) {
		if (credentials == null) {
			credentials = new ArrayList<>();
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

	public Users build() {
		Users keycloakAPIUser = new Users();
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
