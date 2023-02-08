package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

public final class InfinispanContainerSpecBuilder {
	private String extraJvmOpts;
	private String memory;
	private String cpu;

	/**
	 * Specifies JVM options
	 *
	 * @param extraJvmOpts Options to be passed to the JVM.
	 * @return this
	 */
	public InfinispanContainerSpecBuilder extraJvmOpts(String extraJvmOpts) {
		this.extraJvmOpts = extraJvmOpts;
		return this;
	}

	/**
	 * Specify host memory resources to nodes, measured in bytes
	 *
	 * @param memory Host memory resources to node, measured in CPU units.
	 * @return this
	 */
	public InfinispanContainerSpecBuilder memory(String memory) {
		this.memory = memory;
		return this;
	}

	/**
	 * Specify host CPU resources to node, measured in CPU units
	 *
	 * @param cpu Host CPU resources to node, measured in CPU units.
	 * @return this
	 */
	public InfinispanContainerSpecBuilder cpu(String cpu) {
		this.cpu = cpu;
		return this;
	}

	public InfinispanContainerSpec build() {
		InfinispanContainerSpec infinispanContainerSpec = new InfinispanContainerSpec();
		infinispanContainerSpec.setExtraJvmOpts(extraJvmOpts);
		infinispanContainerSpec.setMemory(memory);
		infinispanContainerSpec.setCpu(cpu);
		return infinispanContainerSpec;
	}
}
