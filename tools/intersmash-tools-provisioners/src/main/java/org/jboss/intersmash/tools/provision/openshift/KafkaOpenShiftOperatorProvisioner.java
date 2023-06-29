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

import java.util.List;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.operator.KafkaOperatorApplication;
import org.jboss.intersmash.tools.provision.operator.KafkaOperatorProvisioner;
import org.jboss.intersmash.tools.provision.operator.OperatorProvisioner;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.strimzi.api.kafka.Crds;
import io.strimzi.api.kafka.KafkaList;
import io.strimzi.api.kafka.KafkaTopicList;
import io.strimzi.api.kafka.KafkaUserList;
import io.strimzi.api.kafka.model.Kafka;
import io.strimzi.api.kafka.model.KafkaTopic;
import io.strimzi.api.kafka.model.KafkaUser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Deploys an application that implements {@link KafkaOperatorApplication} interface and which is extended by this
 * class.
 */
@Slf4j
public class KafkaOpenShiftOperatorProvisioner
		// default Operator based provisioner behavior, which implements OLM workflow contract and leverage lifecycle as well
		extends OperatorProvisioner<KafkaOperatorApplication>
		// leverage Kafka common Operator based provisioner behavior
		implements KafkaOperatorProvisioner,
		// leverage common OpenShift provisioning logic
		OpenShiftProvisioner<KafkaOperatorApplication> {

	private static final String OPERATOR_ID = IntersmashConfig.kafkaOperatorPackageManifest();

	public KafkaOpenShiftOperatorProvisioner(@NonNull KafkaOperatorApplication kafkaOperatorApplication) {
		super(kafkaOperatorApplication, KafkaOperatorProvisioner.operatorId());
	}

	/**
	 * Get a client capable of working with {@link Kafka} custom resource on our OpenShift instance.
	 *
	 * @return client for operations with {@link Kafka} custom resource on our OpenShift instance
	 */
	public NonNamespaceOperation<Kafka, KafkaList, Resource<Kafka>> kafkasClient() {
		return Crds.kafkaOperation(OpenShiftProvisioner.openShift).inNamespace(OpenShiftConfig.namespace());
	}

	/**
	 * Get a client capable of working with {@link KafkaUser} custom resource on our OpenShift instance.
	 *
	 * @return client for operations with {@link KafkaUser} custom resource on our OpenShift instance
	 */
	public NonNamespaceOperation<KafkaUser, KafkaUserList, Resource<KafkaUser>> kafkasUserClient() {
		return Crds.kafkaUserOperation(OpenShiftProvisioner.openShift).inNamespace(OpenShiftConfig.namespace());
	}

	/**
	 * Get a client capable of working with {@link KafkaTopic} custom resource on our OpenShift instance.
	 *
	 * @return client for operations with {@link KafkaTopic} custom resource on our OpenShift instance
	 */
	public NonNamespaceOperation<KafkaTopic, KafkaTopicList, Resource<KafkaTopic>> kafkasTopicClient() {
		return Crds.topicOperation(OpenShiftProvisioner.openShift).inNamespace(OpenShiftConfig.namespace());
	}

	@Override
	public String execute(String... args) {
		return OpenShifts.adminBinary().execute(args);
	}

	@Override
	public List<Pod> getPods() {
		return OpenShiftProvisioner.openShift.getLabeledPods("strimzi.io/cluster", getApplication().getName());
	}

	public List<Pod> getClusterOperatorPods() {
		return OpenShiftProvisioner.openShift.getLabeledPods("strimzi.io/kind", "cluster-operator");
	}

	@Override
	public List<Pod> retrieveKafkaPods() {
		return OpenShiftProvisioner.openShift.getLabeledPods("app.kubernetes.io/name", "kafka");
	}

	@Override
	public List<Pod> retrieveKafkaZookeperPods() {
		return OpenShiftProvisioner.openShift.getLabeledPods("app.kubernetes.io/name", "zookeeper");
	}

	@Override
	public void scale(int replicas, boolean wait) {
		KafkaOperatorProvisioner.super.scale(replicas, wait);
	}

	@Override
	public String getOperatorCatalogSource() {
		return IntersmashConfig.kafkaOperatorCatalogSource();
	}

	@Override
	public String getOperatorIndexImage() {
		return IntersmashConfig.kafkaOperatorIndexImage();
	}

	@Override
	public String getOperatorChannel() {
		return IntersmashConfig.kafkaOperatorChannel();
	}

	@Override
	protected String getOperatorNamespace() {
		return null;
	}

	@Override
	protected String getTargetNamespace() {
		return null;
	}

	@Override
	public void logMessage(final String message, Level l) {
		switch (l) {
			case INFO:
				log.info(message);
				break;
			case WARN:
				log.warn(message);
				break;
			case ERROR:
				log.error(message);
				break;
			case DEBUG:
				log.debug(message);
				break;
			case TRACE:
				log.trace(message);
				break;
			default:
				throw new IllegalArgumentException(String.format("Unsupported log level: %s", l.name()));
		}
	}

	@Override
	public List<Pod> retrievePods() {
		return OpenShiftProvisioner.openShift.getPods();
	}

	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> retrieveCustomResourceDefinitions() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}
}
