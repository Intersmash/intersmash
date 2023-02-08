package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.backup.status;

import java.util.List;
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
public class KeycloakBackupStatus {

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
}
