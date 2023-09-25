package org.jboss.intersmash.testsuite.provision.openshift;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.jboss.intersmash.tools.application.openshift.ThreeScaleOperatorApplication;
import org.jboss.intersmash.tools.junit5.IntersmashExtension;
import org.jboss.intersmash.tools.provision.openshift.ThreeScaleOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.OperatorGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import lombok.extern.slf4j.Slf4j;
import net.threescale.apps.v1alpha1.APIManager;

@Slf4j
@CleanBeforeAll
public class ThreeScaleOperatorProvisionerTest {
	private static final ThreeScaleOperatorProvisioner THREE_SCALE_OPERATOR_PROVISIONER = initializeOperatorProvisioner();

	private static final Map<String, String> matchLabels = Collections.singletonMap("app", "3scale-api-management");

	private static ThreeScaleOperatorProvisioner initializeOperatorProvisioner() {
		return new ThreeScaleOperatorProvisioner(
				new ThreeScaleOperatorApplication() {
					private static final String DEFAULT_THREE_SCALE_APP_NAME = "example-three-scale";

					@Override
					public APIManager getAPIManager() {
						return null;
					}

					@Override
					public String getName() {
						return DEFAULT_THREE_SCALE_APP_NAME;
					}
				});
	}

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		IntersmashExtension.operatorCleanup();
		// this creates the catalogsource if necessary
		THREE_SCALE_OPERATOR_PROVISIONER.configure();
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f", OperatorGroup.SINGLE_NAMESPACE.save().getAbsolutePath());
	}

	@AfterAll
	public static void removeOperatorGroup() {
		// this deletes the catalogsource if necessary
		THREE_SCALE_OPERATOR_PROVISIONER.dismiss();
		THREE_SCALE_OPERATOR_PROVISIONER.unsubscribe();
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
	}

	@AfterEach
	public void customResourcesCleanup() {
		THREE_SCALE_OPERATOR_PROVISIONER.undeploy();
	}

	@Test
	public void deploy() {
		THREE_SCALE_OPERATOR_PROVISIONER.deploy();
	}
}
