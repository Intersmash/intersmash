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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jboss.intersmash.testsuite.openshift.OpenShiftTest;
import org.jboss.intersmash.testsuite.openshift.ProjectCreationCapable;
import org.jboss.intersmash.tools.application.openshift.PostgreSQLImageOpenShiftApplication;
import org.jboss.intersmash.tools.application.operator.KeycloakRealmImportOperatorApplication;
import org.jboss.intersmash.tools.junit5.IntersmashExtension;
import org.jboss.intersmash.tools.provision.openshift.KeycloakRealmImportOpenShiftOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.PostgreSQLImageOpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.OperatorGroup;
import org.jboss.intersmash.tools.util.tls.CertificatesUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.k8s.v2alpha1.Keycloak;
import org.keycloak.k8s.v2alpha1.KeycloakRealmImport;
import org.keycloak.k8s.v2alpha1.KeycloakRealmImportSpec;
import org.keycloak.k8s.v2alpha1.KeycloakSpec;
import org.keycloak.k8s.v2alpha1.keycloakrealmimportspec.Realm;
import org.keycloak.k8s.v2alpha1.keycloakrealmimportspec.realm.Users;
import org.keycloak.k8s.v2alpha1.keycloakrealmimportspec.realm.users.Credentials;
import org.keycloak.k8s.v2alpha1.keycloakspec.Db;
import org.keycloak.k8s.v2alpha1.keycloakspec.Hostname;
import org.keycloak.k8s.v2alpha1.keycloakspec.Http;
import org.keycloak.k8s.v2alpha1.keycloakspec.Ingress;
import org.keycloak.k8s.v2alpha1.keycloakspec.db.PasswordSecret;
import org.keycloak.k8s.v2alpha1.keycloakspec.db.UsernameSecret;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
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
@OpenShiftTest
public class KeycloakRealmImportOpenShiftOperatorProvisionerTest implements ProjectCreationCapable {
	private static KeycloakRealmImportOpenShiftOperatorProvisioner KEYCLOAK_OPERATOR_PROVISIONER;

	private static final String POSTGRESQL_NAME = "postgresql";
	private static final String POSTGRESQL_DATABASE = "keycloak";
	private static final String POSTGRESQL_PASSWORD = "pippobaudo1234";
	private static final String POSTGRESQL_USER = "user09M";

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

	private static KeycloakRealmImportOpenShiftOperatorProvisioner initializeOperatorProvisioner(final Keycloak keycloak,
			final String appName) {
		KeycloakRealmImportOpenShiftOperatorProvisioner operatorProvisioner = new KeycloakRealmImportOpenShiftOperatorProvisioner(
				new KeycloakRealmImportOperatorApplication() {

					@Override
					public Keycloak getKeycloak() {
						return keycloak;
					}

					@Override
					public String getName() {
						return appName;
					}
				});
		return operatorProvisioner;
	}

	private String name;
	private String realmName;

