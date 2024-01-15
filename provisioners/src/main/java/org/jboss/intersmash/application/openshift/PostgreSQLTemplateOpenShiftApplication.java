package org.jboss.intersmash.application.openshift;

import org.jboss.intersmash.application.openshift.template.PostgreSQLTemplate;
import org.jboss.intersmash.provision.openshift.PostgreSQLTemplateOpenShiftProvisioner;

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
