package org.jboss.intersmash.tools.application.openshift;

import org.jboss.intersmash.tools.provision.openshift.PostgreSQLImageOpenShiftProvisioner;

/**
 * End user Application interface which presents PostgreSQL image application on OpenShift Container Platform.
 *
 * PostgreSQL application that is supposed to run on OpenShift needs to implement this interface.
 * Usage:
 * <pre>
 *     &#064;Intersmash(
 *     		&#064;Service(PgSQLApp.class)
 *     })
 * </pre>
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link PostgreSQLImageOpenShiftProvisioner}</li>
 * </ul>
 */
public interface PostgreSQLImageOpenShiftApplication extends DBImageOpenShiftApplication {

	default String getName() {
		return "postgresql";
	}

}
