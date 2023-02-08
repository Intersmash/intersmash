package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec;

import java.util.List;
import java.util.Map;

import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.client.spec.KeycloakProtocolMapper;

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
public class KeycloakClientScope {

	private Map<String, String> attributes;

	private String description;

	private String id;

	private String name;

	private String protocol;

	/**
	 * Protocol Mappers.
	 */
	private List<KeycloakProtocolMapper> protocolMappers;
}
