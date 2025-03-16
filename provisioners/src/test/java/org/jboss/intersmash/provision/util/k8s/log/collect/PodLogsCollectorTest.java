/**
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;

/**
 * An integration test to validate the {@link PodLogsCollector} behavior.
 *
 * A cluster is mocked to let the collector work without require a proper e2e test, requiring an actual cluster.
 */
class PodLogsCollectorTest {

	private static final String LOG_WITH_ERRORS = "\n" +
			"12:00:01 INFO This is a line without any errors\n" +
			"12:00:02 WARN We've got a warning here...\n" +
			"12:00:03 ERROR And finally an error :(\n" +
			"12:00:04 INFO Another clean line, though\n";

	private static final String LOG_WITH_NO_ERRORS = "\n" +
			"12:00:01 INFO All clean here...\n" +
			"12:00:02 INFO Really!\n";

	private static NamespacedKubernetesClient client = mock(NamespacedKubernetesClient.class);
	private final static List<Pod> pods = new ArrayList<>();

	/**
	 * Mock a cluster that will be used to test {@link PodLogsCollector} behavior.
	 * The cluster is made of two pods, one which has logs with ERRORS, and one which has clean log traces, just info
	 * messages.
	 */
	@BeforeAll
	static void setupCluster() {
		final PodResource aPodResource = mock(PodResource.class);
		final String aPodName = "pod-0";
		final Pod aPod = mock(Pod.class);
		final ObjectMeta aPodMetadata = new ObjectMetaBuilder()
				.withName(aPodName)
				.build();
		final PodResource anotherPodResource = mock(PodResource.class);
		final String anotherPodName = "pod-1";
		final Pod anotherPod = mock(Pod.class);
		final ObjectMeta anotherPodMetadata = new ObjectMetaBuilder()
				.withName(anotherPodName)
				.build();
		final MixedOperation<Pod, PodList, PodResource> podsMixedOperation = mock(MixedOperation.class);

		// identify target pods
		pods.addAll(Arrays.asList(aPod, anotherPod));
		when(client.pods()).thenReturn(podsMixedOperation);
		when(client.pods().withName(aPodName)).thenReturn(aPodResource);
		when(client.pods().withName(anotherPodName)).thenReturn(anotherPodResource);
		when(aPod.getMetadata()).thenReturn(aPodMetadata);
		when(aPodResource.getLog()).thenReturn(LOG_WITH_ERRORS);
		when(anotherPod.getMetadata()).thenReturn(anotherPodMetadata);
		when(anotherPodResource.getLog()).thenReturn(LOG_WITH_NO_ERRORS);
	}

	/**
	 * Validate the collection of a failing pod logs.
	 * Given the two pods in the cluster, a pod selector is provided for the collector to target only the first pod,
	 * i.e. the one with error log traces.
	 * A pod lines selector is provided as well, to only report the lines containing the "ERROR" marker.
	 */
	@Test
	void test_selectPodByName_withLogErrors() {
		// Arrange
		// build a pod log collector
		final PodLogsCollector podLogsCollector = new PodLogsCollectorBuilder()
				.withClient(client)
				.withPods(pods)
				// filter: only pod with name == "pod-0"
				.withPodSelector(p -> "pod-0".equals(p.getMetadata().getName()))
				// filter server ERROR log lines
				.withLineSelector((line) -> line.contains(" ERROR "))
				.build();
		// Act
		final List<PodLogsReport> reports = podLogsCollector.collect();
		final String report = PodLogsReport.generate(reports);
		// Assert
		Assertions.assertEquals(1, reports.size(), "Unexpected number of collected pod logs");
		final PodLogsReport actualReport = reports.get(0);
		Assertions.assertNotNull(actualReport.getPod(), "Unexpected null pod");
		Assertions.assertNotNull(actualReport.getPodLog(), "Unexpected null pod log");
		Assertions.assertTrue(report.contains("12:00:03 ERROR And finally an error :("));
	}

	/**
	 * Validate the collection of both pods' logs.
	 * No pod selector is provided, nor a lines selector is provided as well, so both the pods' logs should be reported
	 * unfiltered.
	 */
	@Test
	void test_noPodSelector_noLineSelector() {
		// Arrange
		// build a pod log collector
		final PodLogsCollector podLogsCollector = new PodLogsCollectorBuilder()
				.withClient(client)
				.withPods(pods)
				.build();
		// Act
		final List<PodLogsReport> reports = podLogsCollector.collect();
		final String report = PodLogsReport.generate(reports);
		// Assert
		Assertions.assertEquals(2, reports.size(), "Unexpected number of collected pod logs");
		final PodLogsReport aPodReport = reports.stream()
				.filter(r -> "pod-0".equals(r.getPod().getMetadata().getName()))
				.findFirst()
				.orElse(null);
		Assertions.assertNotNull(aPodReport, "Unexpected null pod report");
		Assertions.assertNotNull(aPodReport.getPod(), "Unexpected null pod");
		Assertions.assertNotNull(aPodReport.getPodLog(), "Unexpected null pod log");
		Assertions.assertTrue(report.contains(LOG_WITH_ERRORS));
		final PodLogsReport anotherPodReport = reports.stream()
				.filter(r -> "pod-1".equals(r.getPod().getMetadata().getName()))
				.findFirst()
				.orElse(null);
		Assertions.assertNotNull(anotherPodReport, "Unexpected null pod report");
		Assertions.assertNotNull(anotherPodReport.getPod(), "Unexpected null pod");
		Assertions.assertNotNull(anotherPodReport.getPodLog(), "Unexpected null pod log");
		Assertions.assertTrue(report.contains(LOG_WITH_NO_ERRORS));
	}
}
