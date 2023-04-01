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
package org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.model;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.invoker.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.swagger.annotations.ApiModelProperty;

/**
 * Run
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-07-18T14:39:47.341166292+02:00[Europe/Rome]")
public class Run {
	@JsonIgnore
	private static final Logger logger = LoggerFactory.getLogger(Run.class);

	public static final String SERIALIZED_NAME_ID = "id";
	@SerializedName(SERIALIZED_NAME_ID)
	private String id;

	public static final String SERIALIZED_NAME_BENCHMARK = "benchmark";
	@SerializedName(SERIALIZED_NAME_BENCHMARK)
	private String benchmark;

	public static final String SERIALIZED_NAME_STARTED = "started";
	@SerializedName(SERIALIZED_NAME_STARTED)
	private OffsetDateTime started;

	public static final String SERIALIZED_NAME_TERMINATED = "terminated";
	@SerializedName(SERIALIZED_NAME_TERMINATED)
	private OffsetDateTime terminated;

	public static final String SERIALIZED_NAME_CANCELLED = "cancelled";
	@SerializedName(SERIALIZED_NAME_CANCELLED)
	private Boolean cancelled;

	public static final String SERIALIZED_NAME_COMPLETED = "completed";
	@SerializedName(SERIALIZED_NAME_COMPLETED)
	private Boolean completed;

	public static final String SERIALIZED_NAME_DESCRIPTION = "description";
	@SerializedName(SERIALIZED_NAME_DESCRIPTION)
	private String description;

	public static final String SERIALIZED_NAME_PHASES = "phases";
	@SerializedName(SERIALIZED_NAME_PHASES)
	private List<Phase> phases = null;

	public static final String SERIALIZED_NAME_AGENTS = "agents";
	@SerializedName(SERIALIZED_NAME_AGENTS)
	private List<Agent> agents = null;

	public static final String SERIALIZED_NAME_ERRORS = "errors";
	@SerializedName(SERIALIZED_NAME_ERRORS)
	private List<String> errors = null;

	public Run() {
	}

	public Run id(String id) {

		this.id = id;
		return this;
	}

	/**
	* Get id
	* @return id
	**/
	@javax.annotation.Nonnull
	@ApiModelProperty(example = "1234", required = true, value = "")

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Run benchmark(String benchmark) {

		this.benchmark = benchmark;
		return this;
	}

	/**
	* Get benchmark
	* @return benchmark
	**/
	@javax.annotation.Nonnull
	@ApiModelProperty(example = "my-benchmark", required = true, value = "")

	public String getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(String benchmark) {
		this.benchmark = benchmark;
	}

	public Run started(OffsetDateTime started) {

		this.started = started;
		return this;
	}

	/**
	* Get started
	* @return started
	**/
	@javax.annotation.Nonnull
	@ApiModelProperty(required = true, value = "")

	public OffsetDateTime getStarted() {
		return started;
	}

	public void setStarted(OffsetDateTime started) {
		this.started = started;
	}

	public Run terminated(OffsetDateTime terminated) {

		this.terminated = terminated;
		return this;
	}

	/**
	* Get terminated
	* @return terminated
	**/
	@javax.annotation.Nonnull
	@ApiModelProperty(required = true, value = "")

	public OffsetDateTime getTerminated() {
		return terminated;
	}

	public void setTerminated(OffsetDateTime terminated) {
		this.terminated = terminated;
	}

	public Run cancelled(Boolean cancelled) {

		this.cancelled = cancelled;
		return this;
	}

	/**
	* Get cancelled
	* @return cancelled
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public Boolean getCancelled() {
		return cancelled;
	}

	public void setCancelled(Boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Run completed(Boolean completed) {

		this.completed = completed;
		return this;
	}

	/**
	* Get completed
	* @return completed
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public Boolean getCompleted() {
		return completed;
	}

	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}

	public Run description(String description) {

		this.description = description;
		return this;
	}

	/**
	* Get description
	* @return description
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Run phases(List<Phase> phases) {

		this.phases = phases;
		return this;
	}

	public Run addPhasesItem(Phase phasesItem) {
		if (this.phases == null) {
			this.phases = new ArrayList<>();
		}
		this.phases.add(phasesItem);
		return this;
	}

	/**
	* Get phases
	* @return phases
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public List<Phase> getPhases() {
		return phases;
	}

	public void setPhases(List<Phase> phases) {
		this.phases = phases;
	}

	public Run agents(List<Agent> agents) {

		this.agents = agents;
		return this;
	}

	public Run addAgentsItem(Agent agentsItem) {
		if (this.agents == null) {
			this.agents = new ArrayList<>();
		}
		this.agents.add(agentsItem);
		return this;
	}

	/**
	* Get agents
	* @return agents
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public List<Agent> getAgents() {
		return agents;
	}

	public void setAgents(List<Agent> agents) {
		this.agents = agents;
	}

	public Run errors(List<String> errors) {

		this.errors = errors;
		return this;
	}

	public Run addErrorsItem(String errorsItem) {
		if (this.errors == null) {
			this.errors = new ArrayList<>();
		}
		this.errors.add(errorsItem);
		return this;
	}

	/**
	* Get errors
	* @return errors
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Run run = (Run) o;
		return Objects.equals(this.id, run.id) &&
				Objects.equals(this.benchmark, run.benchmark) &&
				Objects.equals(this.started, run.started) &&
				Objects.equals(this.terminated, run.terminated) &&
				Objects.equals(this.cancelled, run.cancelled) &&
				Objects.equals(this.completed, run.completed) &&
				Objects.equals(this.description, run.description) &&
				Objects.equals(this.phases, run.phases) &&
				Objects.equals(this.agents, run.agents) &&
				Objects.equals(this.errors, run.errors);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, benchmark, started, terminated, cancelled, completed, description, phases, agents, errors);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Run {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    benchmark: ").append(toIndentedString(benchmark)).append("\n");
		sb.append("    started: ").append(toIndentedString(started)).append("\n");
		sb.append("    terminated: ").append(toIndentedString(terminated)).append("\n");
		sb.append("    cancelled: ").append(toIndentedString(cancelled)).append("\n");
		sb.append("    completed: ").append(toIndentedString(completed)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
		sb.append("    phases: ").append(toIndentedString(phases)).append("\n");
		sb.append("    agents: ").append(toIndentedString(agents)).append("\n");
		sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

	public static HashSet<String> openapiFields;
	public static HashSet<String> openapiRequiredFields;

	static {
		// a set of all properties/fields (JSON key names)
		openapiFields = new HashSet<String>();
		openapiFields.add("id");
		openapiFields.add("benchmark");
		openapiFields.add("started");
		openapiFields.add("terminated");
		openapiFields.add("cancelled");
		openapiFields.add("completed");
		openapiFields.add("description");
		openapiFields.add("phases");
		openapiFields.add("agents");
		openapiFields.add("errors");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
		openapiRequiredFields.add("id");
		openapiRequiredFields.add("benchmark");
		openapiRequiredFields.add("started");
		openapiRequiredFields.add("terminated");
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 *
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to Run
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (Run.openapiRequiredFields.isEmpty()) {
				return;
			} else { // has required fields
				throw new IllegalArgumentException(
						String.format("The required field(s) %s in Run is not found in the empty JSON string",
								Run.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!Run.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(
						String.format("The field `%s` in the JSON string is not defined in the `Run` properties. JSON: %s",
								entry.getKey(), jsonObj.toString()));
			}
		}

		// check to make sure all required properties/fields are present in the JSON string
		for (String requiredField : Run.openapiRequiredFields) {
			if (jsonObj.get(requiredField) == null) {
				throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s",
						requiredField, jsonObj.toString()));
			}
		}
		if (jsonObj.get("id") != null && !jsonObj.get("id").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `id` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("id").toString()));
		}
		if (jsonObj.get("benchmark") != null && !jsonObj.get("benchmark").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `benchmark` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("benchmark").toString()));
		}
		if (jsonObj.get("description") != null && !jsonObj.get("description").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `description` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("description").toString()));
		}
		JsonElement jsonArrayphases = jsonObj.get("phases");
		if (jsonArrayphases != null && !(jsonArrayphases instanceof JsonNull)) {
			// ensure the json data is an array
			if (!jsonObj.get("phases").isJsonArray()) {
				throw new IllegalArgumentException(
						String.format("Expected the field `phases` to be an array in the JSON string but got `%s`",
								jsonObj.get("phases").toString()));
			}

			// validate the optional field `phases` (array)
			for (int i = 0; i < JsonArray.class.cast(jsonArrayphases).size(); i++) {
				Phase.validateJsonObject(JsonArray.class.cast(jsonArrayphases).get(i).getAsJsonObject());
			}
			;
		}
		JsonElement jsonArrayagents = jsonObj.get("agents");
		if (jsonArrayagents != null && !(jsonArrayagents instanceof JsonNull)) {
			// ensure the json data is an array
			if (!jsonObj.get("agents").isJsonArray()) {
				throw new IllegalArgumentException(
						String.format("Expected the field `agents` to be an array in the JSON string but got `%s`",
								jsonObj.get("agents").toString()));
			}

			// validate the optional field `agents` (array)
			for (int i = 0; i < JsonArray.class.cast(jsonArrayagents).size(); i++) {
				Agent.validateJsonObject(JsonArray.class.cast(jsonArrayagents).get(i).getAsJsonObject());
			}
			;
		}
		// ensure the json data is an array
		if (jsonObj.get("errors") != null && !jsonObj.get("errors").isJsonArray()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `errors` to be an array in the JSON string but got `%s`",
							jsonObj.get("errors").toString()));
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!Run.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes 'Run' and its subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<Run> thisAdapter = gson.getDelegateAdapter(this, TypeToken.get(Run.class));

			return (TypeAdapter<T>) new TypeAdapter<Run>() {
				@Override
				public void write(JsonWriter out, Run value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public Run read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}
	}

	/**
	 * Create an instance of Run given an JSON string
	 *
	 * @param jsonString JSON string
	 * @return An instance of Run
	 * @throws IOException if the JSON string is invalid with respect to Run
	 */
	public static Run fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, Run.class);
	}

	/**
	 * Convert an instance of Run to an JSON string
	 *
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}
}
