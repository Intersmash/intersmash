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
package org.jboss.intersmash.provision.openshift;

import org.jboss.intersmash.application.operator.KafkaOperatorApplication;
import org.jboss.intersmash.provision.operator.KafkaOperatorProvisioner;

import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClientAdapter;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Deploys an application that implements {@link KafkaOperatorApplication} interface and which is extended by this
 * class.
 */
@Slf4j
public class KafkaOpenShiftOperatorProvisioner
		// leverage Kafka common Operator based provisioner behavior
		extends KafkaOperatorProvisioner<NamespacedOpenShiftClient>
		// leverage common OpenShift provisioning logic
		implements OpenShiftProvisioner<KafkaOperatorApplication> {

	public KafkaOpenShiftOperatorProvisioner(@NonNull KafkaOperatorApplication application) {
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
	// Client related
	// =================================================================================================================
	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}
}
