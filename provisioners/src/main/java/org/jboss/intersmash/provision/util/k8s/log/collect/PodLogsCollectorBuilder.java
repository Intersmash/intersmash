/*
 * Copyright (C) 2025 Red Hat, Inc.
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
package org.jboss.intersmash.provision.util.k8s.log.collect;

import java.util.List;
import java.util.function.Predicate;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;

/**
 * Class to build unique instances of {@link PodLogsCollector}
 */
public class PodLogsCollectorBuilder {
	private NamespacedKubernetesClient client;
	private List<Pod> pods;
	private Predicate<Pod> podSelector;
	private Predicate<String> lineSelector;

	public PodLogsCollectorBuilder withClient(NamespacedKubernetesClient client) {
		this.client = client;
		return this;
	}

	public PodLogsCollectorBuilder withPods(List<Pod> pods) {
		this.pods = pods;
		return this;
	}

	public PodLogsCollectorBuilder withPodSelector(Predicate<Pod> podSelector) {
		this.podSelector = podSelector;
		return this;
	}

	public PodLogsCollectorBuilder withLineSelector(Predicate<String> lineSelector) {
		this.lineSelector = lineSelector;
		return this;
	}

	public PodLogsCollector build() {
		return new PodLogsCollector(client, pods, podSelector, lineSelector);
	}
}
