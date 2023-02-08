package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.MysqlImageOpenShiftApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlImageOpenShiftProvisionerFactory implements ProvisionerFactory<MysqlImageOpenShiftProvisioner> {

	@Override
	public MysqlImageOpenShiftProvisioner getProvisioner(Application application) {
		if (MysqlImageOpenShiftApplication.class.isAssignableFrom(application.getClass()))
			return new MysqlImageOpenShiftProvisioner((MysqlImageOpenShiftApplication) application);
		return null;
	}
}
