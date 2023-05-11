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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jboss.intersmash.tools.application.openshift.KeycloakQuarkusOperatorApplication;
import org.jboss.intersmash.tools.application.openshift.PostgreSQLImageOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.PostgreSQLTemplateOpenShiftApplication;
import org.jboss.intersmash.tools.junit5.IntersmashExtension;
import org.jboss.intersmash.tools.provision.openshift.KeycloakQuarkusOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.PostgreSQLImageOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.PostgreSQLTemplateOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.OperatorGroup;
import org.jboss.intersmash.tools.provision.openshift.template.PostgreSQLTemplate;
import org.jboss.intersmash.tools.util.tls.CertificatesUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.k8s.v2alpha1.Keycloak;
import org.keycloak.k8s.v2alpha1.KeycloakSpec;
import org.keycloak.k8s.v2alpha1.keycloakspec.Db;
import org.keycloak.k8s.v2alpha1.keycloakspec.Hostname;
import org.keycloak.k8s.v2alpha1.keycloakspec.Http;
import org.keycloak.k8s.v2alpha1.keycloakspec.Ingress;
import org.keycloak.k8s.v2alpha1.keycloakspec.db.PasswordSecret;
import org.keycloak.k8s.v2alpha1.keycloakspec.db.UsernameSecret;
import org.slf4j.event.Level;

import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import lombok.extern.slf4j.Slf4j;

/**
 * Test the Keycloak Operator provisioning model and APIs
 *
 * See <br>
 *     - https://github.com/keycloak/keycloak-operator/tree/master/deploy/examples
 * <p>
 * Not all examples are actually run, but these tests are more about the framework functionality verification than
 * about the Keycloak operator testing.
 */
@Slf4j
@CleanBeforeAll
//@Disabled("WIP - Disabled until global-test.properties is configured with the required property")
public class KeycloakQuarkusOperatorProvisionerTest {
	private static KeycloakQuarkusOperatorProvisioner KEYCLOAK_OPERATOR_PROVISIONER;

	private static final String POSTGRESQL_NAME = "postgresql";
	private static final String POSTGRESQL_DATABASE = "keycloak";
	private static final String POSTGRESQL_PASSWORD = "pippobaudo1234";
	private static final String POSTGRESQL_USER = "user09M";

	private static PostgreSQLTemplateOpenShiftProvisioner initializePostgreSQLTemplateProvisioner() {
		PostgreSQLTemplateOpenShiftProvisioner templateProvisioner = new PostgreSQLTemplateOpenShiftProvisioner(
				new PostgreSQLTemplateOpenShiftApplication() {
					@Override
					public String getName() {
						return POSTGRESQL_NAME;
					}

					@Override
					public Map<String, String> getParameters() {
						Map<String, String> parameters = new HashMap<>();
						parameters.put("POSTGRESQL_DATABASE", POSTGRESQL_DATABASE);
						parameters.put("POSTGRESQL_PASSWORD", POSTGRESQL_PASSWORD);
						parameters.put("POSTGRESQL_USER", POSTGRESQL_USER);
						return parameters;
					}

					@Override
					public PostgreSQLTemplate getTemplate() {
						return PostgreSQLTemplate.POSTGRESQL_EPHEMERAL;
					}
				});
		return templateProvisioner;
	}

	private static final PostgreSQLImageOpenShiftApplication pgSQLApplication = new PostgreSQLImageOpenShiftApplication() {
		@Override
		public String getName() {
			return POSTGRESQL_NAME;
		}

		@Override
		public String getUser() {
			return POSTGRESQL_USER;
		}

		@Override
		public String getPassword() {
			return POSTGRESQL_PASSWORD;
		}

		@Override
		public String getDbName() {
			return POSTGRESQL_DATABASE;
		}
	};
	private static final PostgreSQLImageOpenShiftProvisioner POSTGRESQL_IMAGE_PROVISIONER = new PostgreSQLImageOpenShiftProvisioner(
			pgSQLApplication);

