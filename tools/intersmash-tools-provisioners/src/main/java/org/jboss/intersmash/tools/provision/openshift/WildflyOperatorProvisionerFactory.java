package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.WildflyOperatorApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WildflyOperatorProvisionerFactory implements ProvisionerFactory<WildflyOperatorProvisioner> {

	@Override
	public WildflyOperatorProvisioner getProvisioner(Application application) {
		if (WildflyOperatorApplication.class.isAssignableFrom(application.getClass()))
			return new WildflyOperatorProvisioner((WildflyOperatorApplication) application);
		return null;
	}
}
