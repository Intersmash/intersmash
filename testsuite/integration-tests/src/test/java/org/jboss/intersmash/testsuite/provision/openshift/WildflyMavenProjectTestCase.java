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
import org.jboss.intersmash.tools.provision.openshift.WildflyImageOpenShiftProvisioner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cz.xtf.core.config.BuildManagerConfig;
import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.openshift.PodShell;
import cz.xtf.core.openshift.PodShellOutput;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.kubernetes.api.model.Service;

/**
 * This test verifies a binary build can be executed using a local folder containing a maven project using the
 * wildfly-maven-plugin.
 * <br>
 * The code inside the local folder is uploaded to the builder image and the actual maven build is executed
 * inside the builder image via the s2i binary build process.
 */
@CleanBeforeAll
public class WildflyMavenProjectTestCase {
	private static final OpenShift openShift = OpenShifts.master();
	private static final OpenShiftProvisionerTestBase.StaticWildflyImageOpenShiftDeploymentApplication application = OpenShiftProvisionerTestBase
			.getWildflyOpenShiftLocalBinarySourceApplication();
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
		PodShellOutput output = rsh
				.executeWithBash(String.format("$JBOSS_HOME/bin/jboss-cli.sh -c /system-property=%s:read-resource",
						OpenShiftProvisionerTestBase.WILDFLY_TEST_PROPERTY));
		softAssertions.assertThat(output.getError()).as("CLI configuration check: Error should be empty").isEmpty();
		softAssertions.assertThat(output.getOutput()).as("CLI configuration check: Test property was not set by CLI")
				.contains("success", OpenShiftProvisionerTestBase.WILDFLY_TEST_PROPERTY);

		// verify secret is mounted in /etc/secrets
		output = rsh.executeWithBash("cat /etc/secrets/" + TEST_SECRET_FOO);
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

	@Test
	public void verifyOpenShiftConfiguration() {
		// environmentVariables
		Assertions
				.assertThat(
						OpenShifts.master(BuildManagerConfig.namespace()).getBuildConfig(application.getName()).getSpec()
								.getStrategy().getSourceStrategy().getEnv())
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
