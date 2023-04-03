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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.invoker.JSON;

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
 * RequestStats
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-07-18T14:39:47.341166292+02:00[Europe/Rome]")
public class RequestStats {
	public static final String SERIALIZED_NAME_PHASE = "phase";
	@SerializedName(SERIALIZED_NAME_PHASE)
	private String phase;

	public static final String SERIALIZED_NAME_STEP_ID = "stepId";
	@SerializedName(SERIALIZED_NAME_STEP_ID)
	private Integer stepId = 0;

	public static final String SERIALIZED_NAME_METRIC = "metric";
	@SerializedName(SERIALIZED_NAME_METRIC)
	private String metric;

	public static final String SERIALIZED_NAME_SUMMARY = "summary";
	@SerializedName(SERIALIZED_NAME_SUMMARY)
	private Object summary;

	public static final String SERIALIZED_NAME_FAILED_S_L_AS = "failedSLAs";
	@SerializedName(SERIALIZED_NAME_FAILED_S_L_AS)
	private List<String> failedSLAs = null;

	public static final String SERIALIZED_NAME_IS_WARMUP = "isWarmup";
	@SerializedName(SERIALIZED_NAME_IS_WARMUP)
	private Boolean isWarmup;

	public RequestStats() {
	}

	public RequestStats phase(String phase) {

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

	public RequestStats stepId(Integer stepId) {

		this.stepId = stepId;
		return this;
	}

	/**
	* Get stepId
	* @return stepId
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public Integer getStepId() {
		return stepId;
	}

	public void setStepId(Integer stepId) {
		this.stepId = stepId;
	}

	public RequestStats metric(String metric) {

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

	public RequestStats summary(Object summary) {

		this.summary = summary;
		return this;
	}

	/**
	* Get summary
	* @return summary
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public Object getSummary() {
		return summary;
	}

	public void setSummary(Object summary) {
		this.summary = summary;
	}

	public RequestStats failedSLAs(List<String> failedSLAs) {

		this.failedSLAs = failedSLAs;
		return this;
	}

	public RequestStats addFailedSLAsItem(String failedSLAsItem) {
		if (this.failedSLAs == null) {
			this.failedSLAs = new ArrayList<>();
		}
		this.failedSLAs.add(failedSLAsItem);
		return this;
	}

	/**
	* Get failedSLAs
	* @return failedSLAs
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public List<String> getFailedSLAs() {
		return failedSLAs;
	}

	public void setFailedSLAs(List<String> failedSLAs) {
		this.failedSLAs = failedSLAs;
	}

	public RequestStats isWarmup(Boolean isWarmup) {

		this.isWarmup = isWarmup;
		return this;
	}

	/**
	* Get isWarmup
	* @return isWarmup
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public Boolean getIsWarmup() {
		return isWarmup;
	}

	public void setIsWarmup(Boolean isWarmup) {
		this.isWarmup = isWarmup;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RequestStats requestStats = (RequestStats) o;
		return Objects.equals(this.phase, requestStats.phase) &&
				Objects.equals(this.stepId, requestStats.stepId) &&
				Objects.equals(this.metric, requestStats.metric) &&
				Objects.equals(this.summary, requestStats.summary) &&
				Objects.equals(this.failedSLAs, requestStats.failedSLAs) &&
				Objects.equals(this.isWarmup, requestStats.isWarmup);
	}

	@Override
	public int hashCode() {
		return Objects.hash(phase, stepId, metric, summary, failedSLAs, isWarmup);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class RequestStats {\n");
		sb.append("    phase: ").append(toIndentedString(phase)).append("\n");
		sb.append("    stepId: ").append(toIndentedString(stepId)).append("\n");
		sb.append("    metric: ").append(toIndentedString(metric)).append("\n");
		sb.append("    summary: ").append(toIndentedString(summary)).append("\n");
		sb.append("    failedSLAs: ").append(toIndentedString(failedSLAs)).append("\n");
		sb.append("    isWarmup: ").append(toIndentedString(isWarmup)).append("\n");
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
		openapiFields.add("stepId");
		openapiFields.add("metric");
		openapiFields.add("summary");
		openapiFields.add("failedSLAs");
		openapiFields.add("isWarmup");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 *
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to RequestStats
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (RequestStats.openapiRequiredFields.isEmpty()) {
				return;
			} else { // has required fields
				throw new IllegalArgumentException(
						String.format("The required field(s) %s in RequestStats is not found in the empty JSON string",
								RequestStats.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!RequestStats.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(String.format(
						"The field `%s` in the JSON string is not defined in the `RequestStats` properties. JSON: %s",
						entry.getKey(), jsonObj.toString()));
			}
		}
		if (jsonObj.get("phase") != null && !jsonObj.get("phase").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `phase` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("phase").toString()));
		}
		if (jsonObj.get("metric") != null && !jsonObj.get("metric").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `metric` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("metric").toString()));
		}
		// ensure the json data is an array
		if (jsonObj.get("failedSLAs") != null && !jsonObj.get("failedSLAs").isJsonArray()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `failedSLAs` to be an array in the JSON string but got `%s`",
							jsonObj.get("failedSLAs").toString()));
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!RequestStats.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes 'RequestStats' and its subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<RequestStats> thisAdapter = gson.getDelegateAdapter(this, TypeToken.get(RequestStats.class));

			return (TypeAdapter<T>) new TypeAdapter<RequestStats>() {
				@Override
				public void write(JsonWriter out, RequestStats value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public RequestStats read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}
	}

	/**
	 * Create an instance of RequestStats given an JSON string
	 *
	 * @param jsonString JSON string
	 * @return An instance of RequestStats
	 * @throws IOException if the JSON string is invalid with respect to RequestStats
	 */
	public static RequestStats fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, RequestStats.class);
	}

	/**
	 * Convert an instance of RequestStats to an JSON string
	 *
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}
}
