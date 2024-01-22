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
package org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.model;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.invoker.JSON;

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
 * Version
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-07-18T14:39:47.341166292+02:00[Europe/Rome]")
public class Version {
	public static final String SERIALIZED_NAME_VERSION = "version";
	@SerializedName(SERIALIZED_NAME_VERSION)
	private String version;

	public static final String SERIALIZED_NAME_COMMIT_ID = "commitId";
	@SerializedName(SERIALIZED_NAME_COMMIT_ID)
	private String commitId;

	public static final String SERIALIZED_NAME_DEPLOYMENT_ID = "deploymentId";
	@SerializedName(SERIALIZED_NAME_DEPLOYMENT_ID)
	private String deploymentId;

	public static final String SERIALIZED_NAME_SERVER_TIME = "serverTime";
	@SerializedName(SERIALIZED_NAME_SERVER_TIME)
	private OffsetDateTime serverTime;

	public Version() {
	}

	public Version version(String version) {

		this.version = version;
		return this;
	}

	/**
	* Get version
	* @return version
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Version commitId(String commitId) {

		this.commitId = commitId;
		return this;
	}

	/**
	* Get commitId
	* @return commitId
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public Version deploymentId(String deploymentId) {

		this.deploymentId = deploymentId;
		return this;
	}

	/**
	* Get deploymentId
	* @return deploymentId
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public Version serverTime(OffsetDateTime serverTime) {

		this.serverTime = serverTime;
		return this;
	}

	/**
	* Get serverTime
	* @return serverTime
	**/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public OffsetDateTime getServerTime() {
		return serverTime;
	}

	public void setServerTime(OffsetDateTime serverTime) {
		this.serverTime = serverTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Version version = (Version) o;
		return Objects.equals(this.version, version.version) &&
				Objects.equals(this.commitId, version.commitId) &&
				Objects.equals(this.deploymentId, version.deploymentId) &&
				Objects.equals(this.serverTime, version.serverTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(version, commitId, deploymentId, serverTime);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Version {\n");
		sb.append("    version: ").append(toIndentedString(version)).append("\n");
		sb.append("    commitId: ").append(toIndentedString(commitId)).append("\n");
		sb.append("    deploymentId: ").append(toIndentedString(deploymentId)).append("\n");
		sb.append("    serverTime: ").append(toIndentedString(serverTime)).append("\n");
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
		openapiFields.add("version");
		openapiFields.add("commitId");
		openapiFields.add("deploymentId");
		openapiFields.add("serverTime");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 *
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to Version
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (Version.openapiRequiredFields.isEmpty()) {
				return;
			} else { // has required fields
				throw new IllegalArgumentException(
						String.format("The required field(s) %s in Version is not found in the empty JSON string",
								Version.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!Version.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(
						String.format("The field `%s` in the JSON string is not defined in the `Version` properties. JSON: %s",
								entry.getKey(), jsonObj.toString()));
			}
		}
		if (jsonObj.get("version") != null && !jsonObj.get("version").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `version` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("version").toString()));
		}
		if (jsonObj.get("commitId") != null && !jsonObj.get("commitId").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `commitId` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("commitId").toString()));
		}
		if (jsonObj.get("deploymentId") != null && !jsonObj.get("deploymentId").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `deploymentId` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("deploymentId").toString()));
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!Version.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes 'Version' and its subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<Version> thisAdapter = gson.getDelegateAdapter(this, TypeToken.get(Version.class));

			return (TypeAdapter<T>) new TypeAdapter<Version>() {
				@Override
				public void write(JsonWriter out, Version value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public Version read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}
	}

	/**
	 * Create an instance of Version given an JSON string
	 *
	 * @param jsonString JSON string
	 * @return An instance of Version
	 * @throws IOException if the JSON string is invalid with respect to Version
	 */
	public static Version fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, Version.class);
	}

	/**
	 * Convert an instance of Version to an JSON string
	 *
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}
}
