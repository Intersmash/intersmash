/*
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
package org.jboss.intersmash.provision.operator.model.keycloak.backup;

import java.util.Map;

import org.keycloak.v1alpha1.KeycloakBackup;
import org.keycloak.v1alpha1.KeycloakBackupSpec;
import org.keycloak.v1alpha1.keycloakbackupspec.Aws;

import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class KeycloakBackupBuilder {
	private String name;
	private Map<String, String> labels;
	private boolean restore;
	private Aws aws;
	//	private LabelSelector instanceSelector;
	//	private String storageClassName;

	/**
	 * Initialize the {@link KeycloakBackupBuilder} with given resource name.
	 *
	 * @param name resource object name
	 */
	public KeycloakBackupBuilder(String name) {
		this.name = name;
	}

	/**
	 * Initialize the {@link KeycloakBackupBuilder} with given resource name and labels.
	 *
	 * @param name resource object name
	 * @param labels key/value pairs that are attached to objects
	 */
	public KeycloakBackupBuilder(String name, Map<String, String> labels) {
		this.name = name;
		this.labels = labels;
	}

	/**
	 * Controls automatic restore behavior.
	 * Currently not implemented.
	 *
	 * In the future this will be used to trigger automatic restore for a given KeycloakBackup.
	 * Each backup will correspond to a single snapshot of the database (stored either in a
	 * Persistent Volume or AWS). If a user wants to restore it, all he/she needs to do is to
	 * change this flag to true.
	 * Potentially, it will be possible to restore a single backup multiple times.
	 *
	 * @param restore Whether automatic restore for a given {@link KeycloakBackup} should be triggered
	 * @return this
	 */
	public KeycloakBackupBuilder restore(boolean restore) {
		this.restore = restore;
		return this;
	}

	/**
	 * If provided, an automatic database backup will be created on AWS S3 instead of
	 * a local Persistent Volume. If this property is not provided - a local
	 * Persistent Volume backup will be chosen.
	 *
	 * @param aws A {@link Aws} instance storing configuration for creating a backup on AWS
	 * @return this
	 */
	public KeycloakBackupBuilder aws(Aws aws) {
		this.aws = aws;
		return this;
	}

	//	/**
	//	 * Selector for looking up Keycloak Custom Resources.
	//	 */
	//	public KeycloakBackupBuilder instanceSelector(LabelSelector instanceSelector) {
	//		this.instanceSelector = instanceSelector;
	//		return this;
	//	}
	//
	//	/**
	//	 * Name of the StorageClass for Postgresql Backup Persistent Volume Claim.
	//	 */
	//	public KeycloakBackupBuilder storageClassName(String storageClassName) {
	//		this.storageClassName = storageClassName;
	//		return this;
	//	}

	public KeycloakBackup build() {
		KeycloakBackup keycloakBackup = new KeycloakBackup();
		keycloakBackup.setMetadata(new ObjectMeta());
		keycloakBackup.getMetadata().setName(name);
		keycloakBackup.getMetadata().setLabels(labels);

		KeycloakBackupSpec keycloakBackupSpec = new KeycloakBackupSpec();
		keycloakBackupSpec.setRestore(restore);
		keycloakBackupSpec.setAws(aws);
		//		keycloakBackupSpec.setInstanceSelector(instanceSelector);
		//		keycloakBackupSpec.setStorageClassName(storageClassName);
		keycloakBackup.setSpec(keycloakBackupSpec);
		return keycloakBackup;
	}
}
