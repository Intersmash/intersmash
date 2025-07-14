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

import org.jboss.intersmash.application.operator.WildflyOperatorApplication;
import org.jboss.intersmash.provision.operator.WildflyOperatorProvisioner;
import org.jboss.intersmash.provision.operator.model.wildfly.WildFlyServerList;
import org.wildfly.v1alpha1.WildFlyServer;

import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClientAdapter;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import lombok.NonNull;

public class WildflyOpenShiftOperatorProvisioner
		// leverage WildFly common Operator based provisioner behavior
		extends WildflyOperatorProvisioner<NamespacedOpenShiftClient>
		// ... and common OpenShift provisioning logic, too
		implements OpenShiftProvisioner<WildflyOperatorApplication> {

	public WildflyOpenShiftOperatorProvisioner(@NonNull WildflyOperatorApplication application) {
		super(application);
	}

	@Override
	public NamespacedKubernetesClientAdapter<NamespacedOpenShiftClient> client() {
		return OpenShiftProvisioner.super.client();
	}

	@Override
	public String execute(String... args) {
		return OpenShifts.adminBinary().execute(args);
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================

	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}

	@Override
	public HasMetadataOperationsImpl<WildFlyServer, WildFlyServerList> wildflyCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, WildFlyServer.class, WildFlyServerList.class);
	}
}
