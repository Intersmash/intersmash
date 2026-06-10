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
package org.jboss.intersmash.k8s;

import java.nio.file.Paths;

import org.jboss.intersmash.tools.config.IntersmashProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class KubernetesConfig {
	public static final String KUBERNETES_URL = "intersmash.kubernetes.url";
	public static final String KUBERNETES_HOSTNAME = "intersmash.kubernetes.hostname";
	public static final String KUBERNETES_TOKEN = "intersmash.kubernetes.token";
	public static final String KUBERNETES_VERSION = "intersmash.kubernetes.version";
	public static final String KUBERNETES_NAMESPACE = "intersmash.kubernetes.namespace";
	public static final String KUBERNETES_BINARY_PATH = "intersmash.kubernetes.binary.path";
	public static final String KUBERNETES_BINARY_CACHE_ENABLED = "intersmash.kubernetes.binary.cache.enabled";
	public static final String KUBERNETES_BINARY_CACHE_PATH = "intersmash.kubernetes.binary.cache.path";
	public static final String KUBERNETES_BINARY_CACHE_DEFAULT_FOLDER = "kubectl-cache";
	public static final String KUBERNETES_ADMIN_USERNAME = "intersmash.kubernetes.admin.username";
	public static final String KUBERNETES_ADMIN_PASSWORD = "intersmash.kubernetes.admin.password";
	public static final String KUBERNETES_ADMIN_KUBECONFIG = "intersmash.kubernetes.admin.kubeconfig";
	public static final String KUBERNETES_ADMIN_TOKEN = "intersmash.kubernetes.admin.token";
	public static final String KUBERNETES_MASTER_USERNAME = "intersmash.kubernetes.master.username";
	public static final String KUBERNETES_MASTER_PASSWORD = "intersmash.kubernetes.master.password";
	public static final String KUBERNETES_MASTER_KUBECONFIG = "intersmash.kubernetes.master.kubeconfig";
	public static final String KUBERNETES_MASTER_TOKEN = "intersmash.kubernetes.master.token";
	public static final String KUBERNETES_ROUTE_DOMAIN = "intersmash.kubernetes.route_domain";
	public static final String KUBERNETES_PULL_SECRET = "intersmash.kubernetes.pullsecret";
	public static final String KUBERNETES_NAMESPACE_PER_TESTCASE = "intersmash.kubernetes.namespace.per.testcase";

	/**
	 * Used only if intersmash.kubernetes.namespace.per.testcase=true - this property can configure its maximum length. This is useful
	 * in case
	 * where namespace is used in first part of URL of route which must have <64 chars length.
	 */
	public static final String KUBERNETES_NAMESPACE_NAME_LENGTH_LIMIT = "intersmash.kubernetes.namespace.per.testcase.length.limit";

	/**
	 * Used only if intersmash.kubernetes.namespace.per.testcase=true - this property configures default maximum length of namespace
	 * name.
	 */
	private static final String DEFAULT_KUBERNETES_NAMESPACE_NAME_LENGTH_LIMIT = "25";

	private static String getWithOpenShiftFallback(String kubernetesValue, String openShiftFallback) {
		return kubernetesValue != null ? kubernetesValue : openShiftFallback;
	}

	public static String url() {
		return getWithOpenShiftFallback(IntersmashProperties.get(KUBERNETES_URL), OpenShiftConfig.url());
	}

	public static String getKubernetesHostname() {
		return IntersmashProperties.get(KUBERNETES_HOSTNAME, "localhost");
	}

	private static final String CLEAN_KUBERNETES = "intersmash.junit.clean_namespace";

	/**
	 * Used only if intersmash.kubernetes.namespace.per.testcase=true
	 *
	 * @return limit on namespace if it's set by -Dintersmash.kubernetes.namespace.per.testcase.length.limit property
	 */
	public static int getNamespaceLengthLimitForUniqueNamespacePerTest() {
		return Integer.parseInt(IntersmashProperties.get(KUBERNETES_NAMESPACE_NAME_LENGTH_LIMIT,
				DEFAULT_KUBERNETES_NAMESPACE_NAME_LENGTH_LIMIT));
	}

	public static boolean cleanKubernetes() {
		return Boolean.valueOf(IntersmashProperties.get(CLEAN_KUBERNETES, "false"));
	}

	/**
	 * @return if property xtf.openshift.namespace.per.testcase is empty or true then returns true otherwise false
	 */
	public static boolean useNamespacePerTestCase() {
		return IntersmashProperties.get(KUBERNETES_NAMESPACE_PER_TESTCASE) != null
				&& (IntersmashProperties.get(KUBERNETES_NAMESPACE_PER_TESTCASE).equals("")
						|| IntersmashProperties.get(KUBERNETES_NAMESPACE_PER_TESTCASE).toLowerCase().equals("true"));
	}

	/**
	 * @return returns token
	 * @deprecated Use masterToken {@link #masterToken()}
	 */
	@Deprecated
	public static String token() {
		String token = IntersmashProperties.get(KUBERNETES_TOKEN);
		if (token == null) {
			return IntersmashProperties.get(KUBERNETES_MASTER_TOKEN);
		}
		return token;
	}

	public static String adminToken() {
		return getWithOpenShiftFallback(IntersmashProperties.get(KUBERNETES_ADMIN_TOKEN), OpenShiftConfig.adminToken());
	}

	public static String version() {
		return IntersmashProperties.get(KUBERNETES_VERSION);
	}

	/**
	 * Default namespace for currently running test.
	 *
	 * @return Returns namespace as defined in intersmash.kubernetes.namespace property
	 */
	public static String namespace() {
		String ns = IntersmashProperties.get(KUBERNETES_NAMESPACE);
		if (ns == null) {
			ns = OpenShiftConfig.namespace();
		}
		return ns;
	}

	public static String binaryPath() {
		return IntersmashProperties.get(KUBERNETES_BINARY_PATH);
	}

	public static boolean isBinaryCacheEnabled() {
		return Boolean.parseBoolean(IntersmashProperties.get(KUBERNETES_BINARY_CACHE_ENABLED, "true"));
	}

	public static String binaryCachePath() {
		return IntersmashProperties.get(KUBERNETES_BINARY_CACHE_PATH, Paths.get(System.getProperty("java.io.tmpdir"),
				KUBERNETES_BINARY_CACHE_DEFAULT_FOLDER).toAbsolutePath().normalize().toString());
	}

	public static String adminUsername() {
		return getWithOpenShiftFallback(IntersmashProperties.get(KUBERNETES_ADMIN_USERNAME), OpenShiftConfig.adminUsername());
	}

	public static String adminPassword() {
		return getWithOpenShiftFallback(IntersmashProperties.get(KUBERNETES_ADMIN_PASSWORD), OpenShiftConfig.adminPassword());
	}

	public static String adminKubeconfig() {
		return getWithOpenShiftFallback(IntersmashProperties.get(KUBERNETES_ADMIN_KUBECONFIG),
				OpenShiftConfig.adminKubeconfig());
	}

	public static String masterUsername() {
		return getWithOpenShiftFallback(IntersmashProperties.get(KUBERNETES_MASTER_USERNAME), OpenShiftConfig.masterUsername());
	}

	public static String masterPassword() {
		return getWithOpenShiftFallback(IntersmashProperties.get(KUBERNETES_MASTER_PASSWORD), OpenShiftConfig.masterPassword());
	}

	public static String masterKubeconfig() {
		return getWithOpenShiftFallback(IntersmashProperties.get(KUBERNETES_MASTER_KUBECONFIG),
				OpenShiftConfig.masterKubeconfig());
	}

	public static String pullSecret() {
		return getWithOpenShiftFallback(IntersmashProperties.get(KUBERNETES_PULL_SECRET), OpenShiftConfig.pullSecret());
	}

	/**
	 * @return For backwards-compatibility reasons, also returns the value of intersmash.kubernetes.token if
	 * intersmash.kubernetes.master.token not specified. Falls back to OpenShiftConfig if neither is set.
	 */
	public static String masterToken() {
		String masterToken = IntersmashProperties.get(KUBERNETES_MASTER_TOKEN);
		if (masterToken == null) {
			masterToken = IntersmashProperties.get(KUBERNETES_TOKEN);
		}
		if (masterToken == null) {
			masterToken = OpenShiftConfig.masterToken();
		}
		return masterToken;
	}

	public static String routeDomain() {
		return IntersmashProperties.get(KUBERNETES_ROUTE_DOMAIN);
	}
}
