package org.jboss.intersmash.testsuite.provision.openshift;

import org.assertj.core.api.Assertions;
import org.jboss.intersmash.tools.application.openshift.BootableJarOpenShiftApplication;
import org.jboss.intersmash.tools.provision.openshift.BootableJarImageOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.WildflyBootableJarImageOpenShiftProvisioner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cz.xtf.core.bm.BuildManagers;
import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.junit5.annotations.CleanBeforeAll;

@CleanBeforeAll
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