	private static KeycloakQuarkusOperatorProvisioner initializeOperatorProvisioner() {
		KeycloakQuarkusOperatorProvisioner operatorProvisioner = new KeycloakQuarkusOperatorProvisioner(
				new KeycloakQuarkusOperatorApplication() {
					private static final String DEFAULT_KEYCLOAK_APP_NAME = "example-sso";

					@Override
					public Keycloak getKeycloak() {
						Keycloak keycloak = new Keycloak();
						keycloak.getMetadata().setName(DEFAULT_KEYCLOAK_APP_NAME);
						KeycloakSpec spec = new KeycloakSpec();
						spec.setInstances(1L);
						Ingress ingress = new Ingress();
						ingress.setEnabled(true);
						spec.setIngress(ingress);
						Hostname hostname = new Hostname();
						hostname.setHostname(OpenShifts.master().generateHostname(DEFAULT_KEYCLOAK_APP_NAME));
						spec.setHostname(hostname);
						String tlsSecretName = DEFAULT_KEYCLOAK_APP_NAME + "-tls-secret";
						// create key, certificate and tls secret: Keycloak expects the secret to be created beforehand
						CertificatesUtils.CertificateAndKey certificateAndKey = CertificatesUtils
								.generateSelfSignedCertificateAndKey(hostname.getHostname(), tlsSecretName);
						// add TLS config to keycloak using the secret we just created
						Http http = new Http();
						http.setTlsSecret(certificateAndKey.tlsSecret.getMetadata().getName());
						spec.setHttp(http);
						return keycloak;
					}

					@Override
					public String getName() {
						return DEFAULT_KEYCLOAK_APP_NAME;
					}
				});
		return operatorProvisioner;
	}

	private String name;

	private static final Map<String, String> matchLabels = new HashMap<>();

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		// Keycloak
		KEYCLOAK_OPERATOR_PROVISIONER = initializeOperatorProvisioner();

