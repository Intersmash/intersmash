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
package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.spec;

import org.infinispan.v2alpha1.cachespec.AdminAuth;
import org.infinispan.v2alpha1.cachespec.adminauth.Password;
import org.infinispan.v2alpha1.cachespec.adminauth.Username;

public final class AdminAuthBuilder {
	private String secretName;
	private Username username;
	private Password password;

	/**
	 * Set the name of the secret containing both admin username and password
	 *
	 * @param secretName Name of the secret containing both admin username and password
	 * @return this
	 */
	public AdminAuthBuilder secretName(String secretName) {
		this.secretName = secretName;
		return this;
	}

	/**
	 * Set the secret and key containing the admin username for authentication.
	 *
	 * @param username Secret and key containing the admin username for authentication
	 * @return this
	 */
	public AdminAuthBuilder username(Username username) {
		this.username = username;
		return this;
	}

	/**
	 * Set the secret and key containing the admin password for authentication.
	 *
	 * @param password Secret and key containing the admin password for authentication
	 * @return this
	 */
	public AdminAuthBuilder password(Password password) {
		this.password = password;
		return this;
	}

	public AdminAuth build() {
		AdminAuth adminAuth = new AdminAuth();
		adminAuth.setSecretName(secretName);
		adminAuth.setUsername(username);
		adminAuth.setPassword(password);
		return adminAuth;
	}
}
