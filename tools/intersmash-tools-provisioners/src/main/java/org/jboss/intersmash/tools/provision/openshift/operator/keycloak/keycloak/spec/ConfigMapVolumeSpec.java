package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.fabric8.kubernetes.api.model.KeyToPath;
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
public class ConfigMapVolumeSpec {

	/**
	 * ConfigMap name.
	 */
	private String name;

	/**
	 * An absolute path where to mount it.
	 */
	private String mountPath;

	/**
	 * ConfigMap mount details.
	 */
	private List<KeyToPath> items;
}