		KEYCLOAK_OPERATOR_PROVISIONER.configure();
		matchLabels.put("app", "sso");
		IntersmashExtension.operatorCleanup();
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f", OperatorGroup.SINGLE_NAMESPACE.save().getAbsolutePath());
		// clean any leftovers
		KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
	}

	@AfterAll
	public static void removeOperatorGroup() {
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
		KEYCLOAK_OPERATOR_PROVISIONER.dismiss();
		POSTGRESQL_IMAGE_PROVISIONER.undeploy();
		POSTGRESQL_IMAGE_PROVISIONER.postUndeploy();
	}

	@AfterEach
	public void customResourcesCleanup() {

		// delete keycloaks
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> KEYCLOAK_OPERATOR_PROVISIONER
						.keycloakClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		// delete realms
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakRealmImportClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> KEYCLOAK_OPERATOR_PROVISIONER
						.keycloakRealmImportClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
						.delete());
	}

	/**
	 * This test case creates and validates a basic {@link Keycloak} CR
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 *
	 * See
	 * <br> - https://github.com/keycloak/keycloak-operator/tree/master/deploy/examples/keycloak
	 */
	@Test
	public void exampleSso() {
		KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-sso";

			Keycloak keycloak = new Keycloak();
			keycloak.getMetadata().setName(name);
			keycloak.getMetadata().setLabels(matchLabels);
			KeycloakSpec spec = new KeycloakSpec();
			spec.setInstances(1L);
			Ingress ingress = new Ingress();
			ingress.setEnabled(true);
			spec.setIngress(ingress);
			Hostname hostname = new Hostname();
			hostname.setHostname(OpenShifts.master().generateHostname(name));
			// create key, certificate and tls secret: Keycloak expects the secret to be created beforehand
			String tlsSecretName = name + "-tls-secret";
			CertificatesUtils.CertificateAndKey certificateAndKey = CertificatesUtils
					.generateSelfSignedCertificateAndKey(hostname.getHostname(), tlsSecretName);
			// add TLS config to keycloak using the secret we just created
			Http http = new Http();
			http.setTlsSecret(certificateAndKey.tlsSecret.getMetadata().getName());
			spec.setHttp(http);
			spec.setHostname(hostname);
			keycloak.setSpec(spec);

			verifyKeycloak(keycloak, true);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates and validates a {@link Keycloak} CR which uses a PostgreSQL databse
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 *
	 * See
	 * <br> - https://github.com/keycloak/keycloak-operator/tree/master/deploy/examples/keycloak
	 */
	@Test
	public void exampleSsoWithDatabase() {
		KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
		try {
			POSTGRESQL_IMAGE_PROVISIONER.preDeploy();
			POSTGRESQL_IMAGE_PROVISIONER.deploy();

			name = "example-sso";

			Keycloak keycloak = new Keycloak();
			keycloak.getMetadata().setName(name);
			keycloak.getMetadata().setLabels(matchLabels);
			KeycloakSpec spec = new KeycloakSpec();
			spec.setInstances(1L);
			Ingress ingress = new Ingress();
			ingress.setEnabled(true);
			spec.setIngress(ingress);
			Hostname hostname = new Hostname();
			hostname.setHostname(OpenShifts.master().generateHostname(name));
			// create key, certificate and tls secret: Keycloak expects the secret to be created beforehand
			String tlsSecretName = name + "-tls-secret";
			CertificatesUtils.CertificateAndKey certificateAndKey = CertificatesUtils
					.generateSelfSignedCertificateAndKey(hostname.getHostname(), tlsSecretName);
			// add TLS config to keycloak using the secret we just created
			Http http = new Http();
			http.setTlsSecret(certificateAndKey.tlsSecret.getMetadata().getName());
			spec.setHttp(http);
			spec.setHostname(hostname);
			// database
			Db db = new Db();
			db.setDatabase(POSTGRESQL_IMAGE_PROVISIONER.getApplication().getDbName());
			db.setHost(POSTGRESQL_IMAGE_PROVISIONER.getServiceName());
			db.setPort(Integer.toUnsignedLong(POSTGRESQL_IMAGE_PROVISIONER.getPort()));
			UsernameSecret usernameSecret = new UsernameSecret();
			usernameSecret.setName(POSTGRESQL_IMAGE_PROVISIONER.getSecretName());
			usernameSecret.setKey(PostgreSQLImageOpenShiftProvisioner.POSTGRESQL_USER_KEY);
			db.setUsernameSecret(usernameSecret);
			PasswordSecret passwordSecret = new PasswordSecret();
			passwordSecret.setName(POSTGRESQL_IMAGE_PROVISIONER.getSecretName());
			passwordSecret.setKey(PostgreSQLImageOpenShiftProvisioner.POSTGRESQL_PASSWORD_KEY);
			db.setPasswordSecret(passwordSecret);
			spec.setDb(db);
			keycloak.setSpec(spec);

			verifyKeycloak(keycloak, true);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
			POSTGRESQL_IMAGE_PROVISIONER.undeploy();
			POSTGRESQL_IMAGE_PROVISIONER.postUndeploy();
		}
	}

	private void verifyKeycloak(Keycloak keycloak, boolean waitForPods) {
		// create and verify that object exists
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().createOrReplace(keycloak);
		KEYCLOAK_OPERATOR_PROVISIONER.waitFor(keycloak);
		// two pods expected keycloak-0 and keycloak-postgresql-*, keycloak-0 won't start unless keycloak-postgresql-* is ready
		if (waitForPods) {
			OpenShiftWaiters.get(OpenShifts.master(), () -> false)
					.areExactlyNPodsReady(keycloak.getSpec().getInstances().intValue(), "app", keycloak.getKind().toLowerCase())
					.level(Level.DEBUG).waitFor();
			log.debug(KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().withName(name).get().getStatus().toString());
		}
		Assertions.assertEquals(keycloak.getSpec().getHostname().getHostname(),
				KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().withName(name).get().getSpec().getHostname().getHostname());
		if (!Objects.isNull(keycloak.getSpec().getDb())) {
			Assertions.assertEquals(keycloak.getSpec().getDb().getHost(),
					KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().withName(name).get().getSpec().getDb().getHost());
			Assertions.assertEquals(keycloak.getSpec().getDb().getDatabase(),
					KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().withName(name).get().getSpec().getDb().getDatabase());
			Assertions.assertEquals(keycloak.getSpec().getDb().getUsernameSecret().getName(),
					KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().withName(name).get().getSpec().getDb().getUsernameSecret()
							.getName());
			Assertions.assertEquals(keycloak.getSpec().getDb().getUsernameSecret().getKey(),
					KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().withName(name).get().getSpec().getDb().getUsernameSecret()
							.getKey());
			Assertions.assertEquals(keycloak.getSpec().getDb().getPasswordSecret().getName(),
					KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().withName(name).get().getSpec().getDb().getPasswordSecret()
							.getName());
			Assertions.assertEquals(keycloak.getSpec().getDb().getPasswordSecret().getKey(),
					KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().withName(name).get().getSpec().getDb().getPasswordSecret()
							.getKey());
		}

		// delete and verify that object was removed
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
		new SimpleWaiter(() -> KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().list().getItems().size() == 0).level(Level.DEBUG)
				.waitFor();
		if (waitForPods) {
			OpenShiftWaiters.get(OpenShifts.master(), () -> false)
					.areExactlyNPodsReady(0, "app", keycloak.getKind().toLowerCase()).level(Level.DEBUG).waitFor();
		}
	}
}
