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
package org.jboss.intersmash.testsuite.provision.openshift;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.jboss.intersmash.application.operator.ActiveMQOperatorApplication;
import org.jboss.intersmash.junit5.IntersmashExtension;
import org.jboss.intersmash.provision.olm.OperatorGroup;
import org.jboss.intersmash.provision.openshift.ActiveMQOpenShiftOperatorProvisioner;
import org.jboss.intersmash.provision.operator.model.activemq.address.ActiveMQArtemisAddressBuilder;
import org.jboss.intersmash.provision.operator.model.activemq.broker.ActiveMQArtemisBuilder;
import org.jboss.intersmash.provision.operator.model.activemq.broker.spec.DeploymentPlanBuilder;
import org.jboss.intersmash.testsuite.junit5.categories.OpenShiftTest;
import org.jboss.intersmash.testsuite.openshift.ProjectCreationCapable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.amq.broker.v1beta1.ActiveMQArtemis;
import io.amq.broker.v1beta1.ActiveMQArtemisAddress;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CleanBeforeAll
@OpenShiftTest
public class ActiveMQOpenShiftOperatorProvisionerTest implements ProjectCreationCapable {
	private static final ActiveMQOpenShiftOperatorProvisioner activeMQOperatorProvisioner = initializeOperatorProvisioner();

	private static ActiveMQOpenShiftOperatorProvisioner initializeOperatorProvisioner() {
		ActiveMQOpenShiftOperatorProvisioner operatorProvisioner = new ActiveMQOpenShiftOperatorProvisioner(
				new ActiveMQOperatorApplication() {

					private static final String DEFAULT_ACTIVEMQ_APP_NAME = "example-amq-broker";

					@Override
					public ActiveMQArtemis getActiveMQArtemis() {
						return new ActiveMQArtemisBuilder(DEFAULT_ACTIVEMQ_APP_NAME)
								.deploymentPlan(new DeploymentPlanBuilder().size(1).build())
								.build();
					}

					@Override
					public List<ActiveMQArtemisAddress> getActiveMQArtemisAddresses() {
						return Collections.emptyList();
					}

					@Override
					public String getName() {
						return DEFAULT_ACTIVEMQ_APP_NAME;
					}
				});
		return operatorProvisioner;
	}

	private String name;

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		activeMQOperatorProvisioner.configure();
		IntersmashExtension.operatorCleanup(false, true);
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f",
				new OperatorGroup(OpenShiftConfig.namespace()).save().getAbsolutePath());
		// clean any leftovers
		activeMQOperatorProvisioner.unsubscribe();
		// let's configure the provisioner
		activeMQOperatorProvisioner.configure();
	}

	@AfterAll
	public static void removeOperatorGroup() {
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
		activeMQOperatorProvisioner.dismiss();
	}

	@AfterEach
	public void customResourcesCleanup() {
		// delete addresses
		activeMQOperatorProvisioner.activeMQArtemisAddressesClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> activeMQOperatorProvisioner
						.activeMQArtemisAddressesClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
						.delete());
		// delete artemises
		activeMQOperatorProvisioner.activeMQArtemisesClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> activeMQOperatorProvisioner
						.activeMQArtemisesClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
						.delete());
	}

	/**
	 * Test the ActiveMQ Broker Operator based provisioning model by validating the creation of a
	 * {@link ActiveMQArtemisAddress} CR.
	 */
	@Test
	public void addressTest() {
		activeMQOperatorProvisioner.subscribe();
		try {
			name = "amq-address-test";

			ActiveMQArtemisAddress address = new ActiveMQArtemisAddressBuilder(name)
					.addressName("inQueue")
					.queueName("inQueue")
					.routingType(ActiveMQArtemisAddressBuilder.RoutingType.ANYCAST)
					.build();

			// create and verify that object exists
			activeMQOperatorProvisioner.activeMQArtemisAddressesClient().createOrReplace(address);
			Assertions.assertEquals(1, activeMQOperatorProvisioner.activeMQArtemisAddressesClient().list().getItems().size());
			log.debug(activeMQOperatorProvisioner.activeMQArtemisAddress(name).get().getSpec().toString());

			// delete and verify that object was removed
			activeMQOperatorProvisioner.activeMQArtemisAddress(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
					.delete();
			new SimpleWaiter(() -> activeMQOperatorProvisioner.activeMQArtemisAddressesClient().list().getItems().size() == 0)
					.waitFor();
		} finally {
			activeMQOperatorProvisioner.unsubscribe();
		}
	}

	/**
	 * Test the ActiveMQ Operator based provisioning model by validating the creation of a {@link ActiveMQArtemis} CR.
	 */
	@Test
	public void basicBrokerTest() {
		activeMQOperatorProvisioner.subscribe();
		try {
			name = "amq-broker-test";

			ActiveMQArtemis amq = new ActiveMQArtemisBuilder(name)
					.deploymentPlan(new DeploymentPlanBuilder().size(1).build())
					.build();

			// create and verify that object exists
			activeMQOperatorProvisioner.activeMQArtemisesClient().createOrReplace(amq);
			Assertions.assertEquals(1, activeMQOperatorProvisioner.activeMQArtemisesClient().list().getItems().size());
			OpenShifts.master().waiters().areExactlyNPodsReady(1, amq.getKind(), name).waitFor();

			// scaling down to 0 (graceful shutdown)
			amq.getSpec().getDeploymentPlan().setSize(0);
			activeMQOperatorProvisioner.activeMQArtemisesClient().replace(amq);
			new SimpleWaiter(() -> OpenShifts.master().getLabeledPods("application", name + "-app").size() == 0).waitFor();

			// delete and verify that object was removed
			activeMQOperatorProvisioner.activeMQArtemisesClient().withName(name)
					.withPropagationPolicy(DeletionPropagation.FOREGROUND)
					.delete();
			new SimpleWaiter(() -> activeMQOperatorProvisioner.activeMQArtemisesClient().list().getItems().size() == 0)
					.waitFor();
		} finally {
			activeMQOperatorProvisioner.unsubscribe();
		}
	}

	/**
	 * Test actual deploy/scale/undeploy workflow by the operator
	 *
	 * Does subscribe/unsubscribe on its own, so no need to call explicitly here
	 */
	@Test
	public void basicProvisioningTest() {
		try {
			activeMQOperatorProvisioner.deploy();

			int scaledNum = activeMQOperatorProvisioner.getApplication().getActiveMQArtemis().getSpec().getDeploymentPlan()
					.getSize()
					+ 1;
			activeMQOperatorProvisioner.scale(scaledNum, true);
		} finally {
			activeMQOperatorProvisioner.undeploy();
		}
	}

	// TODO https://github.com/rh-messaging/activemq-artemis-operator/tree/master/deploy/examples
}
