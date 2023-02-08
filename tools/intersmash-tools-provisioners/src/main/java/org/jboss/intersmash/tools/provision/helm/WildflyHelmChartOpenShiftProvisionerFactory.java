package org.jboss.intersmash.tools.provision.helm;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.helm.WildflyHelmChartOpenShiftApplication;
import org.jboss.intersmash.tools.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * A provisioner factory that will return a Helm Charts based provisioner for Wildfly, see
 * {@link WildflyHelmChartOpenShiftProvisioner}
 */
@Slf4j
public class WildflyHelmChartOpenShiftProvisionerFactory implements ProvisionerFactory<HelmChartOpenShiftProvisioner> {

	@Override
	public HelmChartOpenShiftProvisioner getProvisioner(Application application) {
		if (WildflyHelmChartOpenShiftApplication.class.isAssignableFrom(application.getClass())) {
			return new WildflyHelmChartOpenShiftProvisioner((WildflyHelmChartOpenShiftApplication) application);
		}
		return null;
	}
}
