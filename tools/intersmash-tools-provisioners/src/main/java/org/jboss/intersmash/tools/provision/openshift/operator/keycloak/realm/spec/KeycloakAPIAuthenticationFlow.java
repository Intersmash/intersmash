package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec;

import java.util.List;

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
public class KeycloakAPIAuthenticationFlow {

	/**
	 * Alias.
	 */
	private String alias;

	/**
	 * Authentication executions.
	 */
	private List<KeycloakAPIAuthenticationExecution> authenticationExecutions;

	/**
	 * Built in.
	 */
	private boolean builtIn;

	/**
	 * Description.
	 */
	private String description;

	/**
	 * ID.
	 */
	private String id;

	/**
	 * Provider ID.
	 */
	private String providerId;

	/**
	 * Top level.
	 */
	private boolean topLevel;
}
