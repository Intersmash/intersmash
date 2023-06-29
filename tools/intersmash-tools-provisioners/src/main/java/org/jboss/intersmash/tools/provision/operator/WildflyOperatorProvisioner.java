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
package org.jboss.intersmash.tools.provision.operator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.operator.WildflyOperatorApplication;
import org.jboss.intersmash.tools.provision.Provisioner;
import org.jboss.intersmash.tools.provision.openshift.OpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.WaitersUtil;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.WildFlyServer;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.WildFlyServerList;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.status.PodStatus;
import org.slf4j.event.Level;

import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;

public interface WildflyOperatorProvisioner extends
		OlmOperatorProvisioner<WildflyOperatorApplication>, Provisioner<WildflyOperatorApplication> {

	// this is the packagemanifest for the hyperfoil operator;
	// you can get it with command:
	// oc get packagemanifest hyperfoil-bundle -o template --template='{{ .metadata.name }}'
	static String operatorId() {
		return IntersmashConfig.wildflyOperatorPackageManifest();
	}

	// this is the name of the Wildfly CustomResourceDefinition
	// you can get it with command:
	// oc get crd wildflyservers.wildfly.org -o template --template='{{ .metadata.name }}'
	default String wildflyCustomResourceDefinitionName() {
		return "wildflyservers.wildfly.org";
	}

	/**
	 * Get a client capable of working with {@link #wildflyCustomResourceDefinitionName} custom resource.
	 *
	 * @return client for operations with {@link #wildflyCustomResourceDefinitionName} custom resource
	 */
	default MixedOperation<WildFlyServer, WildFlyServerList, Resource<WildFlyServer>> buildWildflyClient() {
		CustomResourceDefinition crd = retrieveCustomResourceDefinitions()
				.withName(wildflyCustomResourceDefinitionName()).get();
		CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
		if (!retrieveCustomResourceDefinitions().list().getItems().contains(wildflyCustomResourceDefinitionName())) {
			throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
					wildflyCustomResourceDefinitionName(), operatorId()));
		}
		return wildflyCustomResourcesClient(crdc);
	}

	HasMetadataOperationsImpl<WildFlyServer, WildFlyServerList> wildflyCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	NonNamespaceOperation<WildFlyServer, WildFlyServerList, Resource<WildFlyServer>> wildflyServersClient();

	/**
	 * Get a reference to wildFlyServer object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link WildFlyServer} resource definition
	 */
	default Resource<WildFlyServer> wildFlyServer() {
		return wildflyServersClient().withName(getApplication().getName());
	}

	@Override
	default void deploy() {
		FailFastCheck ffCheck = () -> false;
		subscribe();
		wildflyServersClient().createOrReplace(getApplication().getWildflyServer());
		int expected = getApplication().getWildflyServer().getSpec().getReplicas();
		new SimpleWaiter(() -> wildFlyServer().get().getStatus() != null)
				.failFast(ffCheck)
				.reason("Wait for status field to be initialized.")
				.level(Level.DEBUG)
				.waitFor();
		new SimpleWaiter(() -> getPods().size() == expected)
				.failFast(ffCheck)
				.reason("Wait for expected number of replicas to be active.")
				.level(Level.DEBUG)
				.waitFor();
		if (getApplication().getWildflyServer().getSpec().getReplicas() > 0) {
			WaitersUtil.routeIsUp(getURL().toExternalForm())
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	default void scale(int replicas, boolean wait) {
		scale(wildFlyServer(), replicas, wait);
	}

	default void scale(Resource<WildFlyServer> wildFlyServer, int replicas, boolean wait) {
		WildFlyServer tmpServer = wildFlyServer.get();
		int originalReplicas = tmpServer.getSpec().getReplicas();
		tmpServer.getSpec().setReplicas(replicas);
		wildFlyServer().replace(tmpServer);
		if (wait) {
			new SimpleWaiter(() -> this.getPods().size() == replicas)
					.level(Level.DEBUG).waitFor();
		}
		if (originalReplicas == 0 && replicas > 0) {
			WaitersUtil.routeIsUp(getURL().toExternalForm())
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	@Override
	default void undeploy() {
		FailFastCheck ffCheck = () -> false;
		wildFlyServer().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
				.areExactlyNPodsReady(0, "app.kubernetes.io/name", getApplication().getName())
				.level(Level.DEBUG).waitFor();
		unsubscribe();
	}

	default String getOperatorCatalogSource() {
		return IntersmashConfig.wildflyOperatorCatalogSource();
	}

	default String getOperatorIndexImage() {
		return IntersmashConfig.wildflyOperatorIndexImage();
	}

	default String getOperatorChannel() {
		return IntersmashConfig.wildflyOperatorChannel();
	}

	default URL getURL() {
		String url = "http://" + wildFlyServer().get().getStatus().getHosts().get(0);
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(String.format("WILDFLY operator route \"%s\"is malformed.", url), e);
		}
	}

	List<Pod> retrievePods();

	/**
	 * Return the ready pods managed by the operator.
	 *
	 * @return pods which are registered by operator as active and are in ready state
	 */
	default List<Pod> getPods() {
		List<Pod> pods = retrievePods();
		List<String> activeOperatorPodNames = wildFlyServer().get().getStatus().getPods().stream()
				.filter(podStatus -> podStatus.getState().equals("ACTIVE"))
				.map(PodStatus::getName)
				.collect(Collectors.toList());
		return pods.stream()
				.filter(pod -> activeOperatorPodNames.contains(pod.getMetadata().getName()))
				.filter(pod -> pod.getStatus().getContainerStatuses().size() > 0
						&& pod.getStatus().getContainerStatuses().get(0).getReady())
				.collect(Collectors.toList());
	}
}
