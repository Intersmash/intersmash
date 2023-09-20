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

import org.infinispan.v1.infinispanspec.Container;

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

	public Container build() {
		Container infinispanContainerSpec = new Container();
		infinispanContainerSpec.setExtraJvmOpts(extraJvmOpts);
		infinispanContainerSpec.setMemory(memory);
		infinispanContainerSpec.setCpu(cpu);
		return infinispanContainerSpec;
	}
}
