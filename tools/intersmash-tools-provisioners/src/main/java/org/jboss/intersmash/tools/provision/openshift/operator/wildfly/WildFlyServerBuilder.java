/**
 * Copyright (C) 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.intersmash.tools.provision.openshift.operator.wildfly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wildfly.v1alpha1.WildFlyServer;
import org.wildfly.v1alpha1.WildFlyServerSpec;
import org.wildfly.v1alpha1.wildflyserverspec.Env;
import org.wildfly.v1alpha1.wildflyserverspec.EnvFrom;
import org.wildfly.v1alpha1.wildflyserverspec.StandaloneConfigMap;
import org.wildfly.v1alpha1.wildflyserverspec.Storage;

import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class WildFlyServerBuilder {
	private String name;
	private Map<String, String> labels;
	private String applicationImage;
	private Integer replicas;
	private StandaloneConfigMap standaloneConfigMap;
	private Storage storage;
	private String serviceAccountName;
	private List<EnvFrom> envFrom;
	private List<Env> env;
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
	 * Number of instances for a {@link org.wildfly.v1alpha1.WildFlyServer} resource.
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
	 * @param standaloneConfigMap {@link StandaloneConfigMap} instance that stores the desired standalone
	 *                                                           configuration
	 * @return this
	 */
	public WildFlyServerBuilder standaloneConfigMap(StandaloneConfigMap standaloneConfigMap) {
		this.standaloneConfigMap = standaloneConfigMap;
		return this;
	}

	/**
	 * Storage spec to specify how storage should be used.
	 *
	 * @param storage {@link StandaloneConfigMap} instance that contains the storage configuration
	 * @return this
	 */
	public WildFlyServerBuilder storage(Storage storage) {
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
	public WildFlyServerBuilder envFrom(List<EnvFrom> envFrom) {
		this.envFrom = envFrom;
		return this;
	}

	/**
	 * Set an environment variable to be present in the containers from source (either ConfigMap or Secret).
	 *
	 * @param envFrom Desired environment variable to be set in the containers from source (either ConfigMap or Secret).
	 * @return this
	 */
	public WildFlyServerBuilder envFrom(EnvFrom envFrom) {
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
	public WildFlyServerBuilder env(List<Env> env) {
		this.env = env;
		return this;
	}

	/**
	 * Environment variable present in the containers.
	 *
	 * @param env Desired environment variable to be set in the containers
	 * @return this
	 */
	public WildFlyServerBuilder env(Env env) {
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
		if (labels != null) {
			wildFlyServer.getMetadata().setLabels(labels);
		}
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
