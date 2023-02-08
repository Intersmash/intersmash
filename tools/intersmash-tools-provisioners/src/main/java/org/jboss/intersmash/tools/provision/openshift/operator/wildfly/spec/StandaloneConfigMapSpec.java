package org.jboss.intersmash.tools.provision.openshift.operator.wildfly.spec;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <a href="https://github.com/wildfly/wildfly-operator/blob/master/doc/apis.adoc#standaloneconfigmapspec">StandaloneConfigMapSpec</a>
 * defines how WildFly standalone configuration can be read from a {@link io.fabric8.kubernetes.api.model.ConfigMap}.
 * If omitted, WildFly uses its standalone.xml configuration from its image.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class StandaloneConfigMapSpec {

	/**
	 * Name of the {@link io.fabric8.kubernetes.api.model.ConfigMap} containing the standalone configuration XML file.
	 */
	private String name;

	/**
	 * Key of the {@link io.fabric8.kubernetes.api.model.ConfigMap} whose value is the
	 * standalone configuration XML file. If omitted, the spec will look for the standalone.xml key.
	 */
	private String key;
}
