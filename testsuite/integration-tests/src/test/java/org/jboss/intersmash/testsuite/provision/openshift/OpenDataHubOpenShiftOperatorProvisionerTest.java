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
package org.jboss.intersmash.testsuite.provision.openshift;

import java.io.IOException;

import org.jboss.intersmash.application.operator.OpenDataHubOperatorApplication;
import org.jboss.intersmash.junit5.IntersmashExtension;
import org.jboss.intersmash.provision.olm.OperatorGroup;
import org.jboss.intersmash.provision.openshift.OpenDataHubOpenShiftOperatorProvisioner;
import org.jboss.intersmash.testsuite.junit5.categories.NotForProductizedExecutionProfile;
import org.jboss.intersmash.testsuite.junit5.categories.OpenShiftTest;
import org.jboss.intersmash.testsuite.openshift.ProjectCreationCapable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.opendatahub.datasciencecluster.v1.DataScienceCluster;
import io.opendatahub.datasciencecluster.v1.DataScienceClusterBuilder;
import io.opendatahub.datasciencecluster.v1.datascienceclusterspec.components.kserve.Nim;
import io.opendatahub.datasciencecluster.v1.datascienceclusterspec.components.kserve.Serving;
import io.opendatahub.datasciencecluster.v1.datascienceclusterspec.components.kserve.serving.ingressgateway.Certificate;
import io.opendatahub.dscinitialization.v1.DSCInitialization;
import io.opendatahub.dscinitialization.v1.DSCInitializationBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Integration test that validates the functionality of the {@link OpenDataHubOpenShiftOperatorProvisioner} class.
 * The test subscribes the Open Data Hub operator, then defines CRs instances and compare them with the ones returned by
 * OpenShift upon creation.
 */
@Slf4j
@CleanBeforeAll
@OpenShiftTest
@NotForProductizedExecutionProfile
public class OpenDataHubOpenShiftOperatorProvisionerTest implements ProjectCreationCapable {
	private static final OpenDataHubOpenShiftOperatorProvisioner operatorProvisioner = initializeOperatorProvisioner();

	private static OpenDataHubOpenShiftOperatorProvisioner initializeOperatorProvisioner() {
		OpenDataHubOpenShiftOperatorProvisioner operatorProvisioner = new OpenDataHubOpenShiftOperatorProvisioner(
				new OpenDataHubOperatorApplication() {

					private static final String APP_NAME = "example-odh";

					@Override
					public DataScienceCluster getDataScienceCluster() {
						return new DataScienceClusterBuilder()
								.withNewMetadata()
								.withName(APP_NAME)
								.endMetadata()
								.withNewSpec()
								.withNewComponents()
								.withNewKserve()
								.withNewNim()
								.withManagementState(Nim.ManagementState.Managed)
								.endNim()
								.withNewServing()
								.withNewIngressGateway()
								.withNewCertificate()
								.withType(Certificate.Type.OpenshiftDefaultIngress)
								.endCertificate()
								.endIngressGateway()
								.withManagementState(Serving.ManagementState.Managed)
								.endServing()
								.endKserve()
								.withNewModelregistry()
								.withRegistriesNamespace("odh-model-registries")
								.endModelregistry()
								.endComponents()
								.endSpec()
								.build();
					}

					@Override
					public DSCInitialization getDSCInitialization() {
						return new DSCInitializationBuilder()
								.withNewMetadata()
								.withName(APP_NAME)
								.endMetadata()
								.withNewSpec()
								.withApplicationsNamespace(OpenShifts.master().getNamespace())
								.endSpec()
								.build();
					}

					@Override
					public String getName() {
						return APP_NAME;
					}
				});
		return operatorProvisioner;
	}

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		operatorProvisioner.configure();
		IntersmashExtension.operatorCleanup(false, true);
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f",
				new OperatorGroup(OpenShiftConfig.namespace()).save().getAbsolutePath());
		// clean any leftovers
		operatorProvisioner.unsubscribe();
		// Let's skip subscribe operation here since we use regular deploy/undeploy where subscribe is called anyway.
	}

	@AfterAll
	public static void removeOperatorGroup() {
		// clean any leftovers
		operatorProvisioner.unsubscribe();
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
		operatorProvisioner.dismiss();
	}

	@AfterEach
	public void customResourcesCleanup() {
		final String appName = operatorProvisioner.getApplication().getName();
		operatorProvisioner.dataScienceClusterClient().withName(appName).withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
		operatorProvisioner.dscInitializationClient().withName(appName).withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
	}

	/**
	 * This test case creates and validates minimal {@link DataScienceCluster} and {@link DSCInitialization} CRs
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 */
	@Test
	public void testMinimalDataScienceCluster() {
		operatorProvisioner.subscribe();
		try {
			verifyMinimalDataScienceCluster(operatorProvisioner.getApplication().getDataScienceCluster(),
					operatorProvisioner.getApplication().getDSCInitialization());
		} finally {
			operatorProvisioner.unsubscribe();
		}
	}

	private void verifyMinimalDataScienceCluster(final DataScienceCluster dataScienceCluster,
			final DSCInitialization dscInitialization) {
		// create and verify that objects exist
		operatorProvisioner.dataScienceClusterClient().resource(dataScienceCluster).create();
		new SimpleWaiter(() -> operatorProvisioner.dataScienceClusterClient().list().getItems().size() == 1)
				.level(Level.DEBUG)
				.waitFor();
		operatorProvisioner.dscInitializationClient().resource(dscInitialization).create();
		new SimpleWaiter(() -> operatorProvisioner.dscInitializationClient().list().getItems().size() == 1)
				.level(Level.DEBUG)
				.waitFor();
		final DataScienceCluster createdDataScienceCluster = operatorProvisioner.dataScienceCluster().get();
		Assertions.assertNotNull(createdDataScienceCluster);
		// the DataScienceCluster spec gets populated on creation, so we just check that it is not null
		Assertions.assertNotNull(createdDataScienceCluster.getSpec());
		Assertions.assertNotNull(operatorProvisioner.dataScienceCluster().get().getSpec());
		// the Monitoring spec gets populated on creation, so we verify 1st level resources individually
		final DSCInitialization createdDscInitialization = operatorProvisioner.dscInitializationClient()
				.withName(dscInitialization.getMetadata().getName()).get();
		Assertions.assertEquals(dscInitialization.getSpec().getDevFlags(), createdDscInitialization.getSpec().getDevFlags());
		Assertions.assertNotNull(createdDscInitialization.getSpec().getMonitoring());
		Assertions.assertEquals(dscInitialization.getSpec().getServiceMesh(),
				createdDscInitialization.getSpec().getServiceMesh());
		Assertions.assertEquals(dscInitialization.getSpec().getTrustedCABundle(),
				createdDscInitialization.getSpec().getTrustedCABundle());
	}
}
