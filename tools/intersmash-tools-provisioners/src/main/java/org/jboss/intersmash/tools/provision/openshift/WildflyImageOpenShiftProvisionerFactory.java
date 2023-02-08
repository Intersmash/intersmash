package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.WildflyImageOpenShiftApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WildflyImageOpenShiftProvisionerFactory implements ProvisionerFactory<WildflyImageOpenShiftProvisioner> {

	@Override
	public WildflyImageOpenShiftProvisioner getProvisioner(Application application) {
		if (WildflyImageOpenShiftApplication.class.isAssignableFrom(application.getClass()))
			return new WildflyImageOpenShiftProvisioner((WildflyImageOpenShiftApplication) application);
		return null;
	}
}
