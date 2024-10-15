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