	private static final Map<String, String> matchLabels = new HashMap<>();

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		matchLabels.put("app", "sso");
		IntersmashExtension.operatorCleanup(false, true);
		// create operator group - this should be done by InteropExtension
		OpenShifts.adminBinary().execute("apply", "-f",
				new OperatorGroup(OpenShiftConfig.namespace()).save().getAbsolutePath());
	}

	@AfterAll
	public static void removeOperatorGroup() {
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
		if (!Objects.isNull(KEYCLOAK_OPERATOR_PROVISIONER))
			KEYCLOAK_OPERATOR_PROVISIONER.dismiss();
	}

	@AfterEach
	public void customResourcesCleanup() {
		if (!Objects.isNull(KEYCLOAK_OPERATOR_PROVISIONER)) {
			// delete keycloaks
			KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient().list().getItems().stream()
					.map(resource -> resource.getMetadata().getName()).forEach(name -> KEYCLOAK_OPERATOR_PROVISIONER
							.keycloakClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
			// delete realms
			KEYCLOAK_OPERATOR_PROVISIONER.keycloakRealmImportClient().list().getItems()
					.stream()
					.map(resource -> resource.getMetadata().getName()).forEach(name -> KEYCLOAK_OPERATOR_PROVISIONER
							.keycloakRealmImportClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
							.delete());
		}
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
		name = "example-sso";

		final Keycloak keycloak = new Keycloak();
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
				.generateSelfSignedCertificateAndKey(hostname.getHostname().replaceFirst("[.].*$", ""), tlsSecretName);
		// add TLS config to keycloak using the secret we just created
		Http http = new Http();
		http.setTlsSecret(certificateAndKey.tlsSecret.getMetadata().getName());
		spec.setHttp(http);
		spec.setHostname(hostname);
		keycloak.setSpec(spec);

		KEYCLOAK_OPERATOR_PROVISIONER = initializeOperatorProvisioner(keycloak, name);
		KEYCLOAK_OPERATOR_PROVISIONER.configure();
		try {
			KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
			try {
				verifyKeycloak(keycloak, true);
			} finally {
				KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
			}
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.dismiss();
		}
	}

	/**
	 * This test case creates and validates a {@link Keycloak} CR which uses a PostgreSQL database
	 *
	 * Using a database is needed if you want to perform any {@link KeycloakRealmImport} import: after the import is
	 * completed, Keycloak is automatically restarted by the operator and, unless connected to a database, Keycloak
	 * would lose all the information just imported!
	 *
	 * See
	 * <br> - https://github.com/keycloak/keycloak-operator/tree/master/deploy/examples/keycloak
	 */
	@Test
	public void exampleSsoWithDatabase() {
		POSTGRESQL_IMAGE_PROVISIONER.configure();
		try {
			POSTGRESQL_IMAGE_PROVISIONER.preDeploy();
			try {
				POSTGRESQL_IMAGE_PROVISIONER.deploy();
				try {
					name = "example-sso";
					Keycloak keycloak = new Keycloak();
					keycloak.getMetadata().setName(name);
					keycloak.getMetadata().setLabels(matchLabels);
					KeycloakSpec spec = new KeycloakSpec();
					keycloak.setSpec(spec);
					spec.setInstances(1L);
					Ingress ingress = new Ingress();
					ingress.setEnabled(true);
					spec.setIngress(ingress);
					Hostname hostname = new Hostname();
					hostname.setHostname(OpenShifts.master().generateHostname(name));
					// create key, certificate and tls secret: Keycloak expects the secret to be created beforehand
					String tlsSecretName = name + "-tls-secret";
					CertificatesUtils.CertificateAndKey certificateAndKey = CertificatesUtils
							.generateSelfSignedCertificateAndKey(hostname.getHostname().replaceFirst("[.].*$", ""),
									tlsSecretName);
					// add TLS config to keycloak using the secret we just created
					Http http = new Http();
					http.setTlsSecret(certificateAndKey.tlsSecret.getMetadata().getName());
					spec.setHttp(http);
					spec.setHostname(hostname);
					// database
					Db db = new Db();
					db.setVendor("postgres");
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
					db.setDatabase(POSTGRESQL_IMAGE_PROVISIONER.getApplication().getDbName());
					spec.setDb(db);

					realmName = "saml-basic-auth";
					KeycloakRealmImport realmImport = new KeycloakRealmImport();
					realmImport.getMetadata().setName(realmName);
					realmImport.getMetadata().setLabels(matchLabels);
					KeycloakRealmImportSpec spec1 = new KeycloakRealmImportSpec();
					realmImport.setSpec(spec1);
					spec1.setKeycloakCRName(name);
					Realm realm = new Realm();
					spec1.setRealm(realm);
					realm.setId(realmName);
					realm.setRealm(realmName);
					realm.setEnabled(true);
					List<Users> users = new ArrayList<>();
					realm.setUsers(users);
					Users user1 = new Users();
					users.add(user1);
					user1.setUsername("user");
					user1.setEnabled(true);
					Credentials credentials = new Credentials();
					user1.setCredentials(List.of(credentials));
					credentials.setType("password");
					credentials.setValue("LOREDANABERTE1234");

					KEYCLOAK_OPERATOR_PROVISIONER = initializeOperatorProvisioner(keycloak, name);
					KEYCLOAK_OPERATOR_PROVISIONER.configure();
					try {
						KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
						try {
							verifyKeycloak(keycloak, true);
						} finally {
							KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
						}
					} finally {
						KEYCLOAK_OPERATOR_PROVISIONER.dismiss();
					}
				} finally {
					POSTGRESQL_IMAGE_PROVISIONER.undeploy();
				}
			} finally {
				POSTGRESQL_IMAGE_PROVISIONER.postUndeploy();
			}
		} finally {
			POSTGRESQL_IMAGE_PROVISIONER.dismiss();
		}
	}

	private void verifyKeycloak(Keycloak keycloak, boolean waitForPods) {
		verifyKeycloak(keycloak, null, waitForPods);
	}

	private void verifyKeycloak(Keycloak keycloak, KeycloakRealmImport realmImport, boolean waitForPods) {
		NonNamespaceOperation<Keycloak, KubernetesResourceList<Keycloak>, Resource<Keycloak>> keycloakClient = KEYCLOAK_OPERATOR_PROVISIONER
				.keycloakClient();
		NonNamespaceOperation<KeycloakRealmImport, KubernetesResourceList<KeycloakRealmImport>, Resource<KeycloakRealmImport>> keycloakRealmImportClient = KEYCLOAK_OPERATOR_PROVISIONER
				.keycloakRealmImportClient();
		// create and verify that object exists
		keycloakClient.createOrReplace(keycloak);
		KEYCLOAK_OPERATOR_PROVISIONER.waitFor(keycloak);
		// two pods expected keycloak-0 and keycloak-postgresql-*, keycloak-0 won't start unless keycloak-postgresql-* is ready
		if (waitForPods) {
			OpenShiftWaiters.get(OpenShifts.master(), () -> false)
					.areExactlyNPodsReady(keycloak.getSpec().getInstances().intValue(), "app", keycloak.getKind().toLowerCase())
					.level(Level.DEBUG).waitFor();
		}
		Assertions.assertEquals(keycloak.getSpec().getHostname().getHostname(),
				keycloakClient.withName(name).get().getSpec().getHostname().getHostname());
		if (!Objects.isNull(keycloak.getSpec().getDb())) {
			Assertions.assertEquals(keycloak.getSpec().getDb().getHost(),
					keycloakClient.withName(name).get().getSpec().getDb().getHost());
			Assertions.assertEquals(keycloak.getSpec().getDb().getDatabase(),
					keycloakClient.withName(name).get().getSpec().getDb().getDatabase());
			Assertions.assertEquals(keycloak.getSpec().getDb().getUsernameSecret().getName(),
					keycloakClient.withName(name).get().getSpec().getDb().getUsernameSecret()
							.getName());
			Assertions.assertEquals(keycloak.getSpec().getDb().getUsernameSecret().getKey(),
					keycloakClient.withName(name).get().getSpec().getDb().getUsernameSecret()
							.getKey());
			Assertions.assertEquals(keycloak.getSpec().getDb().getPasswordSecret().getName(),
					keycloakClient.withName(name).get().getSpec().getDb().getPasswordSecret()
							.getName());
			Assertions.assertEquals(keycloak.getSpec().getDb().getPasswordSecret().getKey(),
					keycloakClient.withName(name).get().getSpec().getDb().getPasswordSecret()
							.getKey());
		}

		// import new realm
		if (realmImport != null) {
			keycloakRealmImportClient.createOrReplace(realmImport);
			KEYCLOAK_OPERATOR_PROVISIONER.waitFor(realmImport);
			KEYCLOAK_OPERATOR_PROVISIONER.waitFor(keycloak);

			Assertions.assertEquals(
					keycloakRealmImportClient.withName(realmImport.getMetadata().getName())
							.get().getSpec().getRealm().getRealm(),
					realmName);

			keycloakRealmImportClient.withName(realmImport.getMetadata().getName())
					.withPropagationPolicy(DeletionPropagation.FOREGROUND)
					.delete();
			new SimpleWaiter(() -> keycloakRealmImportClient.list().getItems()
					.stream()
					.noneMatch(ri -> realmImport.getMetadata().getName().equalsIgnoreCase(ri.getMetadata().getName())));
		}

		// delete and verify that object was removed
		keycloakClient.withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
		new SimpleWaiter(() -> keycloakClient.list().getItems().size() == 0).level(Level.DEBUG)
				.waitFor();
		if (waitForPods) {
			OpenShiftWaiters.get(OpenShifts.master(), () -> false)
					.areExactlyNPodsReady(0, "app", keycloak.getKind().toLowerCase()).level(Level.DEBUG).waitFor();
		}
	}
}
