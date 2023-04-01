/**
 * Copyright (C) 2023 Red Hat, Inc.
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
