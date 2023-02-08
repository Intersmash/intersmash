package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.api.model.KeyToPath;

public final class ConfigMapVolumeSpecBuilder {
	private String name;
	private String mountPath;
	private List<KeyToPath> items;

	/**
	 * Set the ConfigMap name.
	 *
	 * @param name Desired ConfigMap name
	 * @return this
	 */
	public ConfigMapVolumeSpecBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Set the absolute path where to mount the COnfigMap.
	 *
	 * @param mountPath Desired mount path for the ConfigMap
	 * @return this
	 */
	public ConfigMapVolumeSpecBuilder mountPath(String mountPath) {
		this.mountPath = mountPath;
		return this;
	}

	/**
	 * ConfigMap mount details.
	 *
	 * @param items List of {@link KeyToPath} instances representing the ConfigMap mount details
	 * @return this
	 */
	public ConfigMapVolumeSpecBuilder items(List<KeyToPath> items) {
		this.items = items;
		return this;
	}

	/**
	 * Add one ConfigMap mount detail item.
	 *
	 * @param item {@link KeyToPath} instances representing the ConfigMap mount details that should be added
	 * @return this
	 */
	public ConfigMapVolumeSpecBuilder items(KeyToPath item) {
		if (items == null) {
			items = new ArrayList<>();
		}
		items.add(item);
		return this;
	}

	public ConfigMapVolumeSpec build() {
		ConfigMapVolumeSpec configMapVolumeSpec = new ConfigMapVolumeSpec();
		configMapVolumeSpec.setName(name);
		configMapVolumeSpec.setMountPath(mountPath);
		configMapVolumeSpec.setItems(items);
		return configMapVolumeSpec;
	}
}
