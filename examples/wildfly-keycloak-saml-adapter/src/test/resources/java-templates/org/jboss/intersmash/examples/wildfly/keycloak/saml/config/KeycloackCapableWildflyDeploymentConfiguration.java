package org.jboss.intersmash.examples.wildfly.keycloak.saml.config;

public class KeycloackCapableWildflyDeploymentConfiguration {

	public static String getProjectGroupId() {
		return "${project.groupId}";
	}

	public static String getProjectArtifactId() {
		return "${project.artifactId}";
	}

	public static String getProjectVersion() {
		return "${project.version}";
	}

	public static String deploymentPath() {
		return "${project.basedir}/target/server";
	}
}
