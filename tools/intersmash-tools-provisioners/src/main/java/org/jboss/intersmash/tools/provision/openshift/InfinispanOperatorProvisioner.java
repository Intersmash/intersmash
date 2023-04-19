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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.InfinispanOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.operator.OperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.Cache;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.CacheList;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.Infinispan;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.InfinispanList;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.ExposeSpecBuilder;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.InfinispanConditionBuilder;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.openshift.api.model.Route;
import lombok.NonNull;

public class InfinispanOperatorProvisioner extends OperatorProvisioner<InfinispanOperatorApplication> {
	private static final String INFINISPAN_RESOURCE = "infinispans.infinispan.org";
	private static NonNamespaceOperation<Infinispan, InfinispanList, Resource<Infinispan>> INFINISPAN_CLIENT;

	private static final String INFINISPAN_CACHE_RESOURCE = "caches.infinispan.org";
	private static NonNamespaceOperation<Cache, CacheList, Resource<Cache>> INFINISPAN_CACHES_CLIENT;

	// oc get packagemanifest datagrid -n openshift-marketplace
	private static final String OPERATOR_ID = IntersmashConfig.infinispanOperatorPackageManifest();

	public InfinispanOperatorProvisioner(@NonNull InfinispanOperatorApplication infinispanOperatorApplication) {
		super(infinispanOperatorApplication, OPERATOR_ID);
	}

	public static String getOperatorId() {
		return OPERATOR_ID;
	}

	@Override
	public void deploy() {
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				getApplication().getName());
		subscribe();

		// create custom resources
		int replicas = getApplication().getInfinispan().getSpec().getReplicas();
		infinispansClient().createOrReplace(getApplication().getInfinispan());
		if (getApplication().getCaches().size() > 0)
			cachesClient().createOrReplace(getApplication().getCaches().stream().toArray(Cache[]::new));

