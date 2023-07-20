package org.jboss.intersmash.tools.k8s.client.binary;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
public class KubernetesClientBinaryManagerFactoryTest {
	@SystemStub
	private SystemProperties systemProperties;

	@Test
	public void managerBinaryPathDefaultsToCachedBinaryPathTest() {
		KubernetesClientBinaryManager binaryManager = KubernetesClientBinaryManagerFactory.INSTANCE
				.getKubernetesClientBinaryManager();
		Assertions.assertNotNull(binaryManager);
		Assertions.assertTrue(binaryManager.getBinaryPath().endsWith("tmp/kubectl/kubectl"));
	}
}
