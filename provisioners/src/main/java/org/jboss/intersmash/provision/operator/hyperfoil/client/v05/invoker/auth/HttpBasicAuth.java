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
package org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.auth;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.ApiException;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.Pair;

import okhttp3.Credentials;

public class HttpBasicAuth implements Authentication {
	private String username;
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void applyToParams(List<Pair> queryParams, Map<String, String> headerParams, Map<String, String> cookieParams,
			String payload, String method, URI uri) throws ApiException {
		if (username == null && password == null) {
			return;
		}
		headerParams.put("Authorization", Credentials.basic(
				username == null ? "" : username,
				password == null ? "" : password));
	}
}