		// This might be a litle bit naive, but we need more use cases to see how will this behave and what other
		// use-cases we have to cover wait for infinispan pods - look for "clusterName" in infinispan pod
		if (replicas > 0) {
			OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck).areExactlyNPodsReady(
					replicas, "clusterName", getApplication().getInfinispan().getMetadata().getName()).waitFor();
		}
		// wait for all resources to be ready
		waitForResourceReadiness();
	}

	@Override
	public void undeploy() {
		// delete custom resources
		caches().forEach(keycloakUser -> keycloakUser.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		infinispan().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();

		// wait for 0 pods
		OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
				.areExactlyNPodsReady(0, "clusterName", getApplication().getInfinispan().getMetadata().getName())
				.level(Level.DEBUG)
				.waitFor();
		unsubscribe();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		StatefulSet statefulSet = OpenShiftProvisioner.openShift.getStatefulSet(getApplication().getName());
		if (Objects.isNull(statefulSet)) {
			throw new IllegalStateException(String.format(
					"Impossible to scale non existent StatefulSet with name=\"%s\" to replicas=%d",
					getApplication().getName(),
					replicas));
		}
		String controllerRevisionHash = statefulSet.getStatus().getUpdateRevision();
		Infinispan tmpInfinispan = infinispan().get();
		tmpInfinispan.getSpec().setReplicas(replicas);
		infinispan().replace(tmpInfinispan);
		if (wait) {
			OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
					.areExactlyNPodsReady(replicas, "controller-revision-hash", controllerRevisionHash)
					.level(Level.DEBUG)
					.waitFor();
		}
		if (replicas > 0) {
			//	see https://github.com/kubernetes/apimachinery/blob/v0.20.4/pkg/apis/meta/v1/types.go#L1289
			new SimpleWaiter(
					() -> infinispan().get().getStatus().getConditions().stream()
							.anyMatch(c -> c.getType()
									.equals(InfinispanConditionBuilder.ConditionType.ConditionWellFormed.getValue())
									&& c.getStatus().equals("True")))
					.reason("Wait for infinispan resource to be ready").level(Level.DEBUG).waitFor();
		}
	}

	@Override
	public List<Pod> getPods() {
		StatefulSet statefulSet = OpenShiftProvisioner.openShift.getStatefulSet(getApplication().getName());
		return Objects.nonNull(statefulSet)
				? OpenShiftProvisioner.openShift.getLabeledPods("controller-revision-hash",
						statefulSet.getStatus().getUpdateRevision())
				: Lists.emptyList();
	}

	@Override
	protected String getOperatorCatalogSource() {
		return IntersmashConfig.infinispanOperatorCatalogSource();
	}

	@Override
	protected String getOperatorIndexImage() {
		return IntersmashConfig.infinispanOperatorIndexImage();
	}

	@Override
	protected String getOperatorChannel() {
		return IntersmashConfig.infinispanOperatorChannel();
	}

	/**
	 * The result is affected by the CR definition and specifically the method will return the {@code service} URL in
	 * case the CR {@code .spec.expose.type} is set to {@code NodePort} or {@code LoadBalancer} while it will return the
	 * route URL (i.e. for external access) when {@code .spec.expose.type} is set to {@code Route}
	 * @return The URL for the provisioned Infinispan service
	 */
	@Override
	public URL getURL() {
		final Service defaultInternalService = OpenShiftProvisioner.openShift.getService(getApplication().getName());
		String internalUrl = "http://" + defaultInternalService.getSpec().getClusterIP() + ":11222";
		String externalUrl = null;
		if (getApplication().getInfinispan().getSpec().getExpose() != null) {
			final String exposedType = getApplication().getInfinispan().getSpec().getExpose().getType();
			switch (ExposeSpecBuilder.ExposeType.valueOf(exposedType)) {
				case NodePort:
					//	TODO - check
					// see see https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v1/infinispan_types.go#L107
					externalUrl = "http://"
							+ OpenShiftProvisioner.openShift.getService(getApplication().getName() + "-external").getSpec()
									.getClusterIP()
							+ getApplication().getInfinispan().getSpec().getExpose().getNodePort();
					break;
				case LoadBalancer:
					//	TODO - check
					//	see https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v1/infinispan_types.go#L111
					externalUrl = "http://"
							+ OpenShiftProvisioner.openShift.getService(getApplication().getName() + "-external").getSpec()
									.getExternalIPs().get(0)
							+ getApplication().getInfinispan().getSpec().getExpose().getNodePort();
					break;
				case Route:
					//	https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v1/infinispan_types.go#L116
					Route route = OpenShiftProvisioner.openShift.getRoute(getApplication().getName() + "-external");
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

	// infinispans.infinispan.org

	/**
	 * Get a client capable of working with {@link #INFINISPAN_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #INFINISPAN_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<Infinispan, InfinispanList, Resource<Infinispan>> infinispansClient() {
		if (INFINISPAN_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(INFINISPAN_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(INFINISPAN_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						INFINISPAN_RESOURCE, OPERATOR_ID));
			}

			MixedOperation<Infinispan, InfinispanList, Resource<Infinispan>> infinispansClient = OpenShifts
					.master()
					.customResources(crdc, Infinispan.class, InfinispanList.class);
			INFINISPAN_CLIENT = infinispansClient.inNamespace(OpenShiftConfig.namespace());
		}
		return INFINISPAN_CLIENT;
	}

	/**
	 * Get a reference to infinispan object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link Infinispan} resource definition
	 */
	public Resource<Infinispan> infinispan() {
		return infinispansClient().withName(getApplication().getInfinispan().getMetadata().getName());
	}

	// caches.infinispan.org

	/**
	 * Get a client capable of working with {@link #INFINISPAN_CACHE_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #INFINISPAN_CACHE_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<Cache, CacheList, Resource<Cache>> cachesClient() {
		if (INFINISPAN_CACHES_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(INFINISPAN_CACHE_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(INFINISPAN_CACHE_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						INFINISPAN_CACHE_RESOURCE, OPERATOR_ID));
			}

			MixedOperation<Cache, CacheList, Resource<Cache>> cachesClient = OpenShifts
					.master()
					.customResources(crdc, Cache.class, CacheList.class);
			INFINISPAN_CACHES_CLIENT = cachesClient.inNamespace(OpenShiftConfig.namespace());
		}
		return INFINISPAN_CACHES_CLIENT;
	}

	/**
	 * Get a reference to cache object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the cache custom resource
	 * @return A concrete {@link Resource} instance representing the {@link Cache} resource definition
	 */
	public Resource<Cache> cache(String name) {
		return cachesClient().withName(name);
	}

	/**
	 * Get all caches maintained by the current operator instance.
	 * <p>
	 * Be aware that this method returns just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link Cache} resource definitions
	 */
	public List<Resource<Cache>> caches() {
		InfinispanOperatorApplication infinispanOperatorApplication = getApplication();
		return infinispanOperatorApplication.getCaches().stream()
				.map(cache -> cache.getMetadata().getName())
				.map(this::cache)
				.collect(Collectors.toList());
	}

	private void waitForResourceReadiness() {
		//	see https://github.com/kubernetes/apimachinery/blob/v0.20.4/pkg/apis/meta/v1/types.go#L1289
		new SimpleWaiter(
				() -> infinispan().get().getStatus().getConditions().stream()
						.anyMatch(
								c -> c.getType().equals(InfinispanConditionBuilder.ConditionType.ConditionWellFormed.getValue())
										&& c.getStatus().equals("True")))
				.reason("Wait for infinispan resource to be ready").level(Level.DEBUG)
				.waitFor();
		if (getApplication().getCaches().size() > 0)
			new SimpleWaiter(() -> cachesClient().list().getItems().size() == caches().size())
					.reason("Wait for caches to be ready.").level(Level.DEBUG).waitFor(); // no isReady() for cache
	}
}
