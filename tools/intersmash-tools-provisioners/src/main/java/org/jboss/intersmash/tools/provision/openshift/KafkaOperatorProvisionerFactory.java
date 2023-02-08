package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.KafkaOperatorApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaOperatorProvisionerFactory implements ProvisionerFactory<KafkaOperatorProvisioner> {

	@Override
	public KafkaOperatorProvisioner getProvisioner(Application application) {
		if (KafkaOperatorApplication.class.isAssignableFrom(application.getClass()))
			return new KafkaOperatorProvisioner((KafkaOperatorApplication) application);
		return null;
	}
}
