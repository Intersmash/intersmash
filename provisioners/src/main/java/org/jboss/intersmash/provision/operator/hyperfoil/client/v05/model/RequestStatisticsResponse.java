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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.JSON;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
 * RequestStatisticsResponse
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-07-18T14:39:47.341166292+02:00[Europe/Rome]")
public class RequestStatisticsResponse {
	public static final String SERIALIZED_NAME_STATUS = "status";
	@SerializedName(SERIALIZED_NAME_STATUS)
	private String status;

	public static final String SERIALIZED_NAME_STATISTICS = "statistics";
	@SerializedName(SERIALIZED_NAME_STATISTICS)
	private List<RequestStats> statistics = null;

	public RequestStatisticsResponse() {
	}

	public RequestStatisticsResponse status(String status) {

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

	public RequestStatisticsResponse statistics(List<RequestStats> statistics) {

		this.statistics = statistics;
		return this;
	}

	public RequestStatisticsResponse addStatisticsItem(RequestStats statisticsItem) {
		if (this.statistics == null) {
			this.statistics = new ArrayList<>();
		}
		this.statistics.add(statisticsItem);
		return this;
	}

	/**
	* Get statistics
	* @return statistics
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public List<RequestStats> getStatistics() {
		return statistics;
	}

	public void setStatistics(List<RequestStats> statistics) {
		this.statistics = statistics;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RequestStatisticsResponse requestStatisticsResponse = (RequestStatisticsResponse) o;
		return Objects.equals(this.status, requestStatisticsResponse.status) &&
				Objects.equals(this.statistics, requestStatisticsResponse.statistics);
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, statistics);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class RequestStatisticsResponse {\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    statistics: ").append(toIndentedString(statistics)).append("\n");
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
		openapiFields.add("status");
		openapiFields.add("statistics");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 *
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to RequestStatisticsResponse
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (RequestStatisticsResponse.openapiRequiredFields.isEmpty()) {
				return;
			} else { // has required fields
				throw new IllegalArgumentException(String.format(
						"The required field(s) %s in RequestStatisticsResponse is not found in the empty JSON string",
						RequestStatisticsResponse.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!RequestStatisticsResponse.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(String.format(
						"The field `%s` in the JSON string is not defined in the `RequestStatisticsResponse` properties. JSON: %s",
						entry.getKey(), jsonObj.toString()));
			}
		}
		if (jsonObj.get("status") != null && !jsonObj.get("status").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `status` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("status").toString()));
		}
		JsonArray jsonArraystatistics = jsonObj.getAsJsonArray("statistics");
		if (jsonArraystatistics != null) {
			// ensure the json data is an array
			if (!jsonObj.get("statistics").isJsonArray()) {
				throw new IllegalArgumentException(
						String.format("Expected the field `statistics` to be an array in the JSON string but got `%s`",
								jsonObj.get("statistics").toString()));
			}

			// validate the optional field `statistics` (array)
			for (int i = 0; i < jsonArraystatistics.size(); i++) {
				RequestStats.validateJsonObject(jsonArraystatistics.get(i).getAsJsonObject());
			}
			;
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!RequestStatisticsResponse.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes 'RequestStatisticsResponse' and its subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<RequestStatisticsResponse> thisAdapter = gson.getDelegateAdapter(this,
					TypeToken.get(RequestStatisticsResponse.class));

			return (TypeAdapter<T>) new TypeAdapter<RequestStatisticsResponse>() {
				@Override
				public void write(JsonWriter out, RequestStatisticsResponse value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public RequestStatisticsResponse read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}
	}

	/**
	 * Create an instance of RequestStatisticsResponse given an JSON string
	 *
	 * @param jsonString JSON string
	 * @return An instance of RequestStatisticsResponse
	 * @throws IOException if the JSON string is invalid with respect to RequestStatisticsResponse
	 */
	public static RequestStatisticsResponse fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, RequestStatisticsResponse.class);
	}

	/**
	 * Convert an instance of RequestStatisticsResponse to an JSON string
	 *
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}
}
