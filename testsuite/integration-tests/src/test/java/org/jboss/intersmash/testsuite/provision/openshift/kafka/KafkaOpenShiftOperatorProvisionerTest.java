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
package org.jboss.intersmash.testsuite.provision.openshift.kafka;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import cz.xtf.core.config.XTFConfig;
import org.jboss.intersmash.application.operator.KafkaOperatorApplication;
import org.jboss.intersmash.junit5.IntersmashExtension;
import org.jboss.intersmash.provision.olm.OperatorGroup;
import org.jboss.intersmash.provision.openshift.KafkaOpenShiftOperatorProvisioner;
import org.jboss.intersmash.provision.operator.KafkaOperatorProvisioner;
import org.jboss.intersmash.testsuite.junit5.categories.OpenShiftTest;
import org.jboss.intersmash.testsuite.openshift.ProjectCreationCapable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.base.Strings;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.strimzi.api.kafka.model.kafka.Kafka;
import io.strimzi.api.kafka.model.nodepool.KafkaNodePool;
import io.strimzi.api.kafka.model.nodepool.ProcessRoles;
import lombok.extern.slf4j.Slf4j;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

/**
 * Test class that verifies provisioning features of {@link KafkaOpenShiftOperatorProvisioner} on OpenShift.
 * <p>
 *     The test class verifies provisioning with both latest (i.e. KRaft based) and legacy (i.e. Zookeeper based)
 *     versions of the Strimzi/Streams for Apache Kafka operator. In order to do so it uses respectively the
 *     {@link KafkaKRaftEphemeralOpenShiftOperatorApplication} and {@link KafkaZookeperEphemeralOpenShiftOperatorApplication}
 *     {@link org.jboss.intersmash.application.Application} descriptor variants.
 * </p>
 */
@Slf4j
@CleanBeforeAll
@OpenShiftTest
@ExtendWith(SystemStubsExtension.class)
public class KafkaOpenShiftOperatorProvisionerTest implements ProjectCreationCapable {

	@SystemStub
	private SystemProperties systemProperties;

	private static Stream<Arguments> applicationProvider() {
		return Stream.of(
				Arguments.of(new KafkaKRaftEphemeralOpenShiftOperatorApplication(), "stable"),
				Arguments.of(new KafkaZookeperEphemeralOpenShiftOperatorApplication(), "amq-streams-2.9.x")
		);
	}

	private KafkaOpenShiftOperatorProvisioner initializeOperatorProvisioner(
			final KafkaOperatorApplication kafkaOperatorApplication) {
		return new KafkaOpenShiftOperatorProvisioner(kafkaOperatorApplication);
	}

