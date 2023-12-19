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

import org.jboss.intersmash.tools.application.openshift.helm.WildflyHelmChartOpenShiftApplication;
import org.jboss.intersmash.tools.provision.helm.HelmChartOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.helm.wildfly.WildflyHelmChartOpenShiftProvisioner;
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
	public void basicProvisioningTest() {
		// initialize the application service descriptor
		final WildflyHelmChartOpenShiftApplication application = new WildflyHelmChartOpenShiftExampleApplication();
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
