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

import org.jboss.intersmash.tools.application.openshift.WildflyOperatorApplication;
import org.jboss.intersmash.tools.junit5.IntersmashExtension;
import org.jboss.intersmash.tools.provision.openshift.WildflyOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.OperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.OperatorGroup;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.WildFlyServer;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.WildFlyServerBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.kubernetes.api.model.DeletionPropagation;

@CleanBeforeAll
public class WildflyOperatorProvisionerTest {
	private static final String NAME = "wildfly-operator-test";
	private static final WildflyOperatorProvisioner WILDFLY_OPERATOR_PROVISIONER = initializeOperatorProvisioner();

	private static WildflyOperatorProvisioner initializeOperatorProvisioner() {
		WildflyOperatorProvisioner operatorProvisioner = new WildflyOperatorProvisioner(
				new WildflyOperatorApplication() {
					@Override
					public WildFlyServer getWildflyServer() {
						return new WildFlyServerBuilder(getName())
								.applicationImage("quay.io/wildfly-quickstarts/wildfly-operator-quickstart:18.0")
								.replicas(1)
								.build();
					}

					@Override
					public String getName() {
						return NAME;
					}
				});
		return operatorProvisioner;
	}

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		WILDFLY_OPERATOR_PROVISIONER.configure();
		IntersmashExtension.operatorCleanup();
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f", OperatorGroup.SINGLE_NAMESPACE.save().getAbsolutePath());
		// clean any leftovers
		WILDFLY_OPERATOR_PROVISIONER.unsubscribe();
	}

	@AfterAll
	public static void removeOperatorGroup() {
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
		WILDFLY_OPERATOR_PROVISIONER.dismiss();
	}

	@AfterEach
	public void customResourcesCleanup() {
		WILDFLY_OPERATOR_PROVISIONER.wildflyServersClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> WILDFLY_OPERATOR_PROVISIONER
						.wildflyServersClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
	}

	/**
	 * Test actual deploy/scale/undeploy workflow by the operator
	 *
	 * Does subscribe/unsubscribe on its own, so no need to call explicitly here
	 *
	 * This test adds no further checks here because {@link WildflyOperatorProvisioner#undeploy} does the only useful
	 * thing, i.e. check for 0 app pods, immediately after deleting, then unsubscribe the operator.
	 * Checking through {@link WildflyOperatorProvisioner#getPods()} here would intermittently find a resumed app pod
	 * before unsubscribe (which doesn't wait ATM) finishes.
	 * Basically there could be room for revisiting {@link WildflyOperatorProvisioner#undeploy} and
	 * {@link OperatorProvisioner#unsubscribe()}
	 */
	@Test
	public void basicProvisioningTest() {
		WILDFLY_OPERATOR_PROVISIONER.deploy();
		try {
			Assertions.assertEquals(1, WILDFLY_OPERATOR_PROVISIONER.getPods().size(),
					"Unexpected number of cluster operator pods for '" + WILDFLY_OPERATOR_PROVISIONER.getOperatorId()
							+ "' after deploy");

			int scaledNum = WILDFLY_OPERATOR_PROVISIONER.getApplication().getWildflyServer().getSpec().getReplicas() + 1;
			WILDFLY_OPERATOR_PROVISIONER.scale(scaledNum, true);
			Assertions.assertEquals(scaledNum, WILDFLY_OPERATOR_PROVISIONER.getPods().size(),
					"Unexpected number of cluster operator pods for '" + WILDFLY_OPERATOR_PROVISIONER.getOperatorId()
							+ "' after scaling");
		} finally {
			WILDFLY_OPERATOR_PROVISIONER.undeploy();
		}
	}
}
