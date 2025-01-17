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
package org.jboss.intersmash.test.deployments;

import com.google.common.base.Strings;

import cz.xtf.core.config.XTFConfig;

/**
 * Defines the contract for WILDFLY application services that expose configuration which should be taken into account
 * by the WILDFLY s2i v2 process, i.e. the WildFly Maven plugin, feature packs and BOMs settings
 */
public interface WildflyDeploymentApplicationConfiguration {

	String WILDFLY_EE_FEATURE_PACK_LOCATION = "wildfly.ee-feature-pack.location";
	String WILDFLY_FEATURE_PACK_LOCATION = "wildfly.feature-pack.location";
	String WILDFLY_CLOUD_FEATURE_PACK_LOCATION = "wildfly.cloud-feature-pack.location";
	String WILDFLY_DATASOURCES_FEATURE_PACK_LOCATION = "wildfly.datasources-feature-pack.location";
	String WILDFLY_EE_CHANNEL_GROUPID = "wildfly.ee-channel.groupId";
	String WILDFLY_EE_CHANNEL_ARTIFACTID = "wildfly.ee-channel.artifactId";
	String WILDFLY_EE_CHANNEL_VERSION = "wildfly.ee-channel.version";
	String WILDFLY_BOMS_EE_SERVER_VERSION = "bom.wildfly-ee.version";
	String WILDFLY_MAVEN_PLUGIN_GROUPID = "wildfly-maven-plugin.groupId";
	String WILDFLY_MAVEN_PLUGIN_ARTIFACTID = "wildfly-maven-plugin.artifactId";
	String WILDFLY_MAVEN_PLUGIN_VERSION = "wildfly-maven-plugin.version";
	String WILDFLY_KEYCLOAK_SAML_ADAPTER_FEATURE_PACK_VERSION = "wildfly.keycloak-saml-adapter-feature-pack.version";
	String MAVEN_MIRROR_URL = "maven-mirror.url";

	default String wildflyMavenPluginGroupId() {
		return XTFConfig.get(WILDFLY_MAVEN_PLUGIN_GROUPID);
	}

	default String wildflyMavenPluginArtifactId() {
		return XTFConfig.get(WILDFLY_MAVEN_PLUGIN_ARTIFACTID);
	}

	default String wildflyMavenPluginVersion() {
		return XTFConfig.get(WILDFLY_MAVEN_PLUGIN_VERSION);
	}

	default String eeFeaturePackLocation() {
		return XTFConfig.get(WILDFLY_EE_FEATURE_PACK_LOCATION);
	}

	default String featurePackLocation() {
		return XTFConfig.get(WILDFLY_FEATURE_PACK_LOCATION);
	}

	default String cloudFeaturePackLocation() {
		return XTFConfig.get(WILDFLY_CLOUD_FEATURE_PACK_LOCATION);
	}

	default String datasourcesFeaturePackLocation() {
		return XTFConfig.get(WILDFLY_DATASOURCES_FEATURE_PACK_LOCATION);
	}

	default String eeChannelGroupId() {
		return XTFConfig.get(WILDFLY_EE_CHANNEL_GROUPID);
	}

	default String eeChannelArtifactId() {
		return XTFConfig.get(WILDFLY_EE_CHANNEL_ARTIFACTID);
	}

	default String eeChannelVersion() {
		return XTFConfig.get(WILDFLY_EE_CHANNEL_VERSION);
	}

	default String bomsEeServerVersion() {
		return XTFConfig.get(WILDFLY_BOMS_EE_SERVER_VERSION);
	}

	default String getMavenMirrorUrl() {
		return XTFConfig.get(MAVEN_MIRROR_URL);
	}

	default String keycloakSamlAdapterFeaturePackVersion() {
		return XTFConfig.get(WILDFLY_KEYCLOAK_SAML_ADAPTER_FEATURE_PACK_VERSION);
	}

