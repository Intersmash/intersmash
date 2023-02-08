package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

public final class InfinispanSecurityBuilder {
	private String endpointSecretName;
	private EndpointEncryption endpointEncryption;

	/**
	 * Specify the name of the authentication secret that contains credentials
	 *
	 * @param endpointSecretName Name of the authentication secret that contains credentials
	 * @return this
	 */
	public InfinispanSecurityBuilder endpointSecretName(String endpointSecretName) {
		this.endpointSecretName = endpointSecretName;
		return this;
	}

	/**
	 * Specify how traffic to and from Data Grid endpoints is encrypted
	 *
	 * @param endpointEncryption {@link EndpointEncryption} instance storing configuration for the endpoint encryption
	 * @return this
	 */
	public InfinispanSecurityBuilder endpointEncryption(EndpointEncryption endpointEncryption) {
		this.endpointEncryption = endpointEncryption;
		return this;
	}

	public InfinispanSecurity build() {
		InfinispanSecurity infinispanSecurity = new InfinispanSecurity();
		infinispanSecurity.setEndpointSecretName(endpointSecretName);
		infinispanSecurity.setEndpointEncryption(endpointEncryption);
		return infinispanSecurity;
	}
}
