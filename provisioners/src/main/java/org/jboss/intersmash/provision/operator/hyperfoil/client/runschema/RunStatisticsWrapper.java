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
package org.jboss.intersmash.provision.operator.hyperfoil.client.runschema;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import org.jboss.intersmash.provision.operator.hyperfoil.client.runschema.v07.Agent;
import org.jboss.intersmash.provision.operator.hyperfoil.client.runschema.v07.Failure;
import org.jboss.intersmash.provision.operator.hyperfoil.client.runschema.v07.Info;
import org.jboss.intersmash.provision.operator.hyperfoil.client.runschema.v07.PhaseDetail;
import org.jboss.intersmash.provision.operator.hyperfoil.client.runschema.v07.PhaseStats;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Hyperfoil run results
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
		"$schema",
		"info",
		"total",
		"failure",
		"phase",
		"agent"
})
@Generated("jsonschema2pojo")
public class RunStatisticsWrapper {

	private String JSON;
	private ObjectMapper mapper;
	private JsonNode jsonNode;
	private List<Failure> failures;
	private List<PhaseStats> phaseStats;

	public RunStatisticsWrapper(String JSON) throws JsonProcessingException {
		// parse incoming JSON
		this.JSON = JSON;
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonNode = mapper.readTree(this.JSON);

		// extract "info" from incoming JSON
		JsonNode infoNode = jsonNode.at("/info");
		this.info = this.mapper.treeToValue(infoNode, Info.class);

		// extract "failures" from incoming JSON
		failures = new ArrayList<>();
		JsonNode failuresNode = jsonNode.at("/failures");
		for (JsonNode failureNode : failuresNode) {
			Failure failure = mapper.treeToValue(failureNode, Failure.class);
			failures.add(failure);
		}

		// extract "stats" from incoming JSON
		phaseStats = new ArrayList<>();
		JsonNode statsNode = jsonNode.at("/stats");
		for (JsonNode stat : statsNode) {
			PhaseStats phaseStat = mapper.treeToValue(stat.at("/total"), PhaseStats.class);
			phaseStats.add(phaseStat);
		}
	}

	/**
	 * This should point to http://hyperfoil.io/run-schema/0.6
	 *
	 */
	@JsonProperty("$schema")
	@JsonPropertyDescription("This should point to http://hyperfoil.io/run-schema/0.6")
	private String $schema;
	/**
	 * General information about the run.
	 * (Required)
	 *
	 */
	@JsonProperty("info")
	@JsonPropertyDescription("General information about the run.")
	private Info info;
	/**
	 * Aggregated per-phase results.
	 * (Required)
	 *
	 */
	@JsonProperty("total")
	@JsonPropertyDescription("Aggregated per-phase results.")
	private List<PhaseStats> total = null;
	/**
	 * SLA failures encountered during the run.
	 *
	 */
	@JsonProperty("failure")
	@JsonPropertyDescription("SLA failures encountered during the run.")
	private List<Failure> failure = null;
	/**
	 * Detailed data about phases. Phase names are used as keys.
	 * (Required)
	 *
	 */
	@JsonProperty("phase")
	@JsonPropertyDescription("Detailed data about phases. Phase names are used as keys.")
	private PhaseDetail phase;
	/**
	 * Per-agent statistics
	 *
	 */
	@JsonProperty("agent")
	@JsonPropertyDescription("Per-agent statistics")
	private Agent agent;

	/**
	 * This should point to http://hyperfoil.io/run-schema/0.6
	 *
	 */
	@JsonProperty("$schema")
	public String get$schema() {
		return $schema;
	}

	/**
	 * This should point to http://hyperfoil.io/run-schema/0.6
	 *
	 */
	@JsonProperty("$schema")
	public void set$schema(String $schema) {
		this.$schema = $schema;
	}

	/**
	 * General information about the run.
	 * (Required)
	 *
	 */
	@JsonProperty("info")
	public Info getInfo() {
		return info;
	}

	/**
	 * General information about the run.
	 * (Required)
	 *
	 */
	@JsonProperty("info")
	public void setInfo(Info info) {
		this.info = info;
	}

	/**
	 * Aggregated per-phase results.
	 * (Required)
	 *
	 */
	@JsonProperty("total")
	public List<PhaseStats> getTotal() {
		return total;
	}

	/**
	 * Aggregated per-phase results.
	 * (Required)
	 *
	 */
	@JsonProperty("total")
	public void setTotal(List<PhaseStats> total) {
		this.total = total;
	}

	/**
	 * SLA failures encountered during the run.
	 *
	 */
	@JsonProperty("failure")
	public List<Failure> getFailure() {
		return failure;
	}

	/**
	 * SLA failures encountered during the run.
	 *
	 */
	@JsonProperty("failure")
	public void setFailure(List<Failure> failure) {
		this.failure = failure;
	}

	/**
	 * Detailed data about phases. Phase names are used as keys.
	 * (Required)
	 *
	 */
	@JsonProperty("phase")
	public PhaseDetail getPhase() {
		return phase;
	}

	/**
	 * Detailed data about phases. Phase names are used as keys.
	 * (Required)
	 *
	 */
	@JsonProperty("phase")
	public void setPhase(PhaseDetail phase) {
		this.phase = phase;
	}

	/**
	 * Per-agent statistics
	 *
	 */
	@JsonProperty("agent")
	public Agent getAgent() {
		return agent;
	}

	/**
	 * Per-agent statistics
	 *
	 */
	@JsonProperty("agent")
	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public List<PhaseStats> getPhaseStats() {
		return phaseStats;
	}

}
