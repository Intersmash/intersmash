package org.jboss.intersmash.tools.provision.openshift;

import java.util.List;

import io.fabric8.kubernetes.api.model.Pod;

/**
 * Provisioner is able to return its pods.
 */
public interface HasPods {

	/**
	 * Return pods of the application.
	 * @return a list of related {@link Pod} instances
	 */
	List<Pod> getPods();
}
