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
package org.jboss.intersmash.testsuite.provision.auto;

import org.jboss.intersmash.annotations.Intersmash;
import org.jboss.intersmash.annotations.Service;
import org.jboss.intersmash.annotations.ServiceProvisioner;
import org.jboss.intersmash.annotations.ServiceUrl;
import org.jboss.intersmash.application.openshift.AutoProvisioningOpenShiftApplication;
import org.jboss.intersmash.provision.openshift.OpenShiftProvisioner;
import org.jboss.intersmash.testsuite.junit5.categories.OpenShiftTest;
import org.junit.jupiter.api.Test;

import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;

/**
 * Test cases that cover usage of a {@link AutoProvisioningOpenShiftApplication}
 *
 * It uses some declarative resources and logic owned by {@link AutoProvisioningHelloOpenShiftApplication}
 */
@Slf4j
@Intersmash({
		@Service(AutoProvisioningHelloOpenShiftApplication.class)
})
@OpenShiftTest
public class AutoProvisioningTests {

	@ServiceUrl(AutoProvisioningHelloOpenShiftApplication.class)
	private String appOpenShiftUrl;

	@ServiceProvisioner(AutoProvisioningHelloOpenShiftApplication.class)
	private OpenShiftProvisioner appOpenShiftProvisioner;

	@Test
	public void testAppIsLive() {
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.get(appOpenShiftUrl)
				.then()
				.statusCode(200);
	}

	@Test
	public void testAppCanScale() {
		// don't ask scale to wait, let's wait for the exact number of pods here, since this is the test criteria
		appOpenShiftProvisioner.scale(2, false);
		OpenShiftWaiters.get(OpenShifts.master(), () -> false).areExactlyNPodsReady(
				2, "app", appOpenShiftProvisioner.getApplication().getName()).waitFor();
	}
}
