package org.jboss.intersmash.deployments;

import com.google.common.base.Strings;

/**
 * Defines the contract for WILDFLY application services that expose configuration which should be taken into account
 * by the WILDFLY s2i v2 process, i.e. the WildFly Maven plugin, feature packs and BOMs settings
 */
public interface IntersmashDelpoyableWildflyApplication {

	/**
	 * Server version property value for BOMs
	 *
	 * @return A string that represents a valid version of testable BOMs that could be used for building a given WILDFLY
	 * deployment application
	 */
	String bomServerVersionPropertyValue();

	/**
	 * Server version property name for BOMs
	 *
	 * @return A string that represents the name of a property which could hold a given WILDFLY 8  deployment application
	 * testable BOMs version
	 */
	default String bomServerVersionPropertyName() {
		return "bom.wildfly-ee.version";
	}

	/**
	 * "wildfly-ee-galleon-pack" feature pack GAV
	 *
	 * @return "wildfly-ee-galleon-pack" feature pack GAV e.g. org.wildfly:wildfly-ee-galleon-pack:27.0.0.Alpha4
	 */
	String eeFeaturePackLocation();

	/**
	 * "wildfly-cloud-galleon-pack" feature pack GAV
	 *
	 * @return "wildfly-cloud-galleon-pack" feature pack GAV e.g. org.wildfly.cloud:wildfly-cloud-galleon-pack:2.0.0.Alpha4
	 */
	String cloudFeaturePackLocation();

	/**
	 * channel GAV
	 *
	 * @return channel GAV
	 */
	String eeChannelLocation();

	/**
	 * This must match the variable name in the pom.xml file used to set the "wildfly-ee-galleon-pack" feature pack GAV
	 *
	 * @return variable name in the pom.xml file used to set the feature pack GAV
	 */
	default String eeFeaturePackLocationVarName() {
		return "wildfly.ee-feature-pack.location";
	}

	/**
	 * This must match the variable name in the pom.xml file used to set the "wildfly-cloud-galleon-pack" feature pack GAV
	 *
	 * @return variable name in the pom.xml file used to set the cloud feature pack GAV
	 */
	default String cloudFeaturePackLocationVarName() {
		return "wildfly.cloud-feature-pack.location";
	}

	/**
	 * This must match the variable name in the pom.xml file used to set the channel GAV
	 *
	 * @return variable name in the pom.xml file used to set the channel GAV
	 */
	default String eeChannelLocationVarName() {
		return "wildfly.ee-channel.location";
	}

	/**
	 * This must match the variable name in the pom.xml file used to set the channel GAV
	 *
	 * @return variable name in the pom.xml file used to set the channel GAV
	 */
	default String postgresFeaturePackDriverVersion() {
		return "42.2.8";
	}

	/**
	 * @return "wildfly-maven-plugin" groupId
	 */
	String wildflyMavenPluginGroupId();

	/**
	 * @return "wildfly-maven-plugin" artifactId
	 */
	String wildflyMavenPluginArtifactId();

	/**
	 * @return "wildfly-maven-plugin" version
	 */
	String wildflyMavenPluginVersion();

	/**
	 * This must match the variable name in the pom.xml file used to set the "wildfly-maven-plugin" groupId
	 *
	 * @return variable name in the pom.xml file used to set the "wildfly-maven-plugin" groupId
	 */
	default String wildflyMavenPluginGroupIdName() {
		return "wildfly-maven-plugin.groupId";
	}

	/**
	 * This must match the variable name in the pom.xml file used to set the "wildfly-maven-plugin" artifactId
	 *
	 * @return variable name in the pom.xml file used to set the "wildfly-maven-plugin" artifactId
	 */
	default String wildflyMavenPluginArtifactIdName() {
		return "wildfly-maven-plugin.artifactId";
	}

	/**
	 * This must match the variable name in the pom.xml file used to set the "wildfly-maven-plugin" version
	 *
	 * @return variable name in the pom.xml file used to set the "wildfly-maven-plugin" version
	 */
	default String wildflyMavenPluginVersionName() {
		return "wildfly-maven-plugin.version";
	}

	default String generateAdditionalMavenArgs() {
		String result = "".concat(((Strings.isNullOrEmpty(this.eeFeaturePackLocation()) ? ""
				: (" -D" + this.eeFeaturePackLocationVarName() + "="
						+ this.eeFeaturePackLocation())))
				+ ((Strings.isNullOrEmpty(this.cloudFeaturePackLocation()) ? ""
						: (" -D" + this.cloudFeaturePackLocationVarName() + "="
								+ this.cloudFeaturePackLocation())))
				+ ((Strings.isNullOrEmpty(this.eeChannelLocation()) ? ""
						: (" -D" + this.eeChannelLocationVarName() + "="
								+ this.eeChannelLocation())))
				+ ((Strings.isNullOrEmpty(this.wildflyMavenPluginGroupId()) ? ""
						: (" -D" + this.wildflyMavenPluginGroupIdName() + "="
								+ this.wildflyMavenPluginGroupId())))
				+ ((Strings.isNullOrEmpty(this.wildflyMavenPluginArtifactId()) ? ""
						: (" -D" + this.wildflyMavenPluginArtifactIdName() + "="
								+ this.wildflyMavenPluginArtifactId())))
				+ ((Strings.isNullOrEmpty(this.wildflyMavenPluginVersion()) ? ""
						: (" -D" + this.wildflyMavenPluginVersionName() + "="
								+ this.wildflyMavenPluginVersion())))
				+ ((Strings.isNullOrEmpty(this.bomServerVersionPropertyValue()) ? ""
						: (" -D" + this.bomServerVersionPropertyName() + "="
								+ this.bomServerVersionPropertyValue()))));
		return result;
	}
}
