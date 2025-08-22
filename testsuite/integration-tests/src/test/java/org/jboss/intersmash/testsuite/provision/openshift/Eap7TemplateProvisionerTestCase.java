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

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.jboss.intersmash.application.openshift.Eap7TemplateOpenShiftApplication;
import org.jboss.intersmash.provision.openshift.Eap7TemplateOpenShiftProvisioner;
import org.jboss.intersmash.testsuite.junit5.categories.NotForCommunityExecutionProfile;
import org.jboss.intersmash.testsuite.junit5.categories.OpenShiftTest;
import org.jboss.intersmash.testsuite.openshift.ProjectCreationCapable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.openshift.PodShell;
import cz.xtf.core.openshift.PodShellOutput;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.GitBuildSource;

@CleanBeforeAll
@NotForCommunityExecutionProfile
@OpenShiftTest
public class Eap7TemplateProvisionerTestCase implements ProjectCreationCapable {
	private static final OpenShift openShift = OpenShifts.master();
	private static final Eap7TemplateOpenShiftApplication application = OpenShiftProvisionerTestBase
			.getEap7OpenShiftTemplateApplication();
	private static final Eap7TemplateOpenShiftProvisioner provisioner = new Eap7TemplateOpenShiftProvisioner(application);

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
	public void verifyEapConfiguration() {
		SoftAssertions softAssertions = new SoftAssertions();
		// verify system property added via cli
		PodShell rsh = new PodShell(openShift, openShift.getAnyPod(application.getName()));
		PodShellOutput output = rsh
				.executeWithBash(String.format("$JBOSS_HOME/bin/jboss-cli.sh -c /system-property=%s:read-resource",
						OpenShiftProvisionerTestBase.WILDFLY_TEST_PROPERTY));
		softAssertions.assertThat(output.getError()).as("CLI configuration check: Error should be empty").isEmpty();
		softAssertions.assertThat(output.getOutput()).as("CLI configuration check: Test property was not set by CLI")
				.contains("success", OpenShiftProvisionerTestBase.WILDFLY_TEST_PROPERTY);

		// verify application git
		Optional<BuildConfig> gitBuildConfig = openShift.buildConfigs().list().getItems().stream()
				.filter(buildConfig -> buildConfig.getSpec().getSource().getType().equals("Git"))
				.findFirst();
		softAssertions.assertThat(gitBuildConfig.isPresent()).as("Cannot find a Git build config").isTrue();
		if (gitBuildConfig.isPresent()) {
			softAssertions.assertThat(gitBuildConfig.get().getMetadata().getName()).contains(application.getName());
			GitBuildSource git = gitBuildConfig.get().getSpec().getSource().getGit();
			softAssertions.assertThat(git.getUri()).as("Git repository check")
					.isEqualTo(OpenShiftProvisionerTestBase.EAP7_TEST_APP_REPO);
			softAssertions.assertThat(git.getRef()).as("Git repository reference check")
					.isEqualTo(OpenShiftProvisionerTestBase.EAP7_TEST_APP_REF);
		}
		softAssertions.assertAll();
	}

	/**
	 * Any {@code Secret} or {@code ConfigMap} should be created as a preDeploy() operation by a provisioner.
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
