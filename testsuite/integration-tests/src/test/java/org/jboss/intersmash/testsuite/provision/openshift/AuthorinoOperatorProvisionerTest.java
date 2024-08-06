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
import java.util.List;

import org.jboss.intersmash.application.openshift.AuthorinoOperatorApplication;
import org.jboss.intersmash.junit5.IntersmashExtension;
import org.jboss.intersmash.provision.openshift.AuthorinoOperatorProvisioner;
import org.jboss.intersmash.provision.openshift.operator.resources.OperatorGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.junit5.annotations.CleanBeforeAll;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.kuadrant.authorino.operator.v1beta1.Authorino;
import io.kuadrant.authorino.operator.v1beta1.AuthorinoBuilder;
import io.kuadrant.authorino.operator.v1beta1.authorinospec.HealthzBuilder;
import io.kuadrant.authorino.operator.v1beta1.authorinospec.ListenerBuilder;
import io.kuadrant.authorino.operator.v1beta1.authorinospec.MetricsBuilder;
import io.kuadrant.authorino.operator.v1beta1.authorinospec.OidcServerBuilder;
import io.kuadrant.authorino.operator.v1beta1.authorinospec.TracingBuilder;
import io.kuadrant.authorino.operator.v1beta1.authorinospec.VolumesBuilder;
import io.kuadrant.authorino.operator.v1beta1.authorinospec.listener.PortsBuilder;
import io.kuadrant.authorino.operator.v1beta1.authorinospec.listener.TlsBuilder;
import io.kuadrant.authorino.operator.v1beta1.authorinospec.listener.tls.CertSecretRefBuilder;
import io.kuadrant.authorino.operator.v1beta1.authorinospec.volumes.ItemsBuilder;
import io.kuadrant.authorino.v1beta2.AuthConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CleanBeforeAll
public class AuthorinoOperatorProvisionerTest {

	private static final String APP_NAME = "authorino-minimal-example";
	// see https://github.com/Kuadrant/authorino-operator/blob/main/README.md#requesting-an-authorino-instance
	private static final Authorino AUTHORINO_CR_DEFINITION_MINIMAL_EXAMPLE = new AuthorinoBuilder()
			.withMetadata(
					new ObjectMetaBuilder()
							.withName(APP_NAME)
							.build())
			.withNewSpec()
			.withListener(
					new ListenerBuilder()
							.withTls(
									new TlsBuilder()
											.withEnabled(false)
											.build())
							.build())
			.withOidcServer(
					new OidcServerBuilder()
							.withTls(
									new io.kuadrant.authorino.operator.v1beta1.authorinospec.oidcserver.TlsBuilder()
											.withEnabled(false)
											.build())
							.build())
			.and()
			.build();

	// see https://github.com/Kuadrant/authorino-operator/blob/main/README.md#full-example
	private static final Authorino AUTHORINO_CR_DEFINITION_FULL_EXAMPLE = new AuthorinoBuilder()
			.withMetadata(
					new ObjectMetaBuilder()
							.withName("authorino-test-example")
							.build())
			.withNewSpec()
			.withClusterWide(true)
			.withAuthConfigLabelSelectors("environment=production")
			.withSecretLabelSelectors("authorino.kuadrant.io/component=authorino,environment=production")
			.withReplicas(2)
			.withEvaluatorCacheSize(2L)
			.withLogLevel("info")
			.withLogMode("production")
			.withListener(
					new ListenerBuilder()
							.withPorts(
									new PortsBuilder()
											.withGrpc(50001)
											.withHttp(5001)
											.build())
							.withTls(
									new TlsBuilder()
											.withCertSecretRef(
													new CertSecretRefBuilder()
															.withName("authorino-server-cert")
															.build())
											.build())
							.withTimeout(2L)
							.build())
			.withOidcServer(
					new OidcServerBuilder()
							.withPort(8083)
							.withTls(
									new io.kuadrant.authorino.operator.v1beta1.authorinospec.oidcserver.TlsBuilder()
											.withCertSecretRef(
													new io.kuadrant.authorino.operator.v1beta1.authorinospec.oidcserver.tls.CertSecretRefBuilder()
															.withName("authorino-oidc-server-cert")
															.build())
											.build())
							.build())
			.withMetrics(
					new MetricsBuilder()
							.withPort(8080)
							.withDeep(true)
							.build())
			.withHealthz(
					new HealthzBuilder()
							.withPort(8081)
							.build())
			.withTracing(
					new TracingBuilder()
							.withEndpoint("rpc://otel-collector.observability.svc.cluster.local:4317")
							.withInsecure(true)
							.build())
			.withVolumes(
					new VolumesBuilder()
							.withItems(
									new ItemsBuilder()
											.withName("keycloak-tls-cert")
											.withMountPath("/etc/ssl/certs")
											.withConfigMaps("keycloak-tls-cert")
											.withItems(
													new io.kuadrant.authorino.operator.v1beta1.authorinospec.volumes.items.ItemsBuilder()
															.withKey("keycloak.crt")
															.withPath("keycloak.crt")
															.build())
											.build())
							.withDefaultMode(420)
							.build())
			.and()
			.build();