	/**
	 * Server version property name for BOMs
	 *
	 * @return A string that represents the name of a property which could hold a given WILDFLY 8  deployment application
	 * testable BOMs version
	 */
	default String bomsEeServerVersionPropertyName() {
		return WILDFLY_BOMS_EE_SERVER_VERSION;
	}

	/**
	 * This must match the property name in the pom.xml file used to set the "wildfly-galleon-pack" feature pack GAV
	 *
	 * @return property name in the pom.xml file used to set the feature pack GAV
	 */
	default String featurePackLocationPropertyName() {
		return WILDFLY_FEATURE_PACK_LOCATION;
	}

	/**
	 * This must match the property name in the pom.xml file used to set the "wildfly-ee-galleon-pack" feature pack GAV
	 *
	 * @return property name in the pom.xml file used to set the feature pack GAV
	 */
	default String eeFeaturePackLocationPropertyName() {
		return WILDFLY_EE_FEATURE_PACK_LOCATION;
	}

	/**
	 * This must match the property name in the pom.xml file used to set the "wildfly-cloud-galleon-pack" feature pack GAV
	 *
	 * @return property name in the pom.xml file used to set the cloud feature pack GAV
	 */
	default String cloudFeaturePackLocationPropertyName() {
		return WILDFLY_CLOUD_FEATURE_PACK_LOCATION;
	}

	/**
	 * This must match the property name in the pom.xml file used to set the "wildfly-datasources-galleon-pack" feature pack GAV
	 *
	 * @return property name in the pom.xml file used to set the datasources feature pack GAV
	 */
	default String datasourcesFeaturePackLocationPropertyName() {
		return WILDFLY_DATASOURCES_FEATURE_PACK_LOCATION;
	}

	/**
	 * This must match the property name in the pom.xml file used to set the
	 * "wildfly.keycloak-saml-adapter-feature-pack.version" feature pack version
	 *
	 * @return property name in the pom.xml file used to set the Keycloack SAML Adapter feature pack version
	 */
	default String keycloakSamlAdapterFeaturePackVersionPropertyName() {
		return WILDFLY_KEYCLOAK_SAML_ADAPTER_FEATURE_PACK_VERSION;
	}

	/**
	 * This must match the property name in the pom.xml file used to set the channel GroupId
	 *
	 * @return property name in the pom.xml file used to set the channel artifact groupId
	 */
	default String eeChannelGroupIdPropertyName() {
		return WILDFLY_EE_CHANNEL_GROUPID;
	}

	/**
	 * This must match the property name in the pom.xml file used to set the channel ArtifactId
	 *
	 * @return property name in the pom.xml file used to set the channel artifact artifactId
	 */
	default String eeChannelArtifactIdPropertyName() {
		return WILDFLY_EE_CHANNEL_ARTIFACTID;
	}

	/**
	 * This must match the property name in the pom.xml file used to set the channel Version
	 *
	 * @return property name in the pom.xml file used to set the channel artifact version
	 */
	default String eeChannelVersionPropertyName() {
		return WILDFLY_EE_CHANNEL_VERSION;
	}

	/**
	 * This must match the property name in the pom.xml file used to set the channel GAV
	 *
	 * @return property name in the pom.xml file used to set the channel GAV
	 */
	default String postgresFeaturePackDriverVersion() {
		return "42.2.8";
	}

	/**
	 * This must match the property name in the pom.xml file used to set the "wildfly-maven-plugin" groupId
	 *
	 * @return property name in the pom.xml file used to set the "wildfly-maven-plugin" groupId
	 */
	default String wildflyMavenPluginGroupIdPropertyName() {
		return WILDFLY_MAVEN_PLUGIN_GROUPID;
	}

	/**
	 * This must match the property name in the pom.xml file used to set the "wildfly-maven-plugin" artifactId
	 *
	 * @return property name in the pom.xml file used to set the "wildfly-maven-plugin" artifactId
	 */
	default String wildflyMavenPluginArtifactIdPropertyName() {
		return WILDFLY_MAVEN_PLUGIN_ARTIFACTID;
	}

