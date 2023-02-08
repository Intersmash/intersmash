package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * EndpointEncryption configuration
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class EndpointEncryption {

	/**
	 * Specify the configured endpoint encryption type
	 */
	private String type;

	/**
	 * Names the service certificate CA
	 */
	private String certServiceName;

	/**
	 * Names the secret that contains a service certificate
	 */
	private String certSecretName;
}
