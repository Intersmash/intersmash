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

import java.util.List;

public final class InfinispanSitesSpecBuilder {
	private InfinispanSitesLocalSpec local;
	private List<InfinispanSiteLocationSpec> locations;

	/**
	 * Specify the local site
	 *
	 * @param local {@link InfinispanSitesLocalSpec} that defines the local site configuration
	 * @return this
	 */
	public InfinispanSitesSpecBuilder local(InfinispanSitesLocalSpec local) {
		this.local = local;
		return this;
	}

	/**
	 * Specify the backup locations
	 *
	 * @param locations List of {@link InfinispanSiteLocationSpec} that describe the backup locations configuration
	 * @return this
	 */
	public InfinispanSitesSpecBuilder locations(List<InfinispanSiteLocationSpec> locations) {
		this.locations = locations;
		return this;
	}

	public InfinispanSitesSpec build() {
		InfinispanSitesSpec infinispanSitesSpec = new InfinispanSitesSpec();
		infinispanSitesSpec.setLocal(local);
		infinispanSitesSpec.setLocations(locations);
		return infinispanSitesSpec;
	}
}
