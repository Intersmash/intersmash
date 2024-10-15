package org.jboss.intersmash.k8s.client.binary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jboss.intersmash.k8s.KubernetesConfig;
import org.jboss.intersmash.k8s.client.Kuberneteses;

import com.google.common.base.Strings;

import cz.xtf.core.http.Https;
import io.fabric8.kubernetes.client.VersionInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for resolving Kubernetes client binary path based on the Kubernetes cluster version, either the
 * one referenced by the configuration properties or the actual cluster one.
 *
 * Based on the configuration properties, users can configure the resolver to cache the binary client.
 */
@Slf4j
public class ClusterVersionBasedKubernetesClientBinaryPathResolver implements KubernetesClientBinaryPathResolver {
	private static final String KUBERNETES_CLIENT_BINARY_DOWNLOAD_BASE_URL = "https://dl.k8s.io/release/";
	public static final int BINARY_DOWNLOAD_CONNECTION_TIMEOUT = 20_000;
	public static final int BINARY_DOWNLOAD_READ_TIMEOUT = 300_000;

	@Override
	public String resolve() {
		// gets the cluster version from configuration
		final String configuredClusterVersion = KubernetesConfig.version();
		// priority is given to the configuration, then fallback on k8s APIs for determining the actual cluster version
		final VersionInfo clusterVersionInfo = !Strings.isNullOrEmpty(configuredClusterVersion)
				? new VersionInfo.Builder().withGitVersion(configuredClusterVersion).build()
				: Kuberneteses.admin().getKubernetesVersion();
		// is a local cache configured to be used?
		final boolean cacheEnabled = KubernetesConfig.isBinaryCacheEnabled();
		// let's compute the Kubernetes client binary path
		Path binaryPath;
		if (cacheEnabled) {
			log.debug("Trying to load Kubernetes client binary from cache");
			Path cachePath = getCachePath(clusterVersionInfo);
			binaryPath = cachePath.resolve(BINARY_NAME);
			if (Files.exists(binaryPath)) {
				// the required binary is there already, let's skip the download
				log.debug("Kubernetes client binary is already in cache: {}.", binaryPath.toAbsolutePath());
			} else {
				log.debug("Kubernetes client binary not found in cache, downloading it.");
				downloadKubernetesClient(clusterVersionInfo, binaryPath);
			}
			binaryPath = copyKubernetesClientBinaryToTemporaryRuntimeLocation(binaryPath);
		} else {
			binaryPath = ClusterVersionBasedKubernetesClientBinaryPathResolver.getRuntimeKubectl();
			log.debug("Cache is disabled, downloading Kubernetes client binary to {}.", binaryPath.toAbsolutePath());
			downloadKubernetesClient(clusterVersionInfo, binaryPath);
		}
		return binaryPath.toAbsolutePath().toString();
	}

	/**
	 * Get the proper URL for a Kubernetes client, based on required version and taking the OS into account as well.
	 *
	 * @param clusterVersion {@link VersionInfo} instance holding the required Kubernetes version.
	 * @return A string representing the required Kubernetes client URL
	 */
	private String getBinaryUrlBasedOnKubernetesVersion(final VersionInfo clusterVersion) {
		Objects.requireNonNull(clusterVersion);

		String systemType = "linux";
		String arch = "amd64";
		if (SystemUtils.IS_OS_MAC) {
			systemType = "darwin";
		}
		return String.format("%s/%s/bin/%s/%s/%s", KUBERNETES_CLIENT_BINARY_DOWNLOAD_BASE_URL, clusterVersion.getGitVersion(),
				systemType, arch, BINARY_NAME);
	}

	private void downloadKubernetesClient(final VersionInfo clusterVersionInfo, final Path binaryPath) {
		final String url = getBinaryUrlBasedOnKubernetesVersion(clusterVersionInfo);
		try {
			Https.copyHttpsURLToFile(url, binaryPath.toFile(), BINARY_DOWNLOAD_CONNECTION_TIMEOUT,
					BINARY_DOWNLOAD_READ_TIMEOUT);
		} catch (IOException ioe) {
			throw new IllegalStateException("Failed to download the kubectl binary from " + url, ioe);
		}
	}

	private Path copyKubernetesClientBinaryToTemporaryRuntimeLocation(final Path binaryPath) {
		Objects.requireNonNull(binaryPath);

		final Path runtimeKubectl = ClusterVersionBasedKubernetesClientBinaryPathResolver.getRuntimeKubectl();
		final Set<PosixFilePermission> permissions = Set.of(
				PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE,
				PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE, PosixFilePermission.GROUP_EXECUTE,
				PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE);
		try {
			Files.copy(binaryPath, runtimeKubectl, StandardCopyOption.REPLACE_EXISTING);
			Files.setPosixFilePermissions(runtimeKubectl, permissions);
		} catch (IOException e) {
			throw new IllegalStateException("Error when copying the Kubernetes client binary", e);
		}
		return runtimeKubectl;
	}

	static Path getCachePath(VersionInfo clusterVersionInfo) {
		return Paths.get(KubernetesConfig.binaryCachePath(), clusterVersionInfo.getGitVersion(),
				DigestUtils.md5Hex(clusterVersionInfo.getGitVersion()));
	}

	static Path getRuntimeKubectl() {
		return getProjectKubernetesDir().resolve(BINARY_NAME);
	}

	static Path getProjectKubernetesDir() {
		Path dir = Paths.get(LOCAL_BINARY_CLIENT_TMP_DIR);
		try {
			Files.createDirectories(dir);
		} catch (IOException ioe) {
			throw new IllegalStateException("Failed to create directory " + dir.toAbsolutePath(), ioe);
		}
		return dir;
	}
}
