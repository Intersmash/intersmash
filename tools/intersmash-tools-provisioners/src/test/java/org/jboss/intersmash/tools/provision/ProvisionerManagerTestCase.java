package org.jboss.intersmash.tools.provision;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.ActiveMQOperatorApplication;
import org.jboss.intersmash.tools.application.openshift.BootableJarOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.KafkaOperatorApplication;
import org.jboss.intersmash.tools.application.openshift.KeycloakTemplateOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.MysqlImageOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.PostgreSQLImageOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.PostgreSQLTemplateOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.WildflyImageOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.WildflyOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.ActiveMQOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.KafkaOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.KeycloakTemplateOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.MysqlImageOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.PostgreSQLImageOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.PostgreSQLTemplateOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.WildflyBootableJarImageOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.WildflyImageOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.WildflyOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.template.KeycloakTemplate;
import org.jboss.intersmash.tools.provision.openshift.template.PostgreSQLTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * | Service                                 | Source   | Provisioner                             		|
 * |-----------------------------------------|----------|-----------------------------------------------|
*  | WildflyImageOpenShiftApplication        | IMAGE    | WildflymageOpenShiftProvisioner         		|
 * | InfinispanImageOpenShiftApplication     | IMAGE    | InfinispanImageOpenShiftProvisioner     		|
 * | KeycloakTemplateOpenShiftApplication    | TEMPLATE | KeycloakTemplateOpenShiftProvisioner    		|
 * | MysqlImageOpenShiftApplication          | IMAGE    | MysqlImageOpenShiftProvisioner          		|
 * | PostgreSQLImageOpenShiftApplication     | IMAGE    | PostgreSQLImageOpenShiftProvisioner     		|
 * | EapS2iBuildTemplateApplication          | TEMPLATE | EapS2iBuildTemplateProvisioner          		|
 * | PostgreSQLTemplateOpenShiftApplication  | TEMPLATE | PostgreSQLTemplateOpenShiftProvisioner  		|
 * | WildflyOperatorApplication              | OPERATOR | WildflyOperatorProvisioner              		|
*  | ActiveMQOperatorApplication             | OPERATOR | ActiveMQOperatorProvisioner             		|
 * | KafkaOperatorApplication           	 | OPERATOR | KafkaOperatorProvisioner           	  		|
 * | WildflyBootableJarOpenShiftApplication  | IMAGE    | WildflyBootableJarImageOpenShiftProvisioner	|
 */
public class ProvisionerManagerTestCase {
	private Application application;

	/**
	 * | WildflyImageOpenShiftApplication        | IMAGE    | WildflyImageOpenShiftProvisioner        |
	 */
	@Test
	public void openShiftWildflyImageProvisioner() {
		application = mock(WildflyImageOpenShiftApplication.class);

		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(WildflyImageOpenShiftProvisioner.class, actual.getClass());
	}

	/**
	 * | WildflyBootableJarOpenShiftApplication | IMAGE    | WildflyBootableJarImageOpenShiftProvisioner |
	 */
	@Test
	public void openShiftWildflyBootableJarImageProvisioner() {
		application = mock(BootableJarOpenShiftApplication.class);

		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(WildflyBootableJarImageOpenShiftProvisioner.class, actual.getClass());
	}

	/**
	 * | KeycloakTemplateOpenShiftApplication      | TEMPLATE | KeycloakTemplateOpenShiftProvisioner   |
	 */
	@Test
	public void openShiftKeycloakTemplateProvisioner() {
		application = mock(KeycloakTemplateOpenShiftApplication.class);
		when(((KeycloakTemplateOpenShiftApplication) application).getTemplate()).thenReturn(KeycloakTemplate.X509_HTTPS);

		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(KeycloakTemplateOpenShiftProvisioner.class, actual.getClass());
	}

	/**
	 * | MysqlImageOpenShiftApplication      | IMAGE    | MysqlImageOpenShiftProvisioner      |
	 */
	@Test
	public void openShiftMysqlImageProvisioner() {
		application = mock(MysqlImageOpenShiftApplication.class);

		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(MysqlImageOpenShiftProvisioner.class, actual.getClass());
	}

	/**
	 * | PostgreSQLImageOpenShiftApplication | IMAGE    | PostgreSQLImageOpenShiftProvisioner |
	 */
	@Test
	public void openShiftPostgreSQLImageProvisioner() {
		application = mock(PostgreSQLImageOpenShiftApplication.class);

		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(PostgreSQLImageOpenShiftProvisioner.class, actual.getClass());
	}

	/**
	 * | PostgreSQLTemplateOpenShiftApplication | TEMPLATE | PostgreSQLTemplateOpenShiftProvisioner  |
	 */
	@Test
	public void openShiftPostgreSQLTemplateProvisioner() {
		application = mock(PostgreSQLTemplateOpenShiftApplication.class);
		when(((PostgreSQLTemplateOpenShiftApplication) application).getTemplate())
				.thenReturn(PostgreSQLTemplate.POSTGRESQL_PERSISTENT);

		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(PostgreSQLTemplateOpenShiftProvisioner.class, actual.getClass());
	}

	/**
	 * | WildflyOperatorApplication          |          | WildflyOperatorProvisioner              |
	 */
	@Test
	public void wildflyOperatorProvisioner() {
		application = mock(WildflyOperatorApplication.class);

		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(WildflyOperatorProvisioner.class, actual.getClass());
	}

	/**
	 * | ActiveMQOperatorApplication    |          | ActiveMQOperatorProvisioner        |
	 */
	@Test
	public void activeMQOperatorProvisioner() {
		application = mock(ActiveMQOperatorApplication.class);

		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(ActiveMQOperatorProvisioner.class, actual.getClass());
	}

	/**
	 * | KafkaOperatorApplication    |          | KafkaOperatorProvisioner        |
	 */
	@Test
	public void kafkaOperatorProvisioner() {
		application = mock(KafkaOperatorApplication.class);

		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(KafkaOperatorProvisioner.class, actual.getClass());
	}

	@Test
	public void unsupportedProvisioner() {
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> ProvisionerManager.getProvisioner(() -> "dummy"));
	}
}
