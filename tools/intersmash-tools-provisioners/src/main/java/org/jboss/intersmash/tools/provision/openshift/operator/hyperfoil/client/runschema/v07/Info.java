
package org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.runschema.v07;

import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * General information about the run.
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
		"id",
		"benchmark",
		"startTime",
		"terminateTime",
		"description",
		"cancelled",
		"errors"
})
@Generated("jsonschema2pojo")
public class Info {

	/**
	 * Unique identifier for the run, by convention 4 upper-case hexadecimal digits.
	 * (Required)
	 *
	 */
	@JsonProperty("id")
	@JsonPropertyDescription("Unique identifier for the run, by convention 4 upper-case hexadecimal digits.")
	private String id;
	/**
	 * Name of the executed benchmark.
	 * (Required)
	 *
	 */
	@JsonProperty("benchmark")
	@JsonPropertyDescription("Name of the executed benchmark.")
	private String benchmark;
	/**
	 * Run start time in epoch milliseconds, or negative value if the run did not start.
	 * (Required)
	 *
	 */
	@JsonProperty("startTime")
	@JsonPropertyDescription("Run start time in epoch milliseconds, or negative value if the run did not start.")
	private Long startTime;
	/**
	 * Run end time in epoch milliseconds, or negative value if the run did not complete.
	 * (Required)
	 *
	 */
	@JsonProperty("terminateTime")
	@JsonPropertyDescription("Run end time in epoch milliseconds, or negative value if the run did not complete.")
	private Long terminateTime;
	/**
	 * Arbitrary description of the run (e.g. SUT setup)
	 *
	 */
	@JsonProperty("description")
	@JsonPropertyDescription("Arbitrary description of the run (e.g. SUT setup)")
	private String description;
	/**
	 * True if the run was prematurely cancelled.
	 *
	 */
	@JsonProperty("cancelled")
	@JsonPropertyDescription("True if the run was prematurely cancelled.")
	private Boolean cancelled;
	@JsonProperty("errors")
	private List<Error> errors = null;

	/**
	 * Unique identifier for the run, by convention 4 upper-case hexadecimal digits.
	 * (Required)
	 *
	 */
	@JsonProperty("id")
	public String getId() {
		return id;
	}

	/**
	 * Unique identifier for the run, by convention 4 upper-case hexadecimal digits.
	 * (Required)
	 *
	 */
	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Name of the executed benchmark.
	 * (Required)
	 *
	 */
	@JsonProperty("benchmark")
	public String getBenchmark() {
		return benchmark;
	}

	/**
	 * Name of the executed benchmark.
	 * (Required)
	 *
	 */
	@JsonProperty("benchmark")
	public void setBenchmark(String benchmark) {
		this.benchmark = benchmark;
	}

	/**
	 * Run start time in epoch milliseconds, or negative value if the run did not start.
	 * (Required)
	 *
	 */
	@JsonProperty("startTime")
	public Long getStartTime() {
		return startTime;
	}

	/**
	 * Run start time in epoch milliseconds, or negative value if the run did not start.
	 * (Required)
	 *
	 */
	@JsonProperty("startTime")
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	/**
	 * Run end time in epoch milliseconds, or negative value if the run did not complete.
	 * (Required)
	 *
	 */
	@JsonProperty("terminateTime")
	public Long getTerminateTime() {
		return terminateTime;
	}

	/**
	 * Run end time in epoch milliseconds, or negative value if the run did not complete.
	 * (Required)
	 *
	 */
	@JsonProperty("terminateTime")
	public void setTerminateTime(Long terminateTime) {
		this.terminateTime = terminateTime;
	}

	/**
	 * Arbitrary description of the run (e.g. SUT setup)
	 *
	 */
	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	/**
	 * Arbitrary description of the run (e.g. SUT setup)
	 *
	 */
	@JsonProperty("description")
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * True if the run was prematurely cancelled.
	 *
	 */
	@JsonProperty("cancelled")
	public Boolean getCancelled() {
		return cancelled;
	}

	/**
	 * True if the run was prematurely cancelled.
	 *
	 */
	@JsonProperty("cancelled")
	public void setCancelled(Boolean cancelled) {
		this.cancelled = cancelled;
	}

	@JsonProperty("errors")
	public List<Error> getErrors() {
		return errors;
	}

	@JsonProperty("errors")
	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}

}
