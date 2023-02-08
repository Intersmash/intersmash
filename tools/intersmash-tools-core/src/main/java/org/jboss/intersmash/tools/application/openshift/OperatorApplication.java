package org.jboss.intersmash.tools.application.openshift;

import java.util.Collections;
import java.util.List;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;

/**
 * This interface is not supposed to be implemented by user Applications. See the "Mapping of implemented provisioners"
 * section of Intersmash README.md file for the up-to-date list of supported end users Applications.
 */
public interface OperatorApplication extends OpenShiftApplication, HasSecrets, HasConfigMaps {

	@Override
	default List<Secret> getSecrets() {
		return Collections.emptyList();
	}

	@Override
	default List<ConfigMap> getConfigMaps() {
		return Collections.emptyList();
	}

}
