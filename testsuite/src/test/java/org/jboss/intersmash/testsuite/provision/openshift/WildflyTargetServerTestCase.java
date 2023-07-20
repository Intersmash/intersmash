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

import static org.jboss.intersmash.testsuite.provision.openshift.OpenShiftProvisionerTestBase.TEST_SECRET_BAR;
import static org.jboss.intersmash.testsuite.provision.openshift.OpenShiftProvisionerTestBase.TEST_SECRET_FOO;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.jboss.intersmash.testsuite.openshift.OpenShiftTest;
import org.jboss.intersmash.testsuite.openshift.ProjectCreationCapable;
import org.jboss.intersmash.tools.application.openshift.WildflyImageOpenShiftApplication;
import org.jboss.intersmash.tools.provision.openshift.WildflyImageOpenShiftProvisioner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.openshift.PodShell;
import cz.xtf.core.openshift.PodShellOutput;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.kubernetes.api.model.Service;

/**
 * This test verifies a binary build can be executed starting from a local folder containing an already provisioned
 * server and a deployed application; this is typically the "target/server" folder of a project using the
 * wildfly-maven-plugin.
 * <br>
 * No maven build is performed inside the builder image; it's supposed that the application is build locally with
 * whatever strategy, as long as it produces a "target/server" containing the server and an application deployment
 * which are compatible with a WildFly/EAP s2i v2 binary build.
 */
@CleanBeforeAll
@OpenShiftTest
public class WildflyTargetServerTestCase implements ProjectCreationCapable {
	private static final OpenShift openShift = OpenShifts.master();
	private static final WildflyImageOpenShiftApplication application = OpenShiftProvisionerTestBase
			.getWildflyOpenShiftLocalBinaryTargetServerApplication();
	private static final WildflyImageOpenShiftProvisioner provisioner = new WildflyImageOpenShiftProvisioner(application);

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
	public void verifyConfiguration() {
		SoftAssertions softAssertions = new SoftAssertions();
		// verify system property added via cli
		PodShell rsh = new PodShell(openShift, openShift.getAnyPod(application.getName()));

		// verify secret is mounted in /etc/secrets
		PodShellOutput output = rsh.executeWithBash("cat /etc/secrets/" + TEST_SECRET_FOO);
		softAssertions.assertThat(output.getOutput()).as("Secret check: test secret was not properly mounted")
				.contains(TEST_SECRET_BAR);

		// verify the ping service is created and env variables set correctly
		Service pingService = openShift.getService(application.getPingServiceName());
		softAssertions.assertThat(pingService).as("Ping service creation check").isNotNull();
		Map<String, String> expectedEnvVars = new HashMap<>();
		expectedEnvVars.put("JGROUPS_PING_PROTOCOL", "dns.DNS_PING");
		expectedEnvVars.put("OPENSHIFT_DNS_PING_SERVICE_NAME", application.getPingServiceName());
		expectedEnvVars.put("OPENSHIFT_DNS_PING_SERVICE_PORT", "8888");
		softAssertions.assertThat(openShift.getDeploymentConfigEnvVars(application.getName()))
				.as("Ping service variables check")
				.containsAllEntriesOf(expectedEnvVars);

		softAssertions.assertAll();
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
