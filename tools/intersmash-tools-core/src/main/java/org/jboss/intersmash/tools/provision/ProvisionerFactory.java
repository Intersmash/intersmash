package org.jboss.intersmash.tools.provision;

import org.jboss.intersmash.tools.application.Application;

public interface ProvisionerFactory<T extends Provisioner> {

	/**
	 * @param application A concrete {@link Application} instance that represents the application/service to be
	 *                       provisioned
	 * @return provisioner for {@link Application} or null
	 */
	T getProvisioner(Application application);
}
