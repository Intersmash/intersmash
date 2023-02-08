package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import io.fabric8.kubernetes.api.model.ResourceRequirements;

/**
 * Resources (Requests and Limits) for PostgresDeployment.
 */
public final class PostgresqlDeploymentSpecBuilder {
	private ResourceRequirements resources;

	/**
	 * Set the Resources (Requests and Limits) for the Pods.
	 *
	 * @param resources The Resources (Requests and Limits) for the Pods
	 * @return this
	 */
	public PostgresqlDeploymentSpecBuilder resources(ResourceRequirements resources) {
		this.resources = resources;
		return this;
	}

	public PostgresqlDeploymentSpec build() {
		PostgresqlDeploymentSpec postgresqlDeploymentSpec = new PostgresqlDeploymentSpec();
		postgresqlDeploymentSpec.setResources(resources);
		return postgresqlDeploymentSpec;
	}
}
