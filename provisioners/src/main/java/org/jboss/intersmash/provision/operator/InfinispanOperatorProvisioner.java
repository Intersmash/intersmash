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
package org.jboss.intersmash.provision.operator;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.infinispan.v1.Infinispan;
import org.infinispan.v2alpha1.Cache;
import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.operator.InfinispanOperatorApplication;
import org.jboss.intersmash.provision.Provisioner;
import org.jboss.intersmash.provision.operator.model.infinispan.cache.CacheList;
import org.jboss.intersmash.provision.operator.model.infinispan.infinispan.InfinispanList;
import org.jboss.intersmash.provision.operator.model.infinispan.infinispan.spec.InfinispanConditionBuilder;
import org.slf4j.event.Level;

import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;

/**
 * Defines the contract and default behavior of an Operator based provisioner for the Infinispan Operator
 */
public abstract class InfinispanOperatorProvisioner<C extends NamespacedKubernetesClient> extends
		OperatorProvisioner<InfinispanOperatorApplication, C> implements Provisioner<InfinispanOperatorApplication> {

	public InfinispanOperatorProvisioner(InfinispanOperatorApplication application) {
		super(application, InfinispanOperatorProvisioner.OPERATOR_ID);
	}

	// =================================================================================================================
	// Infinispan related
	// =================================================================================================================
	public List<Pod> getInfinispanPods() {
		return getPods().stream().filter(
				// the following criteria is implemented based on similar requirements taken from the
				// infinispan-operator project, see
				// https://github.com/infinispan/infinispan-operator/blob/main/test/e2e/utils/kubernetes.go#L599-L604
				p -> p.getMetadata().getLabels().entrySet().stream()
						.anyMatch(tl -> "app".equals(tl.getKey()) && "infinispan-pod".equals(tl.getValue())
								&& p.getMetadata().getLabels().entrySet().stream().anyMatch(
										cnl -> "clusterName".equals(cnl.getKey())
												&& getApplication().getName().equals(cnl.getValue()))))
				.collect(Collectors.toList());
	}

	protected Service getService(final String name) {
		return this.client().services().withName(name).get();
	}

	// TODO: check for removal
	//	default List<Pod> getStatefulSetPods() {
	//		StatefulSet statefulSet = getStatefulSet(getApplication().getName());
	//		return Objects.nonNull(statefulSet)
	//				? getPods().stream()
	//				.filter(p -> p.getMetadata().getLabels().get("controller-revision-hash") != null
	//						&& p.getMetadata().getLabels().get("controller-revision-hash")
	//						.equals(statefulSet.getStatus().getUpdateRevision()))
	//				.collect(Collectors.toList())
	//				: List.of();
	//	}

	private void waitForResourceReadiness() {
		// it must be well-formed
		// see https://github.com/kubernetes/apimachinery/blob/v0.20.4/pkg/apis/meta/v1/types.go#L1289
		new SimpleWaiter(
				() -> infinispan().get().getStatus().getConditions().stream()
						.anyMatch(
								c -> c.getType().equals(InfinispanConditionBuilder.ConditionType.ConditionWellFormed.getValue())
										&& c.getStatus().equals("True")))
				.reason("Wait for infinispan resource to be ready").level(Level.DEBUG)
				.waitFor();
		// and with the expected number of Cache CR(s)
		if (getApplication().getCaches().size() > 0)
			new SimpleWaiter(() -> cachesClient().list().getItems().size() == caches().size())
					.reason("Wait for caches to be ready.").level(Level.DEBUG).waitFor();
	}

	// =================================================================================================================
	// Related to generic provisioning behavior
	// =================================================================================================================
	@Override
	public String getOperatorCatalogSource() {
		return IntersmashConfig.infinispanOperatorCatalogSource();
	}

	@Override
	public String getOperatorIndexImage() {
		return IntersmashConfig.infinispanOperatorIndexImage();
	}

	@Override
	public String getOperatorChannel() {
		return IntersmashConfig.infinispanOperatorChannel();
	}

	@Override
	public String getVersion() {
		return IntersmashConfig.infinispanOperatorVersion();
	}

	@Override
	public void deploy() {
		subscribe();
		// create Infinispan CR
		final int replicas = getApplication().getInfinispan().getSpec().getReplicas();
		infinispansClient().createOrReplace(getApplication().getInfinispan());
		if (replicas > 0) {
			new SimpleWaiter(() -> getInfinispanPods().size() == replicas).level(Level.DEBUG).waitFor();
		}
		// create Cache CR(s)
		if (getApplication().getCaches().size() > 0) {
			getApplication().getCaches().stream().forEach((i) -> cachesClient().resource(i).create());
		}
		// wait for all resources to be ready
		waitForResourceReadiness();
	}

	@Override
	public void undeploy() {
		// delete Cache CR(s)
		caches().forEach(keycloakUser -> keycloakUser.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		// delete Infinispan CR
		infinispan().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		// wait for 0 pods
		BooleanSupplier bs = () -> getInfinispanPods().isEmpty();
		new SimpleWaiter(bs, TimeUnit.MINUTES, 2,
				"Waiting for 0 pods with label \"clusterName\"=" + getApplication().getInfinispan().getMetadata().getName())
				.waitFor();
		unsubscribe();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		Infinispan tmpInfinispan = infinispan().get();
		tmpInfinispan.getSpec().setReplicas(replicas);
		infinispan().replace(tmpInfinispan);
		if (wait) {
			// waits for the correct number of Pods representing the Infinispan CR replicas to be ready
			new SimpleWaiter(() -> getInfinispanPods().size() == replicas).level(Level.DEBUG).waitFor();
			if (replicas > 0) {
				waitForResourceReadiness();
			}
		}
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	// this is the packagemanifest for the operator;
	// you can get it with command:
	// oc get packagemanifest <bundle> -o template --template='{{ .metadata.name }}'
	public static String OPERATOR_ID = IntersmashConfig.infinispanOperatorPackageManifest();

	// this is the name of the CustomResourceDefinition(s)
	// you can get it with command:
	// oc get crd <group> -o template --template='{{ .metadata.name }}'
	private static String INFINISPAN_CRD_NAME = "infinispans.infinispan.org";

	private static String INFINISPAN_CACHE_CRD_NAME = "caches.infinispan.org";

	/**
	 * Generic CRD client which is used by client builders default implementation to build the CRDs client
	 *
	 * @return A {@link NonNamespaceOperation} instance that represents a
	 */
	protected abstract NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient();

	// infinispans.infinispan.org
	protected abstract HasMetadataOperationsImpl<Infinispan, InfinispanList> infinispanCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	// caches.infinispan.org
	protected abstract HasMetadataOperationsImpl<Cache, CacheList> cacheCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	private static NonNamespaceOperation<Infinispan, InfinispanList, Resource<Infinispan>> INFINISPAN_CLIENT;
	private static NonNamespaceOperation<Cache, CacheList, Resource<Cache>> INFINISPAN_CACHES_CLIENT;

	public NonNamespaceOperation<Infinispan, InfinispanList, Resource<Infinispan>> infinispansClient() {
		if (INFINISPAN_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(INFINISPAN_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						INFINISPAN_CRD_NAME, OPERATOR_ID));
			}
			INFINISPAN_CLIENT = infinispanCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return INFINISPAN_CLIENT;
	}

	public NonNamespaceOperation<Cache, CacheList, Resource<Cache>> cachesClient() {
		if (INFINISPAN_CACHES_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(INFINISPAN_CACHE_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						INFINISPAN_CACHE_CRD_NAME, OPERATOR_ID));
			}
			INFINISPAN_CACHES_CLIENT = cacheCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return INFINISPAN_CACHES_CLIENT;
	}

	/**
	 * Get a reference to infinispan object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link Infinispan} resource definition
	 */
	public Resource<Infinispan> infinispan() {
		return infinispansClient().withName(getApplication().getInfinispan().getMetadata().getName());
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
}
