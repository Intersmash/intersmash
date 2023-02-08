package org.jboss.intersmash.tools.provision.openshift.operator.wildfly.spec;

import io.fabric8.kubernetes.api.model.EmptyDirVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;

public final class StorageSpecBuilder {
	private EmptyDirVolumeSource emptyDir;
	private PersistentVolumeClaim volumeClaimTemplate;

	/**
	 * Set the {@link EmptyDirVolumeSource} to be used by the WildFly
	 * {@link io.fabric8.kubernetes.api.model.apps.StatefulSet}
	 *
	 * @param emptyDir {@link EmptyDirVolumeSource} to be used by the WildFly
	 * {@link io.fabric8.kubernetes.api.model.apps.StatefulSet}
	 * @return this
	 */
	public StorageSpecBuilder emptyDir(EmptyDirVolumeSource emptyDir) {
		this.emptyDir = emptyDir;
		return this;
	}

	/**
	 * Set the {@link PersistentVolumeClaim} spec to be used by the WildFly
	 * {@link io.fabric8.kubernetes.api.model.apps.StatefulSet}
	 *
	 * @param volumeClaimTemplate A {@link PersistentVolumeClaim} spec to be used by the WildFly
	 * {@link io.fabric8.kubernetes.api.model.apps.StatefulSet}
	 * @return this
	 */
	public StorageSpecBuilder volumeClaimTemplate(PersistentVolumeClaim volumeClaimTemplate) {
		this.volumeClaimTemplate = volumeClaimTemplate;
		return this;
	}

	public StorageSpec build() {
		StorageSpec storageSpec = new StorageSpec();
		storageSpec.setEmptyDir(emptyDir);
		storageSpec.setVolumeClaimTemplate(volumeClaimTemplate);
		return storageSpec;
	}
}
