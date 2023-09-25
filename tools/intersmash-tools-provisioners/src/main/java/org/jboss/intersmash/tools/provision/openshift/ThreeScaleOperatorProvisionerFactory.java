package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.ThreeScaleOperatorApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreeScaleOperatorProvisionerFactory implements ProvisionerFactory<ThreeScaleOperatorProvisioner> {

	@Override
	public ThreeScaleOperatorProvisioner getProvisioner(Application application) {
		if (ThreeScaleOperatorApplication.class.isAssignableFrom(application.getClass()))
			return new ThreeScaleOperatorProvisioner((ThreeScaleOperatorApplication) application);
		return null;
	}
}
