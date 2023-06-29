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
package org.jboss.intersmash.testsuite.provision.k8s;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.jboss.intersmash.testsuite.k8s.KubernetesTest;
import org.jboss.intersmash.testsuite.k8s.NamespaceCreationCapable;
import org.jboss.intersmash.tools.application.operator.HyperfoilOperatorApplication;
import org.jboss.intersmash.tools.junit5.IntersmashExtension;
import org.jboss.intersmash.tools.k8s.KubernetesConfig;
import org.jboss.intersmash.tools.k8s.client.Kuberneteses;
import org.jboss.intersmash.tools.provision.k8s.HyperfoilKubernetesOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.runschema.RunStatisticsWrapper;
import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.HyperfoilApi;
import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.invoker.ApiClient;
import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.invoker.ApiException;
import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.invoker.Configuration;
import org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.v05.model.Run;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.OperatorGroup;
import org.jboss.intersmash.tools.provision.operator.HyperfoilOperatorProvisioner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyperfoil.v1alpha2.Hyperfoil;
import io.hyperfoil.v1alpha2.HyperfoilBuilder;
import io.hyperfoil.v1alpha2.HyperfoilSpec;
import io.hyperfoil.v1alpha2.hyperfoilspec.Route;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@KubernetesTest
public class HyperfoilKubernetesOperatorProvisionerTest implements NamespaceCreationCapable {
	private static final Logger logger = LoggerFactory.getLogger(HyperfoilKubernetesOperatorProvisionerTest.class);
	private static final String NAME = "hyperfoil";
	private static final HyperfoilKubernetesOperatorProvisioner hyperfoilOperatorProvisioner = initializeOperatorProvisioner();

	private static HyperfoilKubernetesOperatorProvisioner initializeOperatorProvisioner() {
		HyperfoilKubernetesOperatorProvisioner operatorProvisioner = new HyperfoilKubernetesOperatorProvisioner(
				new HyperfoilOperatorApplication() {
					@Override
					public Hyperfoil getHyperfoil() {
						Hyperfoil hyperfoil = new HyperfoilBuilder(getName())
								.build();
						HyperfoilSpec spec = new HyperfoilSpec();
						Route route = new Route();
						route.setHost(getName());
						route.setType("http");
						spec.setRoute(route);
						hyperfoil.setSpec(spec);
						return hyperfoil;

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
		IntersmashExtension.operatorCleanup(true, false);
		// create operator group - this should be done by InteropExtension
		Kuberneteses.adminBinary().execute("apply", "-f",
				new OperatorGroup(KubernetesConfig.namespace()).save().getAbsolutePath());
		// clean any leftovers
		hyperfoilOperatorProvisioner.unsubscribe();
	}

	@AfterAll
	public static void removeOperatorGroup() {
		Kuberneteses.adminBinary().execute("delete", "operatorgroup", "--all");
		// there might be leftovers in case of failures
		Kuberneteses.admin().pods().withLabel("role", "agent").delete();
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
				"Unexpected number of cluster operator pods for '" + HyperfoilOperatorProvisioner.operatorId()
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
				"Unexpected number of cluster operator pods for '" + HyperfoilOperatorProvisioner.operatorId()
						+ "' after deploy");
	}
}
