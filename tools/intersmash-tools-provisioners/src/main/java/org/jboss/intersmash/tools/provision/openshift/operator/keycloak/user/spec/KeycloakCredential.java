package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.spec;

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
public class KeycloakCredential {

	/**
	 * Credential Type.
	 */
	private String type;

	/**
	 * Credential Value.
	 */
	private String value;

	/**
	 * True if this credential object is temporary.
	 */
	private boolean temporary;
}