	/**
	 * This must match the property name in the pom.xml file used to set the "wildfly-maven-plugin" version
	 *
	 * @return property name in the pom.xml file used to set the "wildfly-maven-plugin" version
	 */
	default String wildflyMavenPluginVersionPropertyName() {
		return WILDFLY_MAVEN_PLUGIN_VERSION;
	}

	default String generateAdditionalMavenArgs() {
		String result = "".concat(((Strings.isNullOrEmpty(this.eeFeaturePackLocation()) ? ""
				: (" -D" + this.eeFeaturePackLocationPropertyName() + "="
						+ this.eeFeaturePackLocation())))
				+ ((Strings.isNullOrEmpty(this.featurePackLocation()) ? ""
						: (" -D" + this.featurePackLocationPropertyName() + "="
								+ this.featurePackLocation())))
				+ ((Strings.isNullOrEmpty(this.cloudFeaturePackLocation()) ? ""
						: (" -D" + this.cloudFeaturePackLocationPropertyName() + "="
								+ this.cloudFeaturePackLocation())))
				+ ((Strings.isNullOrEmpty(this.datasourcesFeaturePackLocation()) ? ""
						: (" -D" + this.datasourcesFeaturePackLocationPropertyName() + "="
								+ this.datasourcesFeaturePackLocation())))
				+ ((Strings.isNullOrEmpty(this.keycloakSamlAdapterFeaturePackVersion()) ? ""
						: (" -D" + this.keycloakSamlAdapterFeaturePackVersionPropertyName() + "="
								+ this.keycloakSamlAdapterFeaturePackVersion())))
				+ ((Strings.isNullOrEmpty(this.eeChannelGroupId()) ? ""
						: (" -D" + this.eeChannelGroupIdPropertyName() + "="
								+ this.eeChannelGroupId())))
				+ ((Strings.isNullOrEmpty(this.eeChannelArtifactId()) ? ""
						: (" -D" + this.eeChannelArtifactIdPropertyName() + "="
								+ this.eeChannelArtifactId())))
				+ ((Strings.isNullOrEmpty(this.eeChannelVersion()) ? ""
						: (" -D" + this.eeChannelVersionPropertyName() + "="
								+ this.eeChannelVersion())))
				+ ((Strings.isNullOrEmpty(this.wildflyMavenPluginGroupId()) ? ""
						: (" -D" + this.wildflyMavenPluginGroupIdPropertyName() + "="
								+ this.wildflyMavenPluginGroupId())))
				+ ((Strings.isNullOrEmpty(this.wildflyMavenPluginArtifactId()) ? ""
						: (" -D" + this.wildflyMavenPluginArtifactIdPropertyName() + "="
								+ this.wildflyMavenPluginArtifactId())))
				+ ((Strings.isNullOrEmpty(this.wildflyMavenPluginVersion()) ? ""
						: (" -D" + this.wildflyMavenPluginVersionPropertyName() + "="
								+ this.wildflyMavenPluginVersion())))
				+ ((Strings.isNullOrEmpty(this.bomsEeServerVersion()) ? ""
						: (" -D" + this.bomsEeServerVersionPropertyName() + "="
								+ this.bomsEeServerVersion()))));
		// a maven mirror for internal testable artifacts, i.e. which are not released yet, can be provided
		result += result.concat(
				(Strings.isNullOrEmpty(this.getMavenMirrorUrl()) ? ""
						: " -Dmaven-mirror.url=" + this.getMavenMirrorUrl()));
		return result;
	}

	default String getWildflyDeploymentVariantFromStream(final String deploymentStream) {
		switch (deploymentStream) {
			case TestDeploymentProperties.WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_EAP_80:
			case TestDeploymentProperties.WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_EAP_81:
				return "eap";
			case TestDeploymentProperties.WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_EAP_XP5:
			case TestDeploymentProperties.WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_EAP_XP6:
				return "eapxp";
			default:
				throw new IllegalStateException("Unexpected value: " + deploymentStream);
		}
	}
}
