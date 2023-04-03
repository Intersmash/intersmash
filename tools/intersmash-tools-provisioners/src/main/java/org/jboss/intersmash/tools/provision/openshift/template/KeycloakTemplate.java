/**
 * Copyright (C) 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
