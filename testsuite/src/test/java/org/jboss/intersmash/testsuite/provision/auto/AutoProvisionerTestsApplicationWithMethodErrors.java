package org.jboss.intersmash.testsuite.provision.auto;

import java.net.URL;

import org.jboss.intersmash.tools.application.auto.AutoProvisioningExecutionException;
import org.jboss.intersmash.tools.application.openshift.AutoProvisioningOpenShiftApplication;
import org.jboss.intersmash.tools.provision.Provisioner;

import lombok.extern.slf4j.Slf4j;

/**
 * This {@link AutoProvisioningOpenShiftApplication} concrete subclass is providing the methods implementation needed
 * by the declarative provisioning process, which is leveraged by implementing
 * {@link AutoProvisioningOpenShiftApplication}.
 *
 * The goal is to test that when an error occurs on some workflow method (e.g.: {@link Provisioner#deploy()}),
 * it is wrapped up by a custom exception representing Intersmash Auto Provisioning errors, i.e. {@link AutoProvisioningExecutionException}.
 */
@Slf4j
public class AutoProvisionerTestsApplicationWithMethodErrors implements AutoProvisioningOpenShiftApplication {

	@Override
	public void preDeploy() throws AutoProvisioningExecutionException {
		try {
			throw new UnsupportedOperationException(
					this.getClass().getName() + " doesn't support the \"preDeploy\" method yet.");
		} catch (Throwable e) {
			throw new AutoProvisioningExecutionException(e);
		}
	}

	@Override
	public void deploy() throws AutoProvisioningExecutionException {
		try {
			throw new UnsupportedOperationException(this.getClass().getName() + " doesn't support the \"deploy\" method yet.");
		} catch (Throwable e) {
			throw new AutoProvisioningExecutionException(e);
		}
	}

	@Override
	public void scale(int replicas, boolean wait) throws AutoProvisioningExecutionException {
		try {
			throw new UnsupportedOperationException(this.getClass().getName() + " doesn't support the \"scale\" method yet.");
		} catch (Throwable e) {
			throw new AutoProvisioningExecutionException(e);
		}
	}

	@Override
	public void undeploy() throws AutoProvisioningExecutionException {
		try {
			throw new UnsupportedOperationException(
					this.getClass().getName() + " doesn't support the \"undeploy\" method yet.");
		} catch (Throwable e) {
			throw new AutoProvisioningExecutionException(e);
		}
	}

	@Override
	public void postUndeploy() throws AutoProvisioningExecutionException {
		try {
			throw new UnsupportedOperationException(
					this.getClass().getName() + " doesn't support the \"postUndeploy\" method yet.");
		} catch (Throwable e) {
			throw new AutoProvisioningExecutionException(e);
		}
	}

	@Override
	public URL getURL() throws AutoProvisioningExecutionException {
		try {
			throw new UnsupportedOperationException(this.getClass().getName() + " doesn't support the \"getURL\" method yet.");
		} catch (Throwable e) {
			throw new AutoProvisioningExecutionException(e);
		}
	}

	@Override
	public String getName() {
		return "failing-auto-provisioning-app";
	}
}
