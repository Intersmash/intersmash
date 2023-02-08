package org.jboss.intersmash.tools.provision.openshift;

/**
 * Provisioner is able to serve the {@code scale} operation.
 */
public interface Scalable {

	/**
	 * Scale the application to required number of replicas.
	 *
	 * @param replicas number of replicas we want to scale to
	 * @param wait     whether to block the method until the required number of replicas is ready
	 */
	void scale(int replicas, boolean wait);
}
