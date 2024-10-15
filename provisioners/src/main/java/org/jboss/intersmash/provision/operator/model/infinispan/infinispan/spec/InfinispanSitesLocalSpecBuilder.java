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
package org.jboss.intersmash.provision.operator.model.infinispan.infinispan.spec;

import org.infinispan.v1.infinispanspec.service.sites.Local;
import org.infinispan.v1.infinispanspec.service.sites.local.Expose;

public final class InfinispanSitesLocalSpecBuilder {
	private String name;
	private Expose expose;

	/**
	 * Set the local site name
	 *
	 * @param name Local site name
	 * @return this
	 */
	public InfinispanSitesLocalSpecBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Describe how the local site will be exposed externally
	 *
	 * @param expose {@link Expose} instance describing how the local site will be exposed externally
	 * @return this
	 */
	public InfinispanSitesLocalSpecBuilder expose(Expose expose) {
		this.expose = expose;
		return this;
	}

	public Local build() {
		Local infinispanSitesLocalSpec = new Local();
		infinispanSitesLocalSpec.setName(name);
		infinispanSitesLocalSpec.setExpose(expose);
		return infinispanSitesLocalSpec;
	}
}
