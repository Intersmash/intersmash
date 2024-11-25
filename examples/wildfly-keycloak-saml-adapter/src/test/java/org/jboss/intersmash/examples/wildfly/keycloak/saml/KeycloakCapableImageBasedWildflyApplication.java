package org.jboss.intersmash.examples.wildfly.keycloak.saml;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.intersmash.application.input.BinarySource;
import org.jboss.intersmash.application.input.BuildInput;
import org.jboss.intersmash.application.openshift.WildflyImageOpenShiftApplication;
import org.jboss.intersmash.examples.wildfly.keycloak.saml.config.KeycloackCapableWildflyDeploymentConfiguration;

public class KeycloakCapableImageBasedWildflyApplication implements WildflyImageOpenShiftApplication {

	@Override
	public String getName() {
		return "wildfly-app";
	}

	@Override
	public BuildInput getBuildInput() {
		return (BinarySource) () -> {
			Path path = Paths.get(KeycloackCapableWildflyDeploymentConfiguration.deploymentPath());
			if (path.toFile().exists() && path.toFile().isDirectory()) {
				return path;
			}
			throw new RuntimeException("Cannot find sources root directory: " + path.toFile().getAbsolutePath());
		};
	}
}
