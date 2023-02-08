package org.jboss.intersmash.tools.application.openshift;

import java.util.Collections;
import java.util.List;

import cz.xtf.builder.builders.pod.PersistentVolumeClaim;

/**
 * This interface is not supposed to be implemented by user Applications. See the "Mapping of implemented provisioners"
 * section of Intersmash README.md file for the up-to-date list of supported end users Applications.
 */
public interface DBImageOpenShiftApplication extends OpenShiftApplication {
	String getUser();

	String getPassword();

	String getDbName();

	default List<PersistentVolumeClaim> getPersistentVolumeClaims() {
		return Collections.emptyList();
	}
}
