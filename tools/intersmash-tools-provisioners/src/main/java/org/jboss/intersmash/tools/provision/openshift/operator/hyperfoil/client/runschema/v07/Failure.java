
package org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.runschema.v07;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
		"phase",
		"metric",
		"message",
		"start",
		"end",
		"percentileResponseTime"
})
@Generated("jsonschema2pojo")
public class Failure {

	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("phase")
	private String phase;
	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("metric")
	private String metric;
	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("message")
	private String message;
	@JsonProperty("start")
	private Long start;
	@JsonProperty("end")
	private Long end;
	@JsonProperty("percentileResponseTime")
	private Percentiles__1 percentileResponseTime;

	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("phase")
	public String getPhase() {
		return phase;
	}

	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("phase")
	public void setPhase(String phase) {
		this.phase = phase;
	}

	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("metric")
	public String getMetric() {
		return metric;
	}

	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("metric")
	public void setMetric(String metric) {
		this.metric = metric;
	}

	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("message")
	public String getMessage() {
		return message;
	}

	/**
	 *
	 * (Required)
	 *
	 */
	@JsonProperty("message")
	public void setMessage(String message) {
		this.message = message;
	}

	@JsonProperty("start")
	public Long getStart() {
		return start;
	}

	@JsonProperty("start")
	public void setStart(Long start) {
		this.start = start;
	}

	@JsonProperty("end")
	public Long getEnd() {
		return end;
	}

	@JsonProperty("end")
	public void setEnd(Long end) {
		this.end = end;
	}

	@JsonProperty("percentileResponseTime")
	public Percentiles__1 getPercentileResponseTime() {
		return percentileResponseTime;
	}

	@JsonProperty("percentileResponseTime")
	public void setPercentileResponseTime(Percentiles__1 percentileResponseTime) {
		this.percentileResponseTime = percentileResponseTime;
	}

}
