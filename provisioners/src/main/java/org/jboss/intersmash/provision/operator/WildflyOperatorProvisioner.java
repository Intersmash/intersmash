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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.operator.WildflyOperatorApplication;
import org.jboss.intersmash.provision.Provisioner;
import org.jboss.intersmash.provision.operator.model.wildfly.WildFlyServerList;
import org.slf4j.event.Level;
import org.wildfly.v1alpha1.WildFlyServer;
import org.wildfly.v1alpha1.wildflyserverstatus.Pods;

import cz.xtf.core.http.Https;
import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import lombok.NonNull;

public abstract class WildflyOperatorProvisioner<C extends NamespacedKubernetesClient> extends
		OperatorProvisioner<WildflyOperatorApplication, C> implements Provisioner<WildflyOperatorApplication> {

	public WildflyOperatorProvisioner(@NonNull WildflyOperatorApplication application) {
		super(application, OPERATOR_ID);
	}

	/**
	 * Return the ready pods managed by the operator.
	 *
	 * @return pods which are registered by operator as active and are in ready state
	 */
	@Override
	public List<Pod> getPods() {
		final List<Pod> pods = super.getPods();
		final WildFlyServer wildFlyServer = wildFlyServer().get();
		final List<String> activeOperatorPodNames = wildFlyServer == null ? List.of()
				: wildFlyServer().get().getStatus().getPods().stream()
						.filter(podStatus -> podStatus.getState().equals(Pods.State.ACTIVE))
						.map(Pods::getName)
						.collect(Collectors.toList());
		return pods.stream()
				.filter(pod -> activeOperatorPodNames.contains(pod.getMetadata().getName()))
				.filter(pod -> pod.getStatus().getContainerStatuses().size() > 0
						&& pod.getStatus().getContainerStatuses().get(0).getReady())
				.collect(Collectors.toList());

	}

	protected void waitForExactNumberOfLabeledPodsToBeReady(final String labelName, final String labelValue, int replicas) {
		BooleanSupplier bs = () -> getLabeledPods(labelName, labelValue).size() == replicas;
		new SimpleWaiter(bs, TimeUnit.MINUTES, 2,
				"Waiting for pods with label \"" + labelName + "\"=" + labelValue + " to be ready")
				.waitFor();
	}

	// =================================================================================================================
	// Related to generic provisioning behavior
	// =================================================================================================================
	@Override
	protected String getOperatorCatalogSource() {
		return IntersmashConfig.wildflyOperatorCatalogSource();
	}

	@Override
	protected String getOperatorIndexImage() {
		return IntersmashConfig.wildflyOperatorIndexImage();
	}

	@Override
	protected String getOperatorChannel() {
		return IntersmashConfig.wildflyOperatorChannel();
	}

	@Override
	public String getVersion() {
		return IntersmashConfig.wildflyOperatorVersion();
	}

	@Override
	public URL getURL() {
		String url = "http://" + wildFlyServer().get().getStatus().getHosts().get(0);
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(String.format("WILDFLY operator route \"%s\"is malformed.", url), e);
		}
	}

	@Override
	public void deploy() {
		subscribe();
		wildflyServerClient().createOrReplace(getApplication().getWildflyServer());
		int expected = getApplication().getWildflyServer().getSpec().getReplicas();
		new SimpleWaiter(() -> wildFlyServer().get().getStatus() != null)
				.failFast(getFailFastCheck())
				.reason("Wait for status field to be initialized.")
				.level(Level.DEBUG)
				.waitFor();
		new SimpleWaiter(() -> getPods().size() == expected)
				.failFast(getFailFastCheck())
				.reason("Wait for expected number of replicas to be active.")
				.level(Level.DEBUG)
				.waitFor();
		if (getApplication().getWildflyServer().getSpec().getReplicas() > 0) {
			new SimpleWaiter(
					() -> Https.getCode(getURL().toExternalForm()) != 503)
					.reason("Wait until the route is ready to serve.");
		}
	}

	@Override
	public void scale(int replicas, boolean wait) {
		scale(wildFlyServer(), replicas, wait);
	}

	protected void scale(Resource<WildFlyServer> wildFlyServer, int replicas, boolean wait) {
		WildFlyServer tmpServer = wildFlyServer.get();
		int originalReplicas = tmpServer.getSpec().getReplicas();
		tmpServer.getSpec().setReplicas(replicas);
		wildFlyServer().replace(tmpServer);
		if (wait) {
			new SimpleWaiter(() -> this.getPods().size() == replicas)
					.level(Level.DEBUG).waitFor();
		}
		if (originalReplicas == 0 && replicas > 0) {
			new SimpleWaiter(
					() -> Https.getCode(getURL().toExternalForm()) != 503)
					.reason("Wait until the route is ready to serve.");
		}
	}

	@Override
	public void undeploy() {
		wildFlyServer().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		waitForExactNumberOfLabeledPodsToBeReady("app.kubernetes.io/name", getApplication().getName(), 0);
		unsubscribe();
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	// this is the packagemanifest for the WildFly operator;
	// you can get it with command:
	// oc get packagemanifest hyperfoil-bundle -o template --template='{{ .metadata.name }}'
	public static String OPERATOR_ID = IntersmashConfig.wildflyOperatorPackageManifest();

	// this is the name of the Wildfly CustomResourceDefinition
	// you can get it with command:
	// oc get crd wildflyservers.wildfly.org -o template --template='{{ .metadata.name }}'
	protected static String WILDFLY_SERVER_CRD_NAME = "wildflyservers.wildfly.org";
	private static NonNamespaceOperation<WildFlyServer, WildFlyServerList, Resource<WildFlyServer>> WILDFLY_SERVERS_CLIENT;

	/**
	 * Generic CRD client which is used by client builders default implementation to build the CRDs client
	 *
	 * @return A {@link NonNamespaceOperation} instance that represents a
	 */
	protected abstract NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient();

	// wildflyservers.wildfly.org
	protected abstract HasMetadataOperationsImpl<WildFlyServer, WildFlyServerList> wildflyCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	/**
	 * Get a client capable of working with {@link #WILDFLY_SERVER_CRD_NAME} custom resource.
	 *
	 * @return client for operations with {@link #WILDFLY_SERVER_CRD_NAME} custom resource
	 */
	public NonNamespaceOperation<WildFlyServer, WildFlyServerList, Resource<WildFlyServer>> wildflyServerClient() {
		if (WILDFLY_SERVERS_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(WILDFLY_SERVER_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						WILDFLY_SERVER_CRD_NAME, OPERATOR_ID));
			}
			WILDFLY_SERVERS_CLIENT = wildflyCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return WILDFLY_SERVERS_CLIENT;
	}

	/**
	 * Get a reference to wildFlyServer object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link WildFlyServer} resource definition
	 */
	public Resource<WildFlyServer> wildFlyServer() {
		return wildflyServerClient().withName(getApplication().getName());
	}
}
