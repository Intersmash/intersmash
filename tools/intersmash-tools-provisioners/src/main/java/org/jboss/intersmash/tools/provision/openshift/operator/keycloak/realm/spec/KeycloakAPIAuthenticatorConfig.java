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
public class KeycloakAPIAuthenticatorConfig {

	/**
	 * Alias.
	 */
	private String alias;

	/**
	 * Config.
	 */
	private String config;

	/**
	 * ID.
	 */
	private String id;
}
