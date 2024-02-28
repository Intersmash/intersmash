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
package org.jboss.intersmash.provision;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.jboss.intersmash.application.Application;
import org.jboss.intersmash.provision.helm.wildfly.WildflyHelmChartOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.ActiveMQOperatorProvisioner;
import org.jboss.intersmash.provision.openshift.Eap7ImageOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.Eap7LegacyS2iBuildTemplateProvisioner;
import org.jboss.intersmash.provision.openshift.Eap7TemplateOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.HyperfoilOperatorProvisioner;
import org.jboss.intersmash.provision.openshift.InfinispanOperatorProvisioner;
import org.jboss.intersmash.provision.openshift.KafkaOperatorProvisioner;
import org.jboss.intersmash.provision.openshift.KeycloakOperatorProvisioner;
import org.jboss.intersmash.provision.openshift.MysqlImageOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.PostgreSQLImageOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.PostgreSQLTemplateOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.RhSsoOperatorProvisioner;
import org.jboss.intersmash.provision.openshift.RhSsoTemplateOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.WildflyBootableJarImageOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.WildflyImageOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.WildflyOperatorProvisioner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.stubbing.OngoingStubbing;

public class ProvisionerManagerTestCase {
	enum SupportedApplication {
		ActiveMqOperatorApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.ActiveMQOperatorApplication.class),
				ActiveMQOperatorProvisioner.class),
		BootableJarOpenShiftApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.BootableJarOpenShiftApplication.class),
				WildflyBootableJarImageOpenShiftProvisioner.class),
		Eap7ImageOpenShiftApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.Eap7ImageOpenShiftApplication.class),
				Eap7ImageOpenShiftProvisioner.class),
		Eap7LegacyS2iBuildTemplateApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.Eap7LegacyS2iBuildTemplateApplication.class),
				Eap7LegacyS2iBuildTemplateProvisioner.class),
		Eap7TemplateOpenShiftApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.Eap7TemplateOpenShiftApplication.class),
				Eap7TemplateOpenShiftProvisioner.class),
		InfinispanOperatorApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.InfinispanOperatorApplication.class),
				InfinispanOperatorProvisioner.class),
		HyperfoilOperatorApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.HyperfoilOperatorApplication.class),
				HyperfoilOperatorProvisioner.class),
		KafkaOperatorApplication(getApplicationMock(org.jboss.intersmash.application.openshift.KafkaOperatorApplication.class),
				KafkaOperatorProvisioner.class),
		KeycloakOperatorApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.KeycloakOperatorApplication.class),
				KeycloakOperatorProvisioner.class),
		MysqlImageOpenShiftApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.MysqlImageOpenShiftApplication.class),
				MysqlImageOpenShiftProvisioner.class),
		PostgreSQLImageOpenShiftApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.PostgreSQLImageOpenShiftApplication.class),
				PostgreSQLImageOpenShiftProvisioner.class),
		PostgreSQLTemplateOpenShiftApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.PostgreSQLTemplateOpenShiftApplication.class,
						(application) -> when(
								((org.jboss.intersmash.application.openshift.PostgreSQLTemplateOpenShiftApplication) application)
										.getTemplate())
								.thenReturn(
										org.jboss.intersmash.application.openshift.template.PostgreSQLTemplate.POSTGRESQL_PERSISTENT)),
				PostgreSQLTemplateOpenShiftProvisioner.class),
		RhSsoOperatorApplication(getApplicationMock(org.jboss.intersmash.application.openshift.RhSsoOperatorApplication.class),
				RhSsoOperatorProvisioner.class),
		RhSsoTemplateOpenShiftApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.RhSsoTemplateOpenShiftApplication.class,
						(application) -> when(
								((org.jboss.intersmash.application.openshift.RhSsoTemplateOpenShiftApplication) application)
										.getTemplate())
								.thenReturn(org.jboss.intersmash.application.openshift.template.RhSsoTemplate.X509_HTTPS)),
				RhSsoTemplateOpenShiftProvisioner.class),
		WildflyImageOpenShiftApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.WildflyImageOpenShiftApplication.class),
				WildflyImageOpenShiftProvisioner.class),
		WildflyOperatorApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.WildflyOperatorApplication.class),
				WildflyOperatorProvisioner.class),
		WildflyHelmChartOpenShiftApplication(
				getApplicationMock(org.jboss.intersmash.application.openshift.helm.WildflyHelmChartOpenShiftApplication.class),
				WildflyHelmChartOpenShiftProvisioner.class);

		private final Application application;
		private final Class<? extends Provisioner> provisionerClass;

		SupportedApplication(Application application, Class<? extends Provisioner> provisionerClass) {
			this.application = application;
			this.provisionerClass = provisionerClass;
		}

		public Application getApplication() {
			return application;
		}

		public Class<? extends Provisioner> getProvisionerClass() {
			return provisionerClass;
		}

		private static Application getApplicationMock(Class<? extends Application> applicationClazz) {
			return getApplicationMock(applicationClazz, null);
		}

		private static Application getApplicationMock(Class<? extends Application> applicationClazz,
				Function<Application, OngoingStubbing> setExpectations) {
			Application application = mock(applicationClazz);
			if (setExpectations != null) {
				setExpectations.apply(application);
			}
			return application;
		}
	}

	@ParameterizedTest(name = "{displayName}#class({0})")
	@EnumSource(SupportedApplication.class)
	public void testSupportedProvisioner(SupportedApplication supportedApplication) {
		Application application = supportedApplication.getApplication();
		Provisioner actual = ProvisionerManager.getProvisioner(application);
		Assertions.assertEquals(supportedApplication.getProvisionerClass(), actual.getClass());
	}

	@Test
	public void unsupportedProvisioner() {
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> ProvisionerManager.getProvisioner(() -> "dummy"));
	}
}
