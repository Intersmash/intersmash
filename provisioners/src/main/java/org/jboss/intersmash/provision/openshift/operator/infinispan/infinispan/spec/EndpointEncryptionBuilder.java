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
package org.jboss.intersmash.provision.openshift.operator.infinispan.infinispan.spec;

import org.infinispan.v1.infinispanspec.security.EndpointEncryption;

public final class EndpointEncryptionBuilder {
	private EndpointEncryption.Type type;
	private String certServiceName;
	private String certSecretName;

	/**
	 * Specify the configured endpoint encryption type
	 *
	 * @param type The configured endpoint encryption type
	 * @return this
	 */
	public EndpointEncryptionBuilder type(EndpointEncryption.Type type) {
		this.type = type;
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
}
