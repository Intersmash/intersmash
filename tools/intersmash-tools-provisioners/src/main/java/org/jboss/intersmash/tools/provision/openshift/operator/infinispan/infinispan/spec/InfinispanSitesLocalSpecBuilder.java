package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

public final class InfinispanSitesLocalSpecBuilder {
	private String name;
	private ExposeSpec expose;

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
	 * @param expose {@link ExposeSpec} instance describing how the local site will be exposed externally
	 * @return this
	 */
	public InfinispanSitesLocalSpecBuilder expose(ExposeSpec expose) {
		this.expose = expose;
		return this;
	}

	public InfinispanSitesLocalSpec build() {
		InfinispanSitesLocalSpec infinispanSitesLocalSpec = new InfinispanSitesLocalSpec();
		infinispanSitesLocalSpec.setName(name);
		infinispanSitesLocalSpec.setExpose(expose);
		return infinispanSitesLocalSpec;
	}
}
