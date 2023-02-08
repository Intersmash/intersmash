package org.jboss.intersmash.tools.application.openshift;

import java.util.Collections;
import java.util.List;

import org.jboss.intersmash.tools.provision.openshift.template.OpenShiftTemplate;

import io.fabric8.kubernetes.api.model.EnvVar;

/**
 * End user Application interface which presents resources on OpenShift Container Platform created by deploying the
 * eap-s2i-build build template.
 *
 * The template is supposed to be used in conjunction with the WILDFLY Operator - the later can be used to deploy the
 * application image (can be referenced by an actual image link to local image registry, or by an image stream produced
 * by the eap-s2i-build build).
 *
 * See https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.3/html-single/getting_started_with_jboss_eap_for_openshift_container_platform/index#the-eap-s2i-build-template-for-creating-application-images_default
 *
 *
 */
public interface WildflyLegacyS2iBuildTemplateApplication extends TemplateApplication, HasEnvVars {

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
