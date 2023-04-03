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

/**
 * If provided, an automatic database backup will be created on AWS S3 instead of
 * a local Persistent Volume. If this property is not provided - a local
 * Persistent Volume backup will be chosen.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakAWSSpec {
	/**
	 * If provided, the database backup will be encrypted.
	 * Provides a secret name used for encrypting database data.
	 * The secret needs to be in the following form:
	 *
	 *     apiVersion: v1
	 *     kind: Secret
	 *     metadata:
	 *       name: <Secret name>
	 *     type: Opaque
	 *     stringData:
	 *       GPG_PUBLIC_KEY: <GPG Public Key>
	 *       GPG_TRUST_MODEL: <GPG Trust Model>
	 *       GPG_RECIPIENT: <GPG Recipient>
	 *
	 * For more information, please refer to the Operator documentation.
	 */
	private String encryptionKeySecretName;

	/**
	 * Provides a secret name used for connecting to AWS S3 Service.
	 * The secret needs to be in the following form:
	 *
	 *     apiVersion: v1
	 *     kind: Secret
	 *     metadata:
	 *       name: <Secret name>
	 *     type: Opaque
	 *     stringData:
	 *       AWS_S3_BUCKET_NAME: <S3 Bucket Name>
	 *       AWS_ACCESS_KEY_ID: <AWS Access Key ID>
	 *       AWS_SECRET_ACCESS_KEY: <AWS Secret Key>
	 *
	 * For more information, please refer to the Operator documentation.
	 */
	private String credentialsSecretName;

	/**
	 * If specified, it will be used as a schedule for creating a CronJob.
	 */
	private String schedule;
}
