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
