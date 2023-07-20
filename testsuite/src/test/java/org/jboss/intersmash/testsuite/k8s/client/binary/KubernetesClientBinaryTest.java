package org.jboss.intersmash.testsuite.k8s.client.binary;

import org.jboss.intersmash.tools.k8s.client.Kuberneteses;
import org.jboss.intersmash.tools.k8s.client.binary.KubernetesClientBinary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
