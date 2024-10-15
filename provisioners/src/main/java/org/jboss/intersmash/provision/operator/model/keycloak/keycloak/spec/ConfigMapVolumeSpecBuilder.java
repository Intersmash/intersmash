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
package org.jboss.intersmash.provision.operator.model.keycloak.keycloak.spec;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.v1alpha1.keycloakspec.keycloakdeploymentspec.experimental.volumes.items.Items;

public final class ConfigMapVolumeSpecBuilder {
	private String name;
	private String mountPath;
	private List<Items> items;

	/**
	 * Set the ConfigMap name.
	 *
	 * @param name Desired ConfigMap name
	 * @return this
	 */
	public ConfigMapVolumeSpecBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Set the absolute path where to mount the COnfigMap.
	 *
	 * @param mountPath Desired mount path for the ConfigMap
	 * @return this
	 */
	public ConfigMapVolumeSpecBuilder mountPath(String mountPath) {
		this.mountPath = mountPath;
		return this;
	}

	/**
	 * ConfigMap mount details.
	 *
	 * @param items List of {@link Items} instances representing the ConfigMap mount details
	 * @return this
	 */
	public ConfigMapVolumeSpecBuilder items(List<Items> items) {
		this.items = items;
		return this;
	}

	/**
	 * Add one ConfigMap mount detail item.
	 *
	 * @param item {@link Items} instances representing the ConfigMap mount details that should be added
	 * @return this
	 */
	public ConfigMapVolumeSpecBuilder items(Items item) {
		if (items == null) {
			items = new ArrayList<>();
		}
		items.add(item);
		return this;
	}

	public org.keycloak.v1alpha1.keycloakspec.keycloakdeploymentspec.experimental.volumes.Items build() {
		org.keycloak.v1alpha1.keycloakspec.keycloakdeploymentspec.experimental.volumes.Items configMapVolumeSpec = new org.keycloak.v1alpha1.keycloakspec.keycloakdeploymentspec.experimental.volumes.Items();
		configMapVolumeSpec.setName(name);
		configMapVolumeSpec.setMountPath(mountPath);
		configMapVolumeSpec.setItems(items);
		return configMapVolumeSpec;
	}
}
