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
package org.jboss.intersmash.tools.provision;

import static org.mockito.Mockito.mock;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.BootableJarOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.MysqlImageOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.PostgreSQLImageOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.WildflyImageOpenShiftApplication;
import org.jboss.intersmash.tools.application.operator.ActiveMQOperatorApplication;
import org.jboss.intersmash.tools.application.operator.KafkaOperatorApplication;
import org.jboss.intersmash.tools.application.operator.WildflyOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.ActiveMQOpenShiftOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.KafkaOpenShiftOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.MysqlImageOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.PostgreSQLImageOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.WildflyBootableJarImageOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.WildflyImageOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.WildflyOpenShiftOperatorProvisioner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * | Service                                 | Source   | Provisioner                             		|
 * |-----------------------------------------|----------|-----------------------------------------------|
*  | WildflyImageOpenShiftApplication        | IMAGE    | WildflymageOpenShiftProvisioner         		|
 * | InfinispanImageOpenShiftApplication     | IMAGE    | InfinispanImageOpenShiftProvisioner     		|
 * | MysqlImageOpenShiftApplication          | IMAGE    | MysqlImageOpenShiftProvisioner          		|
 * | PostgreSQLImageOpenShiftApplication     | IMAGE    | PostgreSQLImageOpenShiftProvisioner     		|
 * | EapS2iBuildTemplateApplication          | TEMPLATE | EapS2iBuildTemplateProvisioner          		|
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
	 * | WildflyOperatorApplication          |          | WildflyOperatorProvisioner              |
	 */
	@Test
	public void wildflyOperatorProvisioner() {
		application = mock(WildflyOperatorApplication.class);

		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(WildflyOpenShiftOperatorProvisioner.class, actual.getClass());
	}

	/**
	 * | ActiveMQOperatorApplication    |          | ActiveMQOperatorProvisioner        |
	 */
	@Test
	public void activeMQOperatorProvisioner() {
		application = mock(ActiveMQOperatorApplication.class);

		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(ActiveMQOpenShiftOperatorProvisioner.class, actual.getClass());
	}

	/**
	 * | KafkaOperatorApplication    |          | KafkaOperatorProvisioner        |
	 */
	@Test
	public void kafkaOperatorProvisioner() {
		application = mock(KafkaOperatorApplication.class);

		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(KafkaOpenShiftOperatorProvisioner.class, actual.getClass());
	}

	@Test
	public void unsupportedProvisioner() {
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> ProvisionerManager.getProvisioner(() -> "dummy"));
	}
}
