package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.status;

import java.util.List;

import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.spec.CacheCondition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * CacheStatus defines the observed state of Cache
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CacheStatus {

	/**
	 * Conditions list for this cache
	 */
	private List<CacheCondition> conditions;

	/**
	 * Service name that exposes the cache inside the cluster
	 */
	private String serviceName;
}
