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
package io.hyperfoil.v1alpha2;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "lastUpdate", "reason", "status" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class HyperfoilStatus implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * RFC 3339 date and time of the last update.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("lastUpdate")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("RFC 3339 date and time of the last update.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String lastUpdate;

	public String getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/**
	 * Human readable explanation for the status.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("reason")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Human readable explanation for the status.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String reason;

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * "One of: 'Ready', 'Pending' or 'Error'"
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("status")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("\"One of: 'Ready', 'Pending' or 'Error'\"")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
