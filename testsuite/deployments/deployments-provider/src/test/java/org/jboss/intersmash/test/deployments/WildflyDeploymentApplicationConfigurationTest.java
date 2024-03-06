package org.jboss.intersmash.test.deployments;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

@ExtendWith(SystemStubsExtension.class)
public class WildflyDeploymentApplicationConfigurationTest {

	@SystemStub
	private SystemProperties systemProperties;

	@Test
	void method1(SystemProperties properties) {
		properties.set("wildfly-maven-plugin.groupId", "org.wildfly.plugins");
		properties.set("wildfly-maven-plugin.artifactId", "wildfly-maven-plugin");
		properties.set("wildfly-maven-plugin.version", "4.2.2.Final");
		properties.set("wildfly.ee-feature-pack.location", "org.wildfly:wildfly-ee-galleon-pack:30.0.0.Final");
		properties.set("wildfly.feature-pack.location", "org.wildfly:wildfly-galleon-pack:30.0.0.Final");
		properties.set("wildfly.cloud-feature-pack.location", "org.wildfly.cloud:wildfly-cloud-galleon-pack:5.0.1.Final");
		properties.set("wildfly.datasources-feature-pack.location", "org.wildfly:wildfly-datasources-galleon-pack:6.0.0.Final");
		properties.set("wildfly.keycloak-saml-adapter-feature-pack.version", "22.0.3");
		properties.set("wildfly.ee-channel.groupId", "wildfly.ee-channel.groupId.NONE_FOR_WILDFLY");
		properties.set("wildfly.ee-channel.artifactId", "wildfly.ee-channel.artifactId.NONE_FOR_WILDFLY");
		properties.set("wildfly.ee-channel.version", "wildfly.ee-channel.version.NONE_FOR_WILDFLY");
		properties.set("bom.wildfly-ee.version", "30.0.0.Final");

		WildflyDeploymentApplicationConfiguration app = new WildflyDeploymentApplicationConfiguration() {
		};

		Assertions.assertTrue(System.getProperty("wildfly-maven-plugin.groupId")
				.equals(app.wildflyMavenPluginGroupId()));
		Assertions.assertTrue(System.getProperty("wildfly-maven-plugin.artifactId")
				.equals(app.wildflyMavenPluginArtifactId()));
		Assertions.assertTrue(System.getProperty("wildfly-maven-plugin.version")
				.equals(app.wildflyMavenPluginVersion()));
		Assertions.assertTrue(System.getProperty("wildfly.ee-feature-pack.location")
				.equals(app.eeFeaturePackLocation()));
		Assertions.assertTrue(System.getProperty("wildfly.feature-pack.location")
				.equals(app.featurePackLocation()));
		Assertions.assertTrue(System.getProperty("wildfly.cloud-feature-pack.location")
				.equals(app.cloudFeaturePackLocation()));
		Assertions.assertTrue(System.getProperty("wildfly.datasources-feature-pack.location")
				.equals(app.datasourcesFeaturePackLocation()));
		Assertions.assertTrue(System.getProperty("wildfly.keycloak-saml-adapter-feature-pack.version")
				.equals(app.keycloakSamlAdapterFeaturePackVersion()));
		Assertions.assertTrue(System.getProperty("wildfly.ee-channel.groupId")
				.equals(app.eeChannelGroupId()));
		Assertions.assertTrue(System.getProperty("wildfly.ee-channel.artifactId")
				.equals(app.eeChannelArtifactId()));
		Assertions.assertTrue(System.getProperty("wildfly.ee-channel.version")
				.equals(app.eeChannelVersion()));
		Assertions.assertTrue(System.getProperty("bom.wildfly-ee.version")
				.equals(app.bomsEeServerVersion()));
	}
}
