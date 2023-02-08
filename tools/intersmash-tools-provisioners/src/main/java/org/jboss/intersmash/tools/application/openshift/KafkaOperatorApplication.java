package org.jboss.intersmash.tools.application.openshift;

import java.util.List;

import org.jboss.intersmash.tools.provision.openshift.KafkaOperatorProvisioner;

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

	String KAFKA_VERSION = "3.2.3";
	String INTER_BROKER_PROTOCOL_VERSION = "3.2";
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
