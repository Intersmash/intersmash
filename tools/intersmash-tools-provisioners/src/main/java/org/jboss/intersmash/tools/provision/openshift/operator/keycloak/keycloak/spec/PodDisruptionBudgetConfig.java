package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Specify PodDisruptionBudget configuration.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PodDisruptionBudgetConfig {

	/**
	 * If set to true, the operator will create a PodDistruptionBudget for the Keycloak deployment and set its
	 * `maxUnavailable` value to 1.
	 */
	private boolean enabled;
}
