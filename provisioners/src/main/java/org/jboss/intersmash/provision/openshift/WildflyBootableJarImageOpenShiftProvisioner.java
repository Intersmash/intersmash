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
package org.jboss.intersmash.provision.openshift;

import org.jboss.intersmash.application.openshift.BootableJarOpenShiftApplication;

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
