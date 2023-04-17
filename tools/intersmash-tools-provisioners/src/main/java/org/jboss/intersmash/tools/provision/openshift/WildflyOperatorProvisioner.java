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
import java.util.stream.Collectors;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.WildflyOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.operator.OperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.WildFlyServer;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.WildFlyServerList;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.status.PodStatus;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import lombok.NonNull;

public class WildflyOperatorProvisioner extends OperatorProvisioner<WildflyOperatorApplication> {
	private final static String WILDFLY_SERVER_RESOURCE = "wildflyservers.wildfly.org";
	private static NonNamespaceOperation<WildFlyServer, WildFlyServerList, Resource<WildFlyServer>> WILDFLY_SERVERS_CLIENT;
	// oc get packagemanifest wildfly -n openshift-marketplace
	private static final String OPERATOR_ID = IntersmashConfig.wildflyOperatorPackageManifest();

	public WildflyOperatorProvisioner(@NonNull WildflyOperatorApplication wildflyOperatorApplication) {
		super(wildflyOperatorApplication, OPERATOR_ID, "stable");
	}

	public static String getOperatorId() {
		return OPERATOR_ID;
	}

	/**
	 * Get a client capable of working with {@link #WILDFLY_SERVER_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #WILDFLY_SERVER_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<WildFlyServer, WildFlyServerList, Resource<WildFlyServer>> wildflyServersClient() {
		if (WILDFLY_SERVERS_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(WILDFLY_SERVER_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(WILDFLY_SERVER_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						WILDFLY_SERVER_RESOURCE, OPERATOR_ID));
			}

			MixedOperation<WildFlyServer, WildFlyServerList, Resource<WildFlyServer>> wildflyServersClient = OpenShifts
					.master().customResources(crdc, WildFlyServer.class, WildFlyServerList.class);
			WILDFLY_SERVERS_CLIENT = wildflyServersClient.inNamespace(OpenShiftConfig.namespace());
		}
		return WILDFLY_SERVERS_CLIENT;
	}

	/**
	 * Get a reference to wildFlyServer object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link WildFlyServer} resource definition
	 */
	public Resource<WildFlyServer> wildFlyServer() {
		return wildflyServersClient().withName(getApplication().getName());
	}

	@Override
	public void deploy() {
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				getApplication().getName());
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

	@Override
	public void undeploy() {
		wildFlyServer().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
				.areExactlyNPodsReady(0, "app.kubernetes.io/name", getApplication().getName())
				.level(Level.DEBUG).waitFor();
		unsubscribe();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		scale(wildFlyServer(), replicas, wait);
	}

	private void scale(Resource<WildFlyServer> wildFlyServer, int replicas, boolean wait) {
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

	/**
	 * Return the ready pods managed by the operator.
	 *
	 * @return pods which are registered by operator as active and are in ready state
	 */
	@Override
	public List<Pod> getPods() {
		List<Pod> pods = OpenShiftProvisioner.openShift.getPods();
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

	@Override
	protected String getOperatorCatalogSource() {
		return IntersmashConfig.wildflyOperatorCatalogSource();
	}

	@Override
	protected String getOperatorIndexImage() {
		return IntersmashConfig.wildflyOperatorIndexImage();
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
}
