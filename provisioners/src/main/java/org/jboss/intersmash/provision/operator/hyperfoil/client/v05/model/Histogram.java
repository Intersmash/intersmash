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
package org.jboss.intersmash.provision.operator.hyperfoil.client.v05.model;

import java.io.IOException;
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
 * Histogram
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-07-18T14:39:47.341166292+02:00[Europe/Rome]")
public class Histogram {
	public static final String SERIALIZED_NAME_PHASE = "phase";
	@SerializedName(SERIALIZED_NAME_PHASE)
	private String phase;

	public static final String SERIALIZED_NAME_METRIC = "metric";
	@SerializedName(SERIALIZED_NAME_METRIC)
	private String metric;

	public static final String SERIALIZED_NAME_START_TIME = "startTime";
	@SerializedName(SERIALIZED_NAME_START_TIME)
	private Integer startTime;

	public static final String SERIALIZED_NAME_END_TIME = "endTime";
	@SerializedName(SERIALIZED_NAME_END_TIME)
	private Integer endTime;

	public static final String SERIALIZED_NAME_DATA = "data";
	@SerializedName(SERIALIZED_NAME_DATA)
	private String data;

	public Histogram() {
	}

	public Histogram phase(String phase) {

		this.phase = phase;
		return this;
	}

	/**
	* Get phase
	* @return phase
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public Histogram metric(String metric) {

		this.metric = metric;
		return this;
	}

	/**
	* Get metric
	* @return metric
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public Histogram startTime(Integer startTime) {

		this.startTime = startTime;
		return this;
	}

	/**
	* Get startTime
	* @return startTime
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public Integer getStartTime() {
		return startTime;
	}

	public void setStartTime(Integer startTime) {
		this.startTime = startTime;
	}

	public Histogram endTime(Integer endTime) {

		this.endTime = endTime;
		return this;
	}

	/**
	* Get endTime
	* @return endTime
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public Integer getEndTime() {
		return endTime;
	}

	public void setEndTime(Integer endTime) {
		this.endTime = endTime;
	}

	public Histogram data(String data) {

		this.data = data;
		return this;
	}

	/**
	* Get data
	* @return data
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Histogram histogram = (Histogram) o;
		return Objects.equals(this.phase, histogram.phase) &&
				Objects.equals(this.metric, histogram.metric) &&
				Objects.equals(this.startTime, histogram.startTime) &&
				Objects.equals(this.endTime, histogram.endTime) &&
				Objects.equals(this.data, histogram.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(phase, metric, startTime, endTime, data);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Histogram {\n");
		sb.append("    phase: ").append(toIndentedString(phase)).append("\n");
		sb.append("    metric: ").append(toIndentedString(metric)).append("\n");
		sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
		sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
		sb.append("    data: ").append(toIndentedString(data)).append("\n");
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
		openapiFields.add("phase");
		openapiFields.add("metric");
		openapiFields.add("startTime");
		openapiFields.add("endTime");
		openapiFields.add("data");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 *
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to Histogram
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (Histogram.openapiRequiredFields.isEmpty()) {
				return;
			} else { // has required fields
				throw new IllegalArgumentException(
						String.format("The required field(s) %s in Histogram is not found in the empty JSON string",
								Histogram.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!Histogram.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(String.format(
						"The field `%s` in the JSON string is not defined in the `Histogram` properties. JSON: %s",
						entry.getKey(), jsonObj.toString()));
			}
		}
		if (jsonObj.get("phase") != null && !jsonObj.get("phase").isJsonNull() && !jsonObj.get("phase").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `phase` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("phase").toString()));
		}
		if (jsonObj.get("metric") != null && !jsonObj.get("metric").isJsonNull() && !jsonObj.get("metric").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `metric` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("metric").toString()));
		}
		if (jsonObj.get("data") != null && !jsonObj.get("data").isJsonNull() && !jsonObj.get("data").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `data` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("data").toString()));
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!Histogram.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes 'Histogram' and its subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<Histogram> thisAdapter = gson.getDelegateAdapter(this, TypeToken.get(Histogram.class));

			return (TypeAdapter<T>) new TypeAdapter<Histogram>() {
				@Override
				public void write(JsonWriter out, Histogram value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public Histogram read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}
	}

	/**
	 * Create an instance of Histogram given an JSON string
	 *
	 * @param jsonString JSON string
	 * @return An instance of Histogram
	 * @throws IOException if the JSON string is invalid with respect to Histogram
	 */
	public static Histogram fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, Histogram.class);
	}

	/**
	 * Convert an instance of Histogram to an JSON string
	 *
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}
}
