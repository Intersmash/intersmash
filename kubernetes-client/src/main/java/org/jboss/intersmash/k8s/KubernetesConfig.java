package org.jboss.intersmash.k8s;

import java.nio.file.Paths;

import cz.xtf.core.config.XTFConfig;
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

	public static String url() {
		return XTFConfig.get(KUBERNETES_URL);
	}

	public static String getKubernetesHostname() {
		return XTFConfig.get(KUBERNETES_HOSTNAME, "localhost");
	}

	private static final String CLEAN_KUBERNETES = "intersmash.junit.clean_namespace";

	/**
	 * Used only if intersmash.kubernetes.namespace.per.testcase=true
	 *
	 * @return limit on namespace if it's set by -Dintersmash.kubernetes.namespace.per.testcase.length.limit property
	 */
	public static int getNamespaceLengthLimitForUniqueNamespacePerTest() {
		return Integer.parseInt(XTFConfig.get(KUBERNETES_NAMESPACE_NAME_LENGTH_LIMIT,
				DEFAULT_KUBERNETES_NAMESPACE_NAME_LENGTH_LIMIT));
	}

	public static boolean cleanKubernetes() {
		return Boolean.valueOf(XTFConfig.get(CLEAN_KUBERNETES, "false"));
	}

	/**
	 * @return if property xtf.openshift.namespace.per.testcase is empty or true then returns true otherwise false
	 */
	public static boolean useNamespacePerTestCase() {
		return XTFConfig.get(KUBERNETES_NAMESPACE_PER_TESTCASE) != null
				&& (XTFConfig.get(KUBERNETES_NAMESPACE_PER_TESTCASE).equals("")
						|| XTFConfig.get(KUBERNETES_NAMESPACE_PER_TESTCASE).toLowerCase().equals("true"));
	}

	/**
	 * @return returns token
	 * @deprecated Use masterToken {@link #masterToken()}
	 */
	@Deprecated
	public static String token() {
		String token = XTFConfig.get(KUBERNETES_TOKEN);
		if (token == null) {
			return XTFConfig.get(KUBERNETES_MASTER_TOKEN);
		}
		return token;
	}

	public static String adminToken() {
		return XTFConfig.get(KUBERNETES_ADMIN_TOKEN);
	}

	public static String version() {
		return XTFConfig.get(KUBERNETES_VERSION);
	}

	/**
	 * Default namespace for currently running test.
	 *
	 * @return Returns namespace as defined in intersmash.kubernetes.namespace property
	 */
	public static String namespace() {
		return XTFConfig.get(KUBERNETES_NAMESPACE);
	}

	public static String binaryPath() {
		return XTFConfig.get(KUBERNETES_BINARY_PATH);
	}

	public static boolean isBinaryCacheEnabled() {
		return Boolean.parseBoolean(XTFConfig.get(KUBERNETES_BINARY_CACHE_ENABLED, "true"));
	}

	public static String binaryCachePath() {
		return XTFConfig.get(KUBERNETES_BINARY_CACHE_PATH, Paths.get(System.getProperty("java.io.tmpdir"),
				KUBERNETES_BINARY_CACHE_DEFAULT_FOLDER).toAbsolutePath().normalize().toString());
	}

	public static String adminUsername() {
		return XTFConfig.get(KUBERNETES_ADMIN_USERNAME);
	}

	public static String adminPassword() {
		return XTFConfig.get(KUBERNETES_ADMIN_PASSWORD);
	}

	public static String adminKubeconfig() {
		return XTFConfig.get(KUBERNETES_ADMIN_KUBECONFIG);
	}

	public static String masterUsername() {
		return XTFConfig.get(KUBERNETES_MASTER_USERNAME);
	}

	public static String masterPassword() {
		return XTFConfig.get(KUBERNETES_MASTER_PASSWORD);
	}

	public static String masterKubeconfig() {
		return XTFConfig.get(KUBERNETES_MASTER_KUBECONFIG);
	}

	public static String pullSecret() {
		return XTFConfig.get(KUBERNETES_PULL_SECRET);
	}

	/**
	 * @return For backwards-compatibility reasons, also returns the value of intersmash.kubernetes.token if
	 * intersmash.kubernetes.master.token not specified
	 */
	public static String masterToken() {
		String masterToken = XTFConfig.get(KUBERNETES_MASTER_TOKEN);
		if (masterToken == null) {
			return XTFConfig.get(KUBERNETES_TOKEN);
		}
		return masterToken;
	}

	public static String routeDomain() {
		return XTFConfig.get(KUBERNETES_ROUTE_DOMAIN);
	}
}
