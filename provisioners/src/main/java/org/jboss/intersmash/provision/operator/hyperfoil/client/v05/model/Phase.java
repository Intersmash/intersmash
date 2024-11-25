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
package org.jboss.intersmash.provision.operator.hyperfoil.client.v05.model;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.JSON;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.swagger.annotations.ApiModelProperty;

/**
 * Phase
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-07-18T14:39:47.341166292+02:00[Europe/Rome]")
public class Phase {
	public static final String SERIALIZED_NAME_NAME = "name";
	@SerializedName(SERIALIZED_NAME_NAME)
	private String name;

	public static final String SERIALIZED_NAME_STATUS = "status";
	@SerializedName(SERIALIZED_NAME_STATUS)
	private String status;

	public static final String SERIALIZED_NAME_TYPE = "type";
	@SerializedName(SERIALIZED_NAME_TYPE)
	private String type;

	public static final String SERIALIZED_NAME_STARTED = "started";
	@SerializedName(SERIALIZED_NAME_STARTED)
	private OffsetDateTime started;

	public static final String SERIALIZED_NAME_REMAINING = "remaining";
	@SerializedName(SERIALIZED_NAME_REMAINING)
	private String remaining;

	public static final String SERIALIZED_NAME_COMPLETED = "completed";
	@SerializedName(SERIALIZED_NAME_COMPLETED)
	private OffsetDateTime completed;

	public static final String SERIALIZED_NAME_FAILED = "failed";
	@SerializedName(SERIALIZED_NAME_FAILED)
	private Boolean failed;

	public static final String SERIALIZED_NAME_TOTAL_DURATION = "totalDuration";
	@SerializedName(SERIALIZED_NAME_TOTAL_DURATION)
	private String totalDuration;

	public static final String SERIALIZED_NAME_DESCRIPTION = "description";
	@SerializedName(SERIALIZED_NAME_DESCRIPTION)
	private String description;

	public Phase() {
	}

	public Phase name(String name) {

		this.name = name;
		return this;
	}

	/**
	* Get name
	* @return name
	**/
	@javax.annotation.Nonnull
	@ApiModelProperty(required = true, value = "")

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Phase status(String status) {

		this.status = status;
		return this;
	}

	/**
	* Get status
	* @return status
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Phase type(String type) {

		this.type = type;
		return this;
	}

	/**
	* Get type
	* @return type
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Phase started(OffsetDateTime started) {

		this.started = started;
		return this;
	}

	/**
	* Get started
	* @return started
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public OffsetDateTime getStarted() {
		return started;
	}

	public void setStarted(OffsetDateTime started) {
		this.started = started;
	}

	public Phase remaining(String remaining) {

		this.remaining = remaining;
		return this;
	}

	/**
	* Get remaining
	* @return remaining
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public String getRemaining() {
		return remaining;
	}

	public void setRemaining(String remaining) {
		this.remaining = remaining;
	}

	public Phase completed(OffsetDateTime completed) {

		this.completed = completed;
		return this;
	}

	/**
	* Get completed
	* @return completed
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public OffsetDateTime getCompleted() {
		return completed;
	}

	public void setCompleted(OffsetDateTime completed) {
		this.completed = completed;
	}

	public Phase failed(Boolean failed) {

		this.failed = failed;
		return this;
	}

	/**
	* Get failed
	* @return failed
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public Boolean getFailed() {
		return failed;
	}

	public void setFailed(Boolean failed) {
		this.failed = failed;
	}

	public Phase totalDuration(String totalDuration) {

		this.totalDuration = totalDuration;
		return this;
	}

	/**
	* Get totalDuration
	* @return totalDuration
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public String getTotalDuration() {
		return totalDuration;
	}

	public void setTotalDuration(String totalDuration) {
		this.totalDuration = totalDuration;
	}

	public Phase description(String description) {

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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Phase phase = (Phase) o;
		return Objects.equals(this.name, phase.name) &&
				Objects.equals(this.status, phase.status) &&
				Objects.equals(this.type, phase.type) &&
				Objects.equals(this.started, phase.started) &&
				Objects.equals(this.remaining, phase.remaining) &&
				Objects.equals(this.completed, phase.completed) &&
				Objects.equals(this.failed, phase.failed) &&
				Objects.equals(this.totalDuration, phase.totalDuration) &&
				Objects.equals(this.description, phase.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, status, type, started, remaining, completed, failed, totalDuration, description);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Phase {\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    type: ").append(toIndentedString(type)).append("\n");
		sb.append("    started: ").append(toIndentedString(started)).append("\n");
		sb.append("    remaining: ").append(toIndentedString(remaining)).append("\n");
		sb.append("    completed: ").append(toIndentedString(completed)).append("\n");
		sb.append("    failed: ").append(toIndentedString(failed)).append("\n");
		sb.append("    totalDuration: ").append(toIndentedString(totalDuration)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
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
		openapiFields.add("name");
		openapiFields.add("status");
		openapiFields.add("type");
		openapiFields.add("started");
		openapiFields.add("remaining");
		openapiFields.add("completed");
		openapiFields.add("failed");
		openapiFields.add("totalDuration");
		openapiFields.add("description");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
		openapiRequiredFields.add("name");
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 *
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to Phase
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (Phase.openapiRequiredFields.isEmpty()) {
				return;
			} else { // has required fields
				throw new IllegalArgumentException(
						String.format("The required field(s) %s in Phase is not found in the empty JSON string",
								Phase.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!Phase.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(
						String.format("The field `%s` in the JSON string is not defined in the `Phase` properties. JSON: %s",
								entry.getKey(), jsonObj.toString()));
			}
		}

		// check to make sure all required properties/fields are present in the JSON string
		for (String requiredField : Phase.openapiRequiredFields) {
			if (jsonObj.get(requiredField) == null) {
				throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s",
						requiredField, jsonObj.toString()));
			}
		}
		if (jsonObj.get("name") != null && !jsonObj.get("name").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `name` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("name").toString()));
		}
		if (jsonObj.get("status") != null && !jsonObj.get("status").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `status` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("status").toString()));
		}
		if (jsonObj.get("type") != null && !jsonObj.get("type").isJsonNull() && !jsonObj.get("type").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `type` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("type").toString()));
		}
		if (jsonObj.get("remaining") != null && !jsonObj.get("remaining").isJsonNull()
				&& !jsonObj.get("remaining").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `remaining` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("remaining").toString()));
		}
		if (jsonObj.get("totalDuration") != null && !jsonObj.get("totalDuration").isJsonNull()
				&& !jsonObj.get("totalDuration").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `totalDuration` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("totalDuration").toString()));
		}
		if (jsonObj.get("description") != null && !jsonObj.get("description").isJsonNull()
				&& !jsonObj.get("description").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `description` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("description").toString()));
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!Phase.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes 'Phase' and its subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<Phase> thisAdapter = gson.getDelegateAdapter(this, TypeToken.get(Phase.class));

			return (TypeAdapter<T>) new TypeAdapter<Phase>() {
				@Override
				public void write(JsonWriter out, Phase value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public Phase read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}
	}

	/**
	 * Create an instance of Phase given an JSON string
	 *
	 * @param jsonString JSON string
	 * @return An instance of Phase
	 * @throws IOException if the JSON string is invalid with respect to Phase
	 */
	public static Phase fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, Phase.class);
	}

	/**
	 * Convert an instance of Phase to an JSON string
	 *
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}
}
