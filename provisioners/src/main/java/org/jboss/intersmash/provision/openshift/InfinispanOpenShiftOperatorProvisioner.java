/*
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
package org.jboss.intersmash.provision.openshift;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import org.assertj.core.util.Lists;
import org.infinispan.v1.Infinispan;
import org.infinispan.v1.infinispanspec.Expose;
import org.infinispan.v2alpha1.Cache;
import org.jboss.intersmash.application.operator.InfinispanOperatorApplication;
import org.jboss.intersmash.provision.operator.InfinispanOperatorProvisioner;
import org.jboss.intersmash.provision.operator.model.infinispan.cache.CacheList;
import org.jboss.intersmash.provision.operator.model.infinispan.infinispan.InfinispanList;

import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.NamespacedKubernetesClientAdapter;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import lombok.NonNull;

public class InfinispanOpenShiftOperatorProvisioner
		// leverage Infinispan common Operator based provisioner behavior
		extends InfinispanOperatorProvisioner<NamespacedOpenShiftClient>
		// ... and common OpenShift provisioning logic, too
		implements OpenShiftProvisioner<InfinispanOperatorApplication> {

	public InfinispanOpenShiftOperatorProvisioner(
			@NonNull InfinispanOperatorApplication application) {
		super(application);
	}

	@Override
	public NamespacedKubernetesClientAdapter<NamespacedOpenShiftClient> client() {
		return OpenShiftProvisioner.super.client();
	}

	@Override
	public String execute(String... args) {
		return OpenShiftProvisioner.super.execute(args);
	}

	// =================================================================================================================
	// Infinispan related
	// =================================================================================================================
	@Override
	public List<Pod> getPods() {
		StatefulSet statefulSet = OpenShiftProvisioner.openShift.getStatefulSet(getApplication().getName());
		return Objects.nonNull(statefulSet)
				? OpenShiftProvisioner.openShift.getLabeledPods("controller-revision-hash",
						statefulSet.getStatus().getUpdateRevision())
				: Lists.emptyList();
	}

	public Route getRoute(final String name) {
		return OpenShiftProvisioner.openShift.getRoute(name);
	}

	// =================================================================================================================
	// Related to generic provisioning behavior
	// =================================================================================================================
	/**
	 * The result is affected by the CR definition and specifically the method will return the {@code service} URL in
	 * case the CR {@code .spec.expose.type} is set to {@code NodePort} or {@code LoadBalancer} while it will return the
	 * route URL (i.e. for external access) when {@code .spec.expose.type} is set to {@code Route}
	 *
	 * @return a {@link URL} instance that represents the Infinispan service route URL
	 */
	@Override
	public URL getURL() {
		final Service defaultInternalService = getService(getApplication().getName());
		String internalUrl = "http://" + defaultInternalService.getSpec().getClusterIP() + ":11222";
		String externalUrl = null;
		if (getApplication().getInfinispan().getSpec().getExpose() != null) {
			final String exposedType = getApplication().getInfinispan().getSpec().getExpose().getType().getValue();
			switch (Expose.Type.valueOf(exposedType)) {
				case NodePort:
					//	TODO - check
					// see see https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v1/infinispan_types.go#L107
					externalUrl = "http://"
							+ getService(getApplication().getName() + "-external").getSpec()
									.getClusterIP()
							+ getApplication().getInfinispan().getSpec().getExpose().getNodePort();
					break;
				case LoadBalancer:
					//	TODO - check
					//	see https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v1/infinispan_types.go#L111
					externalUrl = "http://"
							+ getService(getApplication().getName() + "-external").getSpec()
									.getExternalIPs().get(0)
							+ getApplication().getInfinispan().getSpec().getExpose().getNodePort();
					break;
				case Route:
					//	https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v1/infinispan_types.go#L116
					Route route = getRoute(getApplication().getName() + "-external");
					externalUrl = "https://" + route.getSpec().getHost();
					break;
				default:
					throw new UnsupportedOperationException(String.format("Unsupported .spec.expose.type: %s", exposedType));
			}
		}
		try {
			return new URL(externalUrl == null ? internalUrl : externalUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(String.format("Infinispan operator Internal URL \"%s\" is malformed.", internalUrl), e);
		}
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}

	@Override
	public HasMetadataOperationsImpl<Infinispan, InfinispanList> infinispanCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, Infinispan.class, InfinispanList.class);
	}

	@Override
	public HasMetadataOperationsImpl<Cache, CacheList> cacheCustomResourcesClient(CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, Cache.class, CacheList.class);
	}
}
