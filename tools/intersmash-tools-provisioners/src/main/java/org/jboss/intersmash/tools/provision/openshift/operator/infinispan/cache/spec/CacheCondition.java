package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
/**
 * CacheCondition define a condition of the cluster
 */
public class CacheCondition {

	/**
	 * Type is the type of the condition.
	 */
	private String type;

	/**
	 * Status is the status of the condition.
	 */
	private String status;

	/**
	 * Human-readable message indicating details about last transition.
	 */
	private String message;
}
