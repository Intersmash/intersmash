package org.jboss.intersmash.testsuite.provision.openshift;

import org.assertj.core.api.Assertions;
import org.jboss.intersmash.tools.application.openshift.MysqlImageOpenShiftApplication;
import org.jboss.intersmash.tools.provision.openshift.MysqlImageOpenShiftProvisioner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import lombok.extern.slf4j.Slf4j;

@CleanBeforeAll
@Slf4j
public class MysqlImageTestCase {
	private static final OpenShift openShift = OpenShifts.master();
	private static final MysqlImageOpenShiftApplication application = OpenShiftProvisionerTestBase
			.getMysqlOpenShiftApplication();
	private static final MysqlImageOpenShiftProvisioner provisioner = new MysqlImageOpenShiftProvisioner(application);

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
