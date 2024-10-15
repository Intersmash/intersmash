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
package org.jboss.intersmash.provision.operator.hyperfoil.client.v05;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.ApiCallback;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.ApiClient;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.ApiException;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.ApiResponse;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.Configuration;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.Pair;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.model.Histogram;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.model.RequestStatisticsResponse;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.model.Run;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.model.Version;

import com.google.gson.reflect.TypeToken;

public class HyperfoilApi {
	private ApiClient localVarApiClient;
	private int localHostIndex;
	private String localCustomBaseUrl;

	public HyperfoilApi() {
		this(Configuration.getDefaultApiClient());
	}

	public HyperfoilApi(ApiClient apiClient) {
		this.localVarApiClient = apiClient;
	}

	public ApiClient getApiClient() {
		return localVarApiClient;
	}

	public void setApiClient(ApiClient apiClient) {
		this.localVarApiClient = apiClient;
	}

	public int getHostIndex() {
		return localHostIndex;
	}

	public void setHostIndex(int hostIndex) {
		this.localHostIndex = hostIndex;
	}

	public String getCustomBaseUrl() {
		return localCustomBaseUrl;
	}

	public void setCustomBaseUrl(String customBaseUrl) {
		this.localCustomBaseUrl = customBaseUrl;
	}

	/**
	 * Build call for addBenchmark
	 * @param ifMatch If we are updating an existing benchmark, expected previous version. (optional)
	 * @param storedFilesBenchmark Name of previously uploaded benchmark where extra files should be loaded from during multi-part upload. Usually this is the same benchmark unless it is being renamed. (optional)
	 * @param body Benchmark definition. (optional)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call addBenchmarkCall(String ifMatch, String storedFilesBenchmark, File body, final ApiCallback _callback)
			throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = body;

		// create path and map variables
		String localVarPath = "/benchmark";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		if (storedFilesBenchmark != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("storedFilesBenchmark", storedFilesBenchmark));
		}

		if (ifMatch != null) {
			localVarHeaderParams.put("if-match", localVarApiClient.parameterToString(ifMatch));
		}

		final String[] localVarAccepts = {

		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {
				"application/json", "text/uri-list", "text/vnd.yaml", "application/java-serialized-object",
				"multipart/form-data"
		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call addBenchmarkValidateBeforeCall(String ifMatch, String storedFilesBenchmark, File body,
			final ApiCallback _callback) throws ApiException {

		okhttp3.Call localVarCall = addBenchmarkCall(ifMatch, storedFilesBenchmark, body, _callback);
		return localVarCall;

	}

	/**
	 * Add new benchmark definition.
	 *
	 * @param ifMatch If we are updating an existing benchmark, expected previous version. (optional)
	 * @param storedFilesBenchmark Name of previously uploaded benchmark where extra files should be loaded from during multi-part upload. Usually this is the same benchmark unless it is being renamed. (optional)
	 * @param body Benchmark definition. (optional)
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public void addBenchmark(String ifMatch, String storedFilesBenchmark, File body) throws ApiException {
		addBenchmarkWithHttpInfo(ifMatch, storedFilesBenchmark, body);
	}

	/**
	 * Add new benchmark definition.
	 *
	 * @param ifMatch If we are updating an existing benchmark, expected previous version. (optional)
	 * @param storedFilesBenchmark Name of previously uploaded benchmark where extra files should be loaded from during multi-part upload. Usually this is the same benchmark unless it is being renamed. (optional)
	 * @param body Benchmark definition. (optional)
	 * @return ApiResponse&lt;Void&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Void> addBenchmarkWithHttpInfo(String ifMatch, String storedFilesBenchmark, File body)
			throws ApiException {
		okhttp3.Call localVarCall = addBenchmarkValidateBeforeCall(ifMatch, storedFilesBenchmark, body, null);
		return localVarApiClient.execute(localVarCall);
	}

	/**
	 * Add new benchmark definition. (asynchronously)
	 *
	 * @param ifMatch If we are updating an existing benchmark, expected previous version. (optional)
	 * @param storedFilesBenchmark Name of previously uploaded benchmark where extra files should be loaded from during multi-part upload. Usually this is the same benchmark unless it is being renamed. (optional)
	 * @param body Benchmark definition. (optional)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call addBenchmarkAsync(String ifMatch, String storedFilesBenchmark, File body,
			final ApiCallback<Void> _callback) throws ApiException {

		okhttp3.Call localVarCall = addBenchmarkValidateBeforeCall(ifMatch, storedFilesBenchmark, body, _callback);
		localVarApiClient.executeAsync(localVarCall, _callback);
		return localVarCall;
	}

	/**
	 * Build call for agentCpu
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call agentCpuCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/agentCpu"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call agentCpuValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling agentCpu(Async)");
		}

		okhttp3.Call localVarCall = agentCpuCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Get agent CPU data
	 *
	 * @param runId  (required)
	 * @return Object
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Object agentCpu(String runId) throws ApiException {
		ApiResponse<Object> localVarResp = agentCpuWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * Get agent CPU data
	 *
	 * @param runId  (required)
	 * @return ApiResponse&lt;Object&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Object> agentCpuWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = agentCpuValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Get agent CPU data (asynchronously)
	 *
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call agentCpuAsync(String runId, final ApiCallback<Object> _callback) throws ApiException {

		okhttp3.Call localVarCall = agentCpuValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for createReport
	 * @param runId  (required)
	 * @param source  (optional)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call createReportCall(String runId, String source, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/report"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		if (source != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("source", source));
		}

		final String[] localVarAccepts = {
				"text/html"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call createReportValidateBeforeCall(String runId, String source, final ApiCallback _callback)
			throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling createReport(Async)");
		}

		okhttp3.Call localVarCall = createReportCall(runId, source, _callback);
		return localVarCall;

	}

	/**
	 * Generate HTML report for this run
	 *
	 * @param runId  (required)
	 * @param source  (optional)
	 * @return String
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public String createReport(String runId, String source) throws ApiException {
		ApiResponse<String> localVarResp = createReportWithHttpInfo(runId, source);
		return localVarResp.getData();
	}

	/**
	 * Generate HTML report for this run
	 *
	 * @param runId  (required)
	 * @param source  (optional)
	 * @return ApiResponse&lt;String&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<String> createReportWithHttpInfo(String runId, String source) throws ApiException {
		okhttp3.Call localVarCall = createReportValidateBeforeCall(runId, source, null);
		Type localVarReturnType = new TypeToken<String>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Generate HTML report for this run (asynchronously)
	 *
	 * @param runId  (required)
	 * @param source  (optional)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call createReportAsync(String runId, String source, final ApiCallback<String> _callback)
			throws ApiException {

		okhttp3.Call localVarCall = createReportValidateBeforeCall(runId, source, _callback);
		Type localVarReturnType = new TypeToken<String>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getAgentLog
	 * @param agent  (required)
	 * @param offset  (optional, default to 0)
	 * @param ifMatch Identifier of the previously downloaded log chunk. (optional)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getAgentLogCall(String agent, Integer offset, String ifMatch, final ApiCallback _callback)
			throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/log/{agent}"
				.replaceAll("\\{" + "agent" + "\\}", localVarApiClient.escapeString(agent.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		if (offset != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("offset", offset));
		}

		if (ifMatch != null) {
			localVarHeaderParams.put("if-match", localVarApiClient.parameterToString(ifMatch));
		}

		final String[] localVarAccepts = {
				"text/plain"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getAgentLogValidateBeforeCall(String agent, Integer offset, String ifMatch,
			final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'agent' is set
		if (agent == null) {
			throw new ApiException("Missing the required parameter 'agent' when calling getAgentLog(Async)");
		}

		okhttp3.Call localVarCall = getAgentLogCall(agent, offset, ifMatch, _callback);
		return localVarCall;

	}

	/**
	 * Get controller log.
	 *
	 * @param agent  (required)
	 * @param offset  (optional, default to 0)
	 * @param ifMatch Identifier of the previously downloaded log chunk. (optional)
	 * @return String
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public String getAgentLog(String agent, Integer offset, String ifMatch) throws ApiException {
		ApiResponse<String> localVarResp = getAgentLogWithHttpInfo(agent, offset, ifMatch);
		return localVarResp.getData();
	}

	/**
	 * Get controller log.
	 *
	 * @param agent  (required)
	 * @param offset  (optional, default to 0)
	 * @param ifMatch Identifier of the previously downloaded log chunk. (optional)
	 * @return ApiResponse&lt;String&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<String> getAgentLogWithHttpInfo(String agent, Integer offset, String ifMatch) throws ApiException {
		okhttp3.Call localVarCall = getAgentLogValidateBeforeCall(agent, offset, ifMatch, null);
		Type localVarReturnType = new TypeToken<String>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Get controller log. (asynchronously)
	 *
	 * @param agent  (required)
	 * @param offset  (optional, default to 0)
	 * @param ifMatch Identifier of the previously downloaded log chunk. (optional)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getAgentLogAsync(String agent, Integer offset, String ifMatch, final ApiCallback<String> _callback)
			throws ApiException {

		okhttp3.Call localVarCall = getAgentLogValidateBeforeCall(agent, offset, ifMatch, _callback);
		Type localVarReturnType = new TypeToken<String>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getAllStats
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getAllStatsCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/stats/all"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/zip", "application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getAllStatsValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getAllStats(Async)");
		}

		okhttp3.Call localVarCall = getAllStatsCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Get complete statistics from the run.
	 * This can be invoked only after the run completes. Provides exported statistics either as ZIP file with CSV files or as JSON object.
	 * @param runId  (required)
	 * @return File
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public File getAllStats(String runId) throws ApiException {
		ApiResponse<File> localVarResp = getAllStatsWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * Get complete statistics from the run.
	 * This can be invoked only after the run completes. Provides exported statistics either as ZIP file with CSV files or as JSON object.
	 * @param runId  (required)
	 * @return ApiResponse&lt;File&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<File> getAllStatsWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = getAllStatsValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<File>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Get complete statistics from the run. (asynchronously)
	 * This can be invoked only after the run completes. Provides exported statistics either as ZIP file with CSV files or as JSON object.
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getAllStatsAsync(String runId, final ApiCallback<File> _callback) throws ApiException {

		okhttp3.Call localVarCall = getAllStatsValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<File>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getAllStatsCsv
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getAllStatsCsvCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/stats/all/csv"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/zip"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getAllStatsCsvValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getAllStatsCsv(Async)");
		}

		okhttp3.Call localVarCall = getAllStatsCsvCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Get complete statistics from the run.
	 * This can be invoked only after the run completes. Provides exported statistics as ZIP file with CSV files
	 * @param runId  (required)
	 * @return Object
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Object getAllStatsCsv(String runId) throws ApiException {
		ApiResponse<Object> localVarResp = getAllStatsCsvWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * Get complete statistics from the run.
	 * This can be invoked only after the run completes. Provides exported statistics as ZIP file with CSV files
	 * @param runId  (required)
	 * @return ApiResponse&lt;Object&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Object> getAllStatsCsvWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = getAllStatsCsvValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Get complete statistics from the run. (asynchronously)
	 * This can be invoked only after the run completes. Provides exported statistics as ZIP file with CSV files
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getAllStatsCsvAsync(String runId, final ApiCallback<Object> _callback) throws ApiException {

		okhttp3.Call localVarCall = getAllStatsCsvValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getAllStatsJson
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getAllStatsJsonCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/stats/all/json"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getAllStatsJsonValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getAllStatsJson(Async)");
		}

		okhttp3.Call localVarCall = getAllStatsJsonCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Get complete statistics from the run.
	 * This can be invoked only after the run completes. Provides exported statistics as JSON object.
	 * @param runId  (required)
	 * @return Object
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Object getAllStatsJson(String runId) throws ApiException {
		ApiResponse<Object> localVarResp = getAllStatsJsonWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * Get complete statistics from the run.
	 * This can be invoked only after the run completes. Provides exported statistics as JSON object.
	 * @param runId  (required)
	 * @return ApiResponse&lt;Object&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Object> getAllStatsJsonWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = getAllStatsJsonValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Get complete statistics from the run. (asynchronously)
	 * This can be invoked only after the run completes. Provides exported statistics as JSON object.
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getAllStatsJsonAsync(String runId, final ApiCallback<Object> _callback) throws ApiException {

		okhttp3.Call localVarCall = getAllStatsJsonValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getBenchmark
	 * @param name  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getBenchmarkCall(String name, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/benchmark/{name}"
				.replaceAll("\\{" + "name" + "\\}", localVarApiClient.escapeString(name.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"text/vnd.yaml", "application/java-serialized-object"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getBenchmarkValidateBeforeCall(String name, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'name' is set
		if (name == null) {
			throw new ApiException("Missing the required parameter 'name' when calling getBenchmark(Async)");
		}

		okhttp3.Call localVarCall = getBenchmarkCall(name, _callback);
		return localVarCall;

	}

	/**
	 * Retrieve existing benchmark.
	 *
	 * @param name  (required)
	 * @return Object
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Object getBenchmark(String name) throws ApiException {
		ApiResponse<Object> localVarResp = getBenchmarkWithHttpInfo(name);
		return localVarResp.getData();
	}

	/**
	 * Retrieve existing benchmark.
	 *
	 * @param name  (required)
	 * @return ApiResponse&lt;Object&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Object> getBenchmarkWithHttpInfo(String name) throws ApiException {
		okhttp3.Call localVarCall = getBenchmarkValidateBeforeCall(name, null);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Retrieve existing benchmark. (asynchronously)
	 *
	 * @param name  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getBenchmarkAsync(String name, final ApiCallback<Object> _callback) throws ApiException {

		okhttp3.Call localVarCall = getBenchmarkValidateBeforeCall(name, _callback);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getBenchmarkFiles
	 * @param name  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getBenchmarkFilesCall(String name, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/benchmark/{name}/files"
				.replaceAll("\\{" + "name" + "\\}", localVarApiClient.escapeString(name.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"multipart/form-data"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getBenchmarkFilesValidateBeforeCall(String name, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'name' is set
		if (name == null) {
			throw new ApiException("Missing the required parameter 'name' when calling getBenchmarkFiles(Async)");
		}

		okhttp3.Call localVarCall = getBenchmarkFilesCall(name, _callback);
		return localVarCall;

	}

	/**
	 * Get data files for the benchmark
	 *
	 * @param name  (required)
	 * @return Object
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Object getBenchmarkFiles(String name) throws ApiException {
		ApiResponse<Object> localVarResp = getBenchmarkFilesWithHttpInfo(name);
		return localVarResp.getData();
	}

	/**
	 * Get data files for the benchmark
	 *
	 * @param name  (required)
	 * @return ApiResponse&lt;Object&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Object> getBenchmarkFilesWithHttpInfo(String name) throws ApiException {
		okhttp3.Call localVarCall = getBenchmarkFilesValidateBeforeCall(name, null);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Get data files for the benchmark (asynchronously)
	 *
	 * @param name  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getBenchmarkFilesAsync(String name, final ApiCallback<Object> _callback) throws ApiException {

		okhttp3.Call localVarCall = getBenchmarkFilesValidateBeforeCall(name, _callback);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getBenchmarkForRun
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getBenchmarkForRunCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/benchmark"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"text/vnd.yaml", "application/java-serialized-object"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getBenchmarkForRunValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getBenchmarkForRun(Async)");
		}

		okhttp3.Call localVarCall = getBenchmarkForRunCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Benchmark used for given run.
	 *
	 * @param runId  (required)
	 * @return Object
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Object getBenchmarkForRun(String runId) throws ApiException {
		ApiResponse<Object> localVarResp = getBenchmarkForRunWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * Benchmark used for given run.
	 *
	 * @param runId  (required)
	 * @return ApiResponse&lt;Object&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Object> getBenchmarkForRunWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = getBenchmarkForRunValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Benchmark used for given run. (asynchronously)
	 *
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getBenchmarkForRunAsync(String runId, final ApiCallback<Object> _callback) throws ApiException {

		okhttp3.Call localVarCall = getBenchmarkForRunValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getBenchmarkStructure
	 * @param name  (required)
	 * @param maxCollectionSize  (optional, default to 20)
	 * @param templateParam Template parameters in format KEY&#x3D;VALUE for resolving benchmark without running that. (optional)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getBenchmarkStructureCall(String name, Integer maxCollectionSize, List<String> templateParam,
			final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/benchmark/{name}/structure"
				.replaceAll("\\{" + "name" + "\\}", localVarApiClient.escapeString(name.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		if (maxCollectionSize != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("maxCollectionSize", maxCollectionSize));
		}

		if (templateParam != null) {
			localVarCollectionQueryParams.addAll(localVarApiClient.parameterToPairs("multi", "templateParam", templateParam));
		}

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getBenchmarkStructureValidateBeforeCall(String name, Integer maxCollectionSize,
			List<String> templateParam, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'name' is set
		if (name == null) {
			throw new ApiException("Missing the required parameter 'name' when calling getBenchmarkStructure(Async)");
		}

		okhttp3.Call localVarCall = getBenchmarkStructureCall(name, maxCollectionSize, templateParam, _callback);
		return localVarCall;

	}

	/**
	 * Inspect the rendered structure of the benchmark.
	 *
	 * @param name  (required)
	 * @param maxCollectionSize  (optional, default to 20)
	 * @param templateParam Template parameters in format KEY&#x3D;VALUE for resolving benchmark without running that. (optional)
	 * @return Object
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Object getBenchmarkStructure(String name, Integer maxCollectionSize, List<String> templateParam)
			throws ApiException {
		ApiResponse<Object> localVarResp = getBenchmarkStructureWithHttpInfo(name, maxCollectionSize, templateParam);
		return localVarResp.getData();
	}

	/**
	 * Inspect the rendered structure of the benchmark.
	 *
	 * @param name  (required)
	 * @param maxCollectionSize  (optional, default to 20)
	 * @param templateParam Template parameters in format KEY&#x3D;VALUE for resolving benchmark without running that. (optional)
	 * @return ApiResponse&lt;Object&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Object> getBenchmarkStructureWithHttpInfo(String name, Integer maxCollectionSize,
			List<String> templateParam) throws ApiException {
		okhttp3.Call localVarCall = getBenchmarkStructureValidateBeforeCall(name, maxCollectionSize, templateParam, null);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Inspect the rendered structure of the benchmark. (asynchronously)
	 *
	 * @param name  (required)
	 * @param maxCollectionSize  (optional, default to 20)
	 * @param templateParam Template parameters in format KEY&#x3D;VALUE for resolving benchmark without running that. (optional)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getBenchmarkStructureAsync(String name, Integer maxCollectionSize, List<String> templateParam,
			final ApiCallback<Object> _callback) throws ApiException {

		okhttp3.Call localVarCall = getBenchmarkStructureValidateBeforeCall(name, maxCollectionSize, templateParam, _callback);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getControllerLog
	 * @param offset  (optional, default to 0)
	 * @param ifMatch Identifier of the previously downloaded log chunk. (optional)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getControllerLogCall(Integer offset, String ifMatch, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/log";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		if (offset != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("offset", offset));
		}

		if (ifMatch != null) {
			localVarHeaderParams.put("if-match", localVarApiClient.parameterToString(ifMatch));
		}

		final String[] localVarAccepts = {
				"text/plain"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getControllerLogValidateBeforeCall(Integer offset, String ifMatch, final ApiCallback _callback)
			throws ApiException {

		okhttp3.Call localVarCall = getControllerLogCall(offset, ifMatch, _callback);
		return localVarCall;

	}

	/**
	 * Get controller log.
	 *
	 * @param offset  (optional, default to 0)
	 * @param ifMatch Identifier of the previously downloaded log chunk. (optional)
	 * @return String
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public String getControllerLog(Integer offset, String ifMatch) throws ApiException {
		ApiResponse<String> localVarResp = getControllerLogWithHttpInfo(offset, ifMatch);
		return localVarResp.getData();
	}

	/**
	 * Get controller log.
	 *
	 * @param offset  (optional, default to 0)
	 * @param ifMatch Identifier of the previously downloaded log chunk. (optional)
	 * @return ApiResponse&lt;String&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<String> getControllerLogWithHttpInfo(Integer offset, String ifMatch) throws ApiException {
		okhttp3.Call localVarCall = getControllerLogValidateBeforeCall(offset, ifMatch, null);
		Type localVarReturnType = new TypeToken<String>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Get controller log. (asynchronously)
	 *
	 * @param offset  (optional, default to 0)
	 * @param ifMatch Identifier of the previously downloaded log chunk. (optional)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getControllerLogAsync(Integer offset, String ifMatch, final ApiCallback<String> _callback)
			throws ApiException {

		okhttp3.Call localVarCall = getControllerLogValidateBeforeCall(offset, ifMatch, _callback);
		Type localVarReturnType = new TypeToken<String>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getHistogramStats
	 * @param runId  (required)
	 * @param phase  (required)
	 * @param stepId  (required)
	 * @param metric  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getHistogramStatsCall(String runId, String phase, Integer stepId, String metric,
			final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/stats/histogram"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		if (phase != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("phase", phase));
		}

		if (stepId != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("stepId", stepId));
		}

		if (metric != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("metric", metric));
		}

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getHistogramStatsValidateBeforeCall(String runId, String phase, Integer stepId, String metric,
			final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getHistogramStats(Async)");
		}

		// verify the required parameter 'phase' is set
		if (phase == null) {
			throw new ApiException("Missing the required parameter 'phase' when calling getHistogramStats(Async)");
		}

		// verify the required parameter 'stepId' is set
		if (stepId == null) {
			throw new ApiException("Missing the required parameter 'stepId' when calling getHistogramStats(Async)");
		}

		// verify the required parameter 'metric' is set
		if (metric == null) {
			throw new ApiException("Missing the required parameter 'metric' when calling getHistogramStats(Async)");
		}

		okhttp3.Call localVarCall = getHistogramStatsCall(runId, phase, stepId, metric, _callback);
		return localVarCall;

	}

	/**
	 * Retrieve histogram for given metric.
	 *
	 * @param runId  (required)
	 * @param phase  (required)
	 * @param stepId  (required)
	 * @param metric  (required)
	 * @return List&lt;Histogram&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public List<Histogram> getHistogramStats(String runId, String phase, Integer stepId, String metric) throws ApiException {
		ApiResponse<List<Histogram>> localVarResp = getHistogramStatsWithHttpInfo(runId, phase, stepId, metric);
		return localVarResp.getData();
	}

	/**
	 * Retrieve histogram for given metric.
	 *
	 * @param runId  (required)
	 * @param phase  (required)
	 * @param stepId  (required)
	 * @param metric  (required)
	 * @return ApiResponse&lt;List&lt;Histogram&gt;&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<List<Histogram>> getHistogramStatsWithHttpInfo(String runId, String phase, Integer stepId, String metric)
			throws ApiException {
		okhttp3.Call localVarCall = getHistogramStatsValidateBeforeCall(runId, phase, stepId, metric, null);
		Type localVarReturnType = new TypeToken<List<Histogram>>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Retrieve histogram for given metric. (asynchronously)
	 *
	 * @param runId  (required)
	 * @param phase  (required)
	 * @param stepId  (required)
	 * @param metric  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getHistogramStatsAsync(String runId, String phase, Integer stepId, String metric,
			final ApiCallback<List<Histogram>> _callback) throws ApiException {

		okhttp3.Call localVarCall = getHistogramStatsValidateBeforeCall(runId, phase, stepId, metric, _callback);
		Type localVarReturnType = new TypeToken<List<Histogram>>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getRecentConnections
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getRecentConnectionsCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/connections/recent"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getRecentConnectionsValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getRecentConnections(Async)");
		}

		okhttp3.Call localVarCall = getRecentConnectionsCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Actual numbers of connections for each host:port
	 *
	 * @param runId  (required)
	 * @return Object
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Object getRecentConnections(String runId) throws ApiException {
		ApiResponse<Object> localVarResp = getRecentConnectionsWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * Actual numbers of connections for each host:port
	 *
	 * @param runId  (required)
	 * @return ApiResponse&lt;Object&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Object> getRecentConnectionsWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = getRecentConnectionsValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Actual numbers of connections for each host:port (asynchronously)
	 *
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getRecentConnectionsAsync(String runId, final ApiCallback<Object> _callback) throws ApiException {

		okhttp3.Call localVarCall = getRecentConnectionsValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getRecentSessions
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getRecentSessionsCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/sessions/recent"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getRecentSessionsValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getRecentSessions(Async)");
		}

		okhttp3.Call localVarCall = getRecentSessionsCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Actual numbers of active sessions in each phase
	 *
	 * @param runId  (required)
	 * @return Object
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Object getRecentSessions(String runId) throws ApiException {
		ApiResponse<Object> localVarResp = getRecentSessionsWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * Actual numbers of active sessions in each phase
	 *
	 * @param runId  (required)
	 * @return ApiResponse&lt;Object&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Object> getRecentSessionsWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = getRecentSessionsValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Actual numbers of active sessions in each phase (asynchronously)
	 *
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getRecentSessionsAsync(String runId, final ApiCallback<Object> _callback) throws ApiException {

		okhttp3.Call localVarCall = getRecentSessionsValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getRecentStats
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getRecentStatsCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/stats/recent"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getRecentStatsValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getRecentStats(Async)");
		}

		okhttp3.Call localVarCall = getRecentStatsCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Actual statistics from last 5 seconds of run.
	 *
	 * @param runId  (required)
	 * @return RequestStatisticsResponse
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public RequestStatisticsResponse getRecentStats(String runId) throws ApiException {
		ApiResponse<RequestStatisticsResponse> localVarResp = getRecentStatsWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * Actual statistics from last 5 seconds of run.
	 *
	 * @param runId  (required)
	 * @return ApiResponse&lt;RequestStatisticsResponse&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<RequestStatisticsResponse> getRecentStatsWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = getRecentStatsValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<RequestStatisticsResponse>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Actual statistics from last 5 seconds of run. (asynchronously)
	 *
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getRecentStatsAsync(String runId, final ApiCallback<RequestStatisticsResponse> _callback)
			throws ApiException {

		okhttp3.Call localVarCall = getRecentStatsValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<RequestStatisticsResponse>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getRun
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getRunCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getRunValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getRun(Async)");
		}

		okhttp3.Call localVarCall = getRunCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Get info about run.
	 *
	 * @param runId  (required)
	 * @return Run
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Run getRun(String runId) throws ApiException {
		ApiResponse<Run> localVarResp = getRunWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * Get info about run.
	 *
	 * @param runId  (required)
	 * @return ApiResponse&lt;Run&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Run> getRunWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = getRunValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<Run>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Get info about run. (asynchronously)
	 *
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getRunAsync(String runId, final ApiCallback<Run> _callback) throws ApiException {

		okhttp3.Call localVarCall = getRunValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<Run>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getRunFile
	 * @param runId  (required)
	 * @param _file  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getRunFileCall(String runId, String _file, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/file"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		if (_file != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("file", _file));
		}

		final String[] localVarAccepts = {
				"application/octet-stream"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getRunFileValidateBeforeCall(String runId, String _file, final ApiCallback _callback)
			throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getRunFile(Async)");
		}

		// verify the required parameter '_file' is set
		if (_file == null) {
			throw new ApiException("Missing the required parameter '_file' when calling getRunFile(Async)");
		}

		okhttp3.Call localVarCall = getRunFileCall(runId, _file, _callback);
		return localVarCall;

	}

	/**
	 * Retrieve a custom file in the run directory (generated by hooks)
	 *
	 * @param runId  (required)
	 * @param _file  (required)
	 * @return File
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public File getRunFile(String runId, String _file) throws ApiException {
		ApiResponse<File> localVarResp = getRunFileWithHttpInfo(runId, _file);
		return localVarResp.getData();
	}

	/**
	 * Retrieve a custom file in the run directory (generated by hooks)
	 *
	 * @param runId  (required)
	 * @param _file  (required)
	 * @return ApiResponse&lt;File&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<File> getRunFileWithHttpInfo(String runId, String _file) throws ApiException {
		okhttp3.Call localVarCall = getRunFileValidateBeforeCall(runId, _file, null);
		Type localVarReturnType = new TypeToken<File>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Retrieve a custom file in the run directory (generated by hooks) (asynchronously)
	 *
	 * @param runId  (required)
	 * @param _file  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getRunFileAsync(String runId, String _file, final ApiCallback<File> _callback) throws ApiException {

		okhttp3.Call localVarCall = getRunFileValidateBeforeCall(runId, _file, _callback);
		Type localVarReturnType = new TypeToken<File>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getToken
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getTokenCall(final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/token";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {

		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getTokenValidateBeforeCall(final ApiCallback _callback) throws ApiException {

		okhttp3.Call localVarCall = getTokenCall(_callback);
		return localVarCall;

	}

	/**
	 * Get authorization token
	 * Returns authorization token that can be used instead of credentials with Basic Auth.
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public void getToken() throws ApiException {
		getTokenWithHttpInfo();
	}

	/**
	 * Get authorization token
	 * Returns authorization token that can be used instead of credentials with Basic Auth.
	 * @return ApiResponse&lt;Void&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Void> getTokenWithHttpInfo() throws ApiException {
		okhttp3.Call localVarCall = getTokenValidateBeforeCall(null);
		return localVarApiClient.execute(localVarCall);
	}

	/**
	 * Get authorization token (asynchronously)
	 * Returns authorization token that can be used instead of credentials with Basic Auth.
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getTokenAsync(final ApiCallback<Void> _callback) throws ApiException {

		okhttp3.Call localVarCall = getTokenValidateBeforeCall(_callback);
		localVarApiClient.executeAsync(localVarCall, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getTotalConnections
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getTotalConnectionsCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/connections/total"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getTotalConnectionsValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getTotalConnections(Async)");
		}

		okhttp3.Call localVarCall = getTotalConnectionsCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Total number of connections for each host:port
	 *
	 * @param runId  (required)
	 * @return Object
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Object getTotalConnections(String runId) throws ApiException {
		ApiResponse<Object> localVarResp = getTotalConnectionsWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * Total number of connections for each host:port
	 *
	 * @param runId  (required)
	 * @return ApiResponse&lt;Object&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Object> getTotalConnectionsWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = getTotalConnectionsValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Total number of connections for each host:port (asynchronously)
	 *
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getTotalConnectionsAsync(String runId, final ApiCallback<Object> _callback) throws ApiException {

		okhttp3.Call localVarCall = getTotalConnectionsValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getTotalSessions
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getTotalSessionsCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/sessions/total"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getTotalSessionsValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getTotalSessions(Async)");
		}

		okhttp3.Call localVarCall = getTotalSessionsCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Min/max of active sessions in each phase
	 *
	 * @param runId  (required)
	 * @return Object
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Object getTotalSessions(String runId) throws ApiException {
		ApiResponse<Object> localVarResp = getTotalSessionsWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * Min/max of active sessions in each phase
	 *
	 * @param runId  (required)
	 * @return ApiResponse&lt;Object&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Object> getTotalSessionsWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = getTotalSessionsValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Min/max of active sessions in each phase (asynchronously)
	 *
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getTotalSessionsAsync(String runId, final ApiCallback<Object> _callback) throws ApiException {

		okhttp3.Call localVarCall = getTotalSessionsValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<Object>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getTotalStats
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getTotalStatsCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/stats/total"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getTotalStatsValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling getTotalStats(Async)");
		}

		okhttp3.Call localVarCall = getTotalStatsCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Statistics over the whole duration of phases.
	 *
	 * @param runId  (required)
	 * @return RequestStatisticsResponse
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public RequestStatisticsResponse getTotalStats(String runId) throws ApiException {
		ApiResponse<RequestStatisticsResponse> localVarResp = getTotalStatsWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * Statistics over the whole duration of phases.
	 *
	 * @param runId  (required)
	 * @return ApiResponse&lt;RequestStatisticsResponse&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<RequestStatisticsResponse> getTotalStatsWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = getTotalStatsValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<RequestStatisticsResponse>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Statistics over the whole duration of phases. (asynchronously)
	 *
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getTotalStatsAsync(String runId, final ApiCallback<RequestStatisticsResponse> _callback)
			throws ApiException {

		okhttp3.Call localVarCall = getTotalStatsValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<RequestStatisticsResponse>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for getVersion
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call getVersionCall(final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/version";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call getVersionValidateBeforeCall(final ApiCallback _callback) throws ApiException {

		okhttp3.Call localVarCall = getVersionCall(_callback);
		return localVarCall;

	}

	/**
	 * Fetch controller version
	 *
	 * @return Version
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Version getVersion() throws ApiException {
		ApiResponse<Version> localVarResp = getVersionWithHttpInfo();
		return localVarResp.getData();
	}

	/**
	 * Fetch controller version
	 *
	 * @return ApiResponse&lt;Version&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Version> getVersionWithHttpInfo() throws ApiException {
		okhttp3.Call localVarCall = getVersionValidateBeforeCall(null);
		Type localVarReturnType = new TypeToken<Version>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Fetch controller version (asynchronously)
	 *
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call getVersionAsync(final ApiCallback<Version> _callback) throws ApiException {

		okhttp3.Call localVarCall = getVersionValidateBeforeCall(_callback);
		Type localVarReturnType = new TypeToken<Version>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for killRun
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call killRunCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/kill"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {

		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call killRunValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling killRun(Async)");
		}

		okhttp3.Call localVarCall = killRunCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * Kill this run
	 *
	 * @param runId  (required)
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public void killRun(String runId) throws ApiException {
		killRunWithHttpInfo(runId);
	}

	/**
	 * Kill this run
	 *
	 * @param runId  (required)
	 * @return ApiResponse&lt;Void&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Void> killRunWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = killRunValidateBeforeCall(runId, null);
		return localVarApiClient.execute(localVarCall);
	}

	/**
	 * Kill this run (asynchronously)
	 *
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call killRunAsync(String runId, final ApiCallback<Void> _callback) throws ApiException {

		okhttp3.Call localVarCall = killRunValidateBeforeCall(runId, _callback);
		localVarApiClient.executeAsync(localVarCall, _callback);
		return localVarCall;
	}

	/**
	 * Build call for listAgents
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call listAgentsCall(final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/agents";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call listAgentsValidateBeforeCall(final ApiCallback _callback) throws ApiException {

		okhttp3.Call localVarCall = listAgentsCall(_callback);
		return localVarCall;

	}

	/**
	 * All agents used (in all runs)
	 *
	 * @return List&lt;String&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public List<String> listAgents() throws ApiException {
		ApiResponse<List<String>> localVarResp = listAgentsWithHttpInfo();
		return localVarResp.getData();
	}

	/**
	 * All agents used (in all runs)
	 *
	 * @return ApiResponse&lt;List&lt;String&gt;&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<List<String>> listAgentsWithHttpInfo() throws ApiException {
		okhttp3.Call localVarCall = listAgentsValidateBeforeCall(null);
		Type localVarReturnType = new TypeToken<List<String>>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * All agents used (in all runs) (asynchronously)
	 *
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call listAgentsAsync(final ApiCallback<List<String>> _callback) throws ApiException {

		okhttp3.Call localVarCall = listAgentsValidateBeforeCall(_callback);
		Type localVarReturnType = new TypeToken<List<String>>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for listBenchmarks
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call listBenchmarksCall(final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/benchmark";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call listBenchmarksValidateBeforeCall(final ApiCallback _callback) throws ApiException {

		okhttp3.Call localVarCall = listBenchmarksCall(_callback);
		return localVarCall;

	}

	/**
	 * List defined benchmark names.
	 *
	 * @return List&lt;String&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public List<String> listBenchmarks() throws ApiException {
		ApiResponse<List<String>> localVarResp = listBenchmarksWithHttpInfo();
		return localVarResp.getData();
	}

	/**
	 * List defined benchmark names.
	 *
	 * @return ApiResponse&lt;List&lt;String&gt;&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<List<String>> listBenchmarksWithHttpInfo() throws ApiException {
		okhttp3.Call localVarCall = listBenchmarksValidateBeforeCall(null);
		Type localVarReturnType = new TypeToken<List<String>>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * List defined benchmark names. (asynchronously)
	 *
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call listBenchmarksAsync(final ApiCallback<List<String>> _callback) throws ApiException {

		okhttp3.Call localVarCall = listBenchmarksValidateBeforeCall(_callback);
		Type localVarReturnType = new TypeToken<List<String>>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for listConnections
	 * @param runId  (required)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call listConnectionsCall(String runId, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/connections"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"text/plain"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call listConnectionsValidateBeforeCall(String runId, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling listConnections(Async)");
		}

		okhttp3.Call localVarCall = listConnectionsCall(runId, _callback);
		return localVarCall;

	}

	/**
	 * List connections in plaintext (for debugging)
	 *
	 * @param runId  (required)
	 * @return String
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public String listConnections(String runId) throws ApiException {
		ApiResponse<String> localVarResp = listConnectionsWithHttpInfo(runId);
		return localVarResp.getData();
	}

	/**
	 * List connections in plaintext (for debugging)
	 *
	 * @param runId  (required)
	 * @return ApiResponse&lt;String&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<String> listConnectionsWithHttpInfo(String runId) throws ApiException {
		okhttp3.Call localVarCall = listConnectionsValidateBeforeCall(runId, null);
		Type localVarReturnType = new TypeToken<String>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * List connections in plaintext (for debugging) (asynchronously)
	 *
	 * @param runId  (required)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call listConnectionsAsync(String runId, final ApiCallback<String> _callback) throws ApiException {

		okhttp3.Call localVarCall = listConnectionsValidateBeforeCall(runId, _callback);
		Type localVarReturnType = new TypeToken<String>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for listRuns
	 * @param details  (optional, default to false)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call listRunsCall(Boolean details, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		if (details != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("details", details));
		}

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call listRunsValidateBeforeCall(Boolean details, final ApiCallback _callback) throws ApiException {

		okhttp3.Call localVarCall = listRunsCall(details, _callback);
		return localVarCall;

	}

	/**
	 * List known runs.
	 *
	 * @param details  (optional, default to false)
	 * @return Run
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Run listRuns(Boolean details) throws ApiException {
		ApiResponse<Run> localVarResp = listRunsWithHttpInfo(details);
		return localVarResp.getData();
	}

	/**
	 * List known runs.
	 *
	 * @param details  (optional, default to false)
	 * @return ApiResponse&lt;Run&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Run> listRunsWithHttpInfo(Boolean details) throws ApiException {
		okhttp3.Call localVarCall = listRunsValidateBeforeCall(details, null);
		Type localVarReturnType = new TypeToken<Run>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * List known runs. (asynchronously)
	 *
	 * @param details  (optional, default to false)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call listRunsAsync(Boolean details, final ApiCallback<Run> _callback) throws ApiException {

		okhttp3.Call localVarCall = listRunsValidateBeforeCall(details, _callback);
		Type localVarReturnType = new TypeToken<Run>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for listSessions
	 * @param runId  (required)
	 * @param inactive  (optional, default to false)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call listSessionsCall(String runId, Boolean inactive, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/run/{runId}/sessions"
				.replaceAll("\\{" + "runId" + "\\}", localVarApiClient.escapeString(runId.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		if (inactive != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("inactive", inactive));
		}

		final String[] localVarAccepts = {
				"text/plain"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call listSessionsValidateBeforeCall(String runId, Boolean inactive, final ApiCallback _callback)
			throws ApiException {

		// verify the required parameter 'runId' is set
		if (runId == null) {
			throw new ApiException("Missing the required parameter 'runId' when calling listSessions(Async)");
		}

		okhttp3.Call localVarCall = listSessionsCall(runId, inactive, _callback);
		return localVarCall;

	}

	/**
	 * List sessions in plaintext (for debugging)
	 *
	 * @param runId  (required)
	 * @param inactive  (optional, default to false)
	 * @return String
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public String listSessions(String runId, Boolean inactive) throws ApiException {
		ApiResponse<String> localVarResp = listSessionsWithHttpInfo(runId, inactive);
		return localVarResp.getData();
	}

	/**
	 * List sessions in plaintext (for debugging)
	 *
	 * @param runId  (required)
	 * @param inactive  (optional, default to false)
	 * @return ApiResponse&lt;String&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<String> listSessionsWithHttpInfo(String runId, Boolean inactive) throws ApiException {
		okhttp3.Call localVarCall = listSessionsValidateBeforeCall(runId, inactive, null);
		Type localVarReturnType = new TypeToken<String>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * List sessions in plaintext (for debugging) (asynchronously)
	 *
	 * @param runId  (required)
	 * @param inactive  (optional, default to false)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call listSessionsAsync(String runId, Boolean inactive, final ApiCallback<String> _callback)
			throws ApiException {

		okhttp3.Call localVarCall = listSessionsValidateBeforeCall(runId, inactive, _callback);
		Type localVarReturnType = new TypeToken<String>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for listTemplates
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call listTemplatesCall(final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/template";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call listTemplatesValidateBeforeCall(final ApiCallback _callback) throws ApiException {

		okhttp3.Call localVarCall = listTemplatesCall(_callback);
		return localVarCall;

	}

	/**
	 * List defined template names.
	 *
	 * @return List&lt;String&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public List<String> listTemplates() throws ApiException {
		ApiResponse<List<String>> localVarResp = listTemplatesWithHttpInfo();
		return localVarResp.getData();
	}

	/**
	 * List defined template names.
	 *
	 * @return ApiResponse&lt;List&lt;String&gt;&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<List<String>> listTemplatesWithHttpInfo() throws ApiException {
		okhttp3.Call localVarCall = listTemplatesValidateBeforeCall(null);
		Type localVarReturnType = new TypeToken<List<String>>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * List defined template names. (asynchronously)
	 *
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call listTemplatesAsync(final ApiCallback<List<String>> _callback) throws ApiException {

		okhttp3.Call localVarCall = listTemplatesValidateBeforeCall(_callback);
		Type localVarReturnType = new TypeToken<List<String>>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for openApi
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call openApiCall(final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/openapi";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = {
				"text/vnd.yaml"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call openApiValidateBeforeCall(final ApiCallback _callback) throws ApiException {

		okhttp3.Call localVarCall = openApiCall(_callback);
		return localVarCall;

	}

	/**
	 * Serve this OpenAPI 3 definition.
	 *
	 * @return File
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public File openApi() throws ApiException {
		ApiResponse<File> localVarResp = openApiWithHttpInfo();
		return localVarResp.getData();
	}

	/**
	 * Serve this OpenAPI 3 definition.
	 *
	 * @return ApiResponse&lt;File&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<File> openApiWithHttpInfo() throws ApiException {
		okhttp3.Call localVarCall = openApiValidateBeforeCall(null);
		Type localVarReturnType = new TypeToken<File>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Serve this OpenAPI 3 definition. (asynchronously)
	 *
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call openApiAsync(final ApiCallback<File> _callback) throws ApiException {

		okhttp3.Call localVarCall = openApiValidateBeforeCall(_callback);
		Type localVarReturnType = new TypeToken<File>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}

	/**
	 * Build call for shutdown
	 * @param force  (optional, default to false)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call shutdownCall(Boolean force, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/shutdown";

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		if (force != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("force", force));
		}

		final String[] localVarAccepts = {

		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call shutdownValidateBeforeCall(Boolean force, final ApiCallback _callback) throws ApiException {

		okhttp3.Call localVarCall = shutdownCall(force, _callback);
		return localVarCall;

	}

	/**
	 * Shutdown controller
	 *
	 * @param force  (optional, default to false)
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public void shutdown(Boolean force) throws ApiException {
		shutdownWithHttpInfo(force);
	}

	/**
	 * Shutdown controller
	 *
	 * @param force  (optional, default to false)
	 * @return ApiResponse&lt;Void&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Void> shutdownWithHttpInfo(Boolean force) throws ApiException {
		okhttp3.Call localVarCall = shutdownValidateBeforeCall(force, null);
		return localVarApiClient.execute(localVarCall);
	}

	/**
	 * Shutdown controller (asynchronously)
	 *
	 * @param force  (optional, default to false)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call shutdownAsync(Boolean force, final ApiCallback<Void> _callback) throws ApiException {

		okhttp3.Call localVarCall = shutdownValidateBeforeCall(force, _callback);
		localVarApiClient.executeAsync(localVarCall, _callback);
		return localVarCall;
	}

	/**
	 * Build call for startBenchmark
	 * @param name  (required)
	 * @param desc Run description (optional)
	 * @param xTriggerJob URL of CI job that triggers the run. (optional)
	 * @param runId Run ID of run that was already requested but not started. (optional)
	 * @param templateParam Template parameter in format KEY&#x3D;VALUE (optional)
	 * @param _callback Callback for upload/download progress
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 *

	 */
	public okhttp3.Call startBenchmarkCall(String name, String desc, String xTriggerJob, String runId,
			List<String> templateParam, final ApiCallback _callback) throws ApiException {
		String basePath = null;
		// Operation Servers
		String[] localBasePaths = new String[] {};

		// Determine Base Path to Use
		if (localCustomBaseUrl != null) {
			basePath = localCustomBaseUrl;
		} else if (localBasePaths.length > 0) {
			basePath = localBasePaths[localHostIndex];
		} else {
			basePath = null;
		}

		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/benchmark/{name}/start"
				.replaceAll("\\{" + "name" + "\\}", localVarApiClient.escapeString(name.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, String> localVarCookieParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		if (desc != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("desc", desc));
		}

		if (runId != null) {
			localVarQueryParams.addAll(localVarApiClient.parameterToPair("runId", runId));
		}

		if (templateParam != null) {
			localVarCollectionQueryParams.addAll(localVarApiClient.parameterToPairs("multi", "templateParam", templateParam));
		}

		if (xTriggerJob != null) {
			localVarHeaderParams.put("x-trigger-job", localVarApiClient.parameterToString(xTriggerJob));
		}

		final String[] localVarAccepts = {
				"application/json"
		};
		final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null) {
			localVarHeaderParams.put("Accept", localVarAccept);
		}

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
		if (localVarContentType != null) {
			localVarHeaderParams.put("Content-Type", localVarContentType);
		}

		String[] localVarAuthNames = new String[] {};
		return localVarApiClient.buildCall(basePath, localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
	}

	@SuppressWarnings("rawtypes")
	private okhttp3.Call startBenchmarkValidateBeforeCall(String name, String desc, String xTriggerJob, String runId,
			List<String> templateParam, final ApiCallback _callback) throws ApiException {

		// verify the required parameter 'name' is set
		if (name == null) {
			throw new ApiException("Missing the required parameter 'name' when calling startBenchmark(Async)");
		}

		okhttp3.Call localVarCall = startBenchmarkCall(name, desc, xTriggerJob, runId, templateParam, _callback);
		return localVarCall;

	}

	/**
	 * Start a new run of this benchmark.
	 *
	 * @param name  (required)
	 * @param desc Run description (optional)
	 * @param xTriggerJob URL of CI job that triggers the run. (optional)
	 * @param runId Run ID of run that was already requested but not started. (optional)
	 * @param templateParam Template parameter in format KEY&#x3D;VALUE (optional)
	 * @return Run
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public Run startBenchmark(String name, String desc, String xTriggerJob, String runId, List<String> templateParam)
			throws ApiException {
		ApiResponse<Run> localVarResp = startBenchmarkWithHttpInfo(name, desc, xTriggerJob, runId, templateParam);
		return localVarResp.getData();
	}

	/**
	 * Start a new run of this benchmark.
	 *
	 * @param name  (required)
	 * @param desc Run description (optional)
	 * @param xTriggerJob URL of CI job that triggers the run. (optional)
	 * @param runId Run ID of run that was already requested but not started. (optional)
	 * @param templateParam Template parameter in format KEY&#x3D;VALUE (optional)
	 * @return ApiResponse&lt;Run&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 *

	 */
	public ApiResponse<Run> startBenchmarkWithHttpInfo(String name, String desc, String xTriggerJob, String runId,
			List<String> templateParam) throws ApiException {
		okhttp3.Call localVarCall = startBenchmarkValidateBeforeCall(name, desc, xTriggerJob, runId, templateParam, null);
		Type localVarReturnType = new TypeToken<Run>() {
		}.getType();
		return localVarApiClient.execute(localVarCall, localVarReturnType);
	}

	/**
	 * Start a new run of this benchmark. (asynchronously)
	 *
	 * @param name  (required)
	 * @param desc Run description (optional)
	 * @param xTriggerJob URL of CI job that triggers the run. (optional)
	 * @param runId Run ID of run that was already requested but not started. (optional)
	 * @param templateParam Template parameter in format KEY&#x3D;VALUE (optional)
	 * @param _callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 *

	 */
	public okhttp3.Call startBenchmarkAsync(String name, String desc, String xTriggerJob, String runId,
			List<String> templateParam, final ApiCallback<Run> _callback) throws ApiException {

		okhttp3.Call localVarCall = startBenchmarkValidateBeforeCall(name, desc, xTriggerJob, runId, templateParam, _callback);
		Type localVarReturnType = new TypeToken<Run>() {
		}.getType();
		localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
		return localVarCall;
	}
}
