package org.jboss.intersmash.tools.k8s.client.binary;

import org.jboss.intersmash.tools.k8s.KubernetesConfig;

public class ConfigurationBasedKubernetesClientBinaryPathResolver implements KubernetesClientBinaryPathResolver {

	@Override
	public String resolve() {

		return KubernetesConfig.binaryPath();
	}
}
