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
package org.jboss.intersmash.application.openshift.template;

import org.jboss.intersmash.provision.openshift.template.OpenShiftTemplate;

/**
 * OpenShift template for EAP 7.z (i.e. WildFly Jakarta EE 8 &lt;= 26.1.2) applications.
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
