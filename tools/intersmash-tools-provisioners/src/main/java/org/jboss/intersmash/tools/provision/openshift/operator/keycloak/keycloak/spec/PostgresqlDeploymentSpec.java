package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.fabric8.kubernetes.api.model.ResourceRequirements;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Resources (Requests and Limits) for PostgresDeployment.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PostgresqlDeploymentSpec {

	/**
	 * Resources (Requests and Limits) for the Pods.
	 */
	private ResourceRequirements resources;
}
