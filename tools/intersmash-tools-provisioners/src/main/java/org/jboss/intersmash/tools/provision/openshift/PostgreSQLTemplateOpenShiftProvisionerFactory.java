package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.PostgreSQLTemplateOpenShiftApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides logic to obtain a {@link PostgreSQLTemplateOpenShiftProvisioner} instance that is initialized with a
 * given {@link PostgreSQLTemplateOpenShiftApplication} instance.
 */
@Slf4j
public class PostgreSQLTemplateOpenShiftProvisionerFactory
		implements ProvisionerFactory<PostgreSQLTemplateOpenShiftProvisioner> {

	@Override
	public PostgreSQLTemplateOpenShiftProvisioner getProvisioner(Application application) {
		if (PostgreSQLTemplateOpenShiftApplication.class.isAssignableFrom(application.getClass()))
			return new PostgreSQLTemplateOpenShiftProvisioner((PostgreSQLTemplateOpenShiftApplication) application);
		return null;
	}
}
