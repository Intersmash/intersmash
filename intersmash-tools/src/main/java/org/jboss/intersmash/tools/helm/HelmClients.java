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

import org.jboss.intersmash.tools.config.IntersmashProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Factory for {@link HelmBinary} instances, configured via {@link IntersmashProperties}.
 *
 * <p>Looks up the Helm binary path from {@code intersmash.helm.binary.path}
 * (falling back to {@code xtf.helm.binary.path}), the admin token from
 * {@code intersmash.openshift.admin.token} (falling back to {@code xtf.openshift.admin.token}),
 * and the namespace from {@code intersmash.openshift.namespace} (falling back to {@code xtf.openshift.namespace}).</p>
 */
@Slf4j
public class HelmClients {

	private static final String HELM_BINARY_PATH = "intersmash.helm.binary.path";
	private static final String XTF_HELM_BINARY_PATH = "xtf.helm.binary.path";

	private static final String ADMIN_TOKEN = "intersmash.openshift.admin.token";
	private static final String XTF_ADMIN_TOKEN = "xtf.openshift.admin.token";

	private static final String MASTER_TOKEN = "intersmash.openshift.master.token";
	private static final String XTF_MASTER_TOKEN = "xtf.openshift.master.token";

	private static final String TOKEN = "intersmash.openshift.token";
	private static final String XTF_TOKEN = "xtf.openshift.token";

	private static final String NAMESPACE = "intersmash.openshift.namespace";
	private static final String XTF_NAMESPACE = "xtf.openshift.namespace";

	public static HelmBinary adminBinary() {
		String helmPath = resolveHelmBinaryPath();
		String adminToken = getWithFallback(ADMIN_TOKEN, XTF_ADMIN_TOKEN);
		if (adminToken == null || adminToken.isEmpty()) {
			throw new IllegalStateException(
					"Admin token is not configured. Please set '" + ADMIN_TOKEN + "' or '" + XTF_ADMIN_TOKEN
							+ "' in your properties.");
		}
		String namespace = getWithFallback(NAMESPACE, XTF_NAMESPACE);
		return new HelmBinary(helmPath, adminToken, namespace);
	}

	public static HelmBinary masterBinary() {
		String helmPath = resolveHelmBinaryPath();
		String masterToken = getWithFallback(MASTER_TOKEN, XTF_MASTER_TOKEN);
		if (masterToken == null || masterToken.isEmpty()) {
			masterToken = getWithFallback(TOKEN, XTF_TOKEN);
		}
		if (masterToken == null || masterToken.isEmpty()) {
			throw new IllegalStateException(
					"Master token is not configured. Please set '" + MASTER_TOKEN + "' or '" + XTF_MASTER_TOKEN
							+ "' in your properties.");
		}
		String namespace = getWithFallback(NAMESPACE, XTF_NAMESPACE);
		return new HelmBinary(helmPath, masterToken, namespace);
	}

	private static String resolveHelmBinaryPath() {
		String helmPath = HelmBinaryManagerFactory.INSTANCE.getHelmBinaryPath();
		if (helmPath == null || helmPath.isEmpty()) {
			throw new IllegalStateException(
					"Helm binary path could not be resolved. Set '" + HELM_BINARY_PATH + "' or '" + XTF_HELM_BINARY_PATH
							+ "' in your properties, or ensure the auto-download can reach mirror.openshift.com.");
		}
		return helmPath;
	}

	private static String getWithFallback(String primary, String fallback) {
		String value = IntersmashProperties.get(primary);
		if (value == null && fallback != null) {
			value = IntersmashProperties.get(fallback);
		}
		return value;
	}
}
