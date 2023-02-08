package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.status;

import java.util.List;

import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.InfinispanCondition;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.InfinispanSecurity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * InfinispanStatus defines the observed state of Infinispan
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class InfinispanStatus {

	/**
	 * Conditions that define the Infinispan resource status
	 */
	private List<InfinispanCondition> conditions;

	/**
	 * Names the Infinispan resource stateful set
	 */
	private String statefulSetName;

	/**
	 * Specify the security configuration
	 */
	private InfinispanSecurity security;

	/**
	 * Specify the number of replicas needed at restart
	 */
	private int replicasWantedAtRestart;
}
