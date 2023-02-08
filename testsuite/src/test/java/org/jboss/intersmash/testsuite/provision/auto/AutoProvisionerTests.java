package org.jboss.intersmash.testsuite.provision.auto;

import org.jboss.intersmash.tools.application.openshift.AutoProvisioningOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.OpenShiftApplication;
import org.jboss.intersmash.tools.provision.auto.AutoProvisioningManagedException;
import org.jboss.intersmash.tools.provision.openshift.auto.OpenShiftAutoProvisioner;
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
