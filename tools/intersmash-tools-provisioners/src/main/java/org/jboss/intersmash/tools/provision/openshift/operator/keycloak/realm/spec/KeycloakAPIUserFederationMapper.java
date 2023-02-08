package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec;

import java.util.Map;

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
public class KeycloakAPIUserFederationMapper {

	/**
	 * User federation mapper config.
	 */
	private Map<String, String> config;

	private String name;

	private String id;

	private String federationMapperType;

	/**
	 * The displayName for the user federation provider this mapper applies to.
	 */
	private String federationProviderDisplayName;
}
