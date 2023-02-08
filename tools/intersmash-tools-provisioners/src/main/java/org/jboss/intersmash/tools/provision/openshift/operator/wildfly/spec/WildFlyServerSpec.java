package org.jboss.intersmash.tools.provision.openshift.operator.wildfly.spec;

import java.util.List;

import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.WildFlyServer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.EnvVar;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <a href="https://github.com/wildfly/wildfly-operator/blob/master/doc/apis.adoc#wildflyserverspec">WildFlyServerSpec</a>
 * is a specification of the desired behavior of the WildFly resource.
 * <p>
 * It uses a {@link io.fabric8.kubernetes.api.model.apps.StatefulSet} with a pod spec that mounts the volume specified
 * by storage on /opt/jboss/wildfly/standalone/data.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class WildFlyServerSpec {

	/**
	 * Name of the application image to be deployed.
	 */
	private String applicationImage;

	/**
	 * Number of instances for a {@link WildFlyServer} resource.
	 */
	private Integer replicas;

	/**
	 * Spec to specify how standalone configuration can be read from a ConfigMap.
	 */
	private StandaloneConfigMapSpec standaloneConfigMap;

	/**
	 * Storage spec to specify how storage should be used.
	 */
	private StorageSpec storage;

	/**
	 * Name of the ServiceAccount to use to run the WildFlyServer Pods.
	 */
	private String serviceAccountName;

	/**
	 * List of environment variable present in the containers from source (either ConfigMap or Secret).
	 */
	private List<EnvFromSource> envFrom;

	/**
	 * List of environment variable present in the containers.
	 */
	private List<EnvVar> env;

	/**
	 * If connections from the same client ip are passed to the same WildFlyServer instance/pod each time
	 */
	private Boolean sessionAffinity;

	/**
	 * List of secret names to mount as volumes in the containers. Each secret is mounted as a read-only volume under /etc/secrets/<secret name>
	 */
	private List<String> secrets;

	/**
	 * List of ConfigMap names to mount as volumes in the containers. Each config map is mounted as a read-only volume under /etc/configmaps/<config map name>
	 */
	private List<String> configMaps;

	/**
	 * Disable the creation a route to the HTTP port of the application service.
	 */
	private Boolean disableHTTPRoute;
}
