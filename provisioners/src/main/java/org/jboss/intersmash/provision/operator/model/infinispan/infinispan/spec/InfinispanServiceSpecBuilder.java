/*
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

import org.infinispan.v1.infinispanspec.Service;
import org.infinispan.v1.infinispanspec.service.Container;
import org.infinispan.v1.infinispanspec.service.Sites;

public final class InfinispanServiceSpecBuilder {
	private Service.Type type;
	private Container container;
	private Sites sites;
	private int replicationFactor;

	/**
	 * Specify Infinispan resource service type
	 *
	 * @param type Infinispan resource service type
	 * @return this
	 */
	public InfinispanServiceSpecBuilder type(Service.Type type) {
		this.type = type;
		return this;
	}

	/**
	 * Specify resource requirements specific for service
	 *
	 * @param container Resource requirements specific for service
	 * @return this
	 */
	public InfinispanServiceSpecBuilder container(Container container) {
		this.container = container;
		return this;
	}

	/**
	 * Specify connection information for backup locations
	 *
	 * @param sites Connection information for backup locations
	 * @return this
	 */
	public InfinispanServiceSpecBuilder sites(Sites sites) {
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

	public Service build() {
		Service infinispanServiceSpec = new Service();
		infinispanServiceSpec.setType(type);
		infinispanServiceSpec.setContainer(container);
		infinispanServiceSpec.setSites(sites);
		infinispanServiceSpec.setReplicationFactor(replicationFactor);
		return infinispanServiceSpec;
	}
}
