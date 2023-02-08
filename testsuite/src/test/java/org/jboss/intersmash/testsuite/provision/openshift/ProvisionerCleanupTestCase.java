package org.jboss.intersmash.testsuite.provision.openshift;

import java.util.stream.Stream;

import org.jboss.intersmash.tools.provision.openshift.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.junit5.annotations.CleanBeforeEach;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;

@CleanBeforeEach
public class ProvisionerCleanupTestCase {
	protected static final OpenShift openShift = OpenShifts.master();

	private static Stream<OpenShiftProvisioner> provisionerProvider() {
		return Stream.of(
				new MysqlImageOpenShiftProvisioner((OpenShiftProvisionerTestBase.getMysqlOpenShiftApplication())),
				new WildflyBootableJarImageOpenShiftProvisioner(
						(OpenShiftProvisionerTestBase.getWildflyBootableJarOpenShiftApplication())),
				new PostgreSQLImageOpenShiftProvisioner((OpenShiftProvisionerTestBase.getPostgreSQLOpenShiftApplication())),
				new WildflyImageOpenShiftProvisioner(
						OpenShiftProvisionerTestBase.getWildflyOpenShiftLocalBinaryTargetServerApplication()),
				new KeycloakTemplateOpenShiftProvisioner(OpenShiftProvisionerTestBase.getHttpsKeycloak()));
	}

	@ParameterizedTest(name = "{displayName}#class({0})")
	@MethodSource("provisionerProvider")
	public void undeploy(OpenShiftProvisioner provisioner) {
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
		Assertions.assertNotNull(openShift.configMaps().withName("no-delete").get());
		openShift.configMaps().withName("no-delete").delete();
		openShift.waiters().isProjectClean().waitFor();
	}
}
