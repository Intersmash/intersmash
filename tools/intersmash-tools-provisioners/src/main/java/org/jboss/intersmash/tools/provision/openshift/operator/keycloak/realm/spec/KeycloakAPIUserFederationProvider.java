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
public class KeycloakAPIUserFederationProvider {

	/**
	 * User federation provider config.
	 */
	private Map<String, String> config;

	/**
	 * The display name of this provider instance.
	 */
	private String displayName;

	private int fullSyncPeriod;

	/**
	 * The ID of this provider.
	 */
	private String id;

	/**
	 * The priority of this provider when looking up users or adding a user.
	 */
	private int priority;

	/**
	 * The name of the user provider, such as "ldap", "kerberos" or a custom SPI.
	 */
	private String providerName;
}
