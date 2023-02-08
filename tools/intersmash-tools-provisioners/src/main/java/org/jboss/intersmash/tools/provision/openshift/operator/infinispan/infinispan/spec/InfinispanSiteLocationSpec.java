package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Specify connection information for backup locations
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class InfinispanSiteLocationSpec {

	/**
	 * Location name
	 */
	private String name;

	/**
	 * Location URL
	 */
	private String url;

	/**
	 * Names the secret used to encrypt communication with the location
	 */
	private String secretName;
}
