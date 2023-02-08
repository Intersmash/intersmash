package org.jboss.intersmash.tools.provision.openshift.template;

import java.util.HashMap;
import java.util.Map;

/**
 * OpenShift templates for Keycloak.
 */
public enum KeycloakTemplate implements OpenShiftTemplate {
	HTTPS("https"),
	POSTGRESQL("postgresql"),
	POSTGRESQL_PERSISTENT("postgresql-persistent"),
	X509_HTTPS("x509-https"),
	X509_POSTGRESQL_PERSISTENT("x509-postgresql-persistent");

	private static final Map<String, KeycloakTemplate> BY_LABEL = new HashMap<>();

	static {
		for (KeycloakTemplate e : values()) {
			BY_LABEL.put(e.label, e);
		}
	}

	private String label;

	KeycloakTemplate(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public boolean isX509() {
		return label.contains("x509");
	}

	public static KeycloakTemplate valueOfLabel(String label) {
		return BY_LABEL.get(label);
	}
}
