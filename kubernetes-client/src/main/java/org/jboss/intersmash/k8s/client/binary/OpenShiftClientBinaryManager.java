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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jboss.intersmash.k8s.OpenShiftConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenShiftClientBinaryManager {
	private final String ocBinaryPath;

	OpenShiftClientBinaryManager(final String ocBinaryPath) {
		this.ocBinaryPath = ocBinaryPath;
	}

	public String getBinaryPath() {
		return ocBinaryPath;
	}

	public KubernetesClientBinary masterBinary(final String namespace) {
		Objects.requireNonNull(namespace);
		return getBinary(OpenShiftConfig.masterToken(), OpenShiftConfig.masterUsername(), OpenShiftConfig.masterPassword(),
				OpenShiftConfig.masterKubeconfig(), namespace);
	}

	public KubernetesClientBinary adminBinary(final String namespace) {
		Objects.requireNonNull(namespace);
		return getBinary(OpenShiftConfig.adminToken(), OpenShiftConfig.adminUsername(), OpenShiftConfig.adminPassword(),
				OpenShiftConfig.adminKubeconfig(), namespace);
	}

	private KubernetesClientBinary getBinary(final String token, final String username, final String password,
			final String kubeconfig, String namespace) {
		String configPath = createUniqueConfigFolder().resolve("oc.config").toAbsolutePath().toString();
		KubernetesClientBinary ocBinary;

		if (StringUtils.isNotEmpty(token) || StringUtils.isNotEmpty(username)) {
			ocBinary = new KubernetesClientBinary(ocBinaryPath, configPath);
			if (StringUtils.isNotEmpty(token)) {
				ocBinary.login(OpenShiftConfig.url(), token);
			} else {
				ocBinary.login(OpenShiftConfig.url(), username, password);
			}
		} else {
			final Path actualConfigPath = Paths.get(configPath);
			if (StringUtils.isNotEmpty(kubeconfig)) {
				try {
					Files.write(actualConfigPath,
							Arrays.asList(new KubernetesClientBinary(ocBinaryPath, null)
									.execute("config", "view", "--kubeconfig", kubeconfig, "--flatten")),
							StandardCharsets.UTF_8);
				} catch (IOException e) {
					throw new IllegalStateException("Couldn't create a copy of an existing kubeconfig file", e);
				}
			} else {
				File defaultKubeConfig = Paths.get(getHomeDir(), ".kube", "config").toFile();
				if (defaultKubeConfig.isFile()) {
					try {
						Files.write(actualConfigPath,
								Arrays.asList(new KubernetesClientBinary(ocBinaryPath, null)
										.execute("config", "view", "--kubeconfig", defaultKubeConfig.getAbsolutePath(),
												"--flatten")),
								StandardCharsets.UTF_8);
					} catch (IOException e) {
						throw new IllegalStateException("Couldn't create a copy of the default kubeconfig file", e);
					}
				} else {
					throw new IllegalStateException(defaultKubeConfig.getAbsolutePath()
							+ " does not exist and no other OpenShift master option specified");
				}
			}
			ocBinary = new KubernetesClientBinary(ocBinaryPath, configPath);
		}

		if (StringUtils.isNotEmpty(namespace)) {
			ocBinary.namespace(namespace);
		}

		return ocBinary;
	}

	private Path createUniqueConfigFolder() {
		try {
			return Files.createTempDirectory(getProjectOcConfigDir(), "config");
		} catch (IOException e) {
			throw new IllegalStateException("Temporary folder for oc config couldn't be created", e);
		}
	}

	private static String getHomeDir() {
		String home = System.getenv("HOME");
		if (home != null && !home.isEmpty()) {
			File f = new File(home);
			if (f.exists() && f.isDirectory()) {
				return home;
			}
		}
		return System.getProperty("user.home", ".");
	}

	private Path getProjectOcConfigDir() {
		return Paths.get(OpenShiftClientBinaryPathResolver.LOCAL_BINARY_CLIENT_TMP_DIR);
	}
}
