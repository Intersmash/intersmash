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
package org.jboss.intersmash.application.openshift;

import java.util.Collections;
import java.util.List;

import org.jboss.intersmash.provision.openshift.Eap7LegacyS2iBuildTemplateProvisioner;
import org.jboss.intersmash.provision.openshift.template.OpenShiftTemplate;

import io.fabric8.kubernetes.api.model.EnvVar;

/**
 * End user Application interface which presents resources on OpenShift Container Platform created by deploying the
 * eap-s2i-build build template.
 *
 * The template is supposed to be used in conjunction with the WILDFLY Operator - the latter can be used to deploy the
 * application image (can be referenced by an actual image link to local image registry, or by an image stream produced
 * by the eap-s2i-build build).
 *
 * See https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.4/html-single/getting_started_with_jboss_eap_for_openshift_container_platform/index#the-eap-s2i-build-template-for-creating-application-images_default
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link Eap7LegacyS2iBuildTemplateProvisioner}</li>
 * </ul>
 */
public interface Eap7LegacyS2iBuildTemplateApplication extends TemplateApplication, HasEnvVars {

	/**
	 * Defaults to https://raw.githubusercontent.com/jboss-container-images/jboss-eap-openshift-templates/master/eap-s2i-build.yaml template
	 * @return eap-s2i-build template name
	 */
	@Override
	default OpenShiftTemplate getTemplate() {
		return () -> "eap-s2i-build";
	}

	@Override
	default List<EnvVar> getEnvVars() {
		return Collections.emptyList();
	}
}
