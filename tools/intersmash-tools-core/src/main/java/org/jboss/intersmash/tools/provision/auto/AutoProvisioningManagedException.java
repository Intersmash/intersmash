package org.jboss.intersmash.tools.provision.auto;

import org.jboss.intersmash.tools.application.openshift.AutoProvisioningOpenShiftApplication;
import org.jboss.intersmash.tools.provision.Provisioner;
import org.jboss.intersmash.tools.provision.openshift.auto.OpenShiftAutoProvisioner;

/**
 * {@link RuntimeException} class that is meant to wrap any exception thrown by the
 * {@link AutoProvisioningOpenShiftApplication} which is delegated
 * for the provisioning workflow execution.
 *
 * Since this is a {@code RuntimeException}, it is not needed for the caller - e.g.: provisioner - methods signature to
 * declare it in their {@code throws} clause, thus keeping the implementation compliant with the
 * {@link Provisioner} contract.
 *
 * {@link OpenShiftAutoProvisioner} implementation uses this class in order to wrap the exceptions which are bubbling
 * up from the delegated application code execution in a common way, so that the call code (e.g.: test classes) can
 * handle them appropriately.
 */
public class AutoProvisioningManagedException extends RuntimeException {
	public AutoProvisioningManagedException(Throwable cause) {
		super(cause);
	}
}
