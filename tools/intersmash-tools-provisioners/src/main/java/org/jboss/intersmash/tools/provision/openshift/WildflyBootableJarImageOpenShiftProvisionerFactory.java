package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.BootableJarOpenShiftApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WildflyBootableJarImageOpenShiftProvisionerFactory
		implements ProvisionerFactory<WildflyBootableJarImageOpenShiftProvisioner> {

	@Override
	public WildflyBootableJarImageOpenShiftProvisioner getProvisioner(Application application) {
		if (BootableJarOpenShiftApplication.class.isAssignableFrom(application.getClass()))
			return new WildflyBootableJarImageOpenShiftProvisioner((BootableJarOpenShiftApplication) application);
		return null;
	}
}
