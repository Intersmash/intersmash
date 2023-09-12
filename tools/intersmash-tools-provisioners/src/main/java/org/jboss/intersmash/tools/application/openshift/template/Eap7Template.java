package org.jboss.intersmash.tools.application.openshift.template;

import org.jboss.intersmash.tools.provision.openshift.template.OpenShiftTemplate;

/**
 * OpenShift template for EAP 7.z (i.e. WildFly Jakarta EE 8 <= 26.1.2) applications.
 * <p>
 * See e.g.: https://github.com/jboss-container-images/jboss-eap-7-openshift-image
 */
public enum Eap7Template implements OpenShiftTemplate {
	AMQ_PERSISTENT("amq-persistent"),
	AMQ("amq"),
	BASIC("basic"),
	HTTPS("https"),
	SSO("sso");

	private String name;

	Eap7Template(String name) {
		this.name = name;
	}

	@Override
	public String getLabel() {
		return name;
	}
}
