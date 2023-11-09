package org.jboss.intersmash.tools.application.openshift.template;

import java.util.HashMap;
import java.util.Map;

import org.jboss.intersmash.tools.provision.openshift.template.OpenShiftTemplate;

/**
 * OpenShift template for RH-SSO.
 * <p>
 * See https://github.com/jboss-container-images/redhat-sso-7-openshift-image
 */
public enum RhSsoTemplate implements OpenShiftTemplate {
	HTTPS("https"),
	POSTGRESQL("postgresql"),
	POSTGRESQL_PERSISTENT("postgresql-persistent"),
	X509_HTTPS("x509-https"),
	X509_POSTGRESQL_PERSISTENT("x509-postgresql-persistent");

	private static final Map<String, RhSsoTemplate> BY_LABEL = new HashMap<>();

	static {
		for (RhSsoTemplate e : values()) {
			BY_LABEL.put(e.label, e);
		}
	}

	private String label;

	RhSsoTemplate(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public boolean isX509() {
		return label.contains("x509");
	}

	public static RhSsoTemplate valueOfLabel(String label) {
		return BY_LABEL.get(label);
	}
}
