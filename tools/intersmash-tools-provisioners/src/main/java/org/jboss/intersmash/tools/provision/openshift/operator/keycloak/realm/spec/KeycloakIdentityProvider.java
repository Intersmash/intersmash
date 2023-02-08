package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec;

import java.util.Map;

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
public class KeycloakIdentityProvider {

	/**
	 * Identity Provider Alias.
	 */
	private String alias;

	/**
	 * Identity Provider Display Name.
	 */
	private String displayName;

	/**
	 * Identity Provider Internal ID.
	 */
	private String internalId;

	/**
	 * Identity Provider ID.
	 */
	private String providerId;

	/**
	 * Identity Provider enabled flag.
	 */
	private boolean enabled;

	/**
	 * Identity Provider Trust Email.
	 */
	private boolean trustEmail;

	/**
	 * Identity Provider Store to Token.
	 */
	private boolean storeToken;

	/**
	 * Adds Read Token role when creating this Identity Provider.
	 */
	private boolean addReadTokenRoleOnCreate;

	/**
	 * Identity Provider First Broker Login Flow Alias.
	 */
	private String firstBrokerLoginFlowAlias;

	/**
	 * Identity Provider Post Broker Login Flow Alias.
	 */
	private String postBrokerLoginFlowAlias;

	/**
	 * Identity Provider Link Only setting.
	 */
	private boolean linkOnly;

	/**
	 * Identity Provider config.
	 */
	private Map<String, String> omitempty;
}
