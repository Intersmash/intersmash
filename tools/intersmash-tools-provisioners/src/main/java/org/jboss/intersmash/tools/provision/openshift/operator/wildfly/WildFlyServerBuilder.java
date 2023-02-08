package org.jboss.intersmash.tools.provision.openshift.operator.wildfly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.spec.StandaloneConfigMapSpec;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.spec.StorageSpec;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.spec.WildFlyServerSpec;

import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class WildFlyServerBuilder {
	private String name;
	private Map<String, String> labels;
	private String applicationImage;
	private Integer replicas;
	private StandaloneConfigMapSpec standaloneConfigMap;
	private StorageSpec storage;
	private String serviceAccountName;
	private List<EnvFromSource> envFrom;
	private List<EnvVar> env;
	private Boolean sessionAffinity;
	private List<String> secrets;
	private List<String> configMaps;
	private Boolean disableHTTPRoute;

	/**
	 * Initialize the {@link WildFlyServerBuilder} with given resource name.
	 *
	 * @param name resource object name
	 */
	public WildFlyServerBuilder(String name) {
		this.name = name;
	}

	/**
	 * Initialize the {@link WildFlyServerBuilder} with given resource name and labels.
	 *
	 * @param name resource object name
	 * @param labels key/value pairs that are attached to objects
	 */
	public WildFlyServerBuilder(String name, Map<String, String> labels) {
		this.name = name;
		this.labels = labels;
	}

	/**
	 * Name of the application image to be deployed.
	 *
	 * @param applicationImage Application image that should be deployed
	 * @return this
	 */
	public WildFlyServerBuilder applicationImage(String applicationImage) {
		this.applicationImage = applicationImage;
		return this;
	}

	/**
	 * Number of instances for a {@link WildFlyServer} resource.
	 *
	 * @param replicas Desired number of instances to be deployed
	 * @return this
	 */
	public WildFlyServerBuilder replicas(Integer replicas) {
		this.replicas = replicas;
		return this;
	}

	/**
	 * Spec to specify how standalone configuration can be read from a ConfigMap.
	 *
	 * @param standaloneConfigMap {@link StandaloneConfigMapSpec} instance that stores the desired standalone
	 *                                                           configuration
	 * @return this
	 */
	public WildFlyServerBuilder standaloneConfigMap(StandaloneConfigMapSpec standaloneConfigMap) {
		this.standaloneConfigMap = standaloneConfigMap;
		return this;
	}

	/**
	 * Storage spec to specify how storage should be used.
	 *
	 * @param storage {@link StandaloneConfigMapSpec} instance that contains the storage configuration
	 * @return this
	 */
	public WildFlyServerBuilder storage(StorageSpec storage) {
		this.storage = storage;
		return this;
	}

	/**
	 * Name of the {@code ServiceAccount} to use to run the WildFlyServer Pods.
	 *
	 * @param serviceAccountName Name of of the {@code ServiceAccount} to use to run the WildFlyServer Pods.
	 * @return this
	 */
	public WildFlyServerBuilder serviceAccountName(String serviceAccountName) {
		this.serviceAccountName = serviceAccountName;
		return this;
	}

	/**
	 * Set a list of environment variable present in the containers from source (either ConfigMap or Secret).
	 *
	 * @param envFrom Desired list of environment variable to be set in the containers from source (either ConfigMap or Secret).
	 * @return this
	 */
	public WildFlyServerBuilder envFrom(List<EnvFromSource> envFrom) {
		this.envFrom = envFrom;
		return this;
	}

	/**
	 * Set an environment variable to be present in the containers from source (either ConfigMap or Secret).
	 *
	 * @param envFrom Desired environment variable to be set in the containers from source (either ConfigMap or Secret).
	 * @return this
	 */
	public WildFlyServerBuilder envFrom(EnvFromSource envFrom) {
		if (this.envFrom == null) {
			this.envFrom = new ArrayList<>();
		}
		this.envFrom.add(envFrom);
		return this;
	}

	/**
	 * Set a list of environment variable to be present in the containers.
	 *
	 * @param env Desired List of environment variable to be set in the containers.
	 * @return this
	 */
	public WildFlyServerBuilder env(List<EnvVar> env) {
		this.env = env;
		return this;
	}

	/**
	 * Environment variable present in the containers.
	 *
	 * @param env Desired environment variable to be set in the containers
	 * @return this
	 */
	public WildFlyServerBuilder env(EnvVar env) {
		if (this.env == null) {
			this.env = new ArrayList<>();
		}
		this.env.add(env);
		return this;
	}

	/**
	 * Whether connections from the same client ip are passed to the same WildFlyServer instance/pod each time
	 *
	 * @param sessionAffinity Whether connections from the same client ip should be passed to the same WildFlyServer
	 *                           instance/pod each time
	 * @return this
	 */
	public WildFlyServerBuilder sessionAffinity(Boolean sessionAffinity) {
		this.sessionAffinity = sessionAffinity;
		return this;
	}

	/**
	 * List of secret names to mount as volumes in the containers. Each secret is mounted as a read-only volume under
	 * /etc/secrets/&lt;secret name&gt;
	 *
	 * @param secrets List of desired secret names to mount as volumes in the containers
	 * @return this
	 */
	public WildFlyServerBuilder secrets(List<String> secrets) {
		this.secrets = secrets;
		return this;
	}

	/**
	 * Secret name to mount as volume in the containers. Each secret is mounted as a read-only volume under
	 * /etc/secrets/&lt;secret name&gt;
	 *
	 * @param secret Desired secret names to mount as volume in the containers
	 * @return this
	 */
	public WildFlyServerBuilder secrets(String secret) {
		if (this.secrets == null) {
			this.secrets = new ArrayList<>();
		}
		this.secrets.add(secret);
		return this;
	}

	/**
	 * List of ConfigMap names to mount as volumes in the containers. Each config map is mounted as a read-only
	 * volume under /etc/configmaps/&lt;config map name&gt;
	 *
	 * @param configMaps List of desired config map names to mount as volumes in the containers
	 * @return this
	 */
	public WildFlyServerBuilder configMaps(List<String> configMaps) {
		this.configMaps = configMaps;
		return this;
	}

	/**
	 * ConfigMap name to mount as volumes in the containers. Each config map is mounted as a read-only volume under
	 * /etc/configmaps/&lt;config map name&gt;
	 *
	 * @param configMap Desired config map name to mount as volume in the containers
	 * @return this
	 */
	public WildFlyServerBuilder configMaps(String configMap) {
		if (this.configMaps == null) {
			this.configMaps = new ArrayList<>();
		}
		this.configMaps.add(configMap);
		return this;
	}

	/**
	 * Disable the creation a route to the HTTP port of the application service.
	 *
	 * @param disableHTTPRoute Whether the creation a route to the HTTP port of the application service should be
	 *                         disabled
	 * @return this
	 */
	public WildFlyServerBuilder disableHTTPRoute(Boolean disableHTTPRoute) {
		this.disableHTTPRoute = disableHTTPRoute;
		return this;
	}

	public WildFlyServer build() {
		WildFlyServer wildFlyServer = new WildFlyServer();
		wildFlyServer.setMetadata(new ObjectMeta());
		wildFlyServer.getMetadata().setName(name);
		wildFlyServer.getMetadata().setLabels(labels);

		WildFlyServerSpec wildFlyServerSpec = new WildFlyServerSpec();
		wildFlyServerSpec.setApplicationImage(applicationImage);
		wildFlyServerSpec.setReplicas(replicas);
		wildFlyServerSpec.setStandaloneConfigMap(standaloneConfigMap);
		wildFlyServerSpec.setStorage(storage);
		wildFlyServerSpec.setServiceAccountName(serviceAccountName);
		wildFlyServerSpec.setEnvFrom(envFrom);
		wildFlyServerSpec.setEnv(env);
		wildFlyServerSpec.setSessionAffinity(sessionAffinity);
		wildFlyServerSpec.setSecrets(secrets);
		wildFlyServerSpec.setConfigMaps(configMaps);
		wildFlyServerSpec.setDisableHTTPRoute(disableHTTPRoute);
		wildFlyServer.setSpec(wildFlyServerSpec);

		return wildFlyServer;
	}
}
