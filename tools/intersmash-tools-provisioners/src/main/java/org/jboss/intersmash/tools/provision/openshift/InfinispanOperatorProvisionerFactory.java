package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.InfinispanOperatorApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InfinispanOperatorProvisionerFactory implements ProvisionerFactory<InfinispanOperatorProvisioner> {

	@Override
	public InfinispanOperatorProvisioner getProvisioner(Application application) {
		if (InfinispanOperatorApplication.class.isAssignableFrom(application.getClass()))
			return new InfinispanOperatorProvisioner((InfinispanOperatorApplication) application);
		return null;
	}
}
