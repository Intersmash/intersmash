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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.jboss.intersmash.testsuite.junit5.categories.NotForProductizedExecutionProfile;
import org.jboss.intersmash.tools.application.openshift.HyperfoilOperatorApplication;
import org.jboss.intersmash.tools.junit5.IntersmashExtension;
import org.jboss.intersmash.tools.provision.openshift.HyperfoilOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.runschema.RunStatisticsWrapper;
import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.HyperfoilApi;
import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.invoker.ApiClient;
import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.invoker.ApiException;
import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.invoker.Configuration;
import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.model.Run;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.OperatorGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.hyperfoil.v1alpha2.Hyperfoil;
import io.hyperfoil.v1alpha2.HyperfoilBuilder;

@CleanBeforeAll
@NotForProductizedExecutionProfile
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HyperfoilOperatorProvisionerTest {
	private static final Logger logger = LoggerFactory.getLogger(HyperfoilOperatorProvisionerTest.class);
	private static final String NAME = "hyperfoil";
	private static final HyperfoilOperatorProvisioner hyperfoilOperatorProvisioner = initializeOperatorProvisioner();

	private static HyperfoilOperatorProvisioner initializeOperatorProvisioner() {
		HyperfoilOperatorProvisioner operatorProvisioner = new HyperfoilOperatorProvisioner(
				new HyperfoilOperatorApplication() {
					@Override
					public Hyperfoil getHyperfoil() {
						return new HyperfoilBuilder(
								getName(),
								// see https://github.com/Hyperfoil/hyperfoil-operator/issues/18, "latest" (default) would fail.
								"0.24.2").build();
					}

					@Override
					public String getName() {
						return NAME;
					}
				});
		return operatorProvisioner;
	}

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		hyperfoilOperatorProvisioner.configure();
		IntersmashExtension.operatorCleanup();
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f", OperatorGroup.SINGLE_NAMESPACE.save().getAbsolutePath());
		// clean any leftovers
		hyperfoilOperatorProvisioner.unsubscribe();
	}

	@AfterAll
	public static void removeOperatorGroup() {
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
		// there might be leftovers in case of failures
		OpenShifts.admin().deletePods("role", "agent");
		hyperfoilOperatorProvisioner.dismiss();
	}

	/**
	 * Test subscription of Hyperfoil operator
	 */
	@Test
	@Order(1)
	public void subscribe() {
		hyperfoilOperatorProvisioner.subscribe();
	}

	/**
	 * Test deploy of Hyperfoil
	 */
	@Test
	@Order(2)
	public void deploy() {
		hyperfoilOperatorProvisioner.deploy();
		Assertions.assertEquals(1, hyperfoilOperatorProvisioner.getPods().size(),
				"Unexpected number of cluster operator pods for '" + HyperfoilOperatorProvisioner.getOperatorId()
						+ "' after deploy");
	}

	/**
	 * Test running a Benchmark on Hyperfoil
	 */
	@Test
	@Order(3)
	public void benchmark() throws ApiException, InterruptedException, IOException {
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		defaultClient.setBasePath(hyperfoilOperatorProvisioner.getURL().toString());
		defaultClient.setVerifyingSsl(false);

		HyperfoilApi apiInstance = new HyperfoilApi(defaultClient);
		String storedFilesBenchmark = "k8s-hello-world"; // String | Name of previously uploaded benchmark where extra files should be loaded from during multi-part upload. Usually this is the same benchmark unless it is being renamed.
		File body = new File(
				this.getClass().getClassLoader().getResource("k8s-hello-world.hf.yaml").getPath());
		try {
			apiInstance.addBenchmark(null, storedFilesBenchmark, body);
		} catch (ApiException err) {
			logger.error("Hyperfoil Benchmark add failed:", err);
			Assertions.fail("Hyperfoil Benchmark add failed: " + err.getMessage());
		}
		Run run = null;
		try {
			run = apiInstance.startBenchmark(storedFilesBenchmark, "Hello World Benchmark", null, null,
					Arrays.asList("HOST_URL=http://hyperfoil:8090"));
		} catch (ApiException err) {
			if (run != null) {
				apiInstance.killRun(run.getId());
			}
			logger.error("Hyperfoil Benchmark run failed:", err);
			Assertions.fail("Hyperfoil Benchmark run failed: " + err.getMessage());
		}

		int wait = 18;
		try {
			while (!run.getCompleted() && wait > 0) {
				Thread.sleep(10000);
				run = apiInstance.getRun(run.getId());
				wait--;
			}
		} catch (ApiException err) {
			try {
				apiInstance.killRun(run.getId());
			} catch (Exception ignore) {
			}
			logger.error("Hyperfoil Benchmark wait failed:", err);
			Assertions.fail("Hyperfoil Benchmark wait failed: " + err.getMessage());
		}
		Assertions.assertTrue(run.getErrors().stream().filter(e -> !e.toString().contains("Jitter watchdog was not invoked")
				&& !e.toString().matches(".*CPU [0-9]+ was used for.*")).count() == 0);
		Assertions.assertTrue(wait > 0);

		// consume run statistics
		File allStats = apiInstance.getAllStats(run.getId());
		String JSON = Files.readString(Paths.get(allStats.getAbsolutePath()));
		logger.debug("JSON: {}", JSON);
		RunStatisticsWrapper runStatisticsWrapper = new RunStatisticsWrapper(JSON);
		Assertions.assertTrue(runStatisticsWrapper.getPhaseStats().size() > 0);
	}

	/**
	 * Test undeploy of Hyperfoil
	 */
	@Test
	@Order(4)
	public void undeploy() {
		hyperfoilOperatorProvisioner.undeploy(false);
	}

	/**
	 * Test unsubscribe of Hyperfoil operator
	 */
	@Test
	@Order(5)
	public void unsubscribe() {
		hyperfoilOperatorProvisioner.unsubscribe();
		Assertions.assertEquals(0, hyperfoilOperatorProvisioner.getPods().size(),
				"Unexpected number of cluster operator pods for '" + HyperfoilOperatorProvisioner.getOperatorId()
						+ "' after deploy");
	}
}
