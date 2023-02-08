package org.jboss.intersmash.tools.application.openshift;

import org.jboss.intersmash.tools.application.Application;

import cz.xtf.core.openshift.OpenShifts;

/**
 * Interface representing the Application on OpenShift Container Platform.
 *
 * This interface is not supposed to be implemented by user Applications. See the "Mapping of implemented provisioners"
 * section of Intersmash README.md file for the up-to-date list of supported end users Applications.
 */
public interface OpenShiftApplication extends Application {

	default String getOpenShiftHostName() {
		return OpenShifts.master().generateHostname(getName());
	}

}
