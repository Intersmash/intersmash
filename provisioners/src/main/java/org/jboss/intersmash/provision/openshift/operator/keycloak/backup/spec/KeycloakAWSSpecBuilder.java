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
package org.jboss.intersmash.provision.openshift.operator.keycloak.backup.spec;

import org.keycloak.v1alpha1.keycloakbackupspec.Aws;

/**
 * If provided, an automatic database backup will be created on AWS S3 instead of
 * a local Persistent Volume. If this property is not provided - a local
 * Persistent Volume backup will be chosen.
 */
public final class KeycloakAWSSpecBuilder {
	private String encryptionKeySecretName;
	private String credentialsSecretName;
	private String schedule;

	/**
	 * If provided, the database backup will be encrypted.
	 * Provides a secret name used for encrypting database data.
	 * The secret needs to be in the following form:
	 *
	 *     apiVersion: v1
	 *     kind: Secret
	 *     metadata:
	 *       name: &lt;Secret name&gt;
	 *     type: Opaque
	 *     stringData:
	 *       GPG_PUBLIC_KEY: &lt;GPG Public Key&gt;
	 *       GPG_TRUST_MODEL: &lt;GPG Trust Model&gt;
	 *       GPG_RECIPIENT: &lt;GPG Recipient&gt;
	 *
	 * For more information, please refer to the Operator documentation.
	 *
	 * @param encryptionKeySecretName A secret name used for encrypting database data
	 * @return this
	 */
	public KeycloakAWSSpecBuilder encryptionKeySecretName(String encryptionKeySecretName) {
		this.encryptionKeySecretName = encryptionKeySecretName;
		return this;
	}

	/**
	 * Provides a secret name used for connecting to AWS S3 Service.
	 * The secret needs to be in the following form:
	 *
	 *     apiVersion: v1
	 *     kind: Secret
	 *     metadata:
	 *       name: &lt;Secret name&gt;
	 *     type: Opaque
	 *     stringData:
	 *       AWS_S3_BUCKET_NAME: &lt;S3 Bucket Name&gt;
	 *       AWS_ACCESS_KEY_ID: &lt;AWS Access Key ID&gt;
	 *       AWS_SECRET_ACCESS_KEY: &lt;AWS Secret Key&gt;
	 *
	 * For more information, please refer to the Operator documentation.
	 *
	 * @param credentialsSecretName A secret name used for connecting to AWS S3 Service.
	 * @return this
	 */
	public KeycloakAWSSpecBuilder credentialsSecretName(String credentialsSecretName) {
		this.credentialsSecretName = credentialsSecretName;
		return this;
	}

	/**
	 * If specified, it will be used as a schedule for creating a CronJob.
	 *
	 * @param schedule Expression that will be used as a schedule for creating a CronJob.
	 * @return this
	 */
	public KeycloakAWSSpecBuilder schedule(String schedule) {
		this.schedule = schedule;
		return this;
	}

	public Aws build() {
		Aws keycloakAWSSpec = new Aws();
		keycloakAWSSpec.setEncryptionKeySecretName(encryptionKeySecretName);
		keycloakAWSSpec.setCredentialsSecretName(credentialsSecretName);
		keycloakAWSSpec.setSchedule(schedule);
		return keycloakAWSSpec;
	}
}
