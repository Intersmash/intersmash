/*
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
package org.jboss.intersmash.provision.operator.model.keycloak.keycloak.spec;

import org.keycloak.v1alpha1.keycloakspec.KeycloakDeploymentSpec;
import org.keycloak.v1alpha1.keycloakspec.keycloakdeploymentspec.Experimental;
import org.keycloak.v1alpha1.keycloakspec.keycloakdeploymentspec.Resources;

/**
 * Resources (Requests and Limits) for KeycloakDeployment.
 */
public final class KeycloakDeploymentSpecBuilder {
	private Resources resources;
	private Experimental experimental;

	/**
	 * Set the Resources (Requests and Limits) for the Pods.
	 *
	 * @param resources The Resources (Requests and Limits) for the Pods
	 * @return this
	 */
	public KeycloakDeploymentSpecBuilder resources(Resources resources) {
		this.resources = resources;
		return this;
	}

	/**
	 * Experimental section
	 * NOTE: This section might change or get removed without any notice. It may also cause the deployment to behave
	 * in an unpredictable fashion. Please use with care.
	 *
	 * @param experimental The experimental section definition
	 * @return this
	 */
	public KeycloakDeploymentSpecBuilder experimental(Experimental experimental) {
		this.experimental = experimental;
		return this;
	}

	public KeycloakDeploymentSpec build() {
		KeycloakDeploymentSpec keycloakDeploymentSpec = new KeycloakDeploymentSpec();
		keycloakDeploymentSpec.setResources(resources);
		keycloakDeploymentSpec.setExperimental(experimental);
		return keycloakDeploymentSpec;
	}
}
