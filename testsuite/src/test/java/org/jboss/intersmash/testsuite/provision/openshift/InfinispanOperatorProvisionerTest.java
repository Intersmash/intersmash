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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jboss.intersmash.tools.application.openshift.InfinispanOperatorApplication;
import org.jboss.intersmash.tools.junit5.IntersmashExtension;
import org.jboss.intersmash.tools.provision.openshift.InfinispanOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.Cache;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.CacheBuilder;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.Infinispan;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.InfinispanBuilder;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.AutoscaleBuilder;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.InfinispanServiceSpecBuilder;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.OperatorGroup;
import org.junit.jupiter.api.*;
import org.slf4j.event.Level;

import cz.xtf.builder.builders.SecretBuilder;
import cz.xtf.builder.builders.secret.SecretType;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Secret;
import lombok.extern.slf4j.Slf4j;

/**
 * Test cases that cover usage of Infinispan model (and clients) generated from CRDs.
 * Not all examples actually run, but these tests are more about the framework functionality verification than
 * about the Infinispan operator testing.
 */
@Slf4j
@CleanBeforeAll
@Disabled("WIP - Disabled until global-test.properties is configured with the required property")
public class InfinispanOperatorProvisionerTest {
	static final String TEST_SECRET_USERNAME = "developer";
	static final String TEST_SECRET_PASSWORD = "developer";
	static final String TEST_SECRET_NAME = "test-secret";
	static final Secret TEST_SECRET = new SecretBuilder(TEST_SECRET_NAME)
			.setType(SecretType.OPAQUE).addData(TEST_SECRET_USERNAME, TEST_SECRET_PASSWORD.getBytes()).build();
	// Be aware that since we're using the static mock application, not all provisioner methods will work as expected!
	private static final InfinispanOperatorProvisioner INFINISPAN_OPERATOR_PROVISIONER = initializeOperatorProvisioner();

	private static InfinispanOperatorProvisioner initializeOperatorProvisioner() {
		InfinispanOperatorProvisioner operatorProvisioner = new InfinispanOperatorProvisioner(
				new InfinispanOperatorApplication() {

					private static final String DEFAULT_INFINISPAN_APP_NAME = "example-infinispan";

					@Override
					public Infinispan getInfinispan() {
						return new InfinispanBuilder(DEFAULT_INFINISPAN_APP_NAME, matchLabels)
								.replicas(1)
								.build();
					}

					@Override
					public List<Cache> getCaches() {
						return Collections.emptyList();
					}

					@Override
					public String getName() {
						return DEFAULT_INFINISPAN_APP_NAME;
					}
				});
		operatorProvisioner.configure();
		return operatorProvisioner;
	}

