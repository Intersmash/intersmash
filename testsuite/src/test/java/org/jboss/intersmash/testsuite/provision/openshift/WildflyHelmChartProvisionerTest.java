package org.jboss.intersmash.testsuite.provision.openshift;

import org.jboss.intersmash.tools.application.openshift.helm.WildflyHelmChartOpenShiftApplication;
import org.jboss.intersmash.tools.provision.helm.HelmChartOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.helm.WildflyHelmChartOpenShiftProvisioner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cz.xtf.junit5.annotations.CleanBeforeAll;

/**
 * Test case to verify the basic {@link HelmChartOpenShiftProvisioner}
 * life cycle management operations on a WildFly application which release definition is loaded
 * programmatically
 */
@CleanBeforeAll
public class WildflyHelmChartProvisionerTest {

	@Test
	//@Disabled("No artifacts on a Maven repo which is reachable by the Pod")
	public void basicProvisioningTest() {
		// initialize the application service descriptor
		final WildflyHelmChartOpenShiftApplication application = new WildflyHelmChartOpenShiftExampleApplicaton();
		// and now get an EAP 8/WildFly provisioner for that application
		final WildflyHelmChartOpenShiftProvisioner provisioner = new WildflyHelmChartOpenShiftProvisioner(application);
		// deploy
		provisioner.preDeploy();
		try {
			provisioner.deploy();
			try {
				Assertions.assertEquals(1, provisioner.getPods().size(),
						"Unexpected number of cluster operator pods for '"
								+ provisioner.getApplication().getName() + "' after deploy");

				// scale
				int scaledNum = provisioner.getApplication().getRelease().getReplicas() + 1;
				provisioner.scale(scaledNum, true);
				Assertions.assertEquals(scaledNum, provisioner.getPods().size(),
						"Unexpected number of cluster operator pods for '"
								+ provisioner.getApplication().getName()
								+ "' after scaling");
			} finally {
				// undeploy
				provisioner.undeploy();
				Assertions.assertEquals(0, provisioner.getPods().size(),
						"Unexpected number of pods after undeploy");
			}
		} finally {
			provisioner.postUndeploy();
		}
	}
}
