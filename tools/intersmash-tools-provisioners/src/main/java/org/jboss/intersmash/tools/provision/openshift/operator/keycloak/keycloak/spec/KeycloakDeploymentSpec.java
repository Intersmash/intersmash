package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.fabric8.kubernetes.api.model.ResourceRequirements;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Resources (Requests and Limits) for KeycloakDeployment.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakDeploymentSpec {

	/**
	 * Resources (Requests and Limits) for the Pods.
	 */
	private ResourceRequirements resources;

	/**
	 * Experimental section
	 * NOTE: This section might change or get removed without any notice. It may also cause the deployment to behave
	 * in an unpredictable fashion. Please use with care.
	 */
	private ExperimentalSpec experimental;
}
