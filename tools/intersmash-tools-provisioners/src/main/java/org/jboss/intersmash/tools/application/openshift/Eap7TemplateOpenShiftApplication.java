package org.jboss.intersmash.tools.application.openshift;

import org.jboss.intersmash.tools.application.openshift.template.Eap7Template;
import org.jboss.intersmash.tools.provision.openshift.Eap7TemplateOpenShiftProvisioner;

/**
 * End user Application descriptor interface which presents EAP 7 template application on OpenShift Container Platform.
 *
 * See {@link Eap7Template} for available templates the
 * application can represent.
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link Eap7TemplateOpenShiftProvisioner}</li>
 * </ul>
 */
public interface Eap7TemplateOpenShiftApplication
		extends WildflyOpenShiftApplication, TemplateApplication<Eap7Template> {

}
