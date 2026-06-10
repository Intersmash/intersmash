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
package org.jboss.intersmash.k8s.client;

import org.jboss.intersmash.k8s.client.binary.KubernetesClientBinary;
import org.jboss.intersmash.k8s.client.binary.OpenShiftClientBinaryManagerFactory;

/**
 * Factory class for obtaining {@link KubernetesClientBinary} instances with admin or master credentials
 * for OpenShift clusters. This replaces the {@code OpenShifts.adminBinary()} / {@code OpenShifts.masterBinary()}
 * pattern from XTF.
 *
 * <p>Since the {@code intersmash-tools} module cannot depend on {@code kubernetes-client} (circular dependency),
 * this class lives in the {@code kubernetes-client} module and consumer code should use
 * {@code OpenShiftBinaries.adminBinary()} instead of {@code OpenShifts.adminBinary()}.</p>
 */
public class OpenShiftBinaries {

	/**
	 * Returns a {@link KubernetesClientBinary} configured with admin credentials for the default namespace.
	 *
	 * @return an admin binary client
	 */
	public static KubernetesClientBinary adminBinary() {
		return adminBinary(NamespaceManager.getNamespace());
	}

	/**
	 * Returns a {@link KubernetesClientBinary} configured with admin credentials for the given namespace.
	 *
	 * @param namespace the target namespace
	 * @return an admin binary client
	 */
	public static KubernetesClientBinary adminBinary(String namespace) {
		return OpenShiftClientBinaryManagerFactory.INSTANCE.getOpenShiftClientBinaryManager().adminBinary(namespace);
	}

	/**
	 * Returns a {@link KubernetesClientBinary} configured with master credentials for the default namespace.
	 *
	 * @return a master binary client
	 */
	public static KubernetesClientBinary masterBinary() {
		return masterBinary(NamespaceManager.getNamespace());
	}

	/**
	 * Returns a {@link KubernetesClientBinary} configured with master credentials for the given namespace.
	 *
	 * @param namespace the target namespace
	 * @return a master binary client
	 */
	public static KubernetesClientBinary masterBinary(String namespace) {
		return OpenShiftClientBinaryManagerFactory.INSTANCE.getOpenShiftClientBinaryManager().masterBinary(namespace);
	}
}
