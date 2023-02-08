package org.jboss.intersmash.tools.application.openshift.helm;

/**
 * A contract that defines data that an WILDFLY Helm Chart provisioned application service should expose
 */
public interface WildflyHelmChartOpenShiftApplication extends HelmChartOpenShiftApplication {

	/**
	 * The builder image that should be used by the WILDFLY Helm Charts release
	 * @return A string representing the builder image that will be used by the WILDFLY Helm Charts release
	 */
	String getBuilderImage();

	/**
	 * The runtime image that should be used by the WILDFLY Helm Charts release
	 * @return A string representing the runtime image that will be used by the WILDFLY Helm Charts release
	 */
	String getRuntimeImage();
}
