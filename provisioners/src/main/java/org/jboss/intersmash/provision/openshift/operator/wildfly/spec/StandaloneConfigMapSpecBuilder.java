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
package org.jboss.intersmash.provision.openshift.operator.wildfly.spec;

import org.wildfly.v1alpha1.wildflyserverspec.StandaloneConfigMap;

public final class StandaloneConfigMapSpecBuilder {
	private String name;
	private String key;

	/**
	 * Initialize the {@link StandaloneConfigMapSpecBuilder} with given resource name.
	 *
	 * @param name of the {@link io.fabric8.kubernetes.api.model.ConfigMap} containing the standalone configuration XML file.
	 */
	public StandaloneConfigMapSpecBuilder(String name) {
		this.name = name;
	}

	/**
	 * Key of the {@link io.fabric8.kubernetes.api.model.ConfigMap} whose value is the
	 * standalone configuration XML file. If omitted, the spec will look for the standalone.xml key.
	 *
	 * @param key Desired key of for the {@link io.fabric8.kubernetes.api.model.ConfigMap} whose value is the
	 * 	 standalone configuration XML file.
	 * @return this
	 */
	public StandaloneConfigMapSpecBuilder key(String key) {
		this.key = key;
		return this;
	}

	public StandaloneConfigMap build() {
		StandaloneConfigMap standaloneConfigMapSpec = new StandaloneConfigMap();
		standaloneConfigMapSpec.setName(name);
		standaloneConfigMapSpec.setKey(key);
		return standaloneConfigMapSpec;
	}
}
