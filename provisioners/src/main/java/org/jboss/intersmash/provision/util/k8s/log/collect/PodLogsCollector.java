package org.jboss.intersmash.provision.util.k8s.log.collect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.dsl.PodResource;
import lombok.NonNull;

/**
 * A class that collect pods' logs. It must be initialized via its {@link PodLogsCollectorBuilder} to provide a
 * {@link NamespacedKubernetesClient} instance to connect to the cluster, and a collection of
 * {@link Pod} instances which logs must be collected.
 */
public class PodLogsCollector {

	private final NamespacedKubernetesClient client;
	private final List<Pod> pods;

	private final Predicate<Pod> podSelector;
	private final Predicate<String> lineSelector;

	/**
	 * Initialize a new {@link PodLogsCollector} instance
	 * @param client A {@link NamespacedKubernetesClient} instance, must be not null
	 * @param pods A collection of {@link Pod} instances
	 * @param podSelector A {@link Predicate<Pod>} to select a subset of pods to process
	 * @param lineSelector A {@link Predicate<String>} to select a subset of log lines
	 */
	PodLogsCollector(@NonNull final NamespacedKubernetesClient client,
			final List<Pod> pods, final Predicate<Pod> podSelector,
			final Predicate<String> lineSelector) {
		this.client = client;
		this.pods = pods;
		this.podSelector = podSelector;
		this.lineSelector = lineSelector;
	}

	NamespacedKubernetesClient getClient() {
		return client;
	}

	List<Pod> getPods() {
		return pods;
	}

	Predicate<String> getLineSelector() {
		return lineSelector;
	}

	Predicate<Pod> getPodSelector() {
		return podSelector;
	}

	/**
	 * Collect logs from each of the {@link Pod} instances which has been provided.
	 * If a {@link Predicate<Pod>} has been provided to filter the target pods based on some criteria, then the collection
	 * of target pods is matched against it.
	 * Similarly, each of the collected pod log lines are filtered against a {@link Predicate<String>}, when it is
	 * provided to the {@link PodLogsCollector} initialization.
	 *
	 * @return A collection of {@link PodLogsReport} instances, holding the log traces for the processed pods.
	 */
	public List<PodLogsReport> collect() {
		if (pods == null || pods.isEmpty())
			return List.of();
		final List<PodLogsReport> podLogsReports = new ArrayList<>();
		final List<Pod> selectedPods = podSelector == null ? pods
				: pods.stream()
						.filter(podSelector)
						.collect(Collectors.toList());
		selectedPods.stream().forEach(p -> {
			// fetch all lines
			final PodResource podResource = client.pods().withName(p.getMetadata().getName());
			if (Objects.isNull(podResource)) {
				throw new IllegalStateException("Target pod not found");
			}
			String podLog = podResource.getLog();
			if (lineSelector != null) {
				// funnel
				podLog = Arrays.stream(podLog.split(System.lineSeparator())).filter(lineSelector)
						.collect(Collectors.joining(System.lineSeparator()));
			}
			podLogsReports.add(new PodLogsReport(p, podLog));
		});
		return podLogsReports;
	}
}
