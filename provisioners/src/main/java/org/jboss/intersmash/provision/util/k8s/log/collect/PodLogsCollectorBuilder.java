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
