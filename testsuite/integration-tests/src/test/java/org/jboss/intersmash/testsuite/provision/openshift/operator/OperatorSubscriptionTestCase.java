/*
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
package org.jboss.intersmash.testsuite.provision.openshift.operator;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.stream.Stream;

import org.jboss.intersmash.application.Application;
import org.jboss.intersmash.application.operator.ActiveMQOperatorApplication;
import org.jboss.intersmash.application.operator.HyperfoilOperatorApplication;
import org.jboss.intersmash.application.operator.InfinispanOperatorApplication;
import org.jboss.intersmash.application.operator.KafkaOperatorApplication;
import org.jboss.intersmash.application.operator.KeycloakOperatorApplication;
import org.jboss.intersmash.application.operator.RhSsoOperatorApplication;
import org.jboss.intersmash.application.operator.WildflyOperatorApplication;
import org.jboss.intersmash.junit5.IntersmashExtension;
import org.jboss.intersmash.provision.olm.OperatorGroup;
import org.jboss.intersmash.provision.openshift.*;
import org.jboss.intersmash.provision.operator.OperatorProvisioner;
import org.jboss.intersmash.testsuite.IntersmashTestsuiteProperties;
import org.jboss.intersmash.testsuite.junit5.categories.OpenShiftTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.extern.slf4j.Slf4j;

/**
 * Verify the {@link OperatorProvisioner} subscription process.
 */
@Slf4j
@CleanBeforeAll
@OpenShiftTest
public class OperatorSubscriptionTestCase {
	private static final Stream<OperatorProvisioner> COMMON_PROVISIONERS = Stream.of(
			new ActiveMQOpenShiftOperatorProvisioner(mock(ActiveMQOperatorApplication.class)),
			new InfinispanOpenShiftOperatorProvisioner(mock(InfinispanOperatorApplication.class)),
			new KafkaOpenShiftOperatorProvisioner(mock(KafkaOperatorApplication.class)),
			new WildflyOpenShiftOperatorProvisioner(mock(WildflyOperatorApplication.class)),
			new KeycloakOpenShiftOperatorProvisioner(mock(KeycloakOperatorApplication.class)));

	private static final Stream<OperatorProvisioner> COMMUNITY_ONLY_PROVISIONERS = Stream.of(
			new HyperfoilOpenShiftOperatorProvisioner(mock(HyperfoilOperatorApplication.class)));

	private static final Stream<OperatorProvisioner> PRODUCT_ONLY_PROVISIONERS = Stream.of(
			new RhSsoOpenShiftOperatorProvisioner(mock(RhSsoOperatorApplication.class)));

	private static Stream<OperatorProvisioner> provisionerProvider() {
		if (IntersmashTestsuiteProperties.isCommunityTestExecutionProfileEnabled()) {
			return Stream.concat(COMMON_PROVISIONERS, COMMUNITY_ONLY_PROVISIONERS);
		} else if (IntersmashTestsuiteProperties.isProductizedTestExecutionProfileEnabled()) {
			return Stream.concat(COMMON_PROVISIONERS, PRODUCT_ONLY_PROVISIONERS);
		}
		throw new IllegalStateException("Unknown test execution profile.");
	}

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		IntersmashExtension.operatorCleanup(false, true);
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f",
				new OperatorGroup(OpenShiftConfig.namespace()).save().getAbsolutePath());
	}

	@AfterAll
	public static void removeOperatorGroup() {
		// remove operator group - this should be done by InteropExtension
		IntersmashExtension.operatorCleanup(false, true);
	}

	@ParameterizedTest(name = "{displayName}#class({0})")
	@MethodSource("provisionerProvider")
	public void operatorSubscriptionTest(OperatorProvisioner operatorProvisioner) {
		final Application mockedApplication = operatorProvisioner.getApplication();
		Mockito.when(mockedApplication.getName())
				.thenReturn("app-" + mockedApplication.getClass().getSimpleName().substring(0, 10));
		operatorProvisioner.configure();
		try {
			operatorProvisioner.subscribe();
			try {
				log.debug("Pods:");
				OpenShifts.master().getPods().forEach(this::introducePod);
				Assertions.assertTrue(operatorProvisioner.getCustomResourceDefinitions().size() > 0,
						String.format("List of CRDs provided by operator [%s] should not be empty.",
								operatorProvisioner.getPackageManifestName()));
			} finally {
				operatorProvisioner.unsubscribe();
			}
		} finally {
			operatorProvisioner.dismiss();
		}
	}

	private void introducePod(Pod pod) {
		log.debug(pod.getMetadata().getName());
		log.debug(" - labels: " + pod.getMetadata().getLabels().toString());
		log.debug(" - image: " + pod.getSpec().getContainers().get(0).getImage());
	}
}
