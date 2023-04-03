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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Autoscale describe autoscaling configuration for the cluster
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Autoscale {

	/**
	 * Defines the maximum number of nodes for the cluster.
	 */
	private int maxReplicas;

	/**
	 * Defines the minimum number of nodes for the cluster
	 */
	private int minReplicas;

	/**
	 * Configures the maximum threshold, as a percentage, for memory usage on each node
	 */
	private int maxMemUsagePercent;

	/**
	 * Configures the minimum threshold, as a percentage, for memory usage across the cluster
	 */
	private int minMemUsagePercent;
}
