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
package org.jboss.intersmash.provision.olm;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jboss.intersmash.provision.operator.OperatorProvisioner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.EnvVar;

public class SubscriptionTest {

	/**
	 * Verifies serialization/deserialization preserve environment variables
	 */
	@Test
	public void testEnv() throws IOException {
		final String propertyValue = "dummy-prop-value";
		Subscription subscription = new Subscription(
				"openshift-marketplace",
				"my-namespace",
				"redhat-operators",
				"dummy-operator",
				"alpha",
				OperatorProvisioner.INSTALLPLAN_APPROVAL_MANUAL,
				Map.of(
						"PROP_1", propertyValue,
						"PROP_2", propertyValue,
						"PROP_3", propertyValue,
						"PROP_4", "a different value"));
		File subscriptionFile = subscription.save();
		Subscription loadedSubscription = new Subscription();
		loadedSubscription.load(subscriptionFile);
		Assertions.assertEquals(loadedSubscription.getSpec().getInstallPlanApproval(),
				OperatorProvisioner.INSTALLPLAN_APPROVAL_MANUAL);
		for (EnvVar envVar : loadedSubscription.getSpec().getConfig().getEnv()) {
			switch (envVar.getName()) {
				case "PROP_1":
				case "PROP_2":
				case "PROP_3":
					Assertions.assertEquals(envVar.getValue(), propertyValue);
					break;
				case "PROP_4":
					Assertions.assertEquals(envVar.getValue(), "a different value");
					break;
				default:
					break;
			}
		}
	}
}
