/**
 * Copyright (C) 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * KeycloakSpec defines the desired state of Keycloak.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakSpec {

	//	/**
	//	 * When set to true, this Keycloak will be marked as unmanaged and will not be managed by this operator.
	//	 *
	//	 * It can then be used for targeting purposes.
	//	 */
	//	private boolean unmanaged;
	//
	//	/**
	//	 * Contains configuration for external Keycloak instances. Unmanaged needs to be set to true to use this.
	//	 */
	//	private KeycloakExternal external;

	/**
	 * A list of extensions, where each one is a URL to a JAR files that will be deployed in Keycloak.
	 */
	private List<String> extensions;

	/**
	 * Number of Keycloak instances in HA mode. Default is 1.
	 */
	private int instances;

	/**
	 * Controls external Ingress/Route settings.
	 */
	private KeycloakExternalAccess externalAccess;

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
	 * 	        POSTGRES_DATABASE: <Database Name>
	 * 	        POSTGRES_EXTERNAL_ADDRESS: <External Database IP or URL (resolvable by K8s)>
	 * 	        POSTGRES_EXTERNAL_PORT: <External Database Port>
	 * 	        # Strongly recommended to use <'Keycloak CR Name'-postgresql>
	 * 	        POSTGRES_HOST: <Database Service Name>
	 * 	        POSTGRES_PASSWORD: <Database Password>
	 * 	        # Required for AWS Backup functionality
	 * 	        POSTGRES_SUPERUSER: true
	 * 	        POSTGRES_USERNAME: <Database Username>
	 * 	     type: Opaque
	 *
	 * 	Both POSTGRES_EXTERNAL_ADDRESS and POSTGRES_EXTERNAL_PORT are specifically required for creating
	 * 	connection to the external database. The secret name is created using the following convention:
	 * 	      <Custom Resource Name>-db-secret
	 *
	 * 	For more information, please refer to the Operator documentation.
	 */
	private KeycloakExternalDatabase externalDatabase;

	/**
	 * Profile used for controlling Operator behavior. Default is empty.
	 */
	private String profile;

	/**
	 * Specify PodDisruptionBudget configuration.
	 */
	private PodDisruptionBudgetConfig podDisruptionBudget;

	//	/**
	//	 * Resources (Requests and Limits) for KeycloakDeployment.
	//	 */
	//	private KeycloakDeploymentSpec keycloakDeploymentSpec;
	//
	//	/**
	//	 * Resources (Requests and Limits) for PostgresDeployment.
	//	 */
	//	private PostgresqlDeploymentSpec postgresDeploymentSpec;
	//
	//	/**
	//	 * Specify Migration configuration.
	//	 */
	//	private MigrateConfig migration;
	//
	//	/**
	//	 * Name of the StorageClass for Postgresql Persistent Volume Claim.
	//	 */
	//	private String storageClassName;
}
