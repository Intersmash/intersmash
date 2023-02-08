package org.jboss.intersmash.tools.application.openshift;

import java.util.List;

import io.fabric8.kubernetes.api.model.ConfigMap;

/**
 * Providers serving Application implementing this interface are capable of managing the ConfigMap resources.
 *
 * This interface is not supposed to be implemented by user Applications. See the "Mapping of implemented provisioners"
 * section of Intersmash README.md file for the up-to-date list of supported end users Applications.
 */
public interface HasConfigMaps {
	/**
	 * Get {@link ConfigMap} objects associated with an application. It's a task for provider to deploy and undeploy the
	 * resource on tested cluster as a part of standard application lifecycle.
	 *
	 * @return ConfigMap resources associated with Application
	 */
	List<ConfigMap> getConfigMaps();
}
