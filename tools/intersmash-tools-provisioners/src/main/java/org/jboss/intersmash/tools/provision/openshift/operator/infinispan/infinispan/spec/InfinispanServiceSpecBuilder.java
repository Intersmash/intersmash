package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

public final class InfinispanServiceSpecBuilder {
	private String type;
	private InfinispanServiceContainerSpec container;
	private InfinispanSitesSpec sites;
	private int replicationFactor;

	/**
	 * Specify Infinispan resource service type
	 *
	 * @param type Infinispan resource service type
	 * @return this
	 */
	public InfinispanServiceSpecBuilder type(ServiceType type) {
		if (type != ServiceType.None) {
			this.type = type.getValue();
		}
		return this;
	}

	/**
	 * Specify resource requirements specific for service
	 *
	 * @param container Resource requirements specific for service
	 * @return this
	 */
	public InfinispanServiceSpecBuilder container(InfinispanServiceContainerSpec container) {
		this.container = container;
		return this;
	}

	/**
	 * Specify connection information for backup locations
	 *
	 * @param sites Connection information for backup locations
	 * @return this
	 */
	public InfinispanServiceSpecBuilder sites(InfinispanSitesSpec sites) {
		this.sites = sites;
		return this;
	}

	/**
	 * Specify the number of owners
	 *
	 * @param replicationFactor Number of owners
	 * @return this
	 */
	public InfinispanServiceSpecBuilder replicationFactor(int replicationFactor) {
		this.replicationFactor = replicationFactor;
		return this;
	}

	public InfinispanServiceSpec build() {
		InfinispanServiceSpec infinispanServiceSpec = new InfinispanServiceSpec();
		infinispanServiceSpec.setType(type);
		infinispanServiceSpec.setContainer(container);
		infinispanServiceSpec.setSites(sites);
		infinispanServiceSpec.setReplicationFactor(replicationFactor);
		return infinispanServiceSpec;
	}

	public enum ServiceType {
		None(""),
		/**
		 * Deploys Infinispan to act like a cache. This means:
		 * Caches are only used for volatile data.
		 * No support for data persistence.
		 * Cache definitions can still be permanent, but PV size is not configurable.
		 * A default cache is created by default,
		 * Additional caches can be created, but only as copies of default cache.
		 */
		Cache("Cache"),
		/**
		 * Deploys Infinispan to act like a data grid.
		 * More flexibility and more configuration options are available:
		 * Cross-site replication, store cached data in persistence store...etc.
		 */
		DataGrid("DataGrid");

		private String value;

		ServiceType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
