/*
 * Copyright (C) 2025 Red Hat, Inc.
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
package org.jboss.intersmash.testsuite.provision.openshift.kafka;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.application.openshift.OpenShiftApplication;
import org.jboss.intersmash.application.operator.KafkaOperatorApplication;

import io.strimzi.api.kafka.model.kafka.EphemeralStorageBuilder;
import io.strimzi.api.kafka.model.kafka.KRaftMetadataStorage;
import io.strimzi.api.kafka.model.kafka.Kafka;
import io.strimzi.api.kafka.model.kafka.KafkaBuilder;
import io.strimzi.api.kafka.model.kafka.listener.GenericKafkaListener;
import io.strimzi.api.kafka.model.kafka.listener.KafkaListenerType;
import io.strimzi.api.kafka.model.nodepool.KafkaNodePool;
import io.strimzi.api.kafka.model.nodepool.KafkaNodePoolBuilder;
import io.strimzi.api.kafka.model.nodepool.ProcessRoles;
import io.strimzi.api.kafka.model.topic.KafkaTopic;
import io.strimzi.api.kafka.model.topic.KafkaTopicBuilder;
import io.strimzi.api.kafka.model.user.KafkaUser;
import io.strimzi.api.kafka.model.user.KafkaUserBuilder;
import io.strimzi.api.kafka.model.user.acl.AclOperation;
import io.strimzi.api.kafka.model.user.acl.AclResourcePatternType;
import io.strimzi.api.kafka.model.user.acl.AclRule;
import io.strimzi.api.kafka.model.user.acl.AclRuleBuilder;

/**
 * Class that implements {@link KafkaOperatorApplication} and {@link OpenShiftApplication} to describe a service
 * descriptor which is deployed by {@link org.jboss.intersmash.provision.openshift.KafkaOpenShiftOperatorProvisioner}
 * and represents a Strmizi/Streams for Apache Kafka service that supports the latest version, requiring KRaft mode to
 * be enabled.
 */
public class KafkaKRaftEphemeralOpenShiftOperatorApplication implements KafkaOperatorApplication, OpenShiftApplication {
	static final String NAME = "kafka-test";
	private static final String KAFKA_VERSION = KafkaOperatorApplication.KAFKA_VERSION;
	private static final String KAFKA_METADATA_VERSION = KafkaOperatorApplication.METADATA_VERSION;
	private static final int KAFKA_INSTANCE_NUM = KafkaOperatorApplication.KAFKA_INSTANCE_NUM;
	private static final long TOPIC_RECONCILIATION_INTERVAL_MS = KafkaOperatorApplication.TOPIC_RECONCILIATION_INTERVAL_SECONDS
			* 1000;
	private static final long USER_RECONCILIATION_INTERVAL_MS = KafkaOperatorApplication.USER_RECONCILIATION_INTERVAL_SECONDS
			* 1000;

	private static final int KAFKA_PORT = 9092;

	private Kafka kafka;
	private List<KafkaTopic> topics;
	private List<KafkaUser> users;
	private List<KafkaNodePool> nodePools;

	@Override
	public Kafka getKafka() {
		if (kafka == null) {
			Map<String, Object> config = new HashMap<>();
			config.put("default.replication.factor", KAFKA_INSTANCE_NUM);
			config.put("min.insync.replicas", 2);
			config.put("offsets.topic.replication.factor", KAFKA_INSTANCE_NUM);
			config.put("transaction.state.log.min.isr", KAFKA_INSTANCE_NUM);
			config.put("transaction.state.log.replication.factor", KAFKA_INSTANCE_NUM);

			GenericKafkaListener listener = new GenericKafkaListener();
			listener.setName("plain");
			listener.setPort(KAFKA_PORT);
			listener.setType(KafkaListenerType.INTERNAL);
			listener.setTls(false);

			// Initialize Kafka resource
			kafka = new KafkaBuilder()
					.withNewMetadata()
					.withName(NAME)
					.withAnnotations(Map.of(
							KafkaOperatorApplication.STRIMZI_IO_KAFKA_LABEL_NODE_POOLS, "enabled",
							KafkaOperatorApplication.STRIMZI_IO_KAFKA_LABEL_KRAFT, "enabled"))
					.endMetadata()
					.withNewSpec()
					.withNewEntityOperator()
					.withNewTopicOperator().withReconciliationIntervalMs(TOPIC_RECONCILIATION_INTERVAL_MS)
					.endTopicOperator()
					.withNewUserOperator().withReconciliationIntervalMs(USER_RECONCILIATION_INTERVAL_MS)
					.endUserOperator()
					.endEntityOperator()
					.withNewKafka()
					.withConfig(config)
					.withListeners(listener)
					.withNewKafkaAuthorizationSimple()
					.endKafkaAuthorizationSimple()
					.withVersion(KAFKA_VERSION)
					.withMetadataVersion(KAFKA_METADATA_VERSION)
					.endKafka()
					.endSpec()
					.build();
		}

		return kafka;
	}

