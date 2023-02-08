package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.backup.spec;

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

	public KeycloakAWSSpec build() {
		KeycloakAWSSpec keycloakAWSSpec = new KeycloakAWSSpec();
		keycloakAWSSpec.setEncryptionKeySecretName(encryptionKeySecretName);
		keycloakAWSSpec.setCredentialsSecretName(credentialsSecretName);
		keycloakAWSSpec.setSchedule(schedule);
		return keycloakAWSSpec;
	}
}
