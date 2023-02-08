package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

public final class InfinispanServiceContainerSpecBuilder {
	private String storage;

	/**
	 * Specify the size of the persistent volume within the defined service type
	 *
	 * @param storage The size of the persistent volume within the defined service type
	 * @return this
	 */
	public InfinispanServiceContainerSpecBuilder storage(String storage) {
		this.storage = storage;
		return this;
	}

	public InfinispanServiceContainerSpec build() {
		InfinispanServiceContainerSpec infinispanServiceContainerSpec = new InfinispanServiceContainerSpec();
		infinispanServiceContainerSpec.setStorage(storage);
		return infinispanServiceContainerSpec;
	}
}
