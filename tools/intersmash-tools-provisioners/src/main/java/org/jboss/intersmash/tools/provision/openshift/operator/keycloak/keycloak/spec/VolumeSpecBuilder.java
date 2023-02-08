package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

public final class VolumeSpecBuilder {
	private ConfigMapVolumeSpec configMap;

	/**
	 * Set the ConfigMap mount.
	 *
	 * @param configMap The ConfigMap mount configuration
	 * @return this
	 */
	public VolumeSpecBuilder configMap(ConfigMapVolumeSpec configMap) {
		this.configMap = configMap;
		return this;
	}

	public VolumeSpec build() {
		VolumeSpec volumeSpec = new VolumeSpec();
		volumeSpec.setConfigMap(configMap);
		return volumeSpec;
	}
}
