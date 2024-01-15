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
package org.jboss.intersmash.testsuite.provision.auto;

import org.jboss.intersmash.application.openshift.AutoProvisioningOpenShiftApplication;
import org.jboss.intersmash.application.openshift.OpenShiftApplication;
import org.jboss.intersmash.provision.auto.AutoProvisioningManagedException;
import org.jboss.intersmash.provision.openshift.auto.OpenShiftAutoProvisioner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * Smoke test cases that cover usage of
 * {@link OpenShiftAutoProvisioner}
 */
@Slf4j
public class AutoProvisionerTests {

	/**
	 * Tests that expected custom exception is thrown when something goes bad in the
	 * {@link OpenShiftApplication}
	 * which is implementing the Auto Provisioning workflow.
	 */
	@Test
	public void testCustomExceptionsAreThrownForWorkflowErrors() {
		AutoProvisioningOpenShiftApplication application = new AutoProvisionerTestsApplicationWithMethodErrors();
		OpenShiftAutoProvisioner provisioner = new OpenShiftAutoProvisioner(application);

		AutoProvisioningManagedException ex = Assertions.assertThrows(AutoProvisioningManagedException.class,
				() -> provisioner.preDeploy());
		logWrappedUpError(ex);
		ex = Assertions.assertThrows(AutoProvisioningManagedException.class, () -> provisioner.deploy());
		logWrappedUpError(ex);
		ex = Assertions.assertThrows(AutoProvisioningManagedException.class, () -> provisioner.getURL());
		logWrappedUpError(ex);
		ex = Assertions.assertThrows(AutoProvisioningManagedException.class, () -> provisioner.scale(0, false));
		logWrappedUpError(ex);
		ex = Assertions.assertThrows(AutoProvisioningManagedException.class, () -> provisioner.undeploy());
		logWrappedUpError(ex);
		ex = Assertions.assertThrows(AutoProvisioningManagedException.class, () -> provisioner.postUndeploy());
		logWrappedUpError(ex);
	}

	private void logWrappedUpError(AutoProvisioningManagedException ex) {
		log.info(String.format("An error (%s) occurred during the auto provisioning workflow execution: %s",
				ex.getCause().getClass().getName(),
				ex.getMessage()));
	}
}
