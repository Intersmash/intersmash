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
package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

public final class InfinispanSiteLocationSpecBuilder {
	private String name;
	private String url;
	private String secretName;

	/**
	 * Set the location name
	 *
	 * @param name Location name
	 * @return this
	 */
	public InfinispanSiteLocationSpecBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Set the location URL
	 *
	 * @param url Location URL
	 * @return this
	 */
	public InfinispanSiteLocationSpecBuilder url(String url) {
		this.url = url;
		return this;
	}

	/**
	 * Names the secret used to encrypt communication with the location
	 *
	 * @param secretName Name of the secret used to encrypt communication with the location
	 * @return this
	 */
	public InfinispanSiteLocationSpecBuilder secretName(String secretName) {
		this.secretName = secretName;
		return this;
	}

	public InfinispanSiteLocationSpec build() {
		InfinispanSiteLocationSpec infinispanSiteLocationSpec = new InfinispanSiteLocationSpec();
		infinispanSiteLocationSpec.setName(name);
		infinispanSiteLocationSpec.setUrl(url);
		infinispanSiteLocationSpec.setSecretName(secretName);
		return infinispanSiteLocationSpec;
	}
}
