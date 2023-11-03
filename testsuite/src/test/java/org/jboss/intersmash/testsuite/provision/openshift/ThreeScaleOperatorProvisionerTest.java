package org.jboss.intersmash.testsuite.provision.openshift;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
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
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import lombok.extern.slf4j.Slf4j;
import net.threescale.apps.v1alpha1.APIManager;
import net.threescale.apps.v1alpha1.APIManagerBuilder;
import net.threescale.apps.v1alpha1.APIManagerSpec;
import net.threescale.apps.v1alpha1.APIManagerSpecBuilder;
import net.threescale.capabilities.v1beta1.Application;
import net.threescale.capabilities.v1beta1.ApplicationBuilder;
import net.threescale.capabilities.v1beta1.Backend;
import net.threescale.capabilities.v1beta1.BackendBuilder;
import net.threescale.capabilities.v1beta1.DeveloperAccount;
import net.threescale.capabilities.v1beta1.DeveloperAccountBuilder;
import net.threescale.capabilities.v1beta1.DeveloperUser;
import net.threescale.capabilities.v1beta1.DeveloperUserBuilder;
import net.threescale.capabilities.v1beta1.Product;
import net.threescale.capabilities.v1beta1.ProductBuilder;
import net.threescale.capabilities.v1beta1.ProxyConfigPromote;
import net.threescale.capabilities.v1beta1.ProxyConfigPromoteBuilder;

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
						APIManagerSpec apiManagerSpec = new APIManagerSpecBuilder()
								.withNewSystem()
								.withNewAppSpec()
								.withReplicas(1L)
								.endAppSpec()
								.withNewSidekiqSpec()
								.withReplicas(1L)
								.endSidekiqSpec()
								.endSystem()
								.withNewZync()
								.withNewZyncAppSpec()
								.withReplicas(1L)
								.endZyncAppSpec()
								.withNewQueSpec()
								.withReplicas(1L)
								.endQueSpec()
								.endZync()
								.withNewBackend()
								.withNewCronSpec()
								.withReplicas(1L)
								.endCronSpec()
								.withNewListenerSpec()
								.withReplicas(1L)
								.endListenerSpec()
								.withNewWorkerSpec()
								.withReplicas(1L)
								.endWorkerSpec()
								.endBackend()
								.withNewApicast()
								.withNewProductionSpec()
								.withReplicas(1L)
								.endProductionSpec()
								.withNewStagingSpec()
								.withReplicas(1L)
								.endStagingSpec()
								.endApicast()
								.build();
						ObjectMeta metadata = new ObjectMetaBuilder().build();
						metadata.setName(getName());
						APIManager apiManager = new APIManagerBuilder().build();
						apiManager.setMetadata(metadata);
						apiManager.setSpec(apiManagerSpec);
						return apiManager;
					}

					@Override
					public List<DeveloperAccount> getDeveloperAccounts() {
						return List.of(
								new DeveloperAccountBuilder().build());
					}

					@Override
					public List<DeveloperUser> getDeveloperUsers() {
						return List.of(
								new DeveloperUserBuilder().build());
					}

					@Override
					public List<Backend> getBackends() {
						return List.of(
								new BackendBuilder().build());
					}

					@Override
					public List<Product> getProducts() {
						return List.of(
								new ProductBuilder().build());
					}

					@Override
					public List<Application> getApplications() {
						return List.of(
								new ApplicationBuilder().build());
					}

					@Override
					public List<ProxyConfigPromote> getProxyConfigPromotes() {
						return List.of(
								new ProxyConfigPromoteBuilder().build());
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
