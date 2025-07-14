/*
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
package io.hyperfoil.v1alpha2.hyperfoilspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "host", "tls", "type" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Route implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * Host for the route leading to Controller REST endpoint. Example: hyperfoil.apps.cloud.example.com
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("host")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Host for the route leading to Controller REST endpoint. Example: hyperfoil.apps.cloud.example.com")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String host;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Optional for edge and reencrypt routes, required for passthrough; Name of the secret hosting `tls.crt`, `tls.key` and optionally `ca.crt`
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("tls")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Optional for edge and reencrypt routes, required for passthrough; Name of the secret hosting `tls.crt`, `tls.key` and optionally `ca.crt`")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String tls;

	public String getTls() {
		return tls;
	}

	public void setTls(String tls) {
		this.tls = tls;
	}

	/**
	 * Either 'http' (for plain-text routes - not recommended), 'edge', 'reencrypt' or 'passthrough'
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("type")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Either 'http' (for plain-text routes - not recommended), 'edge', 'reencrypt' or 'passthrough'")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
