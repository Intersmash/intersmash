package org.jboss.intersmash.tools.application.openshift;

import org.jboss.intersmash.tools.provision.openshift.MysqlImageOpenShiftProvisioner;

/**
 * End user Application interface which presents MYSQL image application on OpenShift Container Platform.
 *
 * MYSQL application that is supposed to run on OpenShift needs to implement this interface.
 * Usage:
 * <pre>
 *     &#064;Intersmash(
 *           &#064;Service(MysqlApp.class)
 * )
 * </pre>
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link MysqlImageOpenShiftProvisioner}</li>
 * </ul>
 */
public interface MysqlImageOpenShiftApplication extends DBImageOpenShiftApplication {

	default String getName() {
		return "mysql";
	}

}
