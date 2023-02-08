package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * KeycloakUserStatus defines the observed state of KeycloakUser.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakUserStatus {

	/**
	 * Human-readable message indicating details about current operator phase or error.
	 */
	private String message;

	/**
	 * Current phase of the operator.
	 */
	private String phase;
}
