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
package org.jboss.intersmash.application.operator;

import java.util.List;

import io.strimzi.api.kafka.model.kafka.Kafka;
import io.strimzi.api.kafka.model.nodepool.KafkaNodePool;
import io.strimzi.api.kafka.model.topic.KafkaTopic;
import io.strimzi.api.kafka.model.user.KafkaUser;
import org.jboss.intersmash.provision.operator.KafkaOperatorProvisioner;

/**
 * End user Application interface which presents Kafka operator application on OpenShift Container Platform.
 * <p>
 * The application will be deployed by:
 * <ul>
 *     <li>{@link KafkaOperatorProvisioner}</li>
 * </ul>
 */
public interface KafkaOperatorApplication extends OperatorApplication {

	String KAFKA_VERSION = "4.0.0";
	String METADATA_VERSION = "4.0.-IV3";
	int KAFKA_INSTANCE_NUM = 3;
	int TOPIC_RECONCILIATION_INTERVAL_SECONDS = 90;
	long USER_RECONCILIATION_INTERVAL_SECONDS = 120L;
	String STRIMZI_IO_KAFKA_LABEL_CLUSTER = "strimzi.io/cluster";
	String STRIMZI_IO_KAFKA_LABEL_NODE_POOLS = "strimzi.io/node-pools";
	String STRIMZI_IO_KAFKA_LABEL_KRAFT = "strimzi.io/kraft";
	String STRIMZI_IO_KAFKA_LABEL_KRAFT_CONTROLLER_ROLE = "strimzi.io/controller-role";
	String STRIMZI_IO_KAFKA_LABEL_KRAFT_BROKER_ROLE = "strimzi.io/broker-role";

	default boolean isKRaftModeEnabled() {
		return false;
	}

	/**
	 * Provides {@link Kafka} CR definition.
	 * <p>
	 *     Note: even though the Kafka operator supports multiple instances of
	 * 	   these Kafka clusters, with current implementation we expect to have only one.
	 * </p>
	 *
	 * @return {@link Kafka} instance.
	 */
	Kafka getKafka();

	/**
	 * Provides list of {@link KafkaTopic} definitions.
	 *
	 * @return list of {@link KafkaTopic} instances.
	 */
	List<KafkaTopic> getTopics();

	/**
	 * Provides list of {@link KafkaUser} definitions.
	 *
	 * @return list of {@link KafkaUser} instances.
	 */
	List<KafkaUser> getUsers();

	/**
	 * Provides list of {@link KafkaNodePool> definitions for runing Strimzi/Streams for Apache Kafka in KRaft mode.
	 * <p>
	 * @return list of {@link KafkaNodePool> instances
	 */
	List<KafkaNodePool> getNodePools();
}
