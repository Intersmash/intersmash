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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.intersmash.application.openshift.RhSsoOperatorApplication;
import org.jboss.intersmash.junit5.IntersmashExtension;
import org.jboss.intersmash.provision.openshift.RhSsoOperatorProvisioner;
import org.jboss.intersmash.provision.openshift.operator.keycloak.backup.KeycloakBackupBuilder;
import org.jboss.intersmash.provision.openshift.operator.keycloak.backup.spec.KeycloakAWSSpecBuilder;
import org.jboss.intersmash.provision.openshift.operator.keycloak.client.KeycloakClientBuilder;
import org.jboss.intersmash.provision.openshift.operator.keycloak.client.spec.KeycloakAPIClientBuilder;
import org.jboss.intersmash.provision.openshift.operator.keycloak.keycloak.KeycloakBuilder;
import org.jboss.intersmash.provision.openshift.operator.keycloak.keycloak.spec.KeycloakExternalAccessBuilder;
import org.jboss.intersmash.provision.openshift.operator.keycloak.realm.KeycloakRealmBuilder;
import org.jboss.intersmash.provision.openshift.operator.keycloak.realm.spec.KeycloakAPIRealmBuilder;
import org.jboss.intersmash.provision.openshift.operator.keycloak.realm.spec.RedirectorIdentityProviderOverrideBuilder;
import org.jboss.intersmash.provision.openshift.operator.keycloak.user.KeycloakUserBuilder;
import org.jboss.intersmash.provision.openshift.operator.keycloak.user.spec.KeycloakAPIUserBuilder;
import org.jboss.intersmash.provision.openshift.operator.keycloak.user.spec.KeycloakCredentialBuilder;
import org.jboss.intersmash.provision.openshift.operator.resources.OperatorGroup;
import org.jboss.intersmash.testsuite.junit5.categories.NotForCommunityExecutionProfile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.v1alpha1.Keycloak;
import org.keycloak.v1alpha1.KeycloakBackup;
import org.keycloak.v1alpha1.KeycloakClient;
import org.keycloak.v1alpha1.KeycloakRealm;
import org.keycloak.v1alpha1.KeycloakUser;
import org.keycloak.v1alpha1.keycloakclientspec.Client;
import org.keycloak.v1alpha1.keycloakclientspec.RealmSelector;
import org.keycloak.v1alpha1.keycloakrealmspec.InstanceSelector;
import org.keycloak.v1alpha1.keycloakuserspec.User;
import org.keycloak.v1alpha1.keycloakuserspec.user.Credentials;
import org.slf4j.event.Level;

import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
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
@NotForCommunityExecutionProfile
@Deprecated(since = "0.0.2")
public class RhSsoOperatorProvisionerTest {
	// Be aware that since we're using the static mock application, not all provisioner methods will work as expected!
	private static final RhSsoOperatorProvisioner KEYCLOAK_OPERATOR_PROVISIONER = initializeOperatorProvisioner();

