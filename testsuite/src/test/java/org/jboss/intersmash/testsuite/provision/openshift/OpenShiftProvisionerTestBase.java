package org.jboss.intersmash.testsuite.provision.openshift;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Strings;
import org.jboss.intersmash.deployments.IntersmashDelpoyableWildflyApplication;
import org.jboss.intersmash.deployments.IntersmashSharedDeployments;
import org.jboss.intersmash.model.helm.charts.values.wildfly.*;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.BootableJarOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.KafkaOperatorApplication;
import org.jboss.intersmash.tools.application.openshift.KeycloakTemplateOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.MysqlImageOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.PostgreSQLImageOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.WildflyImageOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.input.BinarySource;
import org.jboss.intersmash.tools.application.openshift.input.BuildInput;
import org.jboss.intersmash.tools.application.openshift.input.BuildInputBuilder;
import org.jboss.intersmash.tools.provision.openshift.template.KeycloakTemplate;
import org.jboss.intersmash.tools.util.IntersmashToolsProvisionersProperties;
import org.jboss.intersmash.tools.util.ProcessKeystoreGenerator;

import cz.xtf.builder.builders.SecretBuilder;
import cz.xtf.builder.builders.secret.SecretType;
import cz.xtf.core.config.OpenShiftConfig;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.strimzi.api.kafka.model.AclOperation;
import io.strimzi.api.kafka.model.AclResourcePatternType;
import io.strimzi.api.kafka.model.AclRule;
import io.strimzi.api.kafka.model.AclRuleBuilder;
import io.strimzi.api.kafka.model.Kafka;
import io.strimzi.api.kafka.model.KafkaBuilder;
import io.strimzi.api.kafka.model.KafkaTopic;
import io.strimzi.api.kafka.model.KafkaTopicBuilder;
import io.strimzi.api.kafka.model.KafkaUser;
import io.strimzi.api.kafka.model.KafkaUserBuilder;
import io.strimzi.api.kafka.model.listener.arraylistener.GenericKafkaListener;
import io.strimzi.api.kafka.model.listener.arraylistener.KafkaListenerType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenShiftProvisionerTestBase {
	static final EnvVar TEST_ENV_VAR = new EnvVarBuilder().withName("test-evn-key").withValue("test-evn-value").build();
	static final String TEST_SECRET_FOO = "foo";
	static final String TEST_SECRET_BAR = "bar";

	static final Secret TEST_SECRET = new SecretBuilder("test-secret")
			.setType(SecretType.OPAQUE).addData(TEST_SECRET_FOO, TEST_SECRET_BAR.getBytes()).build();

	static final String WILDFLY_TEST_PROPERTY = "test-property";

	static final String TEST_REPO = IntersmashConfig.deploymentsRepositoryUrl();
	static final String TEST_REF = IntersmashConfig.deploymentsRepositoryRef();

	static BootableJarOpenShiftApplication getWildflyBootableJarOpenShiftApplication() {
		return new BootableJarOpenShiftApplication() {
			@Override
			public BinarySource getBuildInput() {
				return IntersmashSharedDeployments::bootableJarDemoOpenShift;
			}

			@Override
			public List<Secret> getSecrets() {
				List<Secret> secrets = new ArrayList<>();
				secrets.add(TEST_SECRET);
				return Collections.unmodifiableList(secrets);
			}

			@Override
			public List<EnvVar> getEnvVars() {
				List<EnvVar> list = new ArrayList<>();
				list.add(new EnvVarBuilder().withName(TEST_ENV_VAR.getName()).withValue(TEST_ENV_VAR.getValue()).build());
				return Collections.unmodifiableList(list);
			}

			@Override
			public String getName() {
				return "bootable-jar";
			}
		};
	}

	static KeycloakTemplateOpenShiftApplication getHttpsKeycloak() {
		return new KeycloakTemplateOpenShiftApplication() {
			private final String secureAppHostname = "secure-" + getOpenShiftHostName();
			private final Path keystore = ProcessKeystoreGenerator.generateKeystore(secureAppHostname);
			private final String jceksFileName = "jgroups.jceks";
			private final Path truststore = ProcessKeystoreGenerator.getTruststore();

			@Override
			public String getName() {
				return "sso-app";
			}

			@Override
			public Map<String, String> getParameters() {
				Map<String, String> parameters = new HashMap<>();

				parameters.put("APPLICATION_NAME", getName());
				parameters.put("SSO_REALM", "wildfly-realm");
				parameters.put("SSO_SERVICE_USERNAME", "client");
				parameters.put("SSO_SERVICE_PASSWORD", "creator");
				parameters.put("SSO_ADMIN_USERNAME", "admin");
				parameters.put("SSO_ADMIN_PASSWORD", "admin");
				parameters.put("JGROUPS_CLUSTER_PASSWORD", "changeme");
				parameters.put("IMAGE_STREAM_NAMESPACE", OpenShiftConfig.namespace());

				return Collections.unmodifiableMap(parameters);
			}

			@Override
			public List<Secret> getSecrets() {
				SecretBuilder sb;
				try (InputStream is = getClass().getClassLoader().getResourceAsStream("certs/jgroups.jceks")) {
					sb = new SecretBuilder(getName() + "-secret")
							.addData(keystore.getFileName().toString(), keystore)
							.addData(jceksFileName, is)
							.addData(truststore.getFileName().toString(), truststore);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				return Collections.singletonList(sb.build());
			}

			@Override
			public KeycloakTemplate getTemplate() {
				return KeycloakTemplate.X509_HTTPS;
			}
		};
	}

	interface StaticWildflyImageOpenShiftApplication
			extends WildflyImageOpenShiftApplication, IntersmashDelpoyableWildflyApplication {
	};

	static WildflyImageOpenShiftApplication getWildflyOpenShiftImageApplication() {
		return new StaticWildflyImageOpenShiftApplication() {

			@Override
			public String eeFeaturePackLocation() {
				// this value is supposed to be overridden by the CI Jenkins job by passing e.g.
				// "mvn ... -Dwildfly.ee-feature-pack.location="
				return IntersmashConfig.getWildflyEeFeaturePackLocation();
			}

			@Override
			public String cloudFeaturePackLocation() {
				// this value is supposed to be overridden by the CI Jenkins job by passing e.g.
				// "mvn ... -Dwildfly.cloud-feature-pack.location="
				return IntersmashConfig.getWildflyCloudFeaturePackLocation();
			}

			@Override
			public String eeChannelLocation() {
				// this value is supposed to be overridden by the CI Jenkins job by passing e.g.
				// "mvn ... -Dwildfly.ee-channel.location="
				return IntersmashConfig.getWildflyEeChannelLocation();
			}

			@Override
			public String wildflyMavenPluginGroupId() {
				return IntersmashConfig.getWildflyMavenPluginGroupId();
			}

			@Override
			public String wildflyMavenPluginArtifactId() {
				return IntersmashConfig.getWildflyMavenPluginArtifactId();
			}

			@Override
			public String wildflyMavenPluginVersion() {
				return IntersmashConfig.getWildflyMavenPluginVersion();
			}

			@Override
			public List<String> getCliScript() {
				return Arrays
						.asList(String.format("/system-property=%s:add(value=\"%s\")", WILDFLY_TEST_PROPERTY,
								WILDFLY_TEST_PROPERTY));
			}

			@Override
			public BuildInput getBuildInput() {
				return new BuildInputBuilder().uri(TEST_REPO).ref(TEST_REF).build();
			}

			@Override
			public List<EnvVar> getEnvVars() {
				List<EnvVar> list = new ArrayList<>();
				list.add(new EnvVarBuilder().withName(TEST_ENV_VAR.getName()).withValue(TEST_ENV_VAR.getValue()).build());
				list.add(new EnvVarBuilder().withName("CLI_LAUNCH_SCRIPT").withValue("/opt/server/extensions/configure.cli")
						.build());

				list.add(new EnvVarBuilder().withName("ADMIN_USERNAME").withValue("admin").build());
				list.add(new EnvVarBuilder().withName("ADMIN_PASSWORD").withValue("pass.1234").build());
				if (!Strings.isNullOrEmpty(IntersmashConfig.getMavenMirrorUrl())) {
					list.add(new EnvVarBuilder().withName("MAVEN_MIRROR_URL")
							.withValue(IntersmashConfig.getMavenMirrorUrl()).build());
				}

				final String deploymentRelativePath = "deployments/openshift-jakarta-sample-standalone/";
				// let's add configurable deployment additional args:
				// TODO - the `-Dmaven.wagon.http.ssl.insecure=true` property in the next line is a workaround
				//  and should be removed once Intersmash will run on OCP 4.8, which allows for importing custom CA
				String mavenAdditionalArgs = " -f " + deploymentRelativePath + "pom.xml -Dmaven.wagon.http.ssl.insecure=true";
				mavenAdditionalArgs = mavenAdditionalArgs.concat(generateAdditionalMavenArgs());
				// let's pass the profile for building the deployment too...
				mavenAdditionalArgs = mavenAdditionalArgs.concat(
						(Strings.isNullOrEmpty(IntersmashToolsProvisionersProperties.getWildflyDeploymentsBuildProfile()) ? ""
								: " -P" + IntersmashToolsProvisionersProperties.getWildflyDeploymentsBuildProfile()));
				list.add(new EnvVarBuilder().withName("MAVEN_ARGS_APPEND").withValue(mavenAdditionalArgs).build());
				list.add(new EnvVarBuilder().withName("ARTIFACT_DIR").withValue(deploymentRelativePath + "target").build());

				return Collections.unmodifiableList(list);
			}

			@Override
			public List<Secret> getSecrets() {
				List<Secret> secrets = new ArrayList<>();
				secrets.add(TEST_SECRET);
				return Collections.unmodifiableList(secrets);
			}

			@Override
			public String getName() {
				return "wildfly-test-app";
			}

			@Override
			public String getPingServiceName() {
				return "wildfly-ping-service";
			}

			@Override
			public String bomServerVersionPropertyValue() {
				return IntersmashConfig.getWildflyBomsEeServerVersion();
			}
		};
	}

	public static StaticWildflyImageOpenShiftApplication getWildflyOpenShiftLocalBinarySourceApplication() {
		return new StaticWildflyImageOpenShiftApplication() {
			Path app = IntersmashSharedDeployments.findStandaloneDeploymentPath("openshift-jakarta-sample-standalone");

			@Override
			public String eeFeaturePackLocation() {
				// this value is supposed to be overridden by the CI Jenkins job by passing e.g.
				// "mvn ... -Dwildfly.ee-feature-pack.location="
				return IntersmashConfig.getWildflyEeFeaturePackLocation();
			}

			@Override
			public String cloudFeaturePackLocation() {
				// this value is supposed to be overridden by the CI Jenkins job by passing e.g.
				// "mvn ... -Dwildfly.cloud-feature-pack.location="
				return IntersmashConfig.getWildflyCloudFeaturePackLocation();
			}

			@Override
			public String eeChannelLocation() {
				// this value is supposed to be overridden by the CI Jenkins job by passing e.g.
				// "mvn ... -Dwildfly.ee-channel.location="
				return IntersmashConfig.getWildflyEeChannelLocation();
			}

			@Override
			public String wildflyMavenPluginGroupId() {
				return IntersmashConfig.getWildflyMavenPluginGroupId();
			}

			@Override
			public String wildflyMavenPluginArtifactId() {
				return IntersmashConfig.getWildflyMavenPluginArtifactId();
			}

			@Override
			public String wildflyMavenPluginVersion() {
				return IntersmashConfig.getWildflyMavenPluginVersion();
			}

			@Override
			public List<String> getCliScript() {
				return Arrays
						.asList(String.format("/system-property=%s:add(value=\"%s\")", WILDFLY_TEST_PROPERTY,
								WILDFLY_TEST_PROPERTY));
			}

			@Override
			public BuildInput getBuildInput() {
				return new BuildInputBuilder().archive(app).build();
			}

			@Override
			public List<EnvVar> getEnvVars() {
				List<EnvVar> list = new ArrayList<>();
				list.add(new EnvVarBuilder().withName(TEST_ENV_VAR.getName()).withValue(TEST_ENV_VAR.getValue()).build());
				list.add(new EnvVarBuilder().withName("CLI_LAUNCH_SCRIPT").withValue("/opt/server/extensions/configure.cli")
						.build());

				list.add(new EnvVarBuilder().withName("ADMIN_USERNAME").withValue("admin").build());
				list.add(new EnvVarBuilder().withName("ADMIN_PASSWORD").withValue("pass.1234").build());
				if (!Strings.isNullOrEmpty(IntersmashConfig.getMavenMirrorUrl())) {
					list.add(new EnvVarBuilder().withName("MAVEN_MIRROR_URL")
							.withValue(IntersmashConfig.getMavenMirrorUrl()).build());
				}

				// let's add configurable deployment additional args:
				String mavenAdditionalArgs = generateAdditionalMavenArgs();
				// let's pass the profile for building the deployment too...
				mavenAdditionalArgs = mavenAdditionalArgs.concat(
						(Strings.isNullOrEmpty(IntersmashToolsProvisionersProperties.getWildflyDeploymentsBuildProfile()) ? ""
								: " -P" + IntersmashToolsProvisionersProperties.getWildflyDeploymentsBuildProfile()));
				if (!Strings.isNullOrEmpty(mavenAdditionalArgs)) {
					list.add(new EnvVarBuilder().withName("MAVEN_ARGS_APPEND").withValue(mavenAdditionalArgs).build());
				}

				return Collections.unmodifiableList(list);
			}

			@Override
			public List<Secret> getSecrets() {
				List<Secret> secrets = new ArrayList<>();
				secrets.add(TEST_SECRET);
				return Collections.unmodifiableList(secrets);
			}

			@Override
			public String getName() {
				return "wildfly-test-app";
			}

			@Override
			public String getPingServiceName() {
				return "wildfly-ping-service";

			}

			@Override
			public String bomServerVersionPropertyValue() {
				return IntersmashConfig.getWildflyBomsEeServerVersion();
			}
		};
	}

	static WildflyImageOpenShiftApplication getWildflyOpenShiftLocalBinaryTargetServerApplication() {
		return new WildflyImageOpenShiftApplication() {
			Path app;

			private Path getApp() {
				if (app == null) {
					app = Paths.get(
							IntersmashSharedDeployments.findStandaloneDeploymentPath("openshift-jakarta-sample-standalone")
									.toFile().getAbsolutePath(),
							"target", "server");
				}
				return app;
			}

			@Override
			public List<String> getCliScript() {
				return Collections.emptyList();
			}

			@Override
			public BuildInput getBuildInput() {
				return new BuildInputBuilder().archive(getApp()).build();
			}

			@Override
			public List<EnvVar> getEnvVars() {
				List<EnvVar> list = new ArrayList<>();
				list.add(new EnvVarBuilder().withName("ADMIN_USERNAME").withValue("admin").build());
				list.add(new EnvVarBuilder().withName("ADMIN_PASSWORD").withValue("pass.1234").build());
				return Collections.unmodifiableList(list);
			}

			@Override
			public List<Secret> getSecrets() {
				List<Secret> secrets = new ArrayList<>();
				secrets.add(TEST_SECRET);
				return Collections.unmodifiableList(secrets);
			}

			@Override
			public String getName() {
				return "wildfly-test-app";
			}

			@Override
			public String getPingServiceName() {
				return "wildfly-ping-service";
			}
		};
	}

	public static MysqlImageOpenShiftApplication getMysqlOpenShiftApplication() {
		return new MysqlImageOpenShiftApplication() {

			@Override
			public String getUser() {
				return "user";
			}

			@Override
			public String getPassword() {
				return "password";
			}

			@Override
			public String getDbName() {
				return "mysqldb";
			}
		};
	}

	public static PostgreSQLImageOpenShiftApplication getPostgreSQLOpenShiftApplication() {
		return new PostgreSQLImageOpenShiftApplication() {

			@Override
			public String getUser() {
				return "user";
			}

			@Override
			public String getPassword() {
				return "password";
			}

			@Override
			public String getDbName() {
				return "psqldb";
			}
		};
	}

	/**
	 * This method serves just for testing purpose. It implements necessary methods of {@link
	 * KafkaOperatorApplication} interface so we can successfully use it in our tests.
	 *
	 * @return instance of {@link KafkaOperatorApplication} to be used for test purposes
	 */
	public static KafkaOperatorApplication getKafkaApplication() {
		return new KafkaOperatorApplication() {
			static final String NAME = "amq-streams-test";
			private static final String KAFKA_VERSION = KafkaOperatorApplication.KAFKA_VERSION;
			private static final String INTER_BROKER_PROTOCOL_VERSION = KafkaOperatorApplication.INTER_BROKER_PROTOCOL_VERSION;
			private static final int KAFKA_INSTANCE_NUM = KafkaOperatorApplication.KAFKA_INSTANCE_NUM;
			private static final int TOPIC_RECONCILIATION_INTERVAL_SECONDS = KafkaOperatorApplication.TOPIC_RECONCILIATION_INTERVAL_SECONDS;
			private static final long USER_RECONCILIATION_INTERVAL_SECONDS = KafkaOperatorApplication.USER_RECONCILIATION_INTERVAL_SECONDS;

			private static final int KAFKA_PORT = 9092;

			private Kafka kafka;
			private List<KafkaTopic> topics;
			private List<KafkaUser> users;

			@Override
			public Kafka getKafka() {
				if (kafka == null) {
					Map<String, Object> config = new HashMap<>();
					config.put("inter.broker.protocol.version", INTER_BROKER_PROTOCOL_VERSION);
					config.put("offsets.topic.replication.factor", KAFKA_INSTANCE_NUM);
					config.put("transaction.state.log.min.isr", KAFKA_INSTANCE_NUM);
					config.put("transaction.state.log.replication.factor", KAFKA_INSTANCE_NUM);

					config.put("default.replication.factor", KAFKA_INSTANCE_NUM);
					config.put("min.insync.replicas", 2);

					GenericKafkaListener listener = new GenericKafkaListener();
					listener.setName("plain");
					listener.setPort(KAFKA_PORT);
					listener.setType(KafkaListenerType.INTERNAL);
					listener.setTls(false);

					// Initialize Kafka resource
					kafka = new KafkaBuilder()
							.withNewMetadata().withName(NAME).endMetadata()
							.withNewSpec()
							.withNewEntityOperator()
							.withNewTopicOperator().withReconciliationIntervalSeconds(TOPIC_RECONCILIATION_INTERVAL_SECONDS)
							.endTopicOperator()
							.withNewUserOperator().withReconciliationIntervalSeconds(USER_RECONCILIATION_INTERVAL_SECONDS)
							.endUserOperator()
							.endEntityOperator()
							.withNewKafka()
							.withConfig(config)
							.withListeners(listener)
							.withNewKafkaAuthorizationSimple()
							.endKafkaAuthorizationSimple()
							.withReplicas(KAFKA_INSTANCE_NUM)
							.withNewEphemeralStorage().endEphemeralStorage()
							.withVersion(KAFKA_VERSION)
							.endKafka()
							.withNewZookeeper()
							.withReplicas(KAFKA_INSTANCE_NUM)
							.withNewEphemeralStorage().endEphemeralStorage()
							.endZookeeper()
							.endSpec()
							.build();
				}

				return kafka;
			}

			@Override
			public List<KafkaTopic> getTopics() {
				if (topics == null) {
					topics = new LinkedList<>();

					Map<String, String> labels = new HashMap<>();
					labels.put("strimzi.io/cluster", NAME);

					KafkaTopic topic = new KafkaTopicBuilder()
							.withNewMetadata()
							.withName("test-topic")
							.withLabels(labels)
							.endMetadata()
							.withNewSpec()
							.withPartitions(1)
							.withReplicas(1)
							.endSpec()
							.build();

					topics.add(topic);
				}

				return topics;
			}

			@Override
			public List<KafkaUser> getUsers() {
				if (users == null) {
					users = new LinkedList<>();

					Map<String, String> labels = new HashMap<>();
					labels.put("strimzi.io/cluster", NAME);

					AclRule acl = new AclRuleBuilder()
							.withHost("*")
							.withOperation(AclOperation.DESCRIBE)
							.withNewAclRuleTopicResource()
							.withName("test-topic")
							.withPatternType(AclResourcePatternType.LITERAL)
							.endAclRuleTopicResource()
							.build();

					KafkaUser user = new KafkaUserBuilder()
							.withNewMetadata()
							.withName("test-user")
							.withLabels(labels)
							.endMetadata()
							.withNewSpec()
							.withNewKafkaUserTlsClientAuthentication()
							.endKafkaUserTlsClientAuthentication()
							.withNewKafkaUserAuthorizationSimple()
							.withAcls(acl)
							.endKafkaUserAuthorizationSimple()
							.endSpec()
							.build();

					users.add(user);
				}

				return users;
			}

			@Override
			public String getName() {
				return NAME;
			}
		};
	}
}
