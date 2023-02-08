package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.application.openshift.BootableJarOpenShiftApplication;

import cz.xtf.builder.builders.ApplicationBuilder;
import lombok.NonNull;

public class WildflyBootableJarImageOpenShiftProvisioner extends BootableJarImageOpenShiftProvisioner {
	public WildflyBootableJarImageOpenShiftProvisioner(@NonNull BootableJarOpenShiftApplication bootableApplication) {
		super(bootableApplication);
	}

	@Override
	protected void configureAppBuilder(ApplicationBuilder applicationBuilder) {
		applicationBuilder.deploymentConfig().podTemplate().container().addLivenessProbe()
				.setInitialDelay(45)
				.setFailureThreshold(6)
				.createHttpProbe("/health/live", "9990");
		applicationBuilder.deploymentConfig().podTemplate().container().addReadinessProbe()
				.setInitialDelaySeconds(10)
				.setFailureThreshold(6)
				.createHttpProbe("/health/ready", "9990");
	}
}