	@Override
	public List<KafkaTopic> getTopics() {
		if (topics == null) {
			topics = new LinkedList<>();

			Map<String, String> labels = new HashMap<>();
			labels.put(KafkaOperatorApplication.STRIMZI_IO_KAFKA_LABEL_CLUSTER, NAME);

			KafkaTopic topic = new KafkaTopicBuilder()
					.withNewMetadata()
					.withName("test-topic")
					.withLabels(labels)
					.endMetadata()
					.withNewSpec()
					.withPartitions(1)
					.withReplicas(1)
					.endSpec()
					.build();

			topics.add(topic);
		}

		return topics;
	}

	@Override
	public List<KafkaUser> getUsers() {
		if (users == null) {
			users = new LinkedList<>();

			Map<String, String> labels = new HashMap<>();
			labels.put(KafkaOperatorApplication.STRIMZI_IO_KAFKA_LABEL_CLUSTER, NAME);

			AclRule acl = new AclRuleBuilder()
					.withHost("*")
					// starting from Strimzi 0.49 ."operation" is deprecated and ".operations" must be used
					.withOperations(AclOperation.DESCRIBE)
					.withNewAclRuleTopicResource()
					.withName("test-topic")
					.withPatternType(AclResourcePatternType.LITERAL)
					.endAclRuleTopicResource()
					.build();

			KafkaUser user = new KafkaUserBuilder()
					.withNewMetadata()
					.withName("test-user")
					.withLabels(labels)
					.endMetadata()
					.withNewSpec()
					.withNewKafkaUserTlsClientAuthentication()
					.endKafkaUserTlsClientAuthentication()
					.withNewKafkaUserAuthorizationSimple()
					.withAcls(acl)
					.endKafkaUserAuthorizationSimple()
					.endSpec()
					.build();

			users.add(user);
		}

		return users;
	}

	@Override
	public List<KafkaNodePool> getNodePools() {
		if (nodePools == null) {
			nodePools = new LinkedList<>();
			// Kafka in KRaft mode ephemeral with both controller and broker node pools
			final KafkaNodePool controller = new KafkaNodePoolBuilder()
					.withNewMetadata()
					.withName(NAME + "-controller")
					.withLabels(Map.of(KafkaOperatorApplication.STRIMZI_IO_KAFKA_LABEL_CLUSTER, NAME))
					.endMetadata()
					.withNewSpec()
					.withReplicas(KAFKA_INSTANCE_NUM)
					.withRoles(ProcessRoles.CONTROLLER)
					.withStorage(
							new EphemeralStorageBuilder()
									.withId(0)
									.withKraftMetadata(KRaftMetadataStorage.SHARED)
									.build())
					.endSpec()
					.build();
			final KafkaNodePool broker = new KafkaNodePoolBuilder()
					.withNewMetadata()
					.withName(NAME + "-broker")
					.withLabels(Map.of(KafkaOperatorApplication.STRIMZI_IO_KAFKA_LABEL_CLUSTER, NAME))
					.endMetadata()
					.withNewSpec()
					.withReplicas(KAFKA_INSTANCE_NUM)
					.withRoles(ProcessRoles.BROKER)
					.withStorage(
							new EphemeralStorageBuilder()
									.withId(0)
									.withKraftMetadata(KRaftMetadataStorage.SHARED)
									.build())
					.endSpec()
					.build();
			nodePools.addAll(List.of(controller, broker));
		}
		return nodePools;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