	private String name;
	private static final Map<String, String> matchLabels = new HashMap<>();

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		matchLabels.put("app", "datagrid");
		IntersmashExtension.operatorCleanup();
		// let's configure the provisioner
		INFINISPAN_OPERATOR_PROVISIONER.configure();
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f", OperatorGroup.SINGLE_NAMESPACE.save().getAbsolutePath());
		// clean any leftovers
		INFINISPAN_OPERATOR_PROVISIONER.unsubscribe();
	}

	@AfterAll
	public static void removeOperatorGroup() {
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
	}

	@AfterEach
	public void customResourcesCleanup() {
		INFINISPAN_OPERATOR_PROVISIONER.cachesClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> INFINISPAN_OPERATOR_PROVISIONER
						.cachesClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
	}

	/**
	 * This test case creates and validates a minimal {@link Infinispan} CR
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 */
	@Test
	public void testMinimalInfinispan() {
		INFINISPAN_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-infinispan";
			Infinispan infinispan = new InfinispanBuilder(name, matchLabels)
					.replicas(1)
					.build();

			verifyMinimalInfinispan(infinispan, true);
		} finally {
			INFINISPAN_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates and validates a two replicas {@link Infinispan} CR
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 */
	@Test
	public void testMinimalTwoReplicasInfinispan() {
		INFINISPAN_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-infinispan";
			Infinispan infinispan = new InfinispanBuilder(name, matchLabels)
					.replicas(2)
					.build();

			verifyMinimalTwoReplicasInfinispan(infinispan, true);
		} finally {
			INFINISPAN_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates and validates a basic {@link Infinispan} CR, configured for using Cache service type
	 * explicitly
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 */
	@Test
	public void testMinimalTwoReplicasCacheServiceInfinispan() {
		INFINISPAN_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-infinispan";
			Infinispan infinispan = new InfinispanBuilder(name, matchLabels)
					.replicas(2)
					.service(new InfinispanServiceSpecBuilder().type(InfinispanServiceSpecBuilder.ServiceType.Cache)
							.build())
					.build();

			verifyMinimalTwoReplicasInfinispan(infinispan, true);
		} finally {
			INFINISPAN_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates and validates a basic {@link Infinispan} CR, configured for using Cache service type
	 * and autoscale functionality
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 */
	@Test
	public void testMinimalCacheServiceInfinispanWithAutoscale() {
		INFINISPAN_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-infinispan";
			Infinispan infinispan = new InfinispanBuilder(name, matchLabels)
					.replicas(2)
					.service(new InfinispanServiceSpecBuilder()
							.type(InfinispanServiceSpecBuilder.ServiceType.Cache)
							.build())
					.autoscale(new AutoscaleBuilder()
							.maxReplicas(5)
							.maxMemUsagePercent(70)
							.minReplicas(2)
							.minMemUsagePercent(30).build())
					.build();

			verifyMinimalCacheServiceInfinispanWithAutoscale(infinispan, true);
		} finally {
			INFINISPAN_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates and validates a basic {@link Infinispan} CR, configured for using DataGrid service type
	 * and a cache secret
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 */
	@Test
	public void testCacheWithBasicSecretFromTemplate() {
		INFINISPAN_OPERATOR_PROVISIONER.subscribe();
		try {
			final String clusterName = "example-infinispan";
			Infinispan infinispan = new InfinispanBuilder(clusterName, matchLabels)
					.replicas(1)
					.service(new InfinispanServiceSpecBuilder().type(InfinispanServiceSpecBuilder.ServiceType.DataGrid)
							.build())
					.build();
			createAndVerifyInfinispan(infinispan);
			try {
				name = "test-cache-instance";
				OpenShifts.master().createSecret(TEST_SECRET);
				Cache cache = new CacheBuilder("test-cache-definition", matchLabels)
						.clusterName(clusterName)
						.name(name)
						.templateName("org.infinispan.DIST_SYNC")
						.build();
				verifyCacheWithBasicSecretFromTemplate(cache, true);
			} finally {
				deleteAndVerifyMinimalInfinispan(clusterName, false);
			}
		} finally {
			INFINISPAN_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * Test actual deploy/scale/undeploy workflow by the operator
	 *
	 * Does subscribe/unsubscribe on its own, so no need to call explicitly here
	 *
	 * This test adds no further checks after {@link InfinispanOperatorProvisioner#undeploy()} based on
	 * {@link InfinispanOperatorProvisioner#getPods()}, since it looks for a stateful set which would be null at this point.
	 * There is room for evaluating whether to revisit {@link InfinispanOperatorProvisioner} with respect to such logic
	 */
	@Test
	public void basicProvisioningTest() {
		INFINISPAN_OPERATOR_PROVISIONER.deploy();
		try {
			Assertions.assertEquals(1, INFINISPAN_OPERATOR_PROVISIONER.getPods().size(),
					"Unexpected number of cluster operator pods for '" + INFINISPAN_OPERATOR_PROVISIONER.getOperatorId()
							+ "' after deploy");

			int scaledNum = INFINISPAN_OPERATOR_PROVISIONER.getApplication().getInfinispan().getSpec().getReplicas() + 1;
			INFINISPAN_OPERATOR_PROVISIONER.scale(scaledNum, true);
			Assertions.assertEquals(scaledNum, INFINISPAN_OPERATOR_PROVISIONER.getPods().size(),
					"Unexpected number of cluster operator pods for '" + INFINISPAN_OPERATOR_PROVISIONER.getOperatorId()
							+ "' after scaling");
		} finally {
			INFINISPAN_OPERATOR_PROVISIONER.undeploy();
		}
		Assertions.assertEquals(0,
				INFINISPAN_OPERATOR_PROVISIONER.getPods()
						.stream().filter(pod -> Objects.isNull(pod.getMetadata().getDeletionTimestamp()))
						.count(),
				"Unexpected number of cluster operator pods for '" + INFINISPAN_OPERATOR_PROVISIONER.getOperatorId()
						+ "' after undeploy");
	}

	/**
	 * Create and verify a minimal Infinispan resource
	 * @param infinispan the Infinispan resource to be created
	 */
	private void createAndVerifyInfinispan(Infinispan infinispan) {
		// create and verify that object exists
		INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().createOrReplace(infinispan);
		new SimpleWaiter(() -> INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().list().getItems().size() == 1)
				.level(Level.DEBUG)
				.waitFor();
	}

	private void createAndVerifyCache(Cache cache) {
		// create and verify that object exists
		INFINISPAN_OPERATOR_PROVISIONER.cachesClient().createOrReplace(cache);
		new SimpleWaiter(() -> INFINISPAN_OPERATOR_PROVISIONER.cachesClient()
				.list().getItems().stream()
				.filter(c -> c.getSpec().getName().equals(cache.getSpec().getName())).count() == 1)
				.level(Level.DEBUG)
				.waitFor();
	}

	/**
	 * Deletes a named Infinispan resource and checks that no more Infinispan resources nor pods are present
	 * @param waitForPods
	 */
	private void deleteAndVerifyMinimalInfinispan(String name, boolean waitForPods) {
		// delete and verify that object was removed
		INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
		new SimpleWaiter(() -> INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().list().getItems().size() == 0)
				.level(Level.DEBUG)
				.waitFor();
		if (waitForPods) {
			OpenShiftWaiters.get(OpenShifts.master(), () -> false)
					.areExactlyNPodsReady(0, "clusterName", name).level(Level.DEBUG).waitFor();
		}
	}

	private void deleteAndVerifyMinimalInfinispan(boolean waitForPods) {
		deleteAndVerifyMinimalInfinispan(this.name, waitForPods);
	}

	private void deleteAndVerifyCache(boolean waitForPods) {
		// delete and verify that object was removed
		INFINISPAN_OPERATOR_PROVISIONER.cachesClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
		new SimpleWaiter(() -> INFINISPAN_OPERATOR_PROVISIONER.cachesClient().list().getItems().size() == 0).level(Level.DEBUG)
				.waitFor();
	}

	/**
	 * Let's just verify that the minimal Infinispan setup succeeded
	 */
	private void verifyMinimalInfinispan(Infinispan infinispan, boolean waitForPods) {
		// create and verify that object exists
		createAndVerifyInfinispan(infinispan);
		// one pod expected with the "clusterName" label: example-infinispan, i.e. the same name as the CR metadata
		if (waitForPods) {
			OpenShiftWaiters.get(OpenShifts.master(), () -> false)
					.areExactlyNPodsReady(1, "clusterName", name).level(Level.DEBUG).waitFor();
			log.debug(INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getStatus().toString());
		}
		assertMinimalInfinispanCreation(infinispan);
		//	NOTE: created Infinispan .spec and CRD .spec aren't equal because aggregated props are null in CRD .spec

		// delete and verify that object was removed
		deleteAndVerifyMinimalInfinispan(waitForPods);
	}

	/**
	 * Verify that two replicas are up and running and that can be scaled to 3
	 */
	private void verifyMinimalTwoReplicasInfinispan(Infinispan infinispan, boolean waitForPods) {
		// create and verify that object exists
		createAndVerifyInfinispan(infinispan);
		// two pods expected with the "clusterName" label: example-infinispan, i.e. the same name as the CR metadata
		if (waitForPods) {
			OpenShiftWaiters.get(OpenShifts.master(), () -> false)
					.areExactlyNPodsReady(2, "clusterName", name).level(Level.DEBUG).waitFor();
			log.debug(INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getStatus().toString());
		}
		assertMinimalInfinispanCreation(infinispan);

		infinispan.getSpec().setReplicas(3);
		createAndVerifyInfinispan(infinispan);
		// one pods expected with the "clusterName" label: example-infinispan, i.e. the same name as the CR metadata
		if (waitForPods) {
			OpenShiftWaiters.get(OpenShifts.master(), () -> false)
					.areExactlyNPodsReady(3, "clusterName", name).level(Level.DEBUG).waitFor();
			log.debug(INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getStatus().toString());
		}
		deleteAndVerifyMinimalInfinispan(waitForPods);

	}

	/**
	 * Verify that two replicas are up and running and that have the relevant properties set to the same values
	 */
	private void verifyMinimalCacheServiceInfinispanWithAutoscale(Infinispan infinispan, boolean waitForPods) {
		createAndVerifyInfinispan(infinispan);
		// one pods expected with the "clusterName" label: example-infinispan, i.e. the same name as the CR metadata
		if (waitForPods) {
			OpenShiftWaiters.get(OpenShifts.master(), () -> false)
					.areExactlyNPodsReady(2, "clusterName", name).level(Level.DEBUG).waitFor();
			log.debug(INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getStatus().toString());
		}
		assertMinimalInfinispanWithAutoscaleCreation(infinispan);

		// delete and verify that object was removed
		deleteAndVerifyMinimalInfinispan(waitForPods);
	}

	/**
	 * Verify that two replicas are up and running and that have the relevant properties set to the same values
	 */
	private void verifyCacheWithBasicSecretFromTemplate(Cache cache, boolean waitForPods) {
		createAndVerifyCache(cache);

		assertCacheWithBasicSecretFromTemplateCreation(cache);

		// delete and verify that object was removed
		deleteAndVerifyCache(waitForPods);
	}

	/**
	 * Minimal setup + autoscale verification involves: <br>
	 * - the created Infinispan .metadata.name == the CRD .metadata.name <br>
	 * - the created Infinispan .spec.replicas == the CRD .spec.replicas <br>
	 * - the created Infinispan .spec.autoscale properties == the CRD .spec.autoscale properties
	 *
	 * 	Created Infinispan .spec.autoscale and CRD .spec aren't equal because aggregated props are null in CRD .spec.autoscale
	 *
	 * @param infinispan the Infinispan resource which configuration has to be verified
	 */
	private void assertMinimalInfinispanWithAutoscaleCreation(Infinispan infinispan) {
		Assertions.assertEquals(infinispan.getMetadata().getName(),
				INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getMetadata().getName());
		Assertions.assertEquals(infinispan.getSpec().getReplicas(),
				INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getSpec().getReplicas());
		Assertions.assertEquals(infinispan.getSpec().getService().getType(),
				INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getSpec().getService().getType());
		Assertions.assertEquals(infinispan.getSpec().getAutoscale().getMaxReplicas(),
				INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getSpec().getAutoscale()
						.getMaxReplicas());
		Assertions.assertEquals(infinispan.getSpec().getAutoscale().getMaxMemUsagePercent(),
				INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getSpec().getAutoscale()
						.getMaxMemUsagePercent());
		Assertions.assertEquals(infinispan.getSpec().getAutoscale().getMinReplicas(),
				INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getSpec().getAutoscale()
						.getMinReplicas());
		Assertions.assertEquals(infinispan.getSpec().getAutoscale().getMinMemUsagePercent(),
				INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getSpec().getAutoscale()
						.getMinMemUsagePercent());
		//	NOTE: created Infinispan .spec.autoscale and CRD .spec.autoscale aren't equal because aggregated props are null in CRD .spec
	}

	/**
	 * Minimal setup verification involves: <br>
	 * - the created Infinispan .metadata.name == the CRD .metadata.name <br>
	 * - the created Infinispan .spec.replicas == the CRD .spec.replicas
	 *
	 * 	Created Infinispan .spec and CRD .spec aren't equal because aggregated props are null in CRD .spec
	 *
	 * @param infinispan the Infinispan resource which configuration has to be verified
	 */
	private void assertMinimalInfinispanCreation(Infinispan infinispan) {
		Assertions.assertEquals(infinispan.getMetadata().getName(),
				INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getMetadata().getName());
		Assertions.assertEquals(infinispan.getSpec().getReplicas(),
				INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getSpec().getReplicas());
		//	NOTE: created Infinispan .spec and CRD .spec aren't equal because aggregated props are null in CRD .spec
	}

	private void assertCacheWithBasicSecretFromTemplateCreation(Cache cache) {
		Assertions.assertEquals(cache.getMetadata().getName(),
				INFINISPAN_OPERATOR_PROVISIONER.cachesClient().withName(name).get().getMetadata().getName());
		Assertions.assertEquals(cache.getSpec().getClusterName(),
				INFINISPAN_OPERATOR_PROVISIONER.cachesClient().withName(name).get().getSpec().getClusterName());
		Assertions.assertEquals(cache.getSpec().getTemplateName(),
				INFINISPAN_OPERATOR_PROVISIONER.cachesClient().withName(name).get().getSpec().getTemplateName());
	}
}
