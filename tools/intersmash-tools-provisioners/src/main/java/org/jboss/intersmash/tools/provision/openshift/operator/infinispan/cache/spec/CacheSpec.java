package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * CacheSpec defines the desired state of Cache
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CacheSpec {
	/**
	 * Authentication info
	 */
	private AdminAuth adminAuth;
	/**
	 * Name of the cluster where to create the cache
	 */
	private String clusterName;
	/**
	 * Name of the cache to be created. If empty ObjectMeta.Name will be used
	 */
	private String name;
	/**
	 * Cache template in XML format
	 */
	private String template;
	/**
	 * Name of the template to be used to create this cache
	 */
	private String templateName;
}
