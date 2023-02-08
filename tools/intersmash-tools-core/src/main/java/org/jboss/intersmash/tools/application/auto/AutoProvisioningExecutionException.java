package org.jboss.intersmash.tools.application.auto;

import org.jboss.intersmash.tools.application.openshift.AutoProvisioningOpenShiftApplication;

/**
 * Exceptions that happen during the auto provisioning workflow execution
 *
 * {@link AutoProvisioningOpenShiftApplication} provides a way to plug
 * in methods that will implement the provisioning workflow steps.
 * Any code could be executed this way and any error could happen.
 * By defining that {@link AutoProvisioningOpenShiftApplication}
 * methods can throw this exception type, the framework requires for implementations to wrap any original error with
 * the {@code AutoProvisioningException} type.
 * This should allow the for caller code (e.g. test classes, provisioners) to have a common logical interface to handle
 * and interpret errors thrown by implementation code appropriately.
 */
public class AutoProvisioningExecutionException extends Exception {
	public AutoProvisioningExecutionException(Throwable cause) {
		super(cause);
	}
}
