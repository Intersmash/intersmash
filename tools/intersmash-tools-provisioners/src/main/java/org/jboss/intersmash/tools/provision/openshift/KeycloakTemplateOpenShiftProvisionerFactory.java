package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.KeycloakTemplateOpenShiftApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeycloakTemplateOpenShiftProvisionerFactory implements ProvisionerFactory<KeycloakTemplateOpenShiftProvisioner> {

	@Override
	public KeycloakTemplateOpenShiftProvisioner getProvisioner(Application application) {
		if (KeycloakTemplateOpenShiftApplication.class.isAssignableFrom(application.getClass()))
			return new KeycloakTemplateOpenShiftProvisioner((KeycloakTemplateOpenShiftApplication) application);
		return null;
	}
}
