package org.jboss.intersmash.tools.application.openshift;

import java.util.List;

import io.fabric8.kubernetes.api.model.EnvVar;

/**
 * Providers serving Application implementing this interface are capable of managing the environment variables.
 *
 * This interface is not supposed to be implemented by user Applications. See the "Mapping of implemented provisioners"
 * section of Intersmash README.md file for the up-to-date list of supported end users Applications.
 */
public interface HasEnvVars {

	/**
	 * Additional environment variables appended to build config objects created by the provisioner.
	 * @return additional environment variables
	 */
	List<EnvVar> getEnvVars();
}
