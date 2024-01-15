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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
		"startTime",
		"endTime",
		"minResponseTime",
		"meanResponseTime",
		"maxResponseTime",
		"meanSendTime",
		"percentileResponseTime",
		"connectFailureCount",
		"requestCount",
		"responseCount",
		"status_2xx",
		"status_3xx",
		"status_4xx",
		"status_5xx",
		"status_other",
		"invalid",
		"cacheHits",
		"resetCount",
		"timeouts",
		"blockedCount",
		"blockedTime"
})
@Generated("jsonschema2pojo")
public class Summary {

	@JsonProperty("startTime")
	private Long startTime;
	@JsonProperty("endTime")
	private Long endTime;
	@JsonProperty("minResponseTime")
	private Long minResponseTime;
	@JsonProperty("meanResponseTime")
	private Long meanResponseTime;
	@JsonProperty("maxResponseTime")
	private Long maxResponseTime;
	@JsonProperty("meanSendTime")
	private Long meanSendTime;
	@JsonProperty("percentileResponseTime")
	private Percentiles percentileResponseTime;
	@JsonProperty("connectFailureCount")
	private Long connectFailureCount;
	@JsonProperty("requestCount")
	private Long requestCount;
	@JsonProperty("responseCount")
	private Long responseCount;
	@JsonProperty("status_2xx")
	private Long status2xx;
	@JsonProperty("status_3xx")
	private Long status3xx;
	@JsonProperty("status_4xx")
	private Long status4xx;
	@JsonProperty("status_5xx")
	private Long status5xx;
	@JsonProperty("status_other")
	private Long statusOther;
	@JsonProperty("invalid")
	private Long invalid;
	@JsonProperty("cacheHits")
	private Long cacheHits;
	@JsonProperty("resetCount")
	private Long resetCount;
	@JsonProperty("timeouts")
	private Long timeouts;
	@JsonProperty("blockedCount")
	private Long blockedCount;
	@JsonProperty("blockedTime")
	private Long blockedTime;

	@JsonProperty("startTime")
	public Long getStartTime() {
		return startTime;
	}

	@JsonProperty("startTime")
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	@JsonProperty("endTime")
	public Long getEndTime() {
		return endTime;
	}

	@JsonProperty("endTime")
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	@JsonProperty("minResponseTime")
	public Long getMinResponseTime() {
		return minResponseTime;
	}

	@JsonProperty("minResponseTime")
	public void setMinResponseTime(Long minResponseTime) {
		this.minResponseTime = minResponseTime;
	}

	@JsonProperty("meanResponseTime")
	public Long getMeanResponseTime() {
		return meanResponseTime;
	}

	@JsonProperty("meanResponseTime")
	public void setMeanResponseTime(Long meanResponseTime) {
		this.meanResponseTime = meanResponseTime;
	}

	@JsonProperty("maxResponseTime")
	public Long getMaxResponseTime() {
		return maxResponseTime;
	}

	@JsonProperty("maxResponseTime")
	public void setMaxResponseTime(Long maxResponseTime) {
		this.maxResponseTime = maxResponseTime;
	}

	@JsonProperty("meanSendTime")
	public Long getMeanSendTime() {
		return meanSendTime;
	}

	@JsonProperty("meanSendTime")
	public void setMeanSendTime(Long meanSendTime) {
		this.meanSendTime = meanSendTime;
	}

	@JsonProperty("percentileResponseTime")
	public Percentiles getPercentileResponseTime() {
		return percentileResponseTime;
	}

	@JsonProperty("percentileResponseTime")
	public void setPercentileResponseTime(Percentiles percentileResponseTime) {
		this.percentileResponseTime = percentileResponseTime;
	}

	@JsonProperty("connectFailureCount")
	public Long getConnectFailureCount() {
		return connectFailureCount;
	}

	@JsonProperty("connectFailureCount")
	public void setConnectFailureCount(Long connectFailureCount) {
		this.connectFailureCount = connectFailureCount;
	}

	@JsonProperty("requestCount")
	public Long getRequestCount() {
		return requestCount;
	}

	@JsonProperty("requestCount")
	public void setRequestCount(Long requestCount) {
		this.requestCount = requestCount;
	}

	@JsonProperty("responseCount")
	public Long getResponseCount() {
		return responseCount;
	}

	@JsonProperty("responseCount")
	public void setResponseCount(Long responseCount) {
		this.responseCount = responseCount;
	}

	@JsonProperty("status_2xx")
	public Long getStatus2xx() {
		return status2xx;
	}

	@JsonProperty("status_2xx")
	public void setStatus2xx(Long status2xx) {
		this.status2xx = status2xx;
	}

	@JsonProperty("status_3xx")
	public Long getStatus3xx() {
		return status3xx;
	}

	@JsonProperty("status_3xx")
	public void setStatus3xx(Long status3xx) {
		this.status3xx = status3xx;
	}

	@JsonProperty("status_4xx")
	public Long getStatus4xx() {
		return status4xx;
	}

	@JsonProperty("status_4xx")
	public void setStatus4xx(Long status4xx) {
		this.status4xx = status4xx;
	}

	@JsonProperty("status_5xx")
	public Long getStatus5xx() {
		return status5xx;
	}

	@JsonProperty("status_5xx")
	public void setStatus5xx(Long status5xx) {
		this.status5xx = status5xx;
	}

	@JsonProperty("status_other")
	public Long getStatusOther() {
		return statusOther;
	}

	@JsonProperty("status_other")
	public void setStatusOther(Long statusOther) {
		this.statusOther = statusOther;
	}

	@JsonProperty("invalid")
	public Long getInvalid() {
		return invalid;
	}

	@JsonProperty("invalid")
	public void setInvalid(Long invalid) {
		this.invalid = invalid;
	}

	@JsonProperty("cacheHits")
	public Long getCacheHits() {
		return cacheHits;
	}

	@JsonProperty("cacheHits")
	public void setCacheHits(Long cacheHits) {
		this.cacheHits = cacheHits;
	}

	@JsonProperty("resetCount")
	public Long getResetCount() {
		return resetCount;
	}

	@JsonProperty("resetCount")
	public void setResetCount(Long resetCount) {
		this.resetCount = resetCount;
	}

	@JsonProperty("timeouts")
	public Long getTimeouts() {
		return timeouts;
	}

	@JsonProperty("timeouts")
	public void setTimeouts(Long timeouts) {
		this.timeouts = timeouts;
	}

	@JsonProperty("blockedCount")
	public Long getBlockedCount() {
		return blockedCount;
	}

	@JsonProperty("blockedCount")
	public void setBlockedCount(Long blockedCount) {
		this.blockedCount = blockedCount;
	}

	@JsonProperty("blockedTime")
	public Long getBlockedTime() {
		return blockedTime;
	}

	@JsonProperty("blockedTime")
	public void setBlockedTime(Long blockedTime) {
		this.blockedTime = blockedTime;
	}

}
