package org.jboss.intersmash.tools.application.openshift;

import org.jboss.intersmash.tools.provision.openshift.PostgreSQLTemplateOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.template.PostgreSQLTemplate;

/**
 * End user Application interface which presents PostgreSQL template application on OpenShift Container Platform.
 *
 * See {@link PostgreSQLTemplate} for available templates the
 * application can represent.
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link PostgreSQLTemplateOpenShiftProvisioner}</li>
 * </ul>
 */
public interface PostgreSQLTemplateOpenShiftApplication extends DBTemplateOpenShiftApplication<PostgreSQLTemplate> {

	default String getName() {
		return "postgresql";
	}

}
