/**
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum KubernetesClientBinaryManagerFactory {
	INSTANCE;

	private volatile KubernetesClientBinaryManager kubernetesClientBinaryManager;

	public KubernetesClientBinaryManager getKubernetesClientBinaryManager() {
		KubernetesClientBinaryManager localRef = kubernetesClientBinaryManager;
		if (localRef == null) {
			synchronized (KubernetesClientBinaryManager.class) {
				localRef = kubernetesClientBinaryManager;
				if (localRef == null) {
					for (KubernetesClientBinaryPathResolver resolver : resolverList()) {
						String path = resolver.resolve();
						if (path != null) {
							kubernetesClientBinaryManager = localRef = new KubernetesClientBinaryManager(path);
							break;
						}
					}
				}
			}
		}
		return localRef;
	}

	private List<KubernetesClientBinaryPathResolver> resolverList() {
		return Stream.of(
				new ConfigurationBasedKubernetesClientBinaryPathResolver(),
				new ClusterVersionBasedKubernetesClientBinaryPathResolver()).collect(Collectors.toList());
	}
}
