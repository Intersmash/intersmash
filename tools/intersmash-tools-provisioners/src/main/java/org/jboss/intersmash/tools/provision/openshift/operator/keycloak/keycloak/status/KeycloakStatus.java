package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.status;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * KeycloakStatus defines the observed state of Keycloak.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakStatus {

	/**
	 * Current phase of the operator.
	 */
	private String phase;

	/**
	 * Human-readable message indicating details about current operator phase or error.
	 */
	private String message;

	/**
	 * True if all resources are in a ready state and all work is done.
	 */
	private boolean ready;

	/**
	 * A map of all the secondary resources types and names created for this CR.
	 * e.g "Deployment": [ "DeploymentName1", "DeploymentName2" ].
	 */
	private Map<String, List<String>> secondaryResources;

	/**
	 * Version of Keycloak or RHSSO running on the cluster.
	 */
	private String version;

	/**
	 * Service IP and Port for in-cluster access to the keycloak instance.
	 */
	private String internalURL;

	/**
	 * Service IP and Port for external access to the keycloak instance.
	 */
	private String externalURL;

	/**
	 * The secret where the admin credentials are to be found.
	 */
	private String credentialSecret;

}
