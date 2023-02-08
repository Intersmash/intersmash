package org.jboss.intersmash.testsuite.provision.auto;

import org.jboss.intersmash.tools.annotations.Intersmash;
import org.jboss.intersmash.tools.annotations.Service;
import org.jboss.intersmash.tools.annotations.ServiceProvisioner;
import org.jboss.intersmash.tools.annotations.ServiceUrl;
import org.jboss.intersmash.tools.application.openshift.AutoProvisioningOpenShiftApplication;
import org.jboss.intersmash.tools.provision.openshift.OpenShiftProvisioner;
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
