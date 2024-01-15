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
package org.jboss.intersmash.provision.openshift.operator.keycloak.keycloak.spec;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.v1alpha1.keycloakspec.keycloakdeploymentspec.experimental.Volumes;
import org.keycloak.v1alpha1.keycloakspec.keycloakdeploymentspec.experimental.volumes.Items;

public final class VolumesSpecBuilder {
	private List<Items> items;
	private int defaultMode;

	public VolumesSpecBuilder items(List<Items> items) {
		this.items = items;
		return this;
	}

	public VolumesSpecBuilder items(Items item) {
		if (items == null) {
			items = new ArrayList<>();
		}
		items.add(item);
		return this;
	}

	public VolumesSpecBuilder defaultMode(int defaultMode) {
		this.defaultMode = defaultMode;
		return this;
	}

	public Volumes build() {
		Volumes volumesSpec = new Volumes();
		volumesSpec.setItems(items);
		volumesSpec.setDefaultMode(defaultMode);
		return volumesSpec;
	}
}
