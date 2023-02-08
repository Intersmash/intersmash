package org.jboss.intersmash.testsuite.provision.openshift.template;

import java.util.List;

import org.jboss.intersmash.tools.provision.openshift.template.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.openshift.api.model.ImageStream;

/**
 * Verify the basic functionality of OpenShift templates provisioning.
 */
@CleanBeforeAll
public class OpenShiftTemplateProvisionerTestCase {

	/**
	 * Verify that every known {@link OpenShiftTemplateProvisioner} implementation is capable of deploying the image
	 * stream of product the provisioner is build for.
	 */
	@ParameterizedTest(name = "{displayName}#class({0})")
	@ValueSource(classes = { KeycloakTemplateProvisioner.class })
	public void imageStreamTest(Class<? extends OpenShiftTemplateProvisioner> provisionerClass)
			throws IllegalAccessException, InstantiationException {
		OpenShiftTemplateProvisioner templateProvisioner = provisionerClass.newInstance();
		List<ImageStream> is = templateProvisioner.deployImageStreams();
		Assertions.assertTrue(is.size() > 0);
	}

	/**
	 * Verify that all templates registered by {@link KeycloakTemplate} can be deployed
	 */
	@ParameterizedTest(name = "{displayName}#template({0})")
	@EnumSource(KeycloakTemplate.class)
	public void keycloakTemplatesAvailability(KeycloakTemplate keycloakTemplate) {
		new KeycloakTemplateProvisioner().deployTemplate(keycloakTemplate);
	}

	/**
	 * Verify that all templates registered by {@link InfinispanTemplate} can be deployed.
	 */
	@ParameterizedTest(name = "{displayName}#template({0})")
	@EnumSource(InfinispanTemplate.class)
	public void infinispanTemplatesAvailability(InfinispanTemplate infinispanTemplate) {
		new InfinispanTemplateProvisioner().deployTemplate(infinispanTemplate);
	}
}
