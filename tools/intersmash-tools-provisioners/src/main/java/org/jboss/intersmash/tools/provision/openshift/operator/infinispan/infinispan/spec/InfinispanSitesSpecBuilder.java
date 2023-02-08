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
