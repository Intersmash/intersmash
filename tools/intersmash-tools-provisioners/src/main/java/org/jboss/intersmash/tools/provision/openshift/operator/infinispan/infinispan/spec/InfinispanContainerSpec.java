package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Specify resource requirements per container
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class InfinispanContainerSpec {

	/**
	 * Specifies JVM options
	 */
	private String extraJvmOpts;

	/**
	 * Specify host CPU resources to node, measured in CPU units
	 */
	private String memory;

	/**
	 * Specify host memory resources to nodes, measured in bytes
	 */
	private String cpu;
}
