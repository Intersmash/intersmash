package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * InfinispanSecurity info for the user application connection
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class InfinispanSecurity {

	/**
	 * Specify the name of the authentication secret that contains credentials
	 */
	private String endpointSecretName;

	/**
	 * Specify how traffic to and from Data Grid endpoints is encrypted
	 */
	private EndpointEncryption endpointEncryption;
}
