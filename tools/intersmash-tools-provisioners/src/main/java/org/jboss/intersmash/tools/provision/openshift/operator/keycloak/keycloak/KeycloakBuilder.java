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
package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.keycloak.v1alpha1.Keycloak;
import org.keycloak.v1alpha1.KeycloakSpec;
import org.keycloak.v1alpha1.keycloakspec.ExternalAccess;
import org.keycloak.v1alpha1.keycloakspec.ExternalDatabase;
import org.keycloak.v1alpha1.keycloakspec.PodDisruptionBudget;

import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class KeycloakBuilder {
	private String name;
	private Map<String, String> labels;
	//	private boolean unmanaged;
	//	private KeycloakExternal external;
	private List<String> extensions;
	private long instances;
	private ExternalAccess externalAccess;
	private ExternalDatabase externalDatabase;
	private String profile;
	private PodDisruptionBudget podDisruptionBudget;
	//	private KeycloakDeploymentSpec keycloakDeploymentSpec;
	//	private PostgresqlDeploymentSpec postgresDeploymentSpec;
	//	private MigrateConfig migration;
	//	private String storageClassName;

	/**
	 * Initialize the {@link KeycloakBuilder} with given resource name.
	 *
	 * @param name resource object name
	 */
	public KeycloakBuilder(String name) {
		this.name = name;
	}

	/**
	 * Initialize the {@link KeycloakBuilder} with given resource name and labels.
	 *
	 * @param name resource object name
	 * @param labels key/value pairs that are attached to objects
	 */
	public KeycloakBuilder(String name, Map<String, String> labels) {
		this.name = name;
		this.labels = labels;
	}

	//	/**
	//	 * When set to true, this Keycloak will be marked as unmanaged and will not be managed by this operator.
	//	 *
	//	 * It can then be used for targeting purposes.
	//	 */
	//	public KeycloakBuilder unmanaged(boolean unmanaged) {
	//		this.unmanaged = unmanaged;
	//		return this;
	//	}
	//
	//	/**
	//	 * Contains configuration for external Keycloak instances. Unmanaged needs to be set to true to use this.
	//	 */
	//	public KeycloakBuilder external(KeycloakExternal external) {
	//		this.external = external;
	//		return this;
	//	}

	/**
	 * Set a list of extensions, where each one is a URL to a JAR files that will be deployed in Keycloak.
	 *
	 * @param extensions List of extensions that should be added
	 * @return this
	 */
	public KeycloakBuilder extensions(List<String> extensions) {
		this.extensions = extensions;
		return this;
	}

	/**
	 * Add one extension, that is a URL to a JAR files that will be deployed in Keycloak.
	 *
	 * @param extension Extensions that should be added
	 * @return this
	 */
	public KeycloakBuilder extensions(String extension) {
		if (extensions == null) {
			extensions = new ArrayList<>();
		}
		extensions.add(extension);
		return this;
	}

	/**
	 * Set the number of Keycloak instances in HA mode. Default is 1.
	 *
	 * @param instances The desired number of Keycloak instances in HA mode
	 * @return this
	 */
	public KeycloakBuilder instances(int instances) {
		this.instances = instances;
		return this;
	}

	/**
	 * Controls external Ingress/Route settings.
	 *
	 * @param externalAccess Stores external access configuration for Keycloak
	 * @return this
	 */
	public KeycloakBuilder externalAccess(ExternalAccess externalAccess) {
		this.externalAccess = externalAccess;
		return this;
	}

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
	 *
	 * @param externalDatabase Stores external database configuration for Keycloak
	 * @return this
	 */
	public KeycloakBuilder externalDatabase(ExternalDatabase externalDatabase) {
		this.externalDatabase = externalDatabase;
		return this;
	}

	/**
	 * Profile used for controlling Operator behavior. Default is empty.
	 *
	 * @param profile Profile that should be used
	 * @return this
	 */
	public KeycloakBuilder profile(String profile) {
		this.profile = profile;
		return this;
	}

	/**
	 * Specify PodDisruptionBudget configuration.
	 *
	 * @param podDisruptionBudget Stores pod disruption budget configuration
	 * @return this
	 */
	public KeycloakBuilder podDisruptionBudget(PodDisruptionBudget podDisruptionBudget) {
		this.podDisruptionBudget = podDisruptionBudget;
		return this;
	}

	//	/**
	//	 * Resources (Requests and Limits) for KeycloakDeployment.
	//	 */
	//	public KeycloakBuilder keycloakDeploymentSpec(KeycloakDeploymentSpec keycloakDeploymentSpec) {
	//		this.keycloakDeploymentSpec = keycloakDeploymentSpec;
	//		return this;
	//	}
	//
	//	/**
	//	 * Resources (Requests and Limits) for PostgresDeployment.
	//	 */
	//	public KeycloakBuilder postgresDeploymentSpec(PostgresqlDeploymentSpec postgresDeploymentSpec) {
	//		this.postgresDeploymentSpec = postgresDeploymentSpec;
	//		return this;
	//	}
	//
	//	/**
	//	 * Specify Migration configuration.
	//	 */
	//	public KeycloakBuilder migration(MigrateConfig migration) {
	//		this.migration = migration;
	//		return this;
	//	}
	//
	//	/**
	//	 * Name of the StorageClass for Postgresql Persistent Volume Claim.
	//	 */
	//	public KeycloakBuilder storageClassName(String storageClassName) {
	//		this.storageClassName = storageClassName;
	//		return this;
	//	}

	public Keycloak build() {
		Keycloak keycloak = new Keycloak();
		keycloak.setMetadata(new ObjectMeta());
		keycloak.getMetadata().setName(name);
		keycloak.getMetadata().setLabels(labels);

		KeycloakSpec keycloakSpec = new KeycloakSpec();
		//		keycloakSpec.setUnmanaged(unmanaged);
		//		keycloakSpec.setExternal(external);
		keycloakSpec.setExtensions(extensions);
		keycloakSpec.setInstances(instances);
		keycloakSpec.setExternalAccess(externalAccess);
		keycloakSpec.setExternalDatabase(externalDatabase);
		keycloakSpec.setProfile(profile);
		keycloakSpec.setPodDisruptionBudget(podDisruptionBudget);
		//		keycloakSpec.setKeycloakDeploymentSpec(keycloakDeploymentSpec);
		//		keycloakSpec.setPostgresDeploymentSpec(postgresDeploymentSpec);
		//		keycloakSpec.setMigration(migration);
		//		keycloakSpec.setStorageClassName(storageClassName);
		keycloak.setSpec(keycloakSpec);
		return keycloak;
	}
}
