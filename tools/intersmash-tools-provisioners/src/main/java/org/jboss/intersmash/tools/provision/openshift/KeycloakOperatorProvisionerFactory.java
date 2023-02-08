package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.KeycloakOperatorApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeycloakOperatorProvisionerFactory implements ProvisionerFactory<KeycloakOperatorProvisioner> {

	@Override
	public KeycloakOperatorProvisioner getProvisioner(Application application) {
		if (KeycloakOperatorApplication.class.isAssignableFrom(application.getClass()))
			return new KeycloakOperatorProvisioner((KeycloakOperatorApplication) application);
		return null;
	}
}
