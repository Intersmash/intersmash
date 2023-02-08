package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

public final class EndpointEncryptionBuilder {
	private String type;
	private String certServiceName;
	private String certSecretName;

	/**
	 * Specify the configured endpoint encryption type
	 *
	 * @param type The configured endpoint encryption type
	 * @return this
	 */
	public EndpointEncryptionBuilder type(CertificateSourceType type) {
		this.type = type.getValue();
		return this;
	}

	/**
	 * Names the service certificate CA
	 *
	 * @param certServiceName The service certificate CA name
	 * @return this
	 */
	public EndpointEncryptionBuilder certServiceName(String certServiceName) {
		this.certServiceName = certServiceName;
		return this;
	}

	/**
	 * Names the secret that contains a service certificate
	 *
	 * @param certSecretName The name of the secret that contains a service certificate
	 * @return this
	 */
	public EndpointEncryptionBuilder certSecretName(String certSecretName) {
		this.certSecretName = certSecretName;
		return this;
	}

	public EndpointEncryption build() {
		EndpointEncryption endpointEncryption = new EndpointEncryption();
		endpointEncryption.setType(type);
		endpointEncryption.setCertServiceName(certServiceName);
		endpointEncryption.setCertSecretName(certSecretName);
		return endpointEncryption;
	}

	/**
	 * CertificateSourceType specifies all the possible sources for the encryption certificate
	 */
	public enum CertificateSourceType {
		/**
		 * CertificateSourceTypeNoneNoEncryption no certificate encryption disabled
		 */
		CertificateSourceTypeNoneNoEncryption("None"),
		/**
		 * CertificateSourceTypeService certificate coming from a cluster service
		 */
		CertificateSourceTypeService("Service"),
		/**
		 * CertificateSourceTypeServiceLowCase certificate coming from a cluster service
		 */
		CertificateSourceTypeServiceLowCase("service"),
		/**
		 * CertificateSourceTypeSecret certificate coming from a user provided secret
		 */
		CertificateSourceTypeSecret("Secret"),
		/**
		 * CertificateSourceTypeSecretLowCase certificate coming from a user provided secret
		 */
		CertificateSourceTypeSecretLowCase("secret");

		private String value;

		CertificateSourceType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
