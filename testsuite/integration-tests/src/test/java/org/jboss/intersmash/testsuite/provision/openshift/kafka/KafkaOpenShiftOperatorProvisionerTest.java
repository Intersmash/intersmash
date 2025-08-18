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
package org.jboss.intersmash.testsuite.provision.openshift;

import java.io.IOException;

import org.jboss.intersmash.application.operator.KafkaOperatorApplication;
import org.jboss.intersmash.junit5.IntersmashExtension;
import org.jboss.intersmash.provision.olm.OperatorGroup;
import org.jboss.intersmash.provision.openshift.KafkaOpenShiftOperatorProvisioner;
import org.jboss.intersmash.provision.operator.KafkaOperatorProvisioner;
import org.jboss.intersmash.testsuite.junit5.categories.OpenShiftTest;
import org.jboss.intersmash.testsuite.openshift.ProjectCreationCapable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple class that tests basic features of {@link KafkaOpenShiftOperatorProvisioner}.
 */
@Slf4j
@CleanBeforeAll
@OpenShiftTest
public class KafkaOpenShiftOperatorProvisionerTest implements ProjectCreationCapable {
	private static KafkaOperatorApplication application = OpenShiftProvisionerTestBase.getKafkaApplication();
	private static final KafkaOpenShiftOperatorProvisioner operatorProvisioner = initializeOperatorProvisioner();

	private static KafkaOpenShiftOperatorProvisioner initializeOperatorProvisioner() {
		KafkaOpenShiftOperatorProvisioner operatorProvisioner = new KafkaOpenShiftOperatorProvisioner(application);
		return operatorProvisioner;
	}

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		operatorProvisioner.configure();
		IntersmashExtension.operatorCleanup(false, true);
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f",
				new OperatorGroup(OpenShiftConfig.namespace()).save().getAbsolutePath());
		// clean any leftovers
		operatorProvisioner.unsubscribe();
		// Let's skip subscribe operation here since we use regular deploy/undeploy where subscribe is called anyway.
	}

	@AfterAll
	public static void removeOperatorGroup() {
		// clean any leftovers
		operatorProvisioner.unsubscribe();
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
		operatorProvisioner.dismiss();
	}

	@AfterEach
	public void customResourcesCleanup() {
		operatorProvisioner.kafkasClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> operatorProvisioner
						.kafkasClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		operatorProvisioner.kafkasTopicClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> operatorProvisioner
						.kafkasTopicClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		operatorProvisioner.kafkasUserClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> operatorProvisioner
						.kafkasUserClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
	}

	/**
	 * Test actual deploy/scale/undeploy workflow by the operator
	 *
	 * Does subscribe/unsubscribe on its own, so no need to call explicitly here
	 */
	@Test
	public void basicProvisioningTest() {
		operatorProvisioner.deploy();
		try {
			verifyDeployed();

			int scaledNum = operatorProvisioner.getApplication().getKafka().getSpec().getKafka().getReplicas() + 1;
			operatorProvisioner.scale(scaledNum, true);
			verifyScaledDeployed(scaledNum);
		} finally {
			operatorProvisioner.undeploy();
		}
		verifyUndeployed();
	}

	private void verifyDeployed() {
		Assertions.assertEquals(1, operatorProvisioner.getClusterOperatorPods().size(),
				"Unexpected number of cluster operator pods for '" + KafkaOperatorProvisioner.OPERATOR_ID + "'");

		int kafkaReplicas = operatorProvisioner.getApplication().getKafka().getSpec().getKafka().getReplicas();
		int zookeeperReplicas = operatorProvisioner.getApplication().getKafka().getSpec().getZookeeper().getReplicas();

		Assertions.assertEquals(kafkaReplicas, operatorProvisioner.getKafkaPods().size(),
				"Unexpected number of Kafka pods for '" + operatorProvisioner.getApplication().getName() + "'");
		Assertions.assertEquals(zookeeperReplicas, operatorProvisioner.getZookeeperPods().size(),
				"Unexpected number of Zookeeper pods for '" + operatorProvisioner.getApplication().getName() + "'");
		Assertions.assertEquals(1 + kafkaReplicas + zookeeperReplicas, operatorProvisioner.getPods().size(),
				"Unexpected number of pods for '" + operatorProvisioner.getApplication().getName() + "'");

		String testTopicName = operatorProvisioner.getApplication().getTopics().get(0).getMetadata().getName();
		Assertions.assertEquals(1, operatorProvisioner.kafkasTopicClient().list().getItems().stream().filter(
				t -> testTopicName.equals(t.getMetadata().getName())).count(),
				"Unexpected number of topics named '" + testTopicName + "'");
		Assertions.assertTrue(operatorProvisioner.kafkasTopicClient().list().getItems().stream().filter(
				t -> testTopicName.equals(t.getMetadata().getName())).allMatch(
						t -> "Ready".equals(t.getStatus().getConditions().get(0).getType())),
				"Test topic '" + testTopicName + "' is not ready for use");

		String testUserName = operatorProvisioner.getApplication().getUsers().get(0).getMetadata().getName();
		Assertions.assertEquals(1, operatorProvisioner.kafkasUserClient().list().getItems().stream().filter(
				u -> testUserName.equals(u.getMetadata().getName())).count(),
				"Unexpected number of users named '" + testUserName + "'");
		Assertions.assertTrue(operatorProvisioner.kafkasUserClient().list().getItems().stream().filter(
				u -> testUserName.equals(u.getMetadata().getName())).allMatch(
						u -> "Ready".equals(u.getStatus().getConditions().get(0).getType())),
				"Test user '" + testUserName + "' is not ready for use");
	}

	private void verifyScaledDeployed(int scaledNum) {
		Assertions.assertEquals(1, operatorProvisioner.getClusterOperatorPods().size(),
				"Unexpected number of cluster operator pods for '" + KafkaOperatorProvisioner.OPERATOR_ID + "'");

		int zookeeperReplicas = operatorProvisioner.getApplication().getKafka().getSpec().getZookeeper().getReplicas();

		Assertions.assertEquals(scaledNum, operatorProvisioner.getKafkaPods().size(),
				"Unexpected number of Kafka pods for '" + operatorProvisioner.getApplication().getName() + "'");
		Assertions.assertEquals(zookeeperReplicas, operatorProvisioner.getZookeeperPods().size(),
				"Unexpected number of Zookeeper pods for '" + operatorProvisioner.getApplication().getName() + "'");
		Assertions.assertEquals(1 + scaledNum + zookeeperReplicas, operatorProvisioner.getPods().size(),
				"Unexpected number of pods for '" + operatorProvisioner.getApplication().getName() + "'");
	}

	private void verifyUndeployed() {
		Assertions.assertEquals(0, operatorProvisioner.getKafkaPods().size(),
				"Looks like there are some leftover Kafka pods after '" + operatorProvisioner.getApplication().getName()
						+ "' application deletion.");
		Assertions.assertEquals(0, operatorProvisioner.getZookeeperPods().size(),
				"Looks like there are some leftover Zookeeper pods after '" + operatorProvisioner.getApplication().getName()
						+ "' application deletion.");
		Assertions.assertEquals(0, operatorProvisioner.getPods().size(),
				"Looks like there are some leftover pods after '" + operatorProvisioner.getApplication().getName()
						+ "' application deletion.");
	}
}
