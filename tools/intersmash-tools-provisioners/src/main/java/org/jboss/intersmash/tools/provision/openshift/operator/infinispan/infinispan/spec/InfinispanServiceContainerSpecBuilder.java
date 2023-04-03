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
