package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class FederatedIdentity {

	/**
	 * Federated Identity Provider.
	 */
	private String identityProvider;

	/**
	 * Federated Identity User ID.
	 */
	private String userId;

	/**
	 * Federated Identity User Name.
	 */
	private String userName;
}
