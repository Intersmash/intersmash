package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.PostgreSQLImageOpenShiftApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostgreSQLImageOpenShiftProvisionerFactory implements ProvisionerFactory<PostgreSQLImageOpenShiftProvisioner> {

	@Override
	public PostgreSQLImageOpenShiftProvisioner getProvisioner(Application application) {
		if (PostgreSQLImageOpenShiftApplication.class.isAssignableFrom(application.getClass()))
			return new PostgreSQLImageOpenShiftProvisioner((PostgreSQLImageOpenShiftApplication) application);
		return null;
	}
}
