package org.jboss.intersmash.k8s.client.binary;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cz.xtf.core.config.XTFConfig;
import io.fabric8.kubernetes.client.VersionInfo;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
public class KubernetesClientBinaryManagerFactoryTest {
	@SystemStub
	private SystemProperties systemProperties;

	@Test
	public void managerBinaryPathDefaultsToCachedBinaryPathTest() {
		//		final String testedBinaryPath = "/tmp";
		//		systemProperties.set("intersmash.kubernetes.binary.path", testedBinaryPath);
		final VersionInfo clusterVersion = new VersionInfo.Builder().withGitVersion("v1.27.3").build();
		systemProperties.set("intersmash.kubernetes.version", clusterVersion.getGitVersion());
		XTFConfig.loadConfig();
		KubernetesClientBinaryManager binaryManager = KubernetesClientBinaryManagerFactory.INSTANCE
				.getKubernetesClientBinaryManager();
		Assertions.assertNotNull(binaryManager);
		Assertions.assertTrue(binaryManager.getBinaryPath().endsWith("tmp/kubectl/kubectl"));
	}
}
