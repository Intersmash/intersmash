package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.fabric8.kubernetes.api.model.LabelSelector;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * KeycloakUserSpec defines the desired state of KeycloakUser.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakUserSpec {
	/**
	 * Selector for looking up KeycloakRealm Custom Resources.
	 */
	private LabelSelector realmSelector;

	/**
	 * Keycloak User REST object.
	 */
	private KeycloakAPIUser user;
}
