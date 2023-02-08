package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 	Controls external database settings.
 * 	Using an external database requires providing a secret containing credentials
 * 	as well as connection details. Here's an example of such secret:
 *
 * 	    apiVersion: v1
 * 	    kind: Secret
 * 	    metadata:
 * 	        name: keycloak-db-secret
 * 	        namespace: keycloak
 * 	    stringData:
 * 	        POSTGRES_DATABASE: &lt;Database Name&gt;
 * 	        POSTGRES_EXTERNAL_ADDRESS: &lt;External Database IP or URL (resolvable by K8s)&gt;
 * 	        POSTGRES_EXTERNAL_PORT: &lt;External Database Port&gt;
 * 	        # Strongly recommended to use &lt;'Keycloak CR Name'-postgresql&gt;
 * 	        POSTGRES_HOST: &lt;Database Service Name&gt;
 * 	        POSTGRES_PASSWORD: &lt;Database Password&gt;
 * 	        # Required for AWS Backup functionality
 * 	        POSTGRES_SUPERUSER: true
 * 	        POSTGRES_USERNAME: &lt;Database Username&gt;
 * 	     type: Opaque
 *
 * 	Both POSTGRES_EXTERNAL_ADDRESS and POSTGRES_EXTERNAL_PORT are specifically required for creating
 * 	connection to the external database. The secret name is created using the following convention:
 * 	      &lt;Custom Resource Name&gt;-db-secret
 *
 * 	For more information, please refer to the Operator documentation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakExternalDatabase {
	/**
	 * If set to true, the Operator will use an external database.
	 */
	private boolean enabled;
}
