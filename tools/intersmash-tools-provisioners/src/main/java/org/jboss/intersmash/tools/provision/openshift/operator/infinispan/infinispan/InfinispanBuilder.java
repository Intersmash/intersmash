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

import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.Autoscale;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.ExposeSpec;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.InfinispanContainerSpec;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.InfinispanLoggingSpec;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.InfinispanSecurity;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.InfinispanServiceSpec;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.InfinispanSpec;

import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class InfinispanBuilder {
	private String name;
	private Map<String, String> labels;
	private int replicas;
	private String image;
	private InfinispanSecurity security;
	private InfinispanContainerSpec container;
	private InfinispanServiceSpec service;
	private InfinispanLoggingSpec logging;
	private ExposeSpec expose;
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
	 * see https://infinispan.org/infinispan-operator/2.0.x/operator.html#ref_image_crd-ref
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
	 * see https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v1/infinispan_types.go#L14
	 *
	 * @param security {@link InfinispanSecurity} info for the user application connection
	 * @return this
	 */
	public InfinispanBuilder security(InfinispanSecurity security) {
		this.security = security;
		return this;
	}

	/**
	 * Specify resource requirements per container
	 * see https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v1/infinispan_types.go#L46
	 *
	 * @param container {@link InfinispanContainerSpec} instance storing resource requirements per container
	 * @return this
	 */
	public InfinispanBuilder container(InfinispanContainerSpec container) {
		this.container = container;
		return this;
	}

	/**
	 * Specify configuration for specific service
	 * see https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v1/infinispan_types.go#L68
	 *
	 * @param service {@link InfinispanServiceSpec} instance representing configuration for specific service
	 * @return this
	 */
	public InfinispanBuilder service(InfinispanServiceSpec service) {
		this.service = service;
		return this;
	}

	/**
	 * Specify configuration for the logging functionality
	 *
	 * @param logging {@link InfinispanLoggingSpec} instance representing configuration for the logging functionality
	 * @return this
	 */
	public InfinispanBuilder logging(InfinispanLoggingSpec logging) {
		this.logging = logging;
		return this;
	}

	/**
	 * Describe how Infinispan will be exposed externally
	 * see https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v1/infinispan_types.go#L122
	 *
	 * @param expose {@link ExposeSpec} instance, describing how Infinispan will be exposed externally
	 * @return this
	 */
	public InfinispanBuilder expose(ExposeSpec expose) {
		this.expose = expose;
		return this;
	}

	/**
	 * Describe autoscaling configuration for the cluster
	 * see https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v1/infinispan_types.go#L130
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
