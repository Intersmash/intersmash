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
package org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.runschema.v07;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
		"phase",
		"metric",
		"start",
		"end",
		"summary",
		"custom"
})
@Generated("jsonschema2pojo")
public class PhaseStats {

	/**
	 * Name of the phase.
	 * (Required)
	 *
	 */
	@JsonProperty("phase")
	@JsonPropertyDescription("Name of the phase.")
	private String phase;
	/**
	 * Name of the metric.
	 * (Required)
	 *
	 */
	@JsonProperty("metric")
	@JsonPropertyDescription("Name of the metric.")
	private String metric;
	/**
	 * Phase start timestamp, in epoch milliseconds.
	 * (Required)
	 *
	 */
	@JsonProperty("start")
	@JsonPropertyDescription("Phase start timestamp, in epoch milliseconds.")
	private Long start;
	/**
	 * Phase completion timestamp, in epoch milliseconds.
	 * (Required)
	 *
	 */
	@JsonProperty("end")
	@JsonPropertyDescription("Phase completion timestamp, in epoch milliseconds.")
	private Long end;
	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("summary")
	private Summary summary;
	/**
	 * Custom statistics.
	 *
	 */
	@JsonProperty("custom")
	@JsonPropertyDescription("Custom statistics.")
	private Custom custom;

	/**
	 * Name of the phase.
	 * (Required)
	 *
	 */
	@JsonProperty("phase")
	public String getPhase() {
		return phase;
	}

	/**
	 * Name of the phase.
	 * (Required)
	 *
	 */
	@JsonProperty("phase")
	public void setPhase(String phase) {
		this.phase = phase;
	}

	/**
	 * Name of the metric.
	 * (Required)
	 *
	 */
	@JsonProperty("metric")
	public String getMetric() {
		return metric;
	}

	/**
	 * Name of the metric.
	 * (Required)
	 *
	 */
	@JsonProperty("metric")
	public void setMetric(String metric) {
		this.metric = metric;
	}

	/**
	 * Phase start timestamp, in epoch milliseconds.
	 * (Required)
	 *
	 */
	@JsonProperty("start")
	public Long getStart() {
		return start;
	}

	/**
	 * Phase start timestamp, in epoch milliseconds.
	 * (Required)
	 *
	 */
	@JsonProperty("start")
	public void setStart(Long start) {
		this.start = start;
	}

	/**
	 * Phase completion timestamp, in epoch milliseconds.
	 * (Required)
	 *
	 */
	@JsonProperty("end")
	public Long getEnd() {
		return end;
	}

	/**
	 * Phase completion timestamp, in epoch milliseconds.
	 * (Required)
	 *
	 */
	@JsonProperty("end")
	public void setEnd(Long end) {
		this.end = end;
	}

	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("summary")
	public Summary getSummary() {
		return summary;
	}

	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("summary")
	public void setSummary(Summary summary) {
		this.summary = summary;
	}

	/**
	 * Custom statistics.
	 *
	 */
	@JsonProperty("custom")
	public Custom getCustom() {
		return custom;
	}

	/**
	 * Custom statistics.
	 *
	 */
	@JsonProperty("custom")
	public void setCustom(Custom custom) {
		this.custom = custom;
	}

}
