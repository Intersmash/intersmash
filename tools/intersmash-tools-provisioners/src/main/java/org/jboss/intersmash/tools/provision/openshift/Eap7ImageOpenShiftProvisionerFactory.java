package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.Eap7ImageOpenShiftApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Eap7ImageOpenShiftProvisionerFactory implements ProvisionerFactory<Eap7ImageOpenShiftProvisioner> {

	@Override
	public Eap7ImageOpenShiftProvisioner getProvisioner(Application application) {
		if (Eap7ImageOpenShiftApplication.class.isAssignableFrom(application.getClass()))
			return new Eap7ImageOpenShiftProvisioner((Eap7ImageOpenShiftApplication) application);
		return null;
	}
}
