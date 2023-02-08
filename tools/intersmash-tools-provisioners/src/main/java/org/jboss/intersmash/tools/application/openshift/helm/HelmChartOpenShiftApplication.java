package org.jboss.intersmash.tools.application.openshift.helm;

import org.jboss.intersmash.tools.application.openshift.HasSecrets;
import org.jboss.intersmash.tools.application.openshift.OpenShiftApplication;
import org.jboss.intersmash.tools.provision.helm.HelmChartOpenShiftProvisioner;

/**
 * Contract to implement descriptors for any application services that will be provisioned through Helm Charts by
 * {@link HelmChartOpenShiftProvisioner}
 */
public interface HelmChartOpenShiftApplication extends OpenShiftApplication, HasSecrets {

	/**
	 * Instance of {@link HelmChartRelease} that defines the actual release
	 * @return Instance of {@link HelmChartRelease} that defines the actual release
	 */
	HelmChartRelease getRelease();

	/**
	 * The Helm Charts repo URL
	 * @return A string that identifies the used Helm Charts repository URL
	 */
	String getHelmChartsRepositoryUrl();

	/**
	 * The Helm Charts repo URL
	 * @return A string that identifies the used Helm Charts repository branch
	 */
	String getHelmChartsRepositoryRef();

	/**
	 * The Helm Charts repo URL
	 * @return A string that identifies the used Helm Charts repository name
	 */
	String getHelmChartsRepositoryName();
}
