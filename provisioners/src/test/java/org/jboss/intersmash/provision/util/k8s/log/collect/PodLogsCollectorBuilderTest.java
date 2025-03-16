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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;

/**
 * Unit test that validates that {@link PodLogsCollectorBuilder} creates {@link PodLogsCollector} instances
 * accordingly to a given configuration.
 */
class PodLogsCollectorBuilderTest {

	/**
	 * Validates a {@link PodLogsCollector} instance that is built out of a full configuration
	 * @throws MalformedURLException The test uses a {@link URL} instance that represents the cluster master URL,
	 * and this exception would be thrown if such url is not valid.
	 */
	@Test
	void test_allPropertiesAreSet() throws MalformedURLException {
		// Arrange
		final String clusterUrl = "https://my-cluster:8443";
		final NamespacedKubernetesClient client = Mockito.mock(NamespacedKubernetesClient.class);
		Mockito.when(client.getMasterUrl()).thenReturn(new URL(clusterUrl));
		final Pod aPod = new PodBuilder()
				.withNewMetadata()
				.withName("pod-0")
				.endMetadata()
				.build();
		final Pod anotherPod = new PodBuilder()
				.withNewMetadata()
				.withName("pod-1")
				.endMetadata()
				.build();
		final Predicate<Pod> podSelector = (pod) -> "pod-0".equals(pod.getMetadata().getName());
		final Predicate<String> lineSelector = (line) -> line.contains(" FOO ");
		// Act
		PodLogsCollector podLogsCollector = new PodLogsCollectorBuilder()
				.withClient(client)
				.withPods(Arrays.asList(aPod, anotherPod))
				.withPodSelector(podSelector)
				.withLineSelector(lineSelector)
				.build();
		// Assert
		final URL actualClientMasterUrl = podLogsCollector.getClient().getMasterUrl();
		Assertions.assertNotNull(actualClientMasterUrl, "Null client Master URL value");
		Assertions.assertEquals(new URL(clusterUrl), actualClientMasterUrl, "Unexpected client Master URL value");
		final Predicate<Pod> actualPodSelector = podLogsCollector.getPodSelector();
		Assertions.assertNotNull(actualPodSelector, "Null pod selector selector");
		Assertions.assertEquals(podSelector.toString(), actualPodSelector.toString(), "Unexpected container status selector");
		final List<Pod> actualPods = podLogsCollector.getPods();
		Assertions.assertFalse(actualPods.isEmpty(), "Empty collection of collected pod logs");
		Assertions.assertEquals(2, actualPods.size(), "Unexpected number of collected pod logs");
		Assertions.assertTrue(actualPods.stream()
				.anyMatch(p -> aPod.getMetadata().getName().equals(p.getMetadata().getName())),
				"Cannot find the first pod in the collected pod logs");
		Assertions.assertTrue(actualPods.stream()
				.anyMatch(p -> anotherPod.getMetadata().getName().equals(p.getMetadata().getName())),
				"Cannot find the second pod in the collected pod logs");
		final Predicate<String> actualLineSelector = podLogsCollector.getLineSelector();
		Assertions.assertNotNull(actualLineSelector, "Null line selector");
		Assertions.assertEquals(lineSelector.toString(), actualLineSelector.toString(), "Unexpected line selector");
	}
}
