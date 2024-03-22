package org.jboss.intersmash.application.openshift;

import java.nio.file.Path;

import org.jboss.intersmash.application.openshift.template.RhSsoTemplate;

/**
 * End user Application interface which presents RH-SSO template application on OpenShift Container Platform.
 * <p>
 * RH-SSO application that is supposed to run on OpenShift needs to implement this interface.
 * Usage:
 * <pre>
 *     &#064;Appsint(
 *     		&#064;Service(RhssoApp.class)
 *     })
 * </pre>
 * The application will be deployed by:
 * <ul>
 *     <li>{@link org.jboss.intersmash.provision.openshift.RhSsoTemplateOpenShiftProvisioner}</li>
 * </ul>
 * <p>
 * See {@link RhSsoTemplate} for available templates the
 * application can represent.
 */
@Deprecated(since = "0.0.2")
public interface RhSsoTemplateOpenShiftApplication extends TemplateApplication<RhSsoTemplate>, HasSecrets {

	default String getName() {
		return "rh-sso";
	}

	/**
	 * Realm configuration in json format for Keycloak partial import
	 * <p>
	 * https://www.keycloak.org/docs/9.0/server_admin/index.html#importing-a-realm-from-exported-json-file
	 * <p>
	 * Requires template parameters SSO_REALM, SSO_SERVICE_USERNAME, SSO_SERVICE_PASSWORD to be set
	 *
	 * @return Instance of {@link Path} representing a YAML definition for the desired realm configuration
	 */
	default Path getRealmConfigurationFilePath() {
		return null;
	}

	/**
	 * Non x509 templates expose an HTTP route named after the application name;
	 * x509 templates don't expose an HTTP route;
	 *
	 * @return The service HTTP route
	 */
	default String getHttpRouteName() {
		return getTemplate().isX509() ? null : getName();
	}

	/**
	 * Non x509 templates expose an HTTPS route named after the application name with the "secure-" prefix;
	 * x509 templates expose an HTTPS route named after the application name;
	 *
	 * @return The service HTTPS route
	 */
	default String getHttpsRouteName() {
		return getTemplate().isX509() ? getName() : String.format("secure-%s", getName());
	}
}
