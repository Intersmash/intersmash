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

import org.jboss.intersmash.provision.operator.KafkaOperatorProvisioner;

import io.strimzi.api.kafka.model.Kafka;
import io.strimzi.api.kafka.model.KafkaTopic;
import io.strimzi.api.kafka.model.KafkaUser;

/**
 * End user Application interface which presents Kafka operator application on OpenShift Container Platform.
 * <p>
 * The application will be deployed by:
 * <ul>
 *     <li>{@link KafkaOperatorProvisioner}</li>
 * </ul>
 */
public interface KafkaOperatorApplication extends OperatorApplication {

	String KAFKA_VERSION = "3.8.0";
	String INTER_BROKER_PROTOCOL_VERSION = "3.8";
	int KAFKA_INSTANCE_NUM = 3;
	int TOPIC_RECONCILIATION_INTERVAL_SECONDS = 90;
	long USER_RECONCILIATION_INTERVAL_SECONDS = 120L;

	/**
	 * Provides Kafka Cluster definition. Note: even though the Kafka operator supports multiple instances of
	 * these Kafka clusters, with current implementation we expect to have only one.
	 *
	 * @return Kafka Cluster
	 */
	Kafka getKafka();

	/**
	 * Provides list of Kafka Topics definitions.
	 *
	 * @return list of Kafka Topics
	 */
	List<KafkaTopic> getTopics();

	/**
	 * Provides list of Kafka Users definitions.
	 *
	 * @return list of Kafka Users
	 */
	List<KafkaUser> getUsers();

	// TODO will be implemented on demand:
	//	List<KafkaConnect> getKafkaConnects();
	//	List<KafkaConnectS2I> getKafkaConnectSourceToImage();
	//	List<KafkaMirrorMaker> getKafkaMirrorMaker();
	//	List<KafkaBridge> getKafkaBridge();
	//	List<KafkaConnector> getKafkaConnectors();
	//	List<KafkaMirrorMaker2> getKafkaMirrorMaker2s();
	//	List<KafkaRebalance> getKafkaRebalances();
}
