package org.jboss.intersmash.application.openshift;

import java.util.Collections;
import java.util.Map;

import org.jboss.intersmash.provision.openshift.template.OpenShiftTemplate;

/**
 * This interface is not supposed to be implemented by user Applications. See the "Mapping of implemented provisioners"
 * section of Appsint README.md file for the up-to-date list of supported end users Applications.
 */
public interface DBTemplateOpenShiftApplication<TT extends OpenShiftTemplate>
		extends TemplateApplication<TT> {

	@Override
	default Map<String, String> getParameters() {
		return Collections.emptyMap();
	}
}
