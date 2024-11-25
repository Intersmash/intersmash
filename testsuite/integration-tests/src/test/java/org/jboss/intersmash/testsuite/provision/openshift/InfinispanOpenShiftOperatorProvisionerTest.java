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

import org.infinispan.v1.Infinispan;
import org.infinispan.v1.InfinispanBuilder;
import org.infinispan.v1.infinispanspec.Service;
import org.infinispan.v1.infinispanspec.ServiceBuilder;
import org.infinispan.v2alpha1.Cache;
import org.infinispan.v2alpha1.CacheBuilder;
import org.jboss.intersmash.application.operator.InfinispanOperatorApplication;
import org.jboss.intersmash.junit5.IntersmashExtension;
import org.jboss.intersmash.provision.olm.OperatorGroup;
import org.jboss.intersmash.provision.openshift.InfinispanOpenShiftOperatorProvisioner;
import org.jboss.intersmash.provision.operator.InfinispanOperatorProvisioner;
import org.jboss.intersmash.testsuite.junit5.categories.OpenShiftTest;
import org.jboss.intersmash.testsuite.openshift.ProjectCreationCapable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import cz.xtf.builder.builders.SecretBuilder;
import cz.xtf.builder.builders.secret.SecretType;
import cz.xtf.core.config.OpenShiftConfig;
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
@OpenShiftTest
public class InfinispanOpenShiftOperatorProvisionerTest implements ProjectCreationCapable {
	static final String TEST_SECRET_USERNAME = "developer";
	static final String TEST_SECRET_PASSWORD = "developer";
	static final String TEST_SECRET_NAME = "test-secret";
	static final Secret TEST_SECRET = new SecretBuilder(TEST_SECRET_NAME)
			.setType(SecretType.OPAQUE).addData(TEST_SECRET_USERNAME, TEST_SECRET_PASSWORD.getBytes()).build();
	// Be aware that since we're using the static mock application, not all provisioner methods will work as expected!
	private static final InfinispanOpenShiftOperatorProvisioner INFINISPAN_OPERATOR_PROVISIONER = initializeOperatorProvisioner();

	private static InfinispanOpenShiftOperatorProvisioner initializeOperatorProvisioner() {
		InfinispanOpenShiftOperatorProvisioner operatorProvisioner = new InfinispanOpenShiftOperatorProvisioner(
				new InfinispanOperatorApplication() {

					private static final String DEFAULT_INFINISPAN_APP_NAME = "example-infinispan";

					@Override
					public Infinispan getInfinispan() {
						return new InfinispanBuilder()
								.withNewMetadata()
								.withName(DEFAULT_INFINISPAN_APP_NAME)
								.withLabels(matchLabels)
								.endMetadata()
								.withNewSpec()
								.withReplicas(1)
								.endSpec()
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
		return operatorProvisioner;
	}

	private String name;
	private static final Map<String, String> matchLabels = new HashMap<>();

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		// let's configure the provisioner
		INFINISPAN_OPERATOR_PROVISIONER.configure();
		matchLabels.put("app", "datagrid");
		IntersmashExtension.operatorCleanup(false, true);
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f",
				new OperatorGroup(OpenShiftConfig.namespace()).save().getAbsolutePath());
		// clean any leftovers
		INFINISPAN_OPERATOR_PROVISIONER.unsubscribe();
	}

	@AfterAll
	public static void removeOperatorGroup() {
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
		INFINISPAN_OPERATOR_PROVISIONER.dismiss();
	}

	@AfterEach
	public void customResourcesCleanup() {
		// when cleaning up, let's remove
		INFINISPAN_OPERATOR_PROVISIONER.cachesClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> INFINISPAN_OPERATOR_PROVISIONER
						.cachesClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		// ... and the Infinispan CRs
		INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> INFINISPAN_OPERATOR_PROVISIONER
						.infinispansClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
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
			Infinispan infinispan = new InfinispanBuilder()
					.withNewMetadata()
					.withName(name)
					.withLabels(matchLabels)
					.endMetadata()
					.withNewSpec()
					.withReplicas(1)
					.endSpec()
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
			Infinispan infinispan = new InfinispanBuilder()
					.withNewMetadata()
					.withName(name)
					.withLabels(matchLabels)
					.endMetadata()
					.withNewSpec()
					.withReplicas(2)
					.endSpec()
					.build();

			verifyMinimalTwoReplicasInfinispan(infinispan, true);
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
			Infinispan infinispan = new InfinispanBuilder()
					.withNewMetadata()
					.withName(clusterName)
					.withLabels(matchLabels)
					.endMetadata()
					.withNewSpec()
					.withReplicas(1)
					.withService(new ServiceBuilder().withType(Service.Type.DataGrid).build())
					.endSpec()
					.build();
			createAndVerifyInfinispan(infinispan);
			try {
				name = "test-cache-definition";
				OpenShifts.master().createSecret(TEST_SECRET);
				Cache cache = new CacheBuilder()
						.withNewMetadata().withName(name).withLabels(matchLabels).endMetadata()
						.withNewSpec()
						.withClusterName(clusterName)
						.withName("testCache")
						.withTemplate(
								"<distributed-cache mode=\"SYNC\" statistics=\"true\"><encoding media-type=\"application/x-protostream\"/><persistence><file-store/></persistence></distributed-cache>")
						.endSpec()
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
					"Unexpected number of cluster operator pods for '" + InfinispanOperatorProvisioner.OPERATOR_ID
							+ "' after deploy");

			int scaledNum = INFINISPAN_OPERATOR_PROVISIONER.getApplication().getInfinispan().getSpec().getReplicas() + 1;
			INFINISPAN_OPERATOR_PROVISIONER.scale(scaledNum, true);
			Assertions.assertEquals(scaledNum, INFINISPAN_OPERATOR_PROVISIONER.getPods().size(),
					"Unexpected number of cluster operator pods for '" + InfinispanOperatorProvisioner.OPERATOR_ID
							+ "' after scaling");
		} finally {
			INFINISPAN_OPERATOR_PROVISIONER.undeploy();
		}
		Assertions.assertEquals(0,
				INFINISPAN_OPERATOR_PROVISIONER.getPods()
						.stream().filter(pod -> Objects.isNull(pod.getMetadata().getDeletionTimestamp()))
						.count(),
				"Unexpected number of cluster operator pods for '" + InfinispanOperatorProvisioner.OPERATOR_ID
						+ "' after undeploy");
	}

