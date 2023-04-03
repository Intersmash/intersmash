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
package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.backup.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakBackupSpec {
	/**
	 * Controls automatic restore behavior.
	 * Currently not implemented.
	 *
	 * In the future this will be used to trigger automatic restore for a given KeycloakBackup.
	 * Each backup will correspond to a single snapshot of the database (stored either in a
	 * Persistent Volume or AWS). If a user wants to restore it, all he/she needs to do is to
	 * change this flag to true.
	 * Potentially, it will be possible to restore a single backup multiple times.
	 */
	private boolean restore;

	/**
	 * If provided, an automatic database backup will be created on AWS S3 instead of
	 * a local Persistent Volume. If this property is not provided - a local
	 * Persistent Volume backup will be chosen.
	 */
	private KeycloakAWSSpec aws;

	//	/**
	//	 * Selector for looking up Keycloak Custom Resources.
	//	 */
	//	private LabelSelector instanceSelector;

	//	/**
	//	 * Name of the StorageClass for Postgresql Backup Persistent Volume Claim.
	//	 */
	//	private String storageClassName;
}
