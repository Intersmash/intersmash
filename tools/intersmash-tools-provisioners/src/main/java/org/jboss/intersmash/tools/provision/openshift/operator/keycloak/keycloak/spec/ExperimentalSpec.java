package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.fabric8.kubernetes.api.model.EnvVar;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Experimental section
 * NOTE: This section might change or get removed without any notice. It may also cause the deployment to behave
 * in an unpredictable fashion. Please use with care.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ExperimentalSpec {

	/**
	 *  Arguments to the entrypoint. Translates into Container CMD.
	 */
	private List<String> args;

	/**
	 * Container command. Translates into Container ENTRYPOINT.
	 */
	private List<String> command;

	/**
	 * List of environment variables to set in the container.
	 */
	private List<EnvVar> env;

	/**
	 * Additional volume mounts
	 */
	private VolumesSpec volumes;
}
