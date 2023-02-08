package org.jboss.intersmash.tools.application.openshift;

import java.util.Collections;
import java.util.List;

import io.fabric8.kubernetes.api.model.Secret;

/**
 * This interface is not supposed to be implemented by user Applications. See the "Mapping of implemented provisioners"
 * section of Intersmash README.md file for the up-to-date list of supported end users Applications.
 */
public interface WildflyOpenShiftApplication extends OpenShiftApplication, HasSecrets {

	//
	//  WILDFLY/Application configuration
	//

	default List<String> getCliScript() {
		return Collections.emptyList();
	}

	//
	// OpenShift configuration
	//

	@Override
	default List<Secret> getSecrets() {
		return Collections.emptyList();
	}
}
