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
package org.jboss.intersmash.provision.openshift.operator.infinispan.infinispan.spec;

import org.infinispan.v1.infinispanspec.Expose;

public final class ExposeSpecBuilder {
	private Expose.Type type;
	private int nodePort;
	private String host;

	/**
	 * Type specifies different exposition methods for data grid
	 *
	 * @param type The type of different exposition methods for data grid
	 * @return this
	 */
	public ExposeSpecBuilder type(Expose.Type type) {
		this.type = type;
		return this;
	}

	/**
	 * Set the port where Data Grid is exposed
	 *
	 * @param nodePort The port where Data Grid is exposed
	 * @return this
	 */
	public ExposeSpecBuilder nodePort(int nodePort) {
		this.nodePort = nodePort;
		return this;
	}

	/**
	 * Set the host where Data Grid is exposed
	 *
	 * @param host The host where Data Grid is exposed
	 * @return this
	 */
	public ExposeSpecBuilder host(String host) {
		this.host = host;
		return this;
	}

	public Expose build() {
		Expose exposeSpec = new Expose();
		exposeSpec.setType(type);
		exposeSpec.setNodePort(nodePort);
		exposeSpec.setHost(host);
		return exposeSpec;
	}
}
