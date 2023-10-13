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

import org.infinispan.v1.infinispanspec.Autoscale;

public final class AutoscaleBuilder {
	private int maxReplicas;
	private int minReplicas;
	private Long maxMemUsagePercent;
	private Long minMemUsagePercent;

	/**
	 * Defines the maximum number of nodes for the cluster.
	 *
	 * @param maxReplicas Maximum number of nodes for the cluster.
	 * @return this
	 */
	public AutoscaleBuilder maxReplicas(int maxReplicas) {
		this.maxReplicas = maxReplicas;
		return this;
	}

	/**
	 * Defines the minimum number of nodes for the cluster
	 *
	 * @param minReplicas Minimum number of nodes for the cluster.
	 * @return this
	 */
	public AutoscaleBuilder minReplicas(int minReplicas) {
		this.minReplicas = minReplicas;
		return this;
	}

	/**
	 * Configures the maximum threshold, as a percentage, for memory usage on each node
	 *
	 * @param maxMemUsagePercent Maximum threshold, as a percentage, for memory usage on each node
	 * @return this
	 */
	public AutoscaleBuilder maxMemUsagePercent(Long maxMemUsagePercent) {
		this.maxMemUsagePercent = maxMemUsagePercent;
		return this;
	}

	/**
	 * Configures the minimum threshold, as a percentage, for memory usage across the cluster
	 *
	 * @param minMemUsagePercent Minimum threshold, as a percentage, for memory usage on each node
	 * @return this
	 */
	public AutoscaleBuilder minMemUsagePercent(Long minMemUsagePercent) {
		this.minMemUsagePercent = minMemUsagePercent;
		return this;
	}

	public Autoscale build() {
		Autoscale autoscale = new Autoscale();
		autoscale.setMaxReplicas(maxReplicas);
		autoscale.setMinReplicas(minReplicas);
		autoscale.setMaxMemUsagePercent(maxMemUsagePercent);
		autoscale.setMinMemUsagePercent(minMemUsagePercent);
		return autoscale;
	}
}
