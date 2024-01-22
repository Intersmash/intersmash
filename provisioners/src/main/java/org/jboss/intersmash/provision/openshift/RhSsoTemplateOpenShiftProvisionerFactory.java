package org.jboss.intersmash.provision.openshift;

import org.jboss.intersmash.application.Application;
import org.jboss.intersmash.application.openshift.RhSsoTemplateOpenShiftApplication;
import org.jboss.intersmash.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RhSsoTemplateOpenShiftProvisionerFactory implements ProvisionerFactory<RhSsoTemplateOpenShiftProvisioner> {

	@Override
	public RhSsoTemplateOpenShiftProvisioner getProvisioner(Application application) {
		if (RhSsoTemplateOpenShiftApplication.class.isAssignableFrom(application.getClass()))
			return new RhSsoTemplateOpenShiftProvisioner((RhSsoTemplateOpenShiftApplication) application);
		return null;
	}
}
