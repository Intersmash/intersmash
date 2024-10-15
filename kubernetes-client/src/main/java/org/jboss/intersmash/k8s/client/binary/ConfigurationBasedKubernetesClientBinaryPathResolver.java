package org.jboss.intersmash.k8s.client.binary;

import org.jboss.intersmash.k8s.KubernetesConfig;

public class ConfigurationBasedKubernetesClientBinaryPathResolver implements KubernetesClientBinaryPathResolver {

	@Override
	public String resolve() {

		return KubernetesConfig.binaryPath();
	}
}
