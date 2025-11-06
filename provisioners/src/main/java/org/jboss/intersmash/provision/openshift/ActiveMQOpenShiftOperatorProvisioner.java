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

import org.jboss.intersmash.application.operator.ActiveMQOperatorApplication;
import org.jboss.intersmash.provision.operator.ActiveMQOperatorProvisioner;
import org.jboss.intersmash.provision.operator.model.activemq.address.ActiveMQArtemisAddressList;
import org.jboss.intersmash.provision.operator.model.activemq.broker.ActiveMQArtemisList;

import cz.xtf.core.openshift.OpenShifts;
import io.amq.broker.v1beta1.ActiveMQArtemis;
import io.amq.broker.v1beta1.ActiveMQArtemisAddress;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClientAdapter;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import lombok.NonNull;

/**
 * ActiveMQ Operator based provisioner for OpenShift
 */
public class ActiveMQOpenShiftOperatorProvisioner
		// leverage ActiveMQ Artemis common Operator based provisioner behavior
		extends ActiveMQOperatorProvisioner<NamespacedOpenShiftClient>
		// ... and common OpenShift provisioning logic, too
		implements OpenShiftProvisioner<ActiveMQOperatorApplication> {

	public ActiveMQOpenShiftOperatorProvisioner(@NonNull ActiveMQOperatorApplication application) {
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

	@Override
	public String executeInNamespace(String namespace, String... args) {
		return OpenShiftProvisioner.super.executeInNamespace(namespace, args);
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}

	@Override
	public HasMetadataOperationsImpl<ActiveMQArtemis, ActiveMQArtemisList> activeMQArtemisCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, ActiveMQArtemis.class, ActiveMQArtemisList.class);
	}

	@Override
	public HasMetadataOperationsImpl<ActiveMQArtemisAddress, ActiveMQArtemisAddressList> activeMQArtemisAddressesCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, ActiveMQArtemisAddress.class, ActiveMQArtemisAddressList.class);
	}
}
