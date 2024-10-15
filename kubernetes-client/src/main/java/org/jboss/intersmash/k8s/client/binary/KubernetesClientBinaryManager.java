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
import org.jboss.intersmash.k8s.KubernetesConfig;
import org.jboss.intersmash.k8s.client.Kuberneteses;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KubernetesClientBinaryManager {
	private final String kubernetesClientBinaryPath;

	KubernetesClientBinaryManager(final String kubernetesClientBinaryPath) {
		this.kubernetesClientBinaryPath = kubernetesClientBinaryPath;
	}

	public String getBinaryPath() {
		return kubernetesClientBinaryPath;
	}

	public KubernetesClientBinary masterBinary(final String namespace) {
		Objects.requireNonNull(namespace);

		return getBinary(KubernetesConfig.masterToken(), KubernetesConfig.masterUsername(), KubernetesConfig.masterPassword(),
				KubernetesConfig.masterKubeconfig(), namespace);
	}

	public KubernetesClientBinary adminBinary(final String namespace) {
		Objects.requireNonNull(namespace);

		return getBinary(KubernetesConfig.adminToken(), KubernetesConfig.adminUsername(), KubernetesConfig.adminPassword(),
				KubernetesConfig.adminKubeconfig(), namespace);
	}

	private KubernetesClientBinary getBinary(final String token, final String username, final String password,
			final String kubeconfig,
			String namespace) {
		String configPath = createUniqueKubernetesConfigFolder().resolve("kube.config").toAbsolutePath().toString();
		KubernetesClientBinary kubernetesClientBinary;

		if (StringUtils.isNotEmpty(token) || StringUtils.isNotEmpty(username)) {
			// If we are using a token or username/password, we start with a nonexisting kubeconfig and do a "kubectl login"
			kubernetesClientBinary = new KubernetesClientBinary(Kuberneteses.getBinaryPath(), configPath);
			if (StringUtils.isNotEmpty(token)) {
				kubernetesClientBinary.login(KubernetesConfig.url(), token);
			} else {
				kubernetesClientBinary.login(KubernetesConfig.url(), username, password);
			}
		} else {
			// If we are using an existing kubeconfig (or a default kubeconfig), we copy the original kubeconfig
			final Path actualConfigPath = Paths.get(configPath);
			if (StringUtils.isNotEmpty(kubeconfig)) {
				// flatten kubeconfig in case it contains certs/keys
				try {
					Files.write(actualConfigPath,
							Arrays.asList(new KubernetesClientBinary(Kuberneteses.getBinaryPath(), null)
									.execute("config", "view", "--kubeconfig", kubeconfig, "--flatten")),
							StandardCharsets.UTF_8);
				} catch (IOException e) {
					throw new IllegalStateException("Couldn't create a copy of an existing Kubernetes configuration file", e);
				}
			} else {
				// We copy the default ~/.kube/config
				File defaultKubeConfig = Paths.get(getHomeDir(), ".kube", "config").toFile();
				if (defaultKubeConfig.isFile()) {
					try {
						Files.write(actualConfigPath,
								Arrays.asList(new KubernetesClientBinary(Kuberneteses.getBinaryPath(), null)
										.execute("config", "view", "--kubeconfig", defaultKubeConfig.getAbsolutePath(),
												"--flatten")),
								StandardCharsets.UTF_8);
					} catch (IOException e) {
						throw new IllegalStateException("Couldn't create a copy of the default Kubernetes configuration file",
								e);
					}
				} else {
					throw new IllegalStateException(defaultKubeConfig.getAbsolutePath()
							+ " does not exist and no other Kubernetes master option specified");
				}
			}
			kubernetesClientBinary = new KubernetesClientBinary(Kuberneteses.getBinaryPath(), configPath);
		}

		if (StringUtils.isNotEmpty(namespace)) {
			kubernetesClientBinary.namespace(namespace);
		}

		return kubernetesClientBinary;
	}

	private Path createUniqueKubernetesConfigFolder() {
		try {
			return Files.createTempDirectory(getProjectKubernetesConfigDir(), "config");
		} catch (IOException e) {
			throw new IllegalStateException("Temporary folder for kubectl config couldn't be created", e);
		}
	}

	// TODO: this code is duplicated from OpenShifts.getHomeDir
	// it should be revised together with token management
	// https://github.com/xtf-cz/xtf/issues/464
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

	private Path getProjectKubernetesConfigDir() {
		return Paths.get(KubernetesClientBinaryPathResolver.LOCAL_BINARY_CLIENT_TMP_DIR);
	}
}
