package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Specify configuration for specific service
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class InfinispanServiceSpec {

	/**
	 * Specify Infinispan resource service type
	 */
	private String type;

	/**
	 * Specify resource requirements specific for service
	 */
	private InfinispanServiceContainerSpec container;

	/**
	 * Specify connection information for backup locations
	 */
	private InfinispanSitesSpec sites;

	/**
	 * Specify the number of owners
	 */
	private int replicationFactor;
}
