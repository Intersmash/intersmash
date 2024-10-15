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
package org.jboss.intersmash.testsuite.provision.operator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.http.HttpStatus;
import org.jboss.intersmash.provision.operator.HyperfoilOperatorProvisioner;
import org.jboss.intersmash.provision.operator.hyperfoil.client.runschema.RunStatisticsWrapper;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.HyperfoilApi;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.ApiClient;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.ApiException;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker.Configuration;
import org.jboss.intersmash.provision.operator.hyperfoil.client.v05.model.Run;
import org.junit.jupiter.api.Assertions;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HyperfoilOperatorProvisionerTests {
	public static final String APPLICATION_SERVICE_NAME = "hyperfoil";

	/**
	 * Verify deployment of HyperFoil via its operator (includes subscription logic)
	 */
	public static void verifyDeploy(HyperfoilOperatorProvisioner provisioner) {
		provisioner.deploy();
		Assertions.assertEquals(2, provisioner.getPods().size(),
				"Unexpected number of cluster operator pods for '" + HyperfoilOperatorProvisioner.OPERATOR_ID
						+ "' after deploy");
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.get(provisioner.getURL()).then().statusCode(HttpStatus.SC_OK);
	}

	/**
	 * Verify running a Benchmark on Hyperfoil
	 */
	public static void verifyBenchmark(final HyperfoilOperatorProvisioner provisioner)
			throws ApiException, InterruptedException, IOException {
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		defaultClient.setBasePath(provisioner.getURL().toExternalForm());
		defaultClient.setVerifyingSsl(false);

		HyperfoilApi apiInstance = new HyperfoilApi(defaultClient);
		String storedFilesBenchmark = "k8s-hello-world"; // String | Name of previously uploaded benchmark where extra files should be loaded from during multi-part upload. Usually this is the same benchmark unless it is being renamed.
		File body = new File(
				HyperfoilOperatorProvisionerTests.class.getClassLoader().getResource("k8s-hello-world.hf.yaml").getPath());
		try {
			apiInstance.addBenchmark(null, storedFilesBenchmark, body);
		} catch (ApiException err) {
			log.error("Hyperfoil Benchmark add failed:", err);
			Assertions.fail("Hyperfoil Benchmark add failed: " + err.getMessage());
		}
		Run run = apiInstance.startBenchmark(storedFilesBenchmark, "Hello World Benchmark", null, null,
				Arrays.asList("HOST_URL=http://hyperfoil:8090"));
		try {
			int wait = 18;
			try {
				while (Boolean.FALSE.equals(run.getCompleted()) && wait > 0) {
					Thread.sleep(10000);
					run = apiInstance.getRun(run.getId());
					wait--;
				}
			} catch (ApiException err) {
				log.error("Hyperfoil Benchmark wait failed:", err);
				throw err;
			}
			Assertions.assertTrue(run.getErrors().stream().filter(e -> !e.toString().contains("Jitter watchdog was not invoked")
					&& !e.toString().matches(".*CPU [0-9]+ was used for.*")).count() == 0);
			Assertions.assertTrue(wait > 0);

			// consume run statistics
			File allStats = apiInstance.getAllStats(run.getId());
			String JSON = Files.readString(Paths.get(allStats.getAbsolutePath()));
			log.debug("JSON: {}", JSON);
			RunStatisticsWrapper runStatisticsWrapper = new RunStatisticsWrapper(JSON);
			Assertions.assertFalse(runStatisticsWrapper.getPhaseStats().isEmpty());
		} catch (ApiException err) {
			log.error("Hyperfoil Benchmark run failed:", err);
			Assertions.fail("Hyperfoil Benchmark run failed: " + err.getMessage());
		} finally {
			// stop the run
			try {
				if (run != null) {
					apiInstance.killRun(run.getId());
				}
			} catch (Exception ex) {
				log.warn("HyperFoil benchmark run couldn't be stopped properly: ", ex);
			}
		}
	}

	/**
	 * Verify undeployment of HyperFoil Operator and resources, including subscription removal
	 */
	public static void verifyUndeploy(HyperfoilOperatorProvisioner provisioner) {
		provisioner.undeploy();
		Assertions.assertEquals(0, provisioner.getPods().size(),
				"Unexpected number of cluster operator pods for '" + HyperfoilOperatorProvisioner.OPERATOR_ID
						+ "' after deploy");
	}
}