	/**
	 * Create and verify a minimal Infinispan resource
	 * @param infinispan the Infinispan resource to be created
	 */
	private void createAndVerifyInfinispan(Infinispan infinispan) {
		// create and verify that exactly 1 object (Infinispan CR) exists
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
			// checking for size of Infinispan CR pod set is 0
			new SimpleWaiter(
					() -> INFINISPAN_OPERATOR_PROVISIONER.getInfinispanPods().isEmpty())
					.level(Level.DEBUG).waitFor();
		}
	}

	private void deleteAndVerifyMinimalInfinispan(boolean waitForPods) {
		deleteAndVerifyMinimalInfinispan(this.name, waitForPods);
	}

	private void deleteAndVerifyCache(boolean waitForPods) {
		// delete and verify that object was removed
		INFINISPAN_OPERATOR_PROVISIONER.cachesClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
		new SimpleWaiter(() -> INFINISPAN_OPERATOR_PROVISIONER.cachesClient().list().getItems().size() == 0)
				.level(Level.DEBUG)
				.waitFor();
	}

	/**
	 * Let's just verify that the minimal Infinispan setup succeeded
	 */
	private void verifyMinimalInfinispan(final Infinispan infinispan, final boolean waitForPods) {
		// create and verify that object exists
		createAndVerifyInfinispan(infinispan);
		if (waitForPods) {
			// a correct number of Infinispan CRs has been created
			new SimpleWaiter(
					() -> INFINISPAN_OPERATOR_PROVISIONER.getInfinispanPods().size() == infinispan.getSpec().getReplicas())
					.level(Level.DEBUG).waitFor();
			log.debug(INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getStatus().toString());
		}
		assertMinimalInfinispanCreation(infinispan);
		//	NOTE: created Infinispan .spec and CRD .spec aren't equal because aggregated props are null in CRD .spec

		// delete and verify that object was removed
		deleteAndVerifyMinimalInfinispan(waitForPods);
	}

	/**
	 * Verify that two replicas are up and running
	 */
	private void verifyMinimalTwoReplicasInfinispan(Infinispan infinispan, boolean waitForPods) {
		// create and verify that object exists
		createAndVerifyInfinispan(infinispan);
		if (waitForPods) {
			// a correct number of Infinispan CRs has been created
			new SimpleWaiter(
					() -> INFINISPAN_OPERATOR_PROVISIONER.getInfinispanPods().size() == infinispan.getSpec().getReplicas())
					.level(Level.DEBUG).waitFor();
			log.debug(INFINISPAN_OPERATOR_PROVISIONER.infinispansClient().withName(name).get().getStatus().toString());
		}
		assertMinimalInfinispanCreation(infinispan);
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
