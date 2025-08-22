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

import org.keycloak.v1alpha1.keycloakspec.PodDisruptionBudget;

/**
 * Specify PodDisruptionBudget configuration.
 */
public final class PodDisruptionBudgetConfigBuilder {
	private boolean enabled;

	/**
	 * If set to true, the operator will create a PodDistruptionBudget for the Keycloak deployment and set its
	 * `maxUnavailable` value to 1.
	 *
	 * @param enabled Whether the operator should create a PodDistruptionBudget for the Keycloak deployment
	 * @return this
	 */
	public PodDisruptionBudgetConfigBuilder enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public PodDisruptionBudget build() {
		PodDisruptionBudget podDisruptionBudgetConfig = new PodDisruptionBudget();
		podDisruptionBudgetConfig.setEnabled(enabled);
		return podDisruptionBudgetConfig;
	}
}
