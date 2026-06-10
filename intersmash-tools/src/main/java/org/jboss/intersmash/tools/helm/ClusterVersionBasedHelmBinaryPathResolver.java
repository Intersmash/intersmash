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
package org.jboss.intersmash.tools.helm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jboss.intersmash.tools.config.IntersmashProperties;
import org.jboss.intersmash.tools.http.HttpsUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ClusterVersionBasedHelmBinaryPathResolver implements HelmBinaryPathResolver {

	private static final String HELM_DOWNLOAD_BASE_URL = "https://mirror.openshift.com/pub/openshift-v4/clients/helm";
	private static final int DOWNLOAD_CONNECTION_TIMEOUT = 20_000;
	private static final int DOWNLOAD_READ_TIMEOUT = 300_000;

	private static final String HELM_CLIENT_VERSION = "intersmash.helm.client.version";
	private static final String XTF_HELM_CLIENT_VERSION = "xtf.helm.client.version";
	private static final String HELM_BINARY_CACHE_ENABLED = "intersmash.helm.binary.cache.enabled";
	private static final String XTF_HELM_BINARY_CACHE_ENABLED = "xtf.helm.binary.cache.enabled";
	private static final String HELM_BINARY_CACHE_PATH = "intersmash.helm.binary.cache.path";
	private static final String XTF_HELM_BINARY_CACHE_PATH = "xtf.helm.binary.cache.path";
	private static final String HELM_BINARY_CACHE_DEFAULT_FOLDER = "helm-cache";

	@Override
	public String resolve() {
		final boolean cacheEnabled = isCacheEnabled();
		final String helmClientVersion = getHelmClientVersion();
		final String clientUrl = getHelmClientUrl(helmClientVersion);
		final Path archivePath = getCachedOrDownloadArchive(clientUrl, helmClientVersion, cacheEnabled);
		return unpackArchive(archivePath, !cacheEnabled);
	}

	private String unpackArchive(final Path archivePath, final boolean deleteArchiveWhenDone) {
		Objects.requireNonNull(archivePath);

		try {
			List<String> args = Stream
					.of("tar", "-xf", archivePath.toAbsolutePath().toString(), "-C",
							getProjectHelmDir().toAbsolutePath().toString(), "--no-same-owner")
					.collect(Collectors.toList());
			ProcessBuilder pb = new ProcessBuilder(args);
			pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			pb.redirectError(ProcessBuilder.Redirect.INHERIT);

			int result = pb.start().waitFor();
			if (result != 0) {
				throw new IOException("Failed to execute: " + args);
			}
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException("Failed to extract helm binary " + archivePath.toAbsolutePath(), e);
		}

		try {
			if (deleteArchiveWhenDone) {
				Files.delete(archivePath);
			}
		} catch (IOException ioe) {
			log.warn("Could not delete Helm client archive {}", archivePath.toAbsolutePath(), ioe);
		}

		try (Stream<Path> helmDirContents = Files.list(getProjectHelmDir())) {
			Path helmBinaryFile = helmDirContents.collect(Collectors.toList()).get(0);
			if (!helmBinaryFile.getFileName().toString().equals(BINARY_NAME)) {
				Files.move(helmBinaryFile, helmBinaryFile.resolveSibling(BINARY_NAME), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new IllegalStateException("Error when extracting Helm client binary", e);
		}
		return getProjectHelmDir().resolve(BINARY_NAME).toAbsolutePath().toString();
	}

	private Path getCachedOrDownloadArchive(final String url, final String version, final boolean cacheEnabled) {
		Objects.requireNonNull(url);

		log.debug("Trying to load Helm client archive from cache (enabled: {}) or download from {}.", cacheEnabled, url);
		Path archivePath;
		if (cacheEnabled) {
			Path cachePath = Paths.get(getCachePath(), version, DigestUtils.md5Hex(url));
			archivePath = cachePath.resolve("helm.tar.gz");
			if (Files.exists(archivePath)) {
				log.debug("Helm client archive is already in cache: {}.", archivePath.toAbsolutePath());
				return archivePath;
			}
			log.debug("Helm client archive not found in cache, downloading it.");
		} else {
			archivePath = getProjectHelmDir().resolve("helm.tar.gz");
			log.debug("Cache is disabled, downloading Helm client archive to {}.", archivePath.toAbsolutePath());
		}

		try {
			Files.createDirectories(archivePath.getParent());
			HttpsUtils.copyHttpsURLToFile(url, archivePath.toFile(), DOWNLOAD_CONNECTION_TIMEOUT, DOWNLOAD_READ_TIMEOUT);
		} catch (IOException ioe) {
			throw new IllegalStateException("Failed to download helm binary from " + url, ioe);
		}
		return archivePath;
	}

	private String getHelmClientUrl(final String helmClientVersion) {
		final String systemType = SystemUtils.IS_OS_MAC ? "darwin" : "linux";
		final String arch = getSystemArch();
		return String.format("%s/%s/helm-%s-%s.tar.gz", HELM_DOWNLOAD_BASE_URL, helmClientVersion, systemType, arch);
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

	private Path getProjectHelmDir() {
		Path dir = Paths.get(LOCAL_BINARY_CLIENT_TMP_DIR);
		try {
			Files.createDirectories(dir);
		} catch (IOException ioe) {
			throw new IllegalStateException("Failed to create directory " + dir.toAbsolutePath(), ioe);
		}
		return dir;
	}

	private static String getHelmClientVersion() {
		String version = IntersmashProperties.get(HELM_CLIENT_VERSION);
		if (version == null) {
			version = IntersmashProperties.get(XTF_HELM_CLIENT_VERSION);
		}
		return version != null ? version : "latest";
	}

	private static boolean isCacheEnabled() {
		String val = IntersmashProperties.get(HELM_BINARY_CACHE_ENABLED);
		if (val == null) {
			val = IntersmashProperties.get(XTF_HELM_BINARY_CACHE_ENABLED);
		}
		return val == null || Boolean.parseBoolean(val);
	}

	private static String getCachePath() {
		String path = IntersmashProperties.get(HELM_BINARY_CACHE_PATH);
		if (path == null) {
			path = IntersmashProperties.get(XTF_HELM_BINARY_CACHE_PATH);
		}
		if (path == null) {
			path = Paths.get(System.getProperty("java.io.tmpdir"), HELM_BINARY_CACHE_DEFAULT_FOLDER)
					.toAbsolutePath().normalize().toString();
		}
		return path;
	}
}
