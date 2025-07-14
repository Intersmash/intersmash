/*
 * Copyright (C) 2025 Red Hat, Inc.
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
package org.jboss.intersmash.application.openshift.template;

import java.util.HashMap;
import java.util.Map;

import org.jboss.intersmash.provision.openshift.template.OpenShiftTemplate;

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