	private static final AuthorinoOperatorApplication AUTHORINO_APPLICATION_MINIMAL_EXAMPLE = new AuthorinoOperatorApplication() {
		@Override
		public Authorino getAuthorino() {
			return AUTHORINO_CR_DEFINITION_MINIMAL_EXAMPLE;
		}

		@Override
		public List<AuthConfig> getAuthConfigs() {
			return List.of();
		}

		@Override
		public String getName() {
			return APP_NAME;
		}
	};

	private static final AuthorinoOperatorApplication AUTHORINO_APPLICATION_FULL_EXAMPLE = new AuthorinoOperatorApplication() {
		@Override
		public Authorino getAuthorino() {
			return AUTHORINO_CR_DEFINITION_FULL_EXAMPLE;
		}

		@Override
		public List<AuthConfig> getAuthConfigs() {
			return List.of();
		}

		@Override
		public String getName() {
			return APP_NAME;
		}
	};

	private static final AuthorinoOperatorProvisioner provisioner = initializeOperatorProvisioner();

	private static AuthorinoOperatorProvisioner initializeOperatorProvisioner() {
		return new AuthorinoOperatorProvisioner(AUTHORINO_APPLICATION_MINIMAL_EXAMPLE);
	}

	@BeforeAll
	public static void createOperatorGroup() throws IOException {
		provisioner.configure();
		IntersmashExtension.operatorCleanup();
		// create operator group - this should be done by InteropExtension
		OperatorGroup operatorGroup = new OperatorGroup(OpenShiftConfig.namespace(), null);
		final String ogResourcePath = operatorGroup.save().getAbsolutePath();
		OpenShifts.adminBinary().execute("apply", "-f", ogResourcePath);
		// clean any leftovers
		provisioner.unsubscribe();
		// let's configure the provisioner
		provisioner.configure();
	}

	@AfterAll
	public static void removeOperatorGroup() {
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
		provisioner.dismiss();
	}

	@AfterEach
	public void customResourcesCleanup() {
		// delete the AuthConfig CR(s)
		provisioner.authConfigs()
				.forEach(authConfig -> authConfig.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
	}

	/**
	 * Test the Authorino Operator based provisioning model by validating the creation of a {@link Authorino} CR,
	 * as per the full example at https://github.com/Kuadrant/authorino-operator/blob/main/README.md#full-example
	 */
	@Test
	public void minimalExampleTest() {
		provisioner.subscribe();
		try {
			// create and verify that object exists
			final Authorino testedAuthorinoResource = provisioner.getApplication().getAuthorino();
			provisioner.authorinosClient().createOrReplace(testedAuthorinoResource);
			Assertions.assertEquals(1, provisioner.authorinosClient().list().getItems().size());

			final String authorinoApplicationName = provisioner.getApplication().getName();
			OpenShifts.master().waiters().areExactlyNPodsReady(1, "authorino-resource",
					authorinoApplicationName).waitFor();

			// scaling down to 0 (graceful shutdown)
			testedAuthorinoResource.getSpec().setReplicas(0);
			provisioner.authorinosClient().replace(testedAuthorinoResource);
			new SimpleWaiter(
					() -> OpenShifts.master().getLabeledPods("authorino-resource", authorinoApplicationName).size() == 0)
					.waitFor();

			// delete and verify that object was removed
			provisioner.authorinosClient().withName(authorinoApplicationName)
					.withPropagationPolicy(DeletionPropagation.FOREGROUND)
					.delete();
			new SimpleWaiter(() -> provisioner.authorinosClient().list().getItems().size() == 0)
					.waitFor();
		} finally {
			provisioner.unsubscribe();
		}
	}
}