	private void createOperatorGroup(final KafkaOpenShiftOperatorProvisioner operatorProvisioner) throws IOException {
		operatorProvisioner.configure();
		IntersmashExtension.operatorCleanup(false, true);
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f",
				new OperatorGroup(OpenShiftConfig.namespace()).save().getAbsolutePath());
		// clean any leftovers
		operatorProvisioner.unsubscribe();
		// Let's skip subscribe operation here since we use regular deploy/undeploy where subscribe is called anyway.
	}

	private void removeOperatorGroup(final KafkaOpenShiftOperatorProvisioner operatorProvisioner) {
		// clean any leftovers
		operatorProvisioner.unsubscribe();
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
		operatorProvisioner.dismiss();
	}

	/**
	 * Test actual deploy/scale/undeploy workflow by both latest (i.e. KRaft based) and legacy (i.e. Zookeeper based)
	 * versions of the Strimzi/Streams for Apache Kafka operator.
	 */
	@ParameterizedTest(name = "{argumentsWithNames}")
	@MethodSource("applicationProvider")
	public void basicProvisioningTest(KafkaOperatorApplication kafkaOperatorApplication, String desiredChannel)
			throws IOException {
		// set the desired channel
		systemProperties.set("intersmash.kafka.operators.channel", desiredChannel);
		// reload XTF configuration to take the desired channel into account...
		XTFConfig.loadConfig();
		final KafkaOpenShiftOperatorProvisioner operatorProvisioner = initializeOperatorProvisioner(kafkaOperatorApplication);
		createOperatorGroup(operatorProvisioner);
		try {
			operatorProvisioner.deploy();
			try {
				verifyDeployed(operatorProvisioner);
				// when computing desired replicas, we need to discriminate whether the definition
				// is still based on Kafka + Zookeper or KRaft (KafkaNodePool) based
				final KafkaOperatorApplication application = operatorProvisioner.getApplication();
				final Kafka applicationKafkaDefinition = application.getKafka();
				final List<KafkaNodePool> applicationKafkaNodePoolDefinitions = application.getNodePools();
				final KafkaNodePool brokerNodePoolDefinition = applicationKafkaNodePoolDefinitions.stream()
						.filter(np -> np.getSpec().getRoles().contains(ProcessRoles.BROKER))
						.findFirst().orElseThrow(
								() -> new IllegalStateException(
										"There are no KafkaNodePool resources defined with the \"broker\" role"));
				final int scaledNum = application.isKRaftModeEnabled() ? brokerNodePoolDefinition.getSpec().getReplicas()
						: applicationKafkaDefinition.getSpec().getKafka().getReplicas() + 1;
				operatorProvisioner.scale(scaledNum, true);
				verifyScaledDeployed(operatorProvisioner, scaledNum);
			} finally {
				operatorProvisioner.undeploy();
			}
			verifyUndeployed(operatorProvisioner);
		} finally {
			removeOperatorGroup(operatorProvisioner);
		}
	}

	private void verifyDeployed(final KafkaOpenShiftOperatorProvisioner operatorProvisioner) {
		Assertions.assertEquals(1, operatorProvisioner.getClusterOperatorPods().size(),
				"Unexpected number of cluster operator pods for '" + KafkaOperatorProvisioner.OPERATOR_ID + "'");

		final int kafkaReplicas = operatorProvisioner.getApplication().getKafka().getSpec().getKafka().getReplicas();
		int totalPodCount = 0;
		if (!operatorProvisioner.getApplication().isKRaftModeEnabled()) {
			int zookeeperReplicas = operatorProvisioner.getApplication().getKafka().getSpec().getZookeeper().getReplicas();
			Assertions.assertEquals(zookeeperReplicas, operatorProvisioner.getZookeeperPods().size(),
					"Unexpected number of Zookeeper pods for '" + operatorProvisioner.getApplication().getName() + "'");
			totalPodCount += zookeeperReplicas + kafkaReplicas;
		} else {
			final KafkaNodePool controllerNodePool = operatorProvisioner.getApplication().getNodePools().stream()
					.filter(np -> np.getSpec().getRoles().contains(ProcessRoles.CONTROLLER)).findAny()
					.orElseThrow(() -> new IllegalStateException(
							"Kafka cluster configured in KRaft mode must define one KafkaNodePool resource with the \"controller\" role."));
			final int controllerNodePoolReplicas = controllerNodePool.getSpec().getReplicas();
			Assertions.assertEquals(controllerNodePoolReplicas, operatorProvisioner.getControllerNodePoolPods().size(),
					"Unexpected number of KafkaNodePool pods with \"controller\" role for '"
							+ operatorProvisioner.getApplication().getName() + "'");
			totalPodCount += controllerNodePoolReplicas;
			final KafkaNodePool brokerNodePool = operatorProvisioner.getApplication().getNodePools().stream()
					.filter(np -> np.getSpec().getRoles().contains(ProcessRoles.BROKER)).findAny()
					.orElseThrow(() -> new IllegalStateException(
							"Kafka cluster configured in KRaft mode must define one KafkaNodePool resource with the \"broker\" role."));
			final int brokerNodePoolReplicas = brokerNodePool.getSpec().getReplicas();
			Assertions.assertEquals(brokerNodePoolReplicas, operatorProvisioner.getBrokerNodePoolPods().size(),
					"Unexpected number of KafkaNodePool pods with \"broker\" role for '"
							+ operatorProvisioner.getApplication().getName() + "'");
			totalPodCount += brokerNodePoolReplicas;
		}
		Assertions.assertEquals(kafkaReplicas, operatorProvisioner.getKafkaPods().size(),
				"Unexpected number of Kafka pods for '" + operatorProvisioner.getApplication().getName() + "'");
		// "+ 1" here represents the operator controller pod
		Assertions.assertEquals(totalPodCount + 1, operatorProvisioner.getPods().size(),
				"Unexpected total number of pods for '" + operatorProvisioner.getApplication().getName() + "'");
		// Verify topics
		final String testTopicName = operatorProvisioner.getApplication().getTopics().get(0).getMetadata().getName();
		Assertions.assertEquals(1, operatorProvisioner.kafkaTopicResourceClient().list().getItems().stream().filter(
				t -> testTopicName.equals(t.getMetadata().getName())).count(),
				"Unexpected number of topics named '" + testTopicName + "'");
		Assertions.assertTrue(operatorProvisioner.kafkaTopicResourceClient().list().getItems().stream().filter(
				t -> testTopicName.equals(t.getMetadata().getName())).allMatch(
						t -> "Ready".equals(t.getStatus().getConditions().get(0).getType())),
				"Test topic '" + testTopicName + "' is not ready for use");
		// Verify users
		final String testUserName = operatorProvisioner.getApplication().getUsers().get(0).getMetadata().getName();
		Assertions.assertEquals(1, operatorProvisioner.kafkaUserResourceClient().list().getItems().stream().filter(
				u -> testUserName.equals(u.getMetadata().getName())).count(),
				"Unexpected number of users named '" + testUserName + "'");
		Assertions.assertTrue(operatorProvisioner.kafkaUserResourceClient().list().getItems().stream().filter(
				u -> testUserName.equals(u.getMetadata().getName())).allMatch(
						u -> "Ready".equals(u.getStatus().getConditions().get(0).getType())),
				"Test user '" + testUserName + "' is not ready for use");
	}

	private void verifyScaledDeployed(final KafkaOpenShiftOperatorProvisioner operatorProvisioner, int scaledNum) {
		Assertions.assertEquals(1, operatorProvisioner.getClusterOperatorPods().size(),
				"Unexpected number of cluster operator pods for '" + KafkaOperatorProvisioner.OPERATOR_ID + "'");

		final int kafkaReplicas = operatorProvisioner.getKafkaPods().size();
		Assertions.assertEquals(scaledNum, kafkaReplicas,
				"Unexpected number of Kafka pods for '" + operatorProvisioner.getApplication().getName() + "'");
		int totalPodCount = 0;
		if (!operatorProvisioner.getApplication().isKRaftModeEnabled()) {
			int zookeeperReplicas = operatorProvisioner.getApplication().getKafka().getSpec().getZookeeper().getReplicas();
			Assertions.assertEquals(zookeeperReplicas, operatorProvisioner.getZookeeperPods().size(),
					"Unexpected number of Zookeeper pods for '" + operatorProvisioner.getApplication().getName() + "'");
			totalPodCount += zookeeperReplicas + kafkaReplicas;
		} else {
			final KafkaNodePool controllerNodePool = operatorProvisioner.getApplication().getNodePools().stream()
					.filter(np -> np.getSpec().getRoles().contains(ProcessRoles.CONTROLLER)).findAny()
					.orElseThrow(() -> new IllegalStateException(
							"Kafka cluster configured in KRaft mode must define one KafkaNodePool resource with the \"controller\" role."));
			final int controllerNodePoolReplicas = controllerNodePool.getSpec().getReplicas();
			Assertions.assertEquals(controllerNodePoolReplicas, operatorProvisioner.getControllerNodePoolPods().size(),
					"Unexpected number of KafkaNodePool pods with \"controller\" role for '"
							+ operatorProvisioner.getApplication().getName() + "'");
			totalPodCount += controllerNodePoolReplicas;
			final KafkaNodePool brokerNodePool = operatorProvisioner.getApplication().getNodePools().stream()
					.filter(np -> np.getSpec().getRoles().contains(ProcessRoles.BROKER)).findAny()
					.orElseThrow(() -> new IllegalStateException(
							"Kafka cluster configured in KRaft mode must define one KafkaNodePool resource with the \"broker\" role."));
			final int brokerNodePoolReplicas = brokerNodePool.getSpec().getReplicas();
			Assertions.assertEquals(brokerNodePoolReplicas, operatorProvisioner.getBrokerNodePoolPods().size(),
					"Unexpected number of KafkaNodePool pods with \"broker\" role for '"
							+ operatorProvisioner.getApplication().getName() + "'");
			totalPodCount += brokerNodePoolReplicas;
		}
		// "+ 1" here represents the operator controller pod
		Assertions.assertEquals(totalPodCount + 1, operatorProvisioner.getPods().size(),
				"Unexpected number of pods for '" + operatorProvisioner.getApplication().getName() + "'");
	}

	private void verifyUndeployed(final KafkaOpenShiftOperatorProvisioner operatorProvisioner) {
		Assertions.assertEquals(0, operatorProvisioner.getKafkaPods().size(),
				"Looks like there are some leftover Kafka pods after '" + operatorProvisioner.getApplication().getName()
						+ "' application deletion.");
		if (!operatorProvisioner.getApplication().isKRaftModeEnabled()) {
			Assertions.assertEquals(0, operatorProvisioner.getZookeeperPods().size(),
					"Looks like there are some leftover Zookeeper pods after '" + operatorProvisioner.getApplication().getName()
							+ "' application deletion.");
		} else {
			Assertions.assertEquals(0, operatorProvisioner.getControllerNodePoolPods().size(),
					"Unexpected number of KafkaNodePool pods with \"controller\" role for '"
							+ operatorProvisioner.getApplication().getName() + "'");
			Assertions.assertEquals(0, operatorProvisioner.getBrokerNodePoolPods().size(),
					"Unexpected number of KafkaNodePool pods with \"broker\" role for '"
							+ operatorProvisioner.getApplication().getName() + "'");
		}
		Assertions.assertEquals(0, operatorProvisioner.getPods().size(),
				"Looks like there are some leftover pods after '" + operatorProvisioner.getApplication().getName()
						+ "' application deletion.");
	}
}
