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
package org.jboss.intersmash.tools.client;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.jboss.intersmash.tools.config.IntersmashProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Factory class for obtaining {@link OpenShift} client instances configured with admin or master credentials.
 *
 * <p>Credential resolution follows this priority (same as XTF):</p>
 * <ol>
 *   <li>Token (if configured)</li>
 *   <li>Username/password (if configured)</li>
 *   <li>Kubeconfig path (if configured)</li>
 *   <li>Fallback to auto-configuration</li>
 * </ol>
 *
 * <p>This class replaces XTF's {@code cz.xtf.core.openshift.OpenShifts} with an Intersmash-native implementation.</p>
 */
@Slf4j
public class OpenShifts {

	// Property keys with XTF fallback equivalents
	private static final String OPENSHIFT_URL = "intersmash.openshift.url";
	private static final String XTF_URL = "xtf.openshift.url";

	private static final String OPENSHIFT_NAMESPACE = "intersmash.openshift.namespace";
	private static final String XTF_NAMESPACE = "xtf.openshift.namespace";

	private static final String OPENSHIFT_ADMIN_TOKEN = "intersmash.openshift.admin.token";
	private static final String XTF_ADMIN_TOKEN = "xtf.openshift.admin.token";

	private static final String OPENSHIFT_ADMIN_USERNAME = "intersmash.openshift.admin.username";
	private static final String XTF_ADMIN_USERNAME = "xtf.openshift.admin.username";

	private static final String OPENSHIFT_ADMIN_PASSWORD = "intersmash.openshift.admin.password";
	private static final String XTF_ADMIN_PASSWORD = "xtf.openshift.admin.password";

	private static final String OPENSHIFT_ADMIN_KUBECONFIG = "intersmash.openshift.admin.kubeconfig";
	private static final String XTF_ADMIN_KUBECONFIG = "xtf.openshift.admin.kubeconfig";

	private static final String OPENSHIFT_MASTER_TOKEN = "intersmash.openshift.master.token";
	private static final String XTF_MASTER_TOKEN = "xtf.openshift.master.token";

	private static final String OPENSHIFT_TOKEN = "intersmash.openshift.token";
	private static final String XTF_TOKEN = "xtf.openshift.token";

	private static final String OPENSHIFT_MASTER_USERNAME = "intersmash.openshift.master.username";
	private static final String XTF_MASTER_USERNAME = "xtf.openshift.master.username";

	private static final String OPENSHIFT_MASTER_PASSWORD = "intersmash.openshift.master.password";
	private static final String XTF_MASTER_PASSWORD = "xtf.openshift.master.password";

	private static final String OPENSHIFT_MASTER_KUBECONFIG = "intersmash.openshift.master.kubeconfig";
	private static final String XTF_MASTER_KUBECONFIG = "xtf.openshift.master.kubeconfig";

	/**
	 * Returns an admin-privileged OpenShift client for the default namespace.
	 *
	 * @return an admin OpenShift client
	 */
	public static OpenShift admin() {
		return admin(getDefaultNamespace());
	}

	/**
	 * Returns an admin-privileged OpenShift client for the given namespace.
	 *
	 * <p>Credential resolution:</p>
	 * <ol>
	 *   <li>Admin token ({@code intersmash.openshift.admin.token})</li>
	 *   <li>Admin username/password ({@code intersmash.openshift.admin.username/password})</li>
	 *   <li>Admin kubeconfig ({@code intersmash.openshift.admin.kubeconfig})</li>
	 *   <li>Fallback to auto-configuration</li>
	 * </ol>
	 *
	 * @param namespace the target namespace
	 * @return an admin OpenShift client
	 */
	public static OpenShift admin(String namespace) {
		String adminToken = getWithXtfFallback(OPENSHIFT_ADMIN_TOKEN, XTF_ADMIN_TOKEN);
		if (StringUtils.isNotEmpty(adminToken)) {
			return OpenShift.get(url(), namespace, adminToken);
		}

		String adminUsername = getWithXtfFallback(OPENSHIFT_ADMIN_USERNAME, XTF_ADMIN_USERNAME);
		if (StringUtils.isNotEmpty(adminUsername)) {
			String adminPassword = getWithXtfFallback(OPENSHIFT_ADMIN_PASSWORD, XTF_ADMIN_PASSWORD);
			return OpenShift.get(url(), namespace, adminUsername, adminPassword);
		}

		String adminKubeconfig = getWithXtfFallback(OPENSHIFT_ADMIN_KUBECONFIG, XTF_ADMIN_KUBECONFIG);
		if (StringUtils.isNotEmpty(adminKubeconfig)) {
			return OpenShift.get(Paths.get(adminKubeconfig), namespace);
		}

		return OpenShift.get(namespace);
	}

	/**
	 * Returns a master-privileged OpenShift client for the default namespace.
	 *
	 * @return a master OpenShift client
	 */
	public static OpenShift master() {
		return master(getDefaultNamespace());
	}

	/**
	 * Returns a master-privileged OpenShift client for the given namespace.
	 *
	 * <p>Credential resolution:</p>
	 * <ol>
	 *   <li>Master token ({@code intersmash.openshift.master.token}, falling back to
	 *       {@code intersmash.openshift.token})</li>
	 *   <li>Master username/password ({@code intersmash.openshift.master.username/password})</li>
	 *   <li>Master kubeconfig ({@code intersmash.openshift.master.kubeconfig})</li>
	 *   <li>Fallback to auto-configuration</li>
	 * </ol>
	 *
	 * @param namespace the target namespace
	 * @return a master OpenShift client
	 */
	public static OpenShift master(String namespace) {
		String masterToken = masterToken();
		if (StringUtils.isNotEmpty(masterToken)) {
			return OpenShift.get(url(), namespace, masterToken);
		}

		String masterUsername = getWithXtfFallback(OPENSHIFT_MASTER_USERNAME, XTF_MASTER_USERNAME);
		if (StringUtils.isNotEmpty(masterUsername)) {
			String masterPassword = getWithXtfFallback(OPENSHIFT_MASTER_PASSWORD, XTF_MASTER_PASSWORD);
			return OpenShift.get(url(), namespace, masterUsername, masterPassword);
		}

		String masterKubeconfig = getWithXtfFallback(OPENSHIFT_MASTER_KUBECONFIG, XTF_MASTER_KUBECONFIG);
		if (StringUtils.isNotEmpty(masterKubeconfig)) {
			return OpenShift.get(Paths.get(masterKubeconfig), namespace);
		}

		return OpenShift.get(namespace);
	}

	// ========================
	// Internal helpers
	// ========================

	private static String url() {
		return getWithXtfFallback(OPENSHIFT_URL, XTF_URL);
	}

	private static String masterToken() {
		String masterToken = getWithXtfFallback(OPENSHIFT_MASTER_TOKEN, XTF_MASTER_TOKEN);
		if (masterToken == null) {
			return getWithXtfFallback(OPENSHIFT_TOKEN, XTF_TOKEN);
		}
		return masterToken;
	}

	private static String getDefaultNamespace() {
		String namespace = getWithXtfFallback(OPENSHIFT_NAMESPACE, XTF_NAMESPACE);
		if (StringUtils.isEmpty(namespace)) {
			throw new IllegalStateException(
					"No default namespace configured. Set 'intersmash.openshift.namespace' or 'xtf.openshift.namespace'.");
		}
		return namespace;
	}

	private static String getWithXtfFallback(String intersmashKey, String xtfKey) {
		String value = IntersmashProperties.get(intersmashKey);
		if (value == null && xtfKey != null) {
			value = IntersmashProperties.get(xtfKey);
		}
		return value;
	}
}