	private static RhSsoOperatorProvisioner initializeOperatorProvisioner() {
		RhSsoOperatorProvisioner operatorProvisioner = new RhSsoOperatorProvisioner(
				new RhSsoOperatorApplication() {
					private static final String DEFAULT_KEYCLOAK_APP_NAME = "example-sso";

					@Override
					public Keycloak getKeycloak() {
						return new KeycloakBuilder(DEFAULT_KEYCLOAK_APP_NAME, matchLabels)
								.instances(1)
								.externalAccess(new KeycloakExternalAccessBuilder().enabled(true).build())
								.build();
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
	}

	@AfterEach
	public void customResourcesCleanup() {
		// delete backups
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakBackupsClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> KEYCLOAK_OPERATOR_PROVISIONER
						.keycloakBackupsClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		// delete clients
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakClientsClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> KEYCLOAK_OPERATOR_PROVISIONER
						.keycloakClientsClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		// delete keycloaks
		KEYCLOAK_OPERATOR_PROVISIONER.keycloaksClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> KEYCLOAK_OPERATOR_PROVISIONER
						.keycloaksClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		// delete realms
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakRealmsClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> KEYCLOAK_OPERATOR_PROVISIONER
						.keycloakRealmsClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		// delete users
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakUsersClient().list().getItems().stream()
				.map(resource -> resource.getMetadata().getName()).forEach(name -> KEYCLOAK_OPERATOR_PROVISIONER
						.keycloakUsersClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
	}

	/**
	 * This test case creates and validates a one-time AWS {@link KeycloakBackup} CR
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 *
	 * See
	 * <br> - https://github.com/keycloak/keycloak-operator/blob/master/deploy/examples/backup/OneTimeAwsBackup.yaml
	 */
	@Test
	public void oneTimeAwsBackup() {
		KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-keycloakbackup";
			KeycloakBackup keycloakBackup = new KeycloakBackupBuilder(name, matchLabels)
					.aws(new KeycloakAWSSpecBuilder()
							.credentialsSecretName("s3-backup")
							.build())
					//				.instanceSelector(new LabelSelectorBuilder().withMatchLabels(matchLabels).build())
					.build();
			verifyBackup(keycloakBackup);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates and validates a one-time local {@link KeycloakBackup} CR
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 *
	 * See
	 * <br> - https://github.com/keycloak/keycloak-operator/blob/master/deploy/examples/backup/OneTimeLocalBackup.yaml
	 */
	@Test
	public void oneTimeLocalBackup() {
		KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-keycloakbackup";
			KeycloakBackup keycloakBackup = new KeycloakBackupBuilder(name, matchLabels)
					//				.instanceSelector(new LabelSelectorBuilder().withMatchLabels(matchLabels).build())
					.build();
			verifyBackup(keycloakBackup);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates and validates a periodic and AWS-specific {@link KeycloakBackup} CR
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 *
	 * See
	 * <br> - https://github.com/keycloak/keycloak-operator/blob/master/deploy/examples/backup/PeriodicAwsBackup.yaml
	 */
	@Test
	public void periodicAwsBackup() {
		KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-keycloakbackup";
			KeycloakBackup keycloakBackup = new KeycloakBackupBuilder(name, matchLabels)
					.aws(new KeycloakAWSSpecBuilder()
							.schedule("*/2 * * * *")
							.credentialsSecretName("s3-backup")
							.build())
					//				.instanceSelector(new LabelSelectorBuilder().withMatchLabels(matchLabels).build())
					.build();
			verifyBackup(keycloakBackup);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates and validates a basic {@link KeycloakClient} CR
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 *
	 * See
	 * <br> - https://github.com/keycloak/keycloak-operator/blob/master/deploy/examples/client/client-secret.yaml
	 * <br> - https://github.com/keycloak/keycloak-operator/blob/master/deploy/examples/client
	 */
	@Test
	public void clientSecret() {
		KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "client-secret";
			RealmSelector realmSelector = new RealmSelector();
			realmSelector.setMatchLabels(matchLabels);
			Client client = new Client();
			client.setClientId("client-secret");
			client.setSecret("client-secret");
			client.setClientAuthenticatorType("client-secret");
			client.setProtocol("openid-connect");

			KeycloakClient keycloakClient = new KeycloakClientBuilder(name, matchLabels)
					.realmSelector(realmSelector)
					.client(client)
					.build();
			verifyClient(keycloakClient);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	//	/**
	//	 * https://github.com/keycloak/keycloak-operator/blob/master/deploy/examples/keycloak/external-keycloak.yaml
	//	 */
	//	@Test
	//	public void externalKeycloak() {
	//		name = "example-external-keycloak";
	//		Keycloak keycloak = new KeycloakBuilder(name, matchLabels)
	//				.unmanaged(true)
	//				.external(new KeycloakExternalBuilder()
	//						.enabled(true)
	//						.url("https://some.external.keycloak")
	//						.build())
	//				.build();
	//
	//		verifyKeycloak(keycloak, false);
	//	}

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
			Keycloak keycloak = new KeycloakBuilder(name, matchLabels)
					.instances(1)
					.externalAccess(new KeycloakExternalAccessBuilder().enabled(true).build())
					.build();

			verifyKeycloak(keycloak, true);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	//	/**
	//	 * https://github.com/keycloak/keycloak-operator/blob/master/deploy/examples/keycloak/keycloak-with-experimental-settings.yaml
	//	 */
	//	@Test
	//	public void keycloakWithExperimentalSettings() {
	//		ConfigMap configMap = createTestConfigMap();
	//
	//		name = "example-keycloak";
	//		Keycloak keycloak = new KeycloakBuilder(name, matchLabels)
	//				.instances(1)
	//				.externalAccess(new KeycloakExternalAccessBuilder().enabled(true).build())
	//				.keycloakDeploymentSpec(new KeycloakDeploymentSpecBuilder()
	//						.experimental(new ExperimentalSpecBuilder()
	//								.args("-Djboss.as.management.blocking.timeout=600")
	//								.env(new EnvVarBuilder().withName("PROXY_ADDRESS_FORWARDING").withValue("false").build())
	//								.volumes(new VolumesSpecBuilder()
	//										.defaultMode(0777)
	//										.items(new VolumeSpecBuilder().configMap(new ConfigMapVolumeSpecBuilder()
	//												.name(configMap.getMetadata().getName()).mountPath("/test-config").build())
	//												.build())
	//										.build())
	//								.build())
	//						.build())
	//				.build();
	//
	//		verifyKeycloak(keycloak, false);
	//		deleteTestConfigMap(configMap);
	//	}

	/**
	 * This test case creates and validates a {@link KeycloakRealm} CR with related {@link KeycloakUser} instances
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 *
	 * See
	 * <br> - https://github.com/keycloak/keycloak-operator/blob/master/deploy/examples/realm/realm_with_users.yaml
	 */
	@Test
	public void realmWithUsers() {
		KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-keycloakrealm";
			InstanceSelector instanceSelector = new InstanceSelector();
			instanceSelector.setMatchLabels(matchLabels);
			KeycloakRealm keycloakRealm = new KeycloakRealmBuilder(name, matchLabels)
					.realm(new KeycloakAPIRealmBuilder()
							.id("basic")
							.realm("basic")
							.enabled(true)
							.displayName("Basic Realm")
							.eventsListeners("metrics-listener")
							.users(new KeycloakAPIUserBuilder()
									.username("realm_admin")
									.firstName("John")
									.lastName("Doe")
									.email("jdoe@redhat.com")
									.enabled(true)
									.emailVerified(false)
									.realmRoles("offline_access")
									.realmRoles("uma_authorization")
									.clientRole("account",
											Stream.of("manage-account", "view-profile").collect(Collectors.toList()))
									.clientRole("realm-management",
											Stream.of("manage-users", "view-users", "query-users", "create-client")
													.collect(Collectors.toList()))
									.build())
							.build())
					.realmOverrides(new RedirectorIdentityProviderOverrideBuilder()
							.forFlow("browser")
							.identityProvider("openshift-v4")
							.build())
					.instanceSelector(instanceSelector)
					.build();

			verifyRealm(keycloakRealm);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates and validates a basic {@link KeycloakRealm} CR
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 *
	 * See
	 * <br> - https://github.com/keycloak/keycloak-operator/blob/master/deploy/examples/realm/basic_realm.yaml
	 */
	@Test
	public void basicRealm() {
		KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-keycloakrealm";
			KeycloakRealm keycloakRealm = new KeycloakRealmBuilder(name, matchLabels)
					.realm(new KeycloakAPIRealmBuilder()
							.id("user_realm")
							.realm("user_realm")
							.build())
					.build();

			verifyRealm(keycloakRealm);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates a {@link KeycloakRealm} CR and a {@link KeycloakClient} CR which uses SAML protocol
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 *
	 * See
	 * <br> - https://github.com/keycloak/keycloak/blob/master/examples/saml/testsaml.json
	 */
	@Test
	public void samlRealm() {
		KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-keycloakrealm";
			InstanceSelector instanceSelector = new InstanceSelector();
			instanceSelector.setMatchLabels(matchLabels);
			KeycloakRealm keycloakRealm = new KeycloakRealmBuilder(name, matchLabels)
					.realm(new KeycloakAPIRealmBuilder()
							.id("saml-demo")
							.realm("saml-demo")
							.enabled(true)
							.displayName("SAML Realm")
							.users(new KeycloakAPIUserBuilder()
									.username("saml_user")
									.firstName("John")
									.lastName("Doe")
									.email("jdoe@redhat.com")
									.enabled(true)
									.emailVerified(false)
									.realmRoles("manager")
									.credentials(new KeycloakCredentialBuilder()
											.type("password")
											.value("password")
											.build())
									.build())
							.clients(new KeycloakAPIClientBuilder()
									.clientId("http://localhost:8080/sales-post-enc/")
									.enabled(true)
									.fullScopeAllowed(true)
									.baseUrl("http://localhost:8080/sales-post-enc/")
									.adminUrl("http://localhost:8080/sales-post-enc/saml")
									.redirectUris("http://localhost:8080/sales-post-enc/*")
									.attributes(Map.of("saml.server.signature", "true",
											"saml.signature.algorithm", "RSA_SHA512",
											"saml.client.signature", "true",
											"saml.encrypt", "true",
											"saml.authnstatement", "true",
											"saml.signing.private.key",
											"MIICXQIBAAKBgQDb7kwJPkGdU34hicplwfp6/WmNcaLh94TSc7Jyr9Undp5pkyLgb0DE7EIE+6kSs4LsqCb8HDkB0nLD5DXbBJFd8n0WGoKstelvtg6FtVJMnwN7k7yZbfkPECWH9zF70VeOo9vbzrApNRnct8ZhH5fbflRB4JMA9L9R+LbURdoSKQIDAQABAoGBANtbZG9bruoSGp2s5zhzLzd4hczT6Jfk3o9hYjzNb5Z60ymN3Z1omXtQAdEiiNHkRdNxK+EM7TcKBfmoJqcaeTkW8cksVEAW23ip8W9/XsLqmbU2mRrJiKa+KQNDSHqJi1VGyimi4DDApcaqRZcaKDFXg2KDr/Qt5JFD/o9IIIPZAkEA+ZENdBIlpbUfkJh6Ln+bUTss/FZ1FsrcPZWu13rChRMrsmXsfzu9kZUWdUeQ2Dj5AoW2Q7L/cqdGXS7Mm5XhcwJBAOGZq9axJY5YhKrsksvYRLhQbStmGu5LG75suF+rc/44sFq+aQM7+oeRr4VY88Mvz7mk4esdfnk7ae+cCazqJvMCQQCx1L1cZw3yfRSn6S6u8XjQMjWE/WpjulujeoRiwPPY9WcesOgLZZtYIH8nRL6ehEJTnMnahbLmlPFbttxPRUanAkA11MtSIVcKzkhp2KV2ipZrPJWwI18NuVJXb+3WtjypTrGWFZVNNkSjkLnHIeCYlJIGhDd8OL9zAiBXEm6kmgLNAkBWAg0tK2hCjvzsaA505gWQb4X56uKWdb0IzN+fOLB3Qt7+fLqbVQNQoNGzqey6B4MoS1fUKAStqdGTFYPG/+9t",
											"saml.signing.certificate",
											"MIIB1DCCAT0CBgFJGVacCDANBgkqhkiG9w0BAQsFADAwMS4wLAYDVQQDEyVodHRwOi8vbG9jYWxob3N0OjgwODAvc2FsZXMtcG9zdC1lbmMvMB4XDTE0MTAxNjE0MjA0NloXDTI0MTAxNjE0MjIyNlowMDEuMCwGA1UEAxMlaHR0cDovL2xvY2FsaG9zdDo4MDgwL3NhbGVzLXBvc3QtZW5jLzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA2+5MCT5BnVN+IYnKZcH6ev1pjXGi4feE0nOycq/VJ3aeaZMi4G9AxOxCBPupErOC7Kgm/Bw5AdJyw+Q12wSRXfJ9FhqCrLXpb7YOhbVSTJ8De5O8mW35DxAlh/cxe9FXjqPb286wKTUZ3LfGYR+X235UQeCTAPS/Ufi21EXaEikCAwEAATANBgkqhkiG9w0BAQsFAAOBgQBMrfGD9QFfx5v7ld/OAto5rjkTe3R1Qei8XRXfcs83vLaqEzjEtTuLGrJEi55kXuJgBpVmQpnwCCkkjSy0JxbqLDdVi9arfWUxEGmOr01ZHycELhDNaQcFqVMPr5kRHIHgktT8hK2IgCvd3Fy9/JCgUgCPxKfhwecyEOKxUc857g==",
											"saml.encryption.private.key",
											"MIICXQIBAAKBgQDb7kwJPkGdU34hicplwfp6/WmNcaLh94TSc7Jyr9Undp5pkyLgb0DE7EIE+6kSs4LsqCb8HDkB0nLD5DXbBJFd8n0WGoKstelvtg6FtVJMnwN7k7yZbfkPECWH9zF70VeOo9vbzrApNRnct8ZhH5fbflRB4JMA9L9R+LbURdoSKQIDAQABAoGBANtbZG9bruoSGp2s5zhzLzd4hczT6Jfk3o9hYjzNb5Z60ymN3Z1omXtQAdEiiNHkRdNxK+EM7TcKBfmoJqcaeTkW8cksVEAW23ip8W9/XsLqmbU2mRrJiKa+KQNDSHqJi1VGyimi4DDApcaqRZcaKDFXg2KDr/Qt5JFD/o9IIIPZAkEA+ZENdBIlpbUfkJh6Ln+bUTss/FZ1FsrcPZWu13rChRMrsmXsfzu9kZUWdUeQ2Dj5AoW2Q7L/cqdGXS7Mm5XhcwJBAOGZq9axJY5YhKrsksvYRLhQbStmGu5LG75suF+rc/44sFq+aQM7+oeRr4VY88Mvz7mk4esdfnk7ae+cCazqJvMCQQCx1L1cZw3yfRSn6S6u8XjQMjWE/WpjulujeoRiwPPY9WcesOgLZZtYIH8nRL6ehEJTnMnahbLmlPFbttxPRUanAkA11MtSIVcKzkhp2KV2ipZrPJWwI18NuVJXb+3WtjypTrGWFZVNNkSjkLnHIeCYlJIGhDd8OL9zAiBXEm6kmgLNAkBWAg0tK2hCjvzsaA505gWQb4X56uKWdb0IzN+fOLB3Qt7+fLqbVQNQoNGzqey6B4MoS1fUKAStqdGTFYPG/+9t",
											"saml.encryption.certificate",
											"MIIB1DCCAT0CBgFJGVacCDANBgkqhkiG9w0BAQsFADAwMS4wLAYDVQQDEyVodHRwOi8vbG9jYWxob3N0OjgwODAvc2FsZXMtcG9zdC1lbmMvMB4XDTE0MTAxNjE0MjA0NloXDTI0MTAxNjE0MjIyNlowMDEuMCwGA1UEAxMlaHR0cDovL2xvY2FsaG9zdDo4MDgwL3NhbGVzLXBvc3QtZW5jLzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA2+5MCT5BnVN+IYnKZcH6ev1pjXGi4feE0nOycq/VJ3aeaZMi4G9AxOxCBPupErOC7Kgm/Bw5AdJyw+Q12wSRXfJ9FhqCrLXpb7YOhbVSTJ8De5O8mW35DxAlh/cxe9FXjqPb286wKTUZ3LfGYR+X235UQeCTAPS/Ufi21EXaEikCAwEAATANBgkqhkiG9w0BAQsFAAOBgQBMrfGD9QFfx5v7ld/OAto5rjkTe3R1Qei8XRXfcs83vLaqEzjEtTuLGrJEi55kXuJgBpVmQpnwCCkkjSy0JxbqLDdVi9arfWUxEGmOr01ZHycELhDNaQcFqVMPr5kRHIHgktT8hK2IgCvd3Fy9/JCgUgCPxKfhwecyEOKxUc857g=="))
									.protocol("saml")
									.build())
							.build())
					.instanceSelector(instanceSelector)
					.build();

			verifyRealm(keycloakRealm);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates a {@link KeycloakRealm} CR to manage SSO based on the OpenID connect protocol
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 *
	 * See <br>
	 *     - https://developers.redhat.com/blog/2020/01/29/api-login-and-jwt-token-generation-using-keycloak <br>
	 */
	@Test
	public void openIdRealm() {
		KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-keycloakrealm";
			InstanceSelector instanceSelector = new InstanceSelector();
			instanceSelector.setMatchLabels(matchLabels);
			KeycloakRealm keycloakRealm = new KeycloakRealmBuilder(name, matchLabels)
					.realm(new KeycloakAPIRealmBuilder()
							.id("openid-demo")
							.realm("openid-demo")
							.enabled(true)
							.displayName("OpenID Realm")
							.users(new KeycloakAPIUserBuilder()
									.username("openid_user")
									.firstName("John")
									.lastName("Doe")
									.email("jdoe@redhat.com")
									.enabled(true)
									.emailVerified(false)
									.realmRoles("manager")
									.credentials(new KeycloakCredentialBuilder()
											.type("password")
											.value("password")
											.build())
									.build())
							.clients(new KeycloakAPIClientBuilder()
									.clientId("openid-realm-client")
									.enabled(true)
									.fullScopeAllowed(true)
									.redirectUris("http://localhost:8080/sso-openid-mobile-client/*")
									.protocol("openid-connect")
									.build())
							.build())
					.instanceSelector(instanceSelector)
					.build();

			verifyRealm(keycloakRealm);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates and validates a basic {@link KeycloakUser} CR
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 *
	 * See
	 * <br> - https://github.com/keycloak/keycloak-operator/blob/master/deploy/examples/user/basic_user.yaml
	 * <br> - https://github.com/keycloak/keycloak-operator/tree/master/deploy/examples/user
	 *
	 */
	@Test
	public void basicUser() {
		KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-realm-user";
			org.keycloak.v1alpha1.keycloakuserspec.RealmSelector realmSelector = new org.keycloak.v1alpha1.keycloakuserspec.RealmSelector();
			realmSelector.setMatchLabels(matchLabels);
			User user = new User();
			user.setUsername("realm_user");
			user.setFirstName("John");
			user.setLastName("Doe");
			user.setEmail("user@example.com");
			user.setEnabled(true);
			user.setEmailVerified(false);
			KeycloakUser keycloakUser = new KeycloakUserBuilder(name, matchLabels)
					.user(user)
					.realmSelector(realmSelector)
					.build();

			verifyUser(keycloakUser);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * This test case creates and validates a {@link KeycloakUser} CR with related credentials
	 *
	 * This is not an integration test, the goal here is to assess that the created CRs are configured as per the
	 * model specification.
	 *
	 * See
	 * <br> - https://github.com/keycloak/keycloak-operator/blob/master/deploy/examples/user/user_with_credentials.yaml
	 * <br> - https://github.com/keycloak/keycloak-operator/tree/master/deploy/examples/user
	 */
	@Test
	public void userWithCredentials() {
		KEYCLOAK_OPERATOR_PROVISIONER.subscribe();
		try {
			name = "example-realm-user-with-creds";
			org.keycloak.v1alpha1.keycloakuserspec.RealmSelector realmSelector = new org.keycloak.v1alpha1.keycloakuserspec.RealmSelector();
			Credentials credentials = new Credentials();
			credentials.setType("password");
			credentials.setValue("12345");
			User user = new User();
			user.setUsername("creds_user");
			user.setFirstName("Creds");
			user.setLastName("User");
			user.setEmail("creds_user@redhat.com");
			user.setEnabled(true);
			user.setEmailVerified(false);
			user.setCredentials(Collections.singletonList(credentials));
			user.setRealmRoles(Collections.singletonList("offline_access"));
			Map<String, List<String>> clientRoles = new HashMap<>();
			clientRoles.put("account", Stream.of("manage-account").collect(Collectors.toList()));
			clientRoles.put("realm-management", Stream.of("manage-users").collect(Collectors.toList()));
			user.setClientRoles(clientRoles);
			KeycloakUser keycloakUser = new KeycloakUserBuilder(name, matchLabels)
					.user(user)
					.realmSelector(realmSelector)
					.build();

			verifyUser(keycloakUser);
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.unsubscribe();
		}
	}

	/**
	 * Test actual deploy/scale/undeploy workflow by the operator
	 *
	 * Does subscribe/unsubscribe on its own, so no need to call explicitly here.
	 *
	 * This test adds no further checks after {@link RhSsoOperatorProvisioner#undeploy()} based on
	 * {@link RhSsoOperatorProvisioner#getPods()}, since it looks for a stateful set which would be null at this point.
	 * There is room for evaluating whether to revisit {@link RhSsoOperatorProvisioner} with respect to such logic
	 */
	@Test
	public void basicProvisioningTest() {
		KEYCLOAK_OPERATOR_PROVISIONER.deploy();
		try {
			Assertions.assertEquals(1, KEYCLOAK_OPERATOR_PROVISIONER.getPods().size(),
					"Unexpected number of cluster operator pods for '" + KEYCLOAK_OPERATOR_PROVISIONER.getOperatorId()
							+ "' after deploy");

			int scaledNum = KEYCLOAK_OPERATOR_PROVISIONER.getApplication().getKeycloak().getSpec().getInstances().intValue()
					+ 1;
			KEYCLOAK_OPERATOR_PROVISIONER.scale(scaledNum, true);
			Assertions.assertEquals(scaledNum, KEYCLOAK_OPERATOR_PROVISIONER.getPods().size(),
					"Unexpected number of cluster operator pods for '" + KEYCLOAK_OPERATOR_PROVISIONER.getOperatorId()
							+ "' after scaling");
		} finally {
			KEYCLOAK_OPERATOR_PROVISIONER.undeploy();
		}
	}

	private void verifyUser(KeycloakUser keycloakUser) {
		// create and verify that object exists
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakUsersClient().createOrReplace(keycloakUser);
		new SimpleWaiter(() -> KEYCLOAK_OPERATOR_PROVISIONER.keycloakUsersClient().list().getItems().size() == 1)
				.level(Level.DEBUG)
				.waitFor();
		Assertions.assertEquals(keycloakUser.getSpec(), KEYCLOAK_OPERATOR_PROVISIONER.keycloakUser(name).get().getSpec());

		// delete and verify that object was removed
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakUsersClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
		new SimpleWaiter(() -> KEYCLOAK_OPERATOR_PROVISIONER.keycloakUsersClient().list().getItems().size() == 0)
				.level(Level.DEBUG)
				.waitFor();
	}

	private void verifyRealm(KeycloakRealm keycloakRealm) {
		// create and verify that object exists
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakRealmsClient().createOrReplace(keycloakRealm);
		new SimpleWaiter(() -> KEYCLOAK_OPERATOR_PROVISIONER.keycloakRealmsClient().list().getItems().size() == 1)
				.level(Level.DEBUG)
				.waitFor();
		Assertions.assertEquals(keycloakRealm.getSpec(), KEYCLOAK_OPERATOR_PROVISIONER.keycloakRealm(name).get().getSpec());

		// delete and verify that object was removed
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakRealmsClient().withName(name)
				.withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
		new SimpleWaiter(() -> KEYCLOAK_OPERATOR_PROVISIONER.keycloakRealmsClient().list().getItems().size() == 0)
				.level(Level.DEBUG)
				.waitFor();
	}

	private void verifyKeycloak(Keycloak keycloak, boolean waitForPods) {
		// create and verify that object exists
		KEYCLOAK_OPERATOR_PROVISIONER.keycloaksClient().createOrReplace(keycloak);
		KEYCLOAK_OPERATOR_PROVISIONER.waitFor(keycloak);
		// two pods expected keycloak-0 and keycloak-postgresql-*, keycloak-0 won't start unless keycloak-postgresql-* is ready
		if (waitForPods) {
			OpenShiftWaiters.get(OpenShifts.master(), () -> false)
					.areExactlyNPodsReady(2, "app", keycloak.getKind().toLowerCase()).level(Level.DEBUG).waitFor();
			log.debug(KEYCLOAK_OPERATOR_PROVISIONER.keycloaksClient().withName(name).get().getStatus().toString());
		}
		Assertions.assertEquals(keycloak.getSpec(),
				KEYCLOAK_OPERATOR_PROVISIONER.keycloaksClient().withName(name).get().getSpec());

		// delete and verify that object was removed
		KEYCLOAK_OPERATOR_PROVISIONER.keycloaksClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
		new SimpleWaiter(() -> KEYCLOAK_OPERATOR_PROVISIONER.keycloaksClient().list().getItems().size() == 0).level(Level.DEBUG)
				.waitFor();
		if (waitForPods) {
			OpenShiftWaiters.get(OpenShifts.master(), () -> false)
					.areExactlyNPodsReady(0, "app", keycloak.getKind().toLowerCase()).level(Level.DEBUG).waitFor();
		}
	}

	private void verifyBackup(KeycloakBackup keycloakBackup) {
		// create and verify that object exists
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakBackupsClient().createOrReplace(keycloakBackup);
		new SimpleWaiter(() -> KEYCLOAK_OPERATOR_PROVISIONER.keycloakBackupsClient().list().getItems().size() == 1)
				.level(Level.DEBUG).waitFor();
		Assertions.assertEquals(keycloakBackup.getSpec(), KEYCLOAK_OPERATOR_PROVISIONER.keycloakBackup(name).get().getSpec());

		// delete and verify that object was removed
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakBackupsClient().withName(name)
				.withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
		new SimpleWaiter(() -> KEYCLOAK_OPERATOR_PROVISIONER.keycloakBackupsClient().list().getItems().size() == 0)
				.level(Level.DEBUG).waitFor();
	}

	private void verifyClient(KeycloakClient keycloakClient) {
		// create and verify that object exists
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakClientsClient().createOrReplace(keycloakClient);
		new SimpleWaiter(() -> KEYCLOAK_OPERATOR_PROVISIONER.keycloakClientsClient().list().getItems().size() == 1)
				.level(Level.DEBUG).waitFor();
		Assertions.assertEquals(keycloakClient.getSpec(), KEYCLOAK_OPERATOR_PROVISIONER.keycloakClient(name).get().getSpec());

		// delete and verify that object was removed
		KEYCLOAK_OPERATOR_PROVISIONER.keycloakClientsClient().withName(name)
				.withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
		new SimpleWaiter(() -> KEYCLOAK_OPERATOR_PROVISIONER.keycloakUsersClient().list().getItems().size() == 0)
				.level(Level.DEBUG)
				.waitFor();
	}

	private ConfigMap createTestConfigMap() {
		Map<String, String> data = new HashMap<>();
		data.put("test.properties", "blah=true");
		ConfigMap configMap = new ConfigMapBuilder().withNewMetadata().withName("test-config").endMetadata().withData(data)
				.build();
		OpenShifts.master().createConfigMap(configMap);
		new SimpleWaiter(() -> OpenShifts.master().getConfigMap(configMap.getMetadata().getName()) != null).level(Level.DEBUG)
				.waitFor();
		return configMap;
	}

	private void deleteTestConfigMap(ConfigMap configMap) {
		OpenShifts.master().deleteConfigMap(configMap);
		new SimpleWaiter(() -> OpenShifts.master().getConfigMap(configMap.getMetadata().getName()) == null).level(Level.DEBUG)
				.waitFor();
	}
}
