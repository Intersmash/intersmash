package org.jboss.intersmash.tools.application.openshift;

import java.util.Map;

import org.jboss.intersmash.tools.provision.openshift.template.OpenShiftTemplate;

/**
 * Interface representing the Application on OpenShift Container Platform that is deployed by template.
 *
 * This interface is not supposed to be implemented by user Applications. See the "Mapping of implemented provisioners"
 * section of Intersmash README.md file for the up-to-date list of supported end users Applications.
 */
public interface TemplateApplication<TT extends OpenShiftTemplate> extends OpenShiftApplication {

	/**
	 * Define template to used. Look at {@link OpenShiftTemplate} implementations to determine template.
	 * @return A concrete instance of type {@link OpenShiftTemplate}
	 */
	TT getTemplate();

	/**
	 * Template parameters.
	 * @return A {@link Map} instance, storing parameters for the template
	 */
	Map<String, String> getParameters();
}
