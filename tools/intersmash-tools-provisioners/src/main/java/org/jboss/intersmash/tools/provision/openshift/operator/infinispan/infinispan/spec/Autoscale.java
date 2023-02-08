package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Autoscale describe autoscaling configuration for the cluster
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Autoscale {

	/**
	 * Defines the maximum number of nodes for the cluster.
	 */
	private int maxReplicas;

	/**
	 * Defines the minimum number of nodes for the cluster
	 */
	private int minReplicas;

	/**
	 * Configures the maximum threshold, as a percentage, for memory usage on each node
	 */
	private int maxMemUsagePercent;

	/**
	 * Configures the minimum threshold, as a percentage, for memory usage across the cluster
	 */
	private int minMemUsagePercent;
}
