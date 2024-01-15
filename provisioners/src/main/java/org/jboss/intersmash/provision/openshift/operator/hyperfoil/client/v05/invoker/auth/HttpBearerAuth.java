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
package org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.invoker.auth;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.invoker.ApiException;
import org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.invoker.Pair;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-07-18T14:39:47.341166292+02:00[Europe/Rome]")
public class HttpBearerAuth implements Authentication {
	private final String scheme;
	private String bearerToken;

	public HttpBearerAuth(String scheme) {
		this.scheme = scheme;
	}

	/**
	 * Gets the token, which together with the scheme, will be sent as the value of the Authorization header.
	 *
	 * @return The bearer token
	 */
	public String getBearerToken() {
		return bearerToken;
	}

	/**
	 * Sets the token, which together with the scheme, will be sent as the value of the Authorization header.
	 *
	 * @param bearerToken The bearer token to send in the Authorization header
	 */
	public void setBearerToken(String bearerToken) {
		this.bearerToken = bearerToken;
	}

	@Override
	public void applyToParams(List<Pair> queryParams, Map<String, String> headerParams, Map<String, String> cookieParams,
			String payload, String method, URI uri) throws ApiException {
		if (bearerToken == null) {
			return;
		}

		headerParams.put("Authorization", (scheme != null ? upperCaseBearer(scheme) + " " : "") + bearerToken);
	}

	private static String upperCaseBearer(String scheme) {
		return ("bearer".equalsIgnoreCase(scheme)) ? "Bearer" : scheme;
	}
}
