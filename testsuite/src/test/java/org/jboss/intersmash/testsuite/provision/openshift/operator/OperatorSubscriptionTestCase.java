package org.jboss.intersmash.testsuite.provision.openshift.operator;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.jboss.intersmash.tools.application.openshift.ActiveMQOperatorApplication;
import org.jboss.intersmash.tools.application.openshift.InfinispanOperatorApplication;
import org.jboss.intersmash.tools.application.openshift.KafkaOperatorApplication;
import org.jboss.intersmash.tools.application.openshift.KeycloakOperatorApplication;
import org.jboss.intersmash.tools.application.openshift.WildflyOperatorApplication;
import org.jboss.intersmash.tools.junit5.IntersmashExtension;
import org.jboss.intersmash.tools.provision.openshift.ActiveMQOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.InfinispanOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.KafkaOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.KeycloakOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.WildflyOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.OperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.OperatorGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.extern.slf4j.Slf4j;

/**
 * Verify the {@link OperatorProvisioner} functionality.
 */
@Slf4j
@CleanBeforeAll
public class OperatorSubscriptionTestCase {
	private OperatorProvisioner operatorProvisioner;

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		IntersmashExtension.operatorCleanup();
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f", OperatorGroup.SINGLE_NAMESPACE.save().getAbsolutePath());
	}

	@AfterAll
	public static void removeOperatorGroup() {
		// remove operator group - this should be done by InteropExtension
		IntersmashExtension.operatorCleanup();
	}

	@AfterEach
	public void unsubscribe() {
		operatorProvisioner.unsubscribe();
	}

	/**
	 * Try the {@link WildflyOperatorProvisioner} subscription.
	 */
	@Test
	public void wildflyOperatorProvisionerSubscriptionTest() {
		operatorProvisioner = new WildflyOperatorProvisioner(mock(WildflyOperatorApplication.class));
		operatorSubscriptionTest();
	}

	/**
	 * Try the {@link KeycloakOperatorProvisioner} subscription.
	 */
	@Test
	public void keycloakOperatorProvisionerSubscriptionTest() {
		operatorProvisioner = new KeycloakOperatorProvisioner(mock(KeycloakOperatorApplication.class));
		operatorSubscriptionTest();
	}

	/**
	 * Try the {@link InfinispanOperatorProvisioner} subscription.
	 */
	@Test
	public void infinispanOperatorProvisionerSubscriptionTest() {
		operatorProvisioner = new InfinispanOperatorProvisioner(mock(InfinispanOperatorApplication.class));
		operatorSubscriptionTest();
	}

	/**
	 * Try the {@link ActiveMQOperatorProvisioner} subscription.
	 */
	@Test
	public void activeMQProvisionerSubscriptionTest() {
		operatorProvisioner = new ActiveMQOperatorProvisioner(mock(ActiveMQOperatorApplication.class));
		operatorSubscriptionTest();
	}

	@Test
	public void kafkaProvisionerSubscriptionTest() {
		operatorProvisioner = new KafkaOperatorProvisioner(mock(KafkaOperatorApplication.class));
		operatorSubscriptionTest();
	}

	public void operatorSubscriptionTest() {
		operatorProvisioner.configure();
		// clean any leftovers
		operatorProvisioner.unsubscribe();
		operatorProvisioner.subscribe();

		log.debug("Pods:");
		OpenShifts.master().getPods().forEach(this::introducePod);
		Assertions.assertTrue(operatorProvisioner.getCustomResourceDefinitions().size() > 0,
				String.format("List of CRDs provided by operator [%s] should not be empty.",
						operatorProvisioner.getPackageManifestName()));
	}

	private void introducePod(Pod pod) {
		log.debug(pod.getMetadata().getName());
		log.debug(" - labels: " + pod.getMetadata().getLabels().toString());
		log.debug(" - image: " + pod.getSpec().getContainers().get(0).getImage());
	}
}
