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

import org.assertj.core.api.Assertions;
import org.jboss.intersmash.application.openshift.BootableJarOpenShiftApplication;
import org.jboss.intersmash.provision.openshift.BootableJarImageOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.WildflyBootableJarImageOpenShiftProvisioner;
import org.jboss.intersmash.testsuite.junit5.categories.NotForProductizedExecutionProfile;
import org.jboss.intersmash.testsuite.junit5.categories.wildfly.RequiresBootableJarDistribution;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cz.xtf.core.bm.BuildManagers;
import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.junit5.annotations.CleanBeforeAll;

@CleanBeforeAll
@NotForProductizedExecutionProfile
@RequiresBootableJarDistribution
public class WildflyBootableJarTestCase {
	private static final OpenShift openShift = OpenShifts.master();
	private static final BootableJarOpenShiftApplication application = OpenShiftProvisionerTestBase
			.getWildflyBootableJarOpenShiftApplication();
	private static final BootableJarImageOpenShiftProvisioner provisioner = new WildflyBootableJarImageOpenShiftProvisioner(
			application);

	@BeforeAll
	public static void deploy() {
		provisioner.preDeploy();
		provisioner.deploy();
	}

	@AfterAll
	public static void undeploy() {
		provisioner.undeploy();
		provisioner.postUndeploy();
	}

	@Test
	public void verifyOpenShiftConfiguration() {
		// environmentVariables
		Assertions
				.assertThat(BuildManagers.get().openShift().getBuildConfig(application.getName()).getSpec().getStrategy()
						.getSourceStrategy().getEnv())
				.as("Environment variable test").contains(OpenShiftProvisionerTestBase.TEST_ENV_VAR);
	}

	/**
	 * Secret resource should be created as a preDeploy() operation by a provisioner.
	 */
	@Test
	public void verifyDeployHooks() {
		Assertions.assertThat(openShift.getSecret(OpenShiftProvisionerTestBase.TEST_SECRET.getMetadata().getName()))
				.isNotNull();
	}

	@Test
	public void scale() {
		provisioner.scale(1, true);
		openShift.waiters().areExactlyNPodsReady(1, application.getName()).waitFor();
		provisioner.scale(2, true);
		openShift.waiters().areExactlyNPodsReady(2, application.getName()).waitFor();
	}

	@Test
	public void pods() {
		provisioner.scale(2, true);
		Assertions.assertThat(provisioner.getPods().size()).isEqualTo(2);
		provisioner.scale(3, true);
		Assertions.assertThat(provisioner.getPods().size()).isEqualTo(3);
	}
}
