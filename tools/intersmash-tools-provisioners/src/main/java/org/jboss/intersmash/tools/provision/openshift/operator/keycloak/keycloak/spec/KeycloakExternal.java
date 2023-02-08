package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains configuration for external Keycloak instances. Unmanaged needs to be set to true to use this.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakExternal {
	/**
	 * If set to true, this Keycloak will be treated as an external instance.
	 * The unmanaged field also needs to be set to true if this field is true.
	 */
	private boolean enabled;

	/**
	 * The URL to use for the keycloak admin API. Needs to be set if external is true.
	 */
	private String url;
}
