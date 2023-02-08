package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec;

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
public class KeycloakAPIAuthenticationExecution {

	/**
	 * Authenticator.
	 */
	private String authenticator;

	/**
	 * Authenticator Config.
	 */
	private String authenticatorConfig;

	/**
	 * Authenticator flow.
	 */
	private String authenticatorFlow;

	/**
	 * Flow Alias.
	 */
	private String flowAlias;

	/**
	 * Priority.
	 */
	private int priority;

	/**
	 * Requirement [REQUIRED, OPTIONAL, ALTERNATIVE, DISABLED].
	 */
	private String requirement;

	/**
	 * User setup allowed.
	 */
	private boolean userSetupAllowed;
}
