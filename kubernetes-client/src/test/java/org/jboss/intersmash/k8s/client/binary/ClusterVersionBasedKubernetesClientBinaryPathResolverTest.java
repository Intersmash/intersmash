package org.jboss.intersmash.k8s.client.binary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cz.xtf.core.config.XTFConfig;
import io.fabric8.kubernetes.client.VersionInfo;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
public class ClusterVersionBasedKubernetesClientBinaryPathResolverTest {

	@SystemStub
	private SystemProperties systemProperties;

	private ClusterVersionBasedKubernetesClientBinaryPathResolver resolver = new ClusterVersionBasedKubernetesClientBinaryPathResolver();

	@Test
	public void existingKubernetesVersionPathIsResolvedWhenCacheEnabledTest() throws IOException {
		final VersionInfo clusterVersion = new VersionInfo.Builder().withGitVersion("v1.27.3").build();
		systemProperties.set("intersmash.kubernetes.version", clusterVersion.getGitVersion());
		XTFConfig.loadConfig();
		// resolve (which includes the binary client download if it is not cached already) should pass here
		final String resolvedPath = resolver.resolve();
		try {
			// make assertions now
			assertBinaryPathIsProperlyResolved(clusterVersion, resolvedPath);
		} finally {
			Files.deleteIfExists(ClusterVersionBasedKubernetesClientBinaryPathResolver.getRuntimeKubectl());
			Files.deleteIfExists(ClusterVersionBasedKubernetesClientBinaryPathResolver.getProjectKubernetesDir());
		}
	}

	@Test
	public void existingKubernetesVersionPathIsResolvedWhenCacheDisabledTest() throws IOException {
		final VersionInfo clusterVersion = new VersionInfo.Builder().withGitVersion("v1.27.3").build();
		systemProperties.set("intersmash.kubernetes.version", clusterVersion.getGitVersion());
		systemProperties.set("intersmash.kubernetes.binary.cache.enabled", "false");
		try {
			XTFConfig.loadConfig();
			// resolve (which includes the binary client download in this very case) should pass here
			final String resolvedPath = resolver.resolve();
			try {
				assertBinaryPathIsProperlyResolved(clusterVersion, resolvedPath);
			} finally {
				Files.deleteIfExists(ClusterVersionBasedKubernetesClientBinaryPathResolver.getRuntimeKubectl());
				Files.deleteIfExists(ClusterVersionBasedKubernetesClientBinaryPathResolver.getProjectKubernetesDir());
			}
		} finally {
			systemProperties.set("intersmash.kubernetes.binary.cache.enabled", "true");
		}
	}

	@Test
	public void fakeKubernetesVersionPathIsNotResolvedTest() {
		final VersionInfo clusterVersion = new VersionInfo.Builder().withGitVersion("vX.Y.Z").build();
		systemProperties.set("intersmash.kubernetes.version", clusterVersion.getGitVersion());
		XTFConfig.loadConfig();
		// resolve (which includes the binary client download) should fail here
		Assertions.assertThrows(IllegalStateException.class, () -> resolver.resolve());
	}

	private static void assertBinaryPathIsProperlyResolved(VersionInfo clusterVersion, String resolvedPath) {
		// make assertions now
		SoftAssertions softAssertions = new SoftAssertions();
		// path is not null
		softAssertions.assertThat(resolvedPath).isNotNull();
		// path is correct and binary file exists
		Path tmpPath = ClusterVersionBasedKubernetesClientBinaryPathResolver.getRuntimeKubectl();
		softAssertions.assertThat(resolvedPath).isEqualTo(tmpPath.toAbsolutePath().toString());
		softAssertions.assertThat(Files.exists(tmpPath)).isTrue();
		// archive is in cache
		Path cachedPath = ClusterVersionBasedKubernetesClientBinaryPathResolver.getCachePath(clusterVersion);
		softAssertions.assertThat(Files.exists(cachedPath)).isTrue();
		softAssertions.assertAll();
	}
}
