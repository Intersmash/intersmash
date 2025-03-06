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
package org.jboss.intersmash.testsuite.k8s.client.binary;

import org.jboss.intersmash.k8s.client.Kuberneteses;
import org.jboss.intersmash.k8s.client.binary.KubernetesClientBinary;
import org.jboss.intersmash.testsuite.junit5.categories.KubernetesTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@KubernetesTest
public class KubernetesClientBinaryTest {

	private static final KubernetesClientBinary ADMIN_BINARY = Kuberneteses.adminBinary();

	private static final KubernetesClientBinary MASTER_BINARY = Kuberneteses.masterBinary();

	@Test
	void getClusterInfoTest() {
		String actual = ADMIN_BINARY.execute("cluster-info");
		Assertions.assertTrue(actual.contains("Kubernetes control plane"));
		Assertions.assertTrue(actual.contains("is running at"));
		actual = MASTER_BINARY.execute("cluster-info");
		Assertions.assertTrue(actual.contains("Kubernetes control plane"));
		Assertions.assertTrue(actual.contains("is running at"));
	}
}
