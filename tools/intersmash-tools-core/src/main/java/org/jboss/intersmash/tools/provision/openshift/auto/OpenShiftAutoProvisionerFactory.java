package org.jboss.intersmash.tools.provision.openshift.auto;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.AutoProvisioningOpenShiftApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * This factory is responsible for recognizing test classes that use auto (or application dictated, or declarative)
 * provisioning, i.e. classes that implement
 * {@link AutoProvisioningOpenShiftApplication}
 */
@Slf4j
public class OpenShiftAutoProvisionerFactory implements ProvisionerFactory<OpenShiftAutoProvisioner> {

	@Override
	public OpenShiftAutoProvisioner getProvisioner(Application application) {
		if (AutoProvisioningOpenShiftApplication.class.isAssignableFrom(application.getClass()))
			return new OpenShiftAutoProvisioner((AutoProvisioningOpenShiftApplication) application);
		return null;
	}
}
