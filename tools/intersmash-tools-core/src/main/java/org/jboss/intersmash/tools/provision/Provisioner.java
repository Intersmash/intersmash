package org.jboss.intersmash.tools.provision;

import java.net.URL;

import org.jboss.intersmash.tools.application.Application;

/**
 * Application provisioner for deploying and undeploying the application.
 */
public interface Provisioner<T extends Application> {

	/**
	 * Get the {@link Application} provisioned by this {@link Provisioner}.
	 * @return a concrete instance of type {@link Application}
	 */
	T getApplication();

	/**
	 * Task which will be performed by a provisioner prior to {@link #deploy()} operation.
	 */
	void preDeploy();

	/**
	 * Deploy the application.
	 */
	void deploy();

	/**
	 * Undeploy the application.
	 */
	void undeploy();

	/**
	 * Task which will be performed by a provisioner after the {@link #undeploy()} operation.
	 */
	void postUndeploy();

	/**
	 * Get the URL for the application endpoint - depending on the provisioner, this could be BareMetal process,
	 * OpenShift route, ...
	 *
	 * The more specific we go in provisioner hierarchy, the more specific the implementation will be.
	 *
	 * @return Application endpoint
	 */
	URL getURL();

	default void configure() {
		;
	}
}
