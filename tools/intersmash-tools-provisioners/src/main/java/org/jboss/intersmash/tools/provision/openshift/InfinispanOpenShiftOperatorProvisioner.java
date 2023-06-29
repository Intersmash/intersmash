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
package org.jboss.intersmash.tools.provision.openshift;

import java.net.URL;
import java.util.List;

import org.jboss.intersmash.tools.application.operator.InfinispanOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.Cache;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.CacheList;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.Infinispan;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.InfinispanList;
import org.jboss.intersmash.tools.provision.operator.InfinispanOperatorProvisioner;
import org.jboss.intersmash.tools.provision.operator.OperatorProvisioner;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.fabric8.openshift.api.model.Route;
import lombok.NonNull;

public class InfinispanOpenShiftOperatorProvisioner
		// default Operator based provisioner behavior, which implements OLM workflow contract and leverage lifecycle as well
		extends OperatorProvisioner<InfinispanOperatorApplication>
		// leverage Hyperfoil common Operator based provisioner behavior
		implements InfinispanOperatorProvisioner,
		// leverage common OpenShift provisioning logic
		OpenShiftProvisioner<InfinispanOperatorApplication> {
	private static NonNamespaceOperation<Infinispan, InfinispanList, Resource<Infinispan>> INFINISPAN_CLIENT;
	private static NonNamespaceOperation<Cache, CacheList, Resource<Cache>> INFINISPAN_CACHES_CLIENT;

	public InfinispanOpenShiftOperatorProvisioner(
			@NonNull InfinispanOperatorApplication infinispanOperatorApplication) {
		super(infinispanOperatorApplication, InfinispanOperatorProvisioner.operatorId());
	}

	@Override
	public URL getURL() {
		return InfinispanOperatorProvisioner.super.getURL();
	}

	@Override
	public StatefulSet retrieveNamedStatefulSet(final String statefulSetName) {
		return OpenShiftProvisioner.openShift.getStatefulSet(statefulSetName);
	}

	@Override
	public String execute(String... args) {
		return OpenShifts.adminBinary().execute(args);
	}

	@Override
	public void scale(int replicas, boolean wait) {
		InfinispanOperatorProvisioner.super.scale(replicas, wait);
	}

	@Override
	public List<Pod> getPods() {
		return InfinispanOperatorProvisioner.super.getPods();
	}

	@Override
	public String getOperatorCatalogSource() {
		return InfinispanOperatorProvisioner.super.getOperatorCatalogSource();
	}

	@Override
	public String getOperatorIndexImage() {
		return InfinispanOperatorProvisioner.super.getOperatorIndexImage();
	}

	@Override
	public String getOperatorChannel() {
		return InfinispanOperatorProvisioner.super.getOperatorChannel();
	}

	@Override
	public Route retrieveNamedRoute(final String routeName) {
		return OpenShiftProvisioner.openShift.getRoute(routeName);
	}

	@Override
	public Service retrieveNamedService(final String serviceName) {
		return OpenShiftProvisioner.openShift.getService(serviceName);
	}

	@Override
	public HasMetadataOperationsImpl<Infinispan, InfinispanList> infinispanCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().customResources(crdc, Infinispan.class, InfinispanList.class);
	}

	@Override
	public HasMetadataOperationsImpl<Cache, CacheList> cacheCustomResourcesClient(CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().customResources(crdc, Cache.class, CacheList.class);
	}

	@Override
	public NonNamespaceOperation<Infinispan, InfinispanList, Resource<Infinispan>> infinispansClient() {
		if (INFINISPAN_CLIENT == null) {
			INFINISPAN_CLIENT = buildInfinispansClient().inNamespace(OpenShiftConfig.namespace());
		}
		return INFINISPAN_CLIENT;
	}

	@Override
	public NonNamespaceOperation<Cache, CacheList, Resource<Cache>> cachesClient() {
		if (INFINISPAN_CACHES_CLIENT == null) {
			INFINISPAN_CACHES_CLIENT = buildCachesClient().inNamespace(OpenShiftConfig.namespace());
		}
		return INFINISPAN_CACHES_CLIENT;
	}

	@Override
	protected String getOperatorNamespace() {
		return "openshift-marketplace";
	}

	@Override
	protected String getTargetNamespace() {
		return OpenShiftConfig.namespace();
	}

	@Override
	public List<Pod> retrievePods() {
		return OpenShiftProvisioner.openShift.getPods();
	}

	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> retrieveCustomResourceDefinitions() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}
}
