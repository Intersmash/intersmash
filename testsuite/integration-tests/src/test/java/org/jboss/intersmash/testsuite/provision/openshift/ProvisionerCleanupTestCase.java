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
package org.jboss.intersmash.testsuite.provision.openshift;

import java.io.IOException;
import java.util.stream.Stream;

import org.jboss.intersmash.provision.openshift.Eap7ImageOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.Eap7LegacyS2iBuildTemplateProvisioner;
import org.jboss.intersmash.provision.openshift.InfinispanOperatorProvisioner;
import org.jboss.intersmash.provision.openshift.KeycloakOperatorProvisioner;
import org.jboss.intersmash.provision.openshift.MysqlImageOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.OpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.PostgreSQLImageOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.PostgreSQLTemplateOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.RhSsoTemplateOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.WildflyBootableJarImageOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.WildflyImageOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.operator.OperatorProvisioner;
import org.jboss.intersmash.provision.openshift.operator.resources.OperatorGroup;
import org.jboss.intersmash.testsuite.IntersmashTestsuiteProperties;
import org.jboss.intersmash.testsuite.junit5.categories.NotForCommunityExecutionProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.junit5.annotations.CleanBeforeEach;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import lombok.extern.slf4j.Slf4j;

@CleanBeforeEach
@Slf4j
public class ProvisionerCleanupTestCase {
	protected static final OpenShift openShift = OpenShifts.master();

	private static Stream<OpenShiftProvisioner> provisionerProvider() {
		if (IntersmashTestsuiteProperties.isCommunityTestExecutionProfileEnabled()) {
			return Stream.of(
					// Infinispan
					new InfinispanOperatorProvisioner(OpenShiftProvisionerTestBase.getInfinispanOperatorApplication())
					// WildFly
					, new WildflyBootableJarImageOpenShiftProvisioner(
							OpenShiftProvisionerTestBase.getWildflyBootableJarOpenShiftApplication()),
					new WildflyBootableJarImageOpenShiftProvisioner(
							OpenShiftProvisionerTestBase.getEap7BootableJarOpenShiftApplication())
					// Keycloak
					, new KeycloakOperatorProvisioner(
							OpenShiftProvisionerTestBase.getKeycloakOperatorApplication())
					// MySQL
					, new MysqlImageOpenShiftProvisioner(OpenShiftProvisionerTestBase.getMysqlOpenShiftApplication())
					// PostgreSql
					, new PostgreSQLImageOpenShiftProvisioner(
							OpenShiftProvisionerTestBase.getPostgreSQLImageOpenShiftApplication()),
					new PostgreSQLTemplateOpenShiftProvisioner(
							OpenShiftProvisionerTestBase.getPostgreSQLTemplateOpenShiftApplication()));
		} else if (IntersmashTestsuiteProperties.isProductizedTestExecutionProfileEnabled()) {
			return Stream.of(
					// RHDG
					new InfinispanOperatorProvisioner(OpenShiftProvisionerTestBase.getInfinispanOperatorApplication())
					// EAP latest GA
					, new WildflyImageOpenShiftProvisioner(
							OpenShiftProvisionerTestBase.getWildflyOpenShiftLocalBinaryTargetServerApplication())
					// EAP 7
					, new Eap7ImageOpenShiftProvisioner(OpenShiftProvisionerTestBase.getEap7OpenShiftImageApplication())
					// RHSSO 7.6.x
					, new RhSsoTemplateOpenShiftProvisioner(OpenShiftProvisionerTestBase.getHttpsRhSso())
					// RHBK
					, new KeycloakOperatorProvisioner(
							OpenShiftProvisionerTestBase.getKeycloakOperatorApplication())
			);
		} else {
			throw new IllegalStateException(
					String.format("Unknown Intersmash test suite execution profile: %s",
							IntersmashTestsuiteProperties.getTestExecutionProfile()));
		}
	}

	@ParameterizedTest(name = "{displayName}#class({0})")
	@MethodSource("provisionerProvider")
	public void testProvisioningWorkflowCleanup(OpenShiftProvisioner provisioner) throws IOException {
		evalOperatorSetup(provisioner);
		try {
			provisioner.configure();
			try {
				provisioner.preDeploy();
				try {
					provisioner.deploy();
					try {
						openShift.configMaps()
								.create(new ConfigMapBuilder().withNewMetadata().withName("no-delete").endMetadata().build());
					} finally {
						provisioner.undeploy();
					}
				} finally {
					provisioner.postUndeploy();
				}
			} finally {
				provisioner.dismiss();
			}
			Assertions.assertNotNull(openShift.configMaps().withName("no-delete").get());
			openShift.configMaps().withName("no-delete").delete();
			openShift.waiters().isProjectClean().waitFor();
		} finally {
			evalOperatorTeardown(provisioner);
		}
	}

	private static void evalOperatorTeardown(OpenShiftProvisioner provisioner) {
		if (OperatorProvisioner.class.isAssignableFrom(provisioner.getClass())) {
			operatorCleanup();
		}
	}

	private static void evalOperatorSetup(OpenShiftProvisioner provisioner) throws IOException {
		if (OperatorProvisioner.class.isAssignableFrom(provisioner.getClass())) {
			operatorCleanup();
			log.debug("Deploy operatorgroup [{}] to enable operators subscription into tested namespace",
					OperatorGroup.SINGLE_NAMESPACE.getMetadata().getName());
			OpenShifts.adminBinary().execute("apply", "-f",
					OperatorGroup.SINGLE_NAMESPACE.save().getAbsolutePath());
		}
	}

	/**
	 * EapS2iBuild application requires additional image streams to be created. CleanBeforeEach would delete it if the
	 * provisioner is initialized in {@link #provisionerProvider()}, so we need a separate test method.
	 */
	@Test
	@NotForCommunityExecutionProfile
	public void eap7LegacyS2iBuild() {
		Eap7LegacyS2iBuildTemplateProvisioner provisioner = new Eap7LegacyS2iBuildTemplateProvisioner(
				OpenShiftProvisionerTestBase.getEap7LegacyS2iBuildTemplateApplication());
		provisioner.preDeploy();
		provisioner.deploy();
		openShift.configMaps().create(new ConfigMapBuilder().withNewMetadata().withName("no-delete").endMetadata().build());
		provisioner.undeploy();
		provisioner.postUndeploy();
		Assertions.assertNotNull(openShift.configMaps().withName("no-delete").get());
		openShift.configMaps().withName("no-delete").delete();
		// delete the images streams created by EapS2iBuildTemplateApplication
		openShift.imageStreams()
				.withName(((String) provisioner.getApplication().getParameters().get("EAP_IMAGE")).split(":")[0])
				.delete();
		openShift.imageStreams()
				.withName(((String) provisioner.getApplication().getParameters().get("EAP_RUNTIME_IMAGE")).split(":")[0])
				.delete();

		openShift.waiters().isProjectClean().waitFor();
	}

	/**
	 * Clean all OLM related objects.
	 * <p>
	 */
	public static void operatorCleanup() {
		OpenShifts.adminBinary().execute("delete", "subscription", "--all");
		OpenShifts.adminBinary().execute("delete", "csvs", "--all");
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
	}
}
