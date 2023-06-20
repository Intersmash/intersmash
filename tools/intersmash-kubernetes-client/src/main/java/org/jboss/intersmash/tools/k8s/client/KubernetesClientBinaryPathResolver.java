package org.jboss.intersmash.tools.k8s.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jboss.intersmash.tools.k8s.KubernetesConfig;

import cz.xtf.core.http.Https;
import lombok.extern.slf4j.Slf4j;

/**
 * Interface for resolving Kubernetes client binary path
 */
@Slf4j
public class KubernetesClientBinaryPathResolver {
	private static final String KUBERNETES_CLIENT_BINARY_DOWNLOAD_URL = "https://dl.k8s.io/release/";

	/**
	 * Resolves Kubernetes client binary path
	 *
	 * @return Kubernetes client binary path
	 */
	public String resolve() {
		final boolean cacheEnabled = KubernetesConfig.isBinaryCacheEnabled();
		final String kubernetesClientVersion = KubernetesConfig.clientVersion();
		final String clientUrl = getBinaryUrlBasedOnConfiguredKubernetesClientVersion(kubernetesClientVersion);
		return getCachedOrDownloadClientBinary(clientUrl, kubernetesClientVersion, cacheEnabled).toAbsolutePath().toString();
	}

	private Path getCachedOrDownloadClientBinary(final String url, final String version, final boolean cacheEnabled) {
		Objects.requireNonNull(url);

		log.debug("Trying to load Kubernetes client archive from cache (enabled: {}) or download it from {}.", cacheEnabled,
				url);
		Path archivePath;
		if (cacheEnabled) {
			Path cachePath = Paths.get(KubernetesConfig.binaryCachePath(), version, DigestUtils.md5Hex(url));
			archivePath = cachePath.resolve("kubectl");
			if (Files.exists(archivePath)) {
				log.debug("Kubernetes client archive is already in cache: {}.", archivePath.toAbsolutePath());
				return archivePath;
			}
			log.debug("Kubernetes client archive not found in cache, downloading it.");
		} else {
			archivePath = getProjectKubernetesDir().resolve("kubectl.tar.gz");
			log.debug("Cache is disabled, downloading Kubernetes client archive to {}.", archivePath.toAbsolutePath());
		}

		try {
			Https.copyHttpsURLToFile(url, archivePath.toFile(), 20_000, 300_000);
		} catch (IOException ioe) {
			throw new IllegalStateException("Failed to download the kubectl binary from " + url, ioe);
		}
		return archivePath;
	}

	private String getBinaryUrlBasedOnConfiguredKubernetesClientVersion(final String kubernetesClientVersion) {
		String systemType = "linux";
		if (SystemUtils.IS_OS_MAC) {
			systemType = "darwin";
		}
		return String.format("%s/%s/%s/kubectl", KUBERNETES_CLIENT_BINARY_DOWNLOAD_URL, kubernetesClientVersion,
				systemType);
	}

	private Path getProjectKubernetesDir() {
		Path dir = Paths.get("tmp/kubectl/");

		try {
			Files.createDirectories(dir);
		} catch (IOException ioe) {
			throw new IllegalStateException("Failed to create directory " + dir.toAbsolutePath(), ioe);
		}

		return dir;
	}
}
