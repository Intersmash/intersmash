package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AdminAuth description of the auth info
 */
@Deprecated
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class AdminAuth {

	/**
	 * Name of the secret containing both admin username and password
	 */
	private String secretName;

	/**
	 * Secret and key containing the admin username for authentication.
	 */
	private String username;

	/**
	 * Secret and key containing the admin password for authentication.
	 */
	private String password;
}
