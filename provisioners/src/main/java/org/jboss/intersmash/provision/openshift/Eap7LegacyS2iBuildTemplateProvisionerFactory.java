package org.jboss.intersmash.provision.openshift;

import org.jboss.intersmash.application.Application;
import org.jboss.intersmash.application.openshift.Eap7LegacyS2iBuildTemplateApplication;
import org.jboss.intersmash.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Eap7LegacyS2iBuildTemplateProvisionerFactory implements ProvisionerFactory<Eap7LegacyS2iBuildTemplateProvisioner> {

	@Override
	public Eap7LegacyS2iBuildTemplateProvisioner getProvisioner(Application application) {
		if (Eap7LegacyS2iBuildTemplateApplication.class.isAssignableFrom(application.getClass()))
			return new Eap7LegacyS2iBuildTemplateProvisioner((Eap7LegacyS2iBuildTemplateApplication) application);
		return null;
	}
}
