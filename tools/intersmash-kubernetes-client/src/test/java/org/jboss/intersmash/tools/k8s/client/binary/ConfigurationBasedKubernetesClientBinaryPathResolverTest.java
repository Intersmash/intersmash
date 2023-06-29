package org.jboss.intersmash.tools.k8s.client.binary;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.base.Strings;

import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
public class ConfigurationBasedKubernetesClientBinaryPathResolverTest {
	@SystemStub
	private SystemProperties systemProperties;

	private KubernetesClientBinaryPathResolver resolver = new ConfigurationBasedKubernetesClientBinaryPathResolver();

	@Test
	public void resolveTest() {
		final String currentBinaryPath = System.getProperty("intersmash.kubernetes.binary.path");
		final String testedBinaryPath = "/tmp";
		systemProperties.set("intersmash.kubernetes.binary.path", testedBinaryPath);
		try {
			Assertions.assertEquals(testedBinaryPath, resolver.resolve());
		} finally {
			if (!Strings.isNullOrEmpty(currentBinaryPath)) {
				systemProperties.set("intersmash.kubernetes.binary.path", currentBinaryPath);
			}
		}
	}
}
