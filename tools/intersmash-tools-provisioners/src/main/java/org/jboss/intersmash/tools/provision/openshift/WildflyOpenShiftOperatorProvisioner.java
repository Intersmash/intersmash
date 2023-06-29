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

import org.jboss.intersmash.tools.application.operator.WildflyOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.WildFlyServer;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.WildFlyServerList;
import org.jboss.intersmash.tools.provision.operator.OperatorProvisioner;
import org.jboss.intersmash.tools.provision.operator.WildflyOperatorProvisioner;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import lombok.NonNull;

public class WildflyOpenShiftOperatorProvisioner
		// default Operator based provisioner behavior, which implements OLM workflow contract and leverage lifecycle as well
		extends OperatorProvisioner<WildflyOperatorApplication>
		// leverage Wildfly common Operator based provisioner behavior
		implements WildflyOperatorProvisioner,
		// leverage common OpenShift provisioning logic
		OpenShiftProvisioner<WildflyOperatorApplication> {
	private static NonNamespaceOperation<WildFlyServer, WildFlyServerList, Resource<WildFlyServer>> WILDFLY_SERVERS_CLIENT;

	public WildflyOpenShiftOperatorProvisioner(@NonNull WildflyOperatorApplication wildflyOperatorApplication) {
		super(wildflyOperatorApplication, WildflyOperatorProvisioner.operatorId());
	}

	@Override
	public HasMetadataOperationsImpl<WildFlyServer, WildFlyServerList> wildflyCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().customResources(crdc, WildFlyServer.class, WildFlyServerList.class);
	}

	/**
	 * Get a client capable of working with {@link WildflyOperatorProvisioner#wildflyCustomResourceDefinitionName()} ()} custom resource.
	 *
	 * @return client for operations with {@link WildflyOperatorProvisioner#wildflyCustomResourceDefinitionName()} ()} custom resource
	 */
	public NonNamespaceOperation<WildFlyServer, WildFlyServerList, Resource<WildFlyServer>> wildflyServersClient() {
		if (WILDFLY_SERVERS_CLIENT == null) {
			WILDFLY_SERVERS_CLIENT = buildWildflyClient().inNamespace(OpenShiftConfig.namespace());
		}
		return WILDFLY_SERVERS_CLIENT;
	}

	@Override
	public String execute(String... args) {
		return OpenShifts.adminBinary().execute(args);
	}

	@Override
	public void scale(int replicas, boolean wait) {
		WildflyOperatorProvisioner.super.scale(replicas, wait);
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
	public List<Pod> retrievePods() {
		return OpenShiftProvisioner.openShift.getPods();
	}

	@Override
	public List<Pod> getPods() {
		return WildflyOperatorProvisioner.super.getPods();
	}

	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> retrieveCustomResourceDefinitions() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}

	@Override
	public String getOperatorCatalogSource() {
		return WildflyOperatorProvisioner.super.getOperatorCatalogSource();
	}

	@Override
	public String getOperatorIndexImage() {
		return WildflyOperatorProvisioner.super.getOperatorIndexImage();
	}

	@Override
	public String getOperatorChannel() {
		return WildflyOperatorProvisioner.super.getOperatorChannel();
	}

	@Override
	protected String getOperatorNamespace() {
		return "openshift-marketplace";
	}

	@Override
	protected String getTargetNamespace() {
		return OpenShiftConfig.namespace();
	}
}
