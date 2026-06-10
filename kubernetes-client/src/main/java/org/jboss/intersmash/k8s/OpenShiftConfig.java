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

public final class OpenShiftConfig {
	public static final String OPENSHIFT_URL = "intersmash.openshift.url";
	public static final String OPENSHIFT_TOKEN = "intersmash.openshift.token";
	public static final String OPENSHIFT_VERSION = "intersmash.openshift.version";
	public static final String OPENSHIFT_NAMESPACE = "intersmash.openshift.namespace";
	public static final String OPENSHIFT_BINARY_PATH = "intersmash.openshift.binary.path";
	public static final String OPENSHIFT_BINARY_URL_CHANNEL = "intersmash.openshift.binary.url.channel";
	public static final String OPENSHIFT_BINARY_CACHE_ENABLED = "intersmash.openshift.binary.cache.enabled";
	public static final String OPENSHIFT_BINARY_CACHE_PATH = "intersmash.openshift.binary.cache.path";
	public static final String OPENSHIFT_BINARY_CACHE_DEFAULT_FOLDER = "oc-cache";
	public static final String OPENSHIFT_ADMIN_USERNAME = "intersmash.openshift.admin.username";
	public static final String OPENSHIFT_ADMIN_PASSWORD = "intersmash.openshift.admin.password";
	public static final String OPENSHIFT_ADMIN_KUBECONFIG = "intersmash.openshift.admin.kubeconfig";
	public static final String OPENSHIFT_ADMIN_TOKEN = "intersmash.openshift.admin.token";
	public static final String OPENSHIFT_MASTER_USERNAME = "intersmash.openshift.master.username";
	public static final String OPENSHIFT_MASTER_PASSWORD = "intersmash.openshift.master.password";
	public static final String OPENSHIFT_MASTER_KUBECONFIG = "intersmash.openshift.master.kubeconfig";
	public static final String OPENSHIFT_MASTER_TOKEN = "intersmash.openshift.master.token";
	public static final String OPENSHIFT_ROUTE_DOMAIN = "intersmash.openshift.route_domain";
	public static final String OPENSHIFT_PULL_SECRET = "intersmash.openshift.pullsecret";
	public static final String OPENSHIFT_NAMESPACE_PER_TESTCASE = "intersmash.openshift.namespace.per.testcase";
	public static final String OPENSHIFT_NAMESPACE_NAME_LENGTH_LIMIT = "intersmash.openshift.namespace.per.testcase.length.limit";

	private static final String DEFAULT_OPENSHIFT_NAMESPACE_NAME_LENGTH_LIMIT = "25";

	private static String getWithXtfFallback(String intersmashKey, String xtfKey) {
		String value = IntersmashProperties.get(intersmashKey);
		if (value == null && xtfKey != null) {
			value = IntersmashProperties.get(xtfKey);
		}
		return value;
	}

	private static String getWithXtfFallback(String intersmashKey, String xtfKey, String defaultValue) {
		String value = getWithXtfFallback(intersmashKey, xtfKey);
		return value != null ? value : defaultValue;
	}

	public static String url() {
		return getWithXtfFallback(OPENSHIFT_URL, "xtf.openshift.url");
	}

	public static String adminToken() {
		return getWithXtfFallback(OPENSHIFT_ADMIN_TOKEN, "xtf.openshift.admin.token");
	}

	public static String version() {
		return getWithXtfFallback(OPENSHIFT_VERSION, "xtf.openshift.version");
	}

	public static String namespace() {
		return getWithXtfFallback(OPENSHIFT_NAMESPACE, "xtf.openshift.namespace");
	}

	public static boolean useNamespacePerTestCase() {
		String value = getWithXtfFallback(OPENSHIFT_NAMESPACE_PER_TESTCASE, "xtf.openshift.namespace.per.testcase");
		return value != null && (value.isEmpty() || value.equalsIgnoreCase("true"));
	}

	public static int getNamespaceLengthLimitForUniqueNamespacePerTest() {
		return Integer.parseInt(getWithXtfFallback(OPENSHIFT_NAMESPACE_NAME_LENGTH_LIMIT,
				"xtf.openshift.namespace.per.testcase.length.limit", DEFAULT_OPENSHIFT_NAMESPACE_NAME_LENGTH_LIMIT));
	}

	public static String binaryPath() {
		return getWithXtfFallback(OPENSHIFT_BINARY_PATH, "xtf.openshift.binary.path");
	}

	public static String binaryUrlChannelPath() {
		return getWithXtfFallback(OPENSHIFT_BINARY_URL_CHANNEL, "xtf.openshift.binary.url.channel", "stable");
	}

	public static boolean isBinaryCacheEnabled() {
		return Boolean.parseBoolean(
				getWithXtfFallback(OPENSHIFT_BINARY_CACHE_ENABLED, "xtf.openshift.binary.cache.enabled", "true"));
	}

	public static String binaryCachePath() {
		return getWithXtfFallback(OPENSHIFT_BINARY_CACHE_PATH, "xtf.openshift.binary.cache.path",
				Paths.get(System.getProperty("java.io.tmpdir"), OPENSHIFT_BINARY_CACHE_DEFAULT_FOLDER)
						.toAbsolutePath().normalize().toString());
	}

	public static String adminUsername() {
		return getWithXtfFallback(OPENSHIFT_ADMIN_USERNAME, "xtf.openshift.admin.username");
	}

	public static String adminPassword() {
		return getWithXtfFallback(OPENSHIFT_ADMIN_PASSWORD, "xtf.openshift.admin.password");
	}

	public static String adminKubeconfig() {
		return getWithXtfFallback(OPENSHIFT_ADMIN_KUBECONFIG, "xtf.openshift.admin.kubeconfig");
	}

	public static String masterUsername() {
		return getWithXtfFallback(OPENSHIFT_MASTER_USERNAME, "xtf.openshift.master.username");
	}

	public static String masterPassword() {
		return getWithXtfFallback(OPENSHIFT_MASTER_PASSWORD, "xtf.openshift.master.password");
	}

	public static String masterKubeconfig() {
		return getWithXtfFallback(OPENSHIFT_MASTER_KUBECONFIG, "xtf.openshift.master.kubeconfig");
	}

	public static String pullSecret() {
		return getWithXtfFallback(OPENSHIFT_PULL_SECRET, "xtf.openshift.pullsecret");
	}

	public static String masterToken() {
		String masterToken = getWithXtfFallback(OPENSHIFT_MASTER_TOKEN, "xtf.openshift.master.token");
		if (masterToken == null) {
			return getWithXtfFallback(OPENSHIFT_TOKEN, "xtf.openshift.token");
		}
		return masterToken;
	}

	public static String routeDomain() {
		return getWithXtfFallback(OPENSHIFT_ROUTE_DOMAIN, "xtf.openshift.route_domain");
	}
}
