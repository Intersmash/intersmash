package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.client.spec;

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
public class KeycloakProtocolMapper {

	/**
	 * Protocol Mapper ID.
	 */
	private String id;

	/**
	 * Protocol Mapper Name.
	 */
	private String name;

	/**
	 * Protocol to use.
	 */
	private String protocol;

	/**
	 * Protocol Mapper to use.
	 */
	private String protocolMapper;

	/**
	 * True if Consent Screen is required.
	 */
	private boolean consentRequired;

	/**
	 * Text to use for displaying Consent Screen.
	 */
	private String consentText;

	/**
	 * Config options.
	 */
	private Map<String, String> config;
}
