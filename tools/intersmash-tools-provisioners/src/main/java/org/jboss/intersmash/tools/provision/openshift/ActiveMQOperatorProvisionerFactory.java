package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.ActiveMQOperatorApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActiveMQOperatorProvisionerFactory implements ProvisionerFactory<ActiveMQOperatorProvisioner> {

	@Override
	public ActiveMQOperatorProvisioner getProvisioner(Application application) {
		if (ActiveMQOperatorApplication.class.isAssignableFrom(application.getClass()))
			return new ActiveMQOperatorProvisioner((ActiveMQOperatorApplication) application);
		return null;
	}
}
