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

import org.keycloak.k8s.v2alpha1.Keycloak;
import org.keycloak.k8s.v2alpha1.KeycloakSpec;

import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class KeycloakBuilder {
	private String name;
	private Map<String, String> labels;
	private List<String> extensions;
	private int instances;
	private String profile;

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
	 * Profile used for controlling Operator behavior. Default is empty.
	 *
	 * @param profile Profile that should be used
	 * @return this
	 */
	public KeycloakBuilder profile(String profile) {
		this.profile = profile;
		return this;
	}

	public Keycloak build() {
		Keycloak keycloak = new Keycloak();
		keycloak.setMetadata(new ObjectMeta());
		keycloak.getMetadata().setName(name);
		keycloak.getMetadata().setLabels(labels);

		KeycloakSpec keycloakSpec = new KeycloakSpec();
		keycloakSpec.setInstances(Integer.valueOf(instances).longValue());
		keycloak.setSpec(keycloakSpec);
		return keycloak;
	}
}
