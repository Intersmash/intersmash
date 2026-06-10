/*
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
package org.jboss.intersmash.k8s.client.binary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jboss.intersmash.k8s.OpenShiftConfig;
import org.jboss.intersmash.k8s.client.Kuberneteses;
import org.jboss.intersmash.tools.http.HttpsUtils;

import io.fabric8.kubernetes.client.VersionInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterVersionBasedOpenShiftClientBinaryPathResolver implements OpenShiftClientBinaryPathResolver {

	private static final String OCP4_CLIENTS_URL = "https://mirror.openshift.com/pub/openshift-v4";

	@Override
	public String resolve() {
		final String configuredVersion = OpenShiftConfig.version();
		String openshiftVersion;
		if (StringUtils.isNotEmpty(configuredVersion)) {
			openshiftVersion = configuredVersion;
		} else {
			try {
				VersionInfo versionInfo = Kuberneteses.admin().getKubernetesVersion();
				openshiftVersion = versionInfo.getGitVersion();
			} catch (Exception e) {
				log.warn("Could not determine cluster version for oc binary download: {}", e.getMessage());
				return null;
			}
		}

		final boolean cacheEnabled = OpenShiftConfig.isBinaryCacheEnabled();
		final String versionOrChannel = determineVersionOrChannel(openshiftVersion);
		final String clientUrl = getOcp4DownloadUrl(versionOrChannel);

		log.debug("Downloading oc binary from: {}", clientUrl);
		final Path archivePath = getCachedOrDownloadArchive(clientUrl, versionOrChannel, cacheEnabled);
		return unpackArchive(archivePath, !cacheEnabled);
	}

	private String determineVersionOrChannel(String openshiftVersion) {
		if (openshiftVersion.matches("\\d+\\.\\d+\\.\\d+.*")) {
			return openshiftVersion;
		}
		String channel = OpenShiftConfig.binaryUrlChannelPath();
		if (openshiftVersion.matches("\\d+\\.\\d+.*")) {
			return channel + "-"
					+ openshiftVersion.substring(0, openshiftVersion.indexOf('.', openshiftVersion.indexOf('.') + 1));
		}
		return channel;
	}

	private String getOcp4DownloadUrl(String versionOrChannel) {
		final String ocFileName = SystemUtils.IS_OS_MAC ? "openshift-client-mac.tar.gz" : "openshift-client-linux.tar.gz";
		final String arch = getSystemArch();
		return String.format("%s/%s/clients/ocp/%s/%s", OCP4_CLIENTS_URL, arch, versionOrChannel, ocFileName);
	}

	private static String getSystemArch() {
		String osArch = SystemUtils.OS_ARCH;
		if ("aarch64".equals(osArch)) {
			return "arm64";
		} else if ("s390x".equals(osArch)) {
			return "s390x";
		} else if ("ppc64le".equals(osArch)) {
			return "ppc64le";
		}
		return "amd64";
	}

	private Path getCachedOrDownloadArchive(final String url, final String version, final boolean cacheEnabled) {
		Objects.requireNonNull(url);

		log.debug("Trying to load oc client archive from cache (enabled: {}) or download from {}.", cacheEnabled, url);
		Path archivePath;
		if (cacheEnabled) {
			Path cachePath = Paths.get(OpenShiftConfig.binaryCachePath(), version, DigestUtils.md5Hex(url));
			archivePath = cachePath.resolve("oc.tar.gz");
			if (Files.exists(archivePath)) {
				log.debug("oc client archive is already in cache: {}.", archivePath.toAbsolutePath());
				return archivePath;
			}
			log.debug("oc client archive not found in cache, downloading it.");
		} else {
			archivePath = getProjectOcDir().resolve("oc.tar.gz");
			log.debug("Cache is disabled, downloading oc client archive to {}.", archivePath.toAbsolutePath());
		}

		try {
			Files.createDirectories(archivePath.getParent());
			HttpsUtils.copyHttpsURLToFile(url, archivePath.toFile(),
					ClusterVersionBasedKubernetesClientBinaryPathResolver.BINARY_DOWNLOAD_CONNECTION_TIMEOUT,
					ClusterVersionBasedKubernetesClientBinaryPathResolver.BINARY_DOWNLOAD_READ_TIMEOUT);
		} catch (IOException ioe) {
			throw new IllegalStateException("Failed to download oc binary from " + url, ioe);
		}
		return archivePath;
	}

	private String unpackArchive(final Path archivePath, final boolean deleteArchiveWhenDone) {
		Objects.requireNonNull(archivePath);

		try {
			List<String> args = Stream
					.of("tar", "-xf", archivePath.toAbsolutePath().toString(), "-C",
							getProjectOcDir().toAbsolutePath().toString(), "--no-same-owner")
					.collect(Collectors.toList());
			ProcessBuilder pb = new ProcessBuilder(args);
			pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			pb.redirectError(ProcessBuilder.Redirect.INHERIT);

			int result = pb.start().waitFor();
			if (result != 0) {
				throw new IOException("Failed to execute: " + args);
			}
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException("Failed to extract oc binary " + archivePath.toAbsolutePath(), e);
		}

		try {
			if (deleteArchiveWhenDone) {
				Files.delete(archivePath);
			}
		} catch (IOException ioe) {
			log.warn("Could not delete oc client archive {}", archivePath.toAbsolutePath(), ioe);
		}

		Path ocBinary = getProjectOcDir().resolve(BINARY_NAME);
		try {
			Files.setPosixFilePermissions(ocBinary, Set.of(
					PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE,
					PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE,
					PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE));
		} catch (IOException e) {
			log.warn("Could not set executable permissions on {}", ocBinary.toAbsolutePath(), e);
		}

		return ocBinary.toAbsolutePath().toString();
	}

	private Path getProjectOcDir() {
		Path dir = Paths.get(LOCAL_BINARY_CLIENT_TMP_DIR);
		try {
			Files.createDirectories(dir);
		} catch (IOException ioe) {
			throw new IllegalStateException("Failed to create directory " + dir.toAbsolutePath(), ioe);
		}
		return dir;
	}
}
