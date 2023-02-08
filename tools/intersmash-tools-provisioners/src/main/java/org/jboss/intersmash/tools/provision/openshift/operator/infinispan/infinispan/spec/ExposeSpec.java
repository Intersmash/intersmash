package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ExposeSpec describe how Infinispan will be exposed externally
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ExposeSpec {

	/**
	 * Type specifies different exposition methods for data grid
	 */
	private String type;

	/**
	 * Port where Data Grid is exposed
	 */
	private int nodePort;

	/**
	 * Host where Data Grid is exposed
	 */
	private String host;
}
