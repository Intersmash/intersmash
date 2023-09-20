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
package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan;

import java.util.Map;

import org.infinispan.v1.Infinispan;
import org.infinispan.v1.InfinispanSpec;
import org.infinispan.v1.infinispanspec.Autoscale;
import org.infinispan.v1.infinispanspec.Container;
import org.infinispan.v1.infinispanspec.Expose;
import org.infinispan.v1.infinispanspec.Logging;
import org.infinispan.v1.infinispanspec.Security;
import org.infinispan.v1.infinispanspec.Service;

import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class InfinispanBuilder {
	private String name;
	private Map<String, String> labels;
	private int replicas;
	private String image;
	private Security security;
	private Container container;
	private Service service;
	private Logging logging;
	private Expose expose;
	private Autoscale autoscale;

	/**
	 * Initialize the {@link InfinispanBuilder} with given resource name.
	 *
	 * @param name resource object name
	 */
	public InfinispanBuilder(String name) {
		this.name = name;
	}

	/**
	 * Initialize the {@link InfinispanBuilder} with given resource name and labels.
	 *
	 * @param name resource object name
	 * @param labels key/value pairs that are attached to objects
	 */
	public InfinispanBuilder(String name, Map<String, String> labels) {
		this.name = name;
		this.labels = labels;
	}

	/**
	 * Set the number of replicas
	 *
	 * @param replicas Number of replicas to be deployed.
	 * @return this
	 */
	public InfinispanBuilder replicas(int replicas) {
		this.replicas = replicas;
		return this;
	}

	/**
	 * Lets you specify a Infinispan image to use
	 *
	 * @param image Infinispan image to be deployed
	 * @return this
	 */
	public InfinispanBuilder image(String image) {
		this.image = image;
		return this;
	}

	/**
	 * Set Infinispan security info for the user application connection
	 *
	 * @param security {@link Security} info for the user application connection
	 * @return this
	 */
	public InfinispanBuilder security(Security security) {
		this.security = security;
		return this;
	}

	/**
	 * Specify resource requirements per container
	 *
	 * @param container {@link Container} instance storing resource requirements per container
	 * @return this
	 */
	public InfinispanBuilder container(Container container) {
		this.container = container;
		return this;
	}

	/**
	 * Specify configuration for specific service
	 *
	 * @param service {@link Service} instance representing configuration for specific service
	 * @return this
	 */
	public InfinispanBuilder service(Service service) {
		this.service = service;
		return this;
	}

	/**
	 * Specify configuration for the logging functionality
	 *
	 * @param logging {@link Logging} instance representing configuration for the logging functionality
	 * @return this
	 */
	public InfinispanBuilder logging(Logging logging) {
		this.logging = logging;
		return this;
	}

	/**
	 * Describe how Infinispan will be exposed externally
	 *
	 * @param expose {@link Expose} instance, describing how Infinispan will be exposed externally
	 * @return this
	 */
	public InfinispanBuilder expose(Expose expose) {
		this.expose = expose;
		return this;
	}

	/**
	 * Describe autoscaling configuration for the cluster
	 *
	 * @param autoscale {@link Autoscale} instance, describing autoscaling configuration for the cluster
	 * @return this
	 */
	public InfinispanBuilder autoscale(Autoscale autoscale) {
		this.autoscale = autoscale;
		return this;
	}

	public Infinispan build() {
		Infinispan infinispan = new Infinispan();
		infinispan.setMetadata(new ObjectMeta());
		infinispan.getMetadata().setName(name);
		infinispan.getMetadata().setLabels(labels);
		InfinispanSpec infinispanSpec = new InfinispanSpec();
		infinispanSpec.setReplicas(replicas);
		infinispanSpec.setImage(image);
		infinispanSpec.setSecurity(security);
		infinispanSpec.setContainer(container);
		infinispanSpec.setService(service);
		infinispanSpec.setLogging(logging);
		infinispanSpec.setExpose(expose);
		infinispanSpec.setAutoscale(autoscale);
		infinispan.setSpec(infinispanSpec);
		return infinispan;
	}
}
