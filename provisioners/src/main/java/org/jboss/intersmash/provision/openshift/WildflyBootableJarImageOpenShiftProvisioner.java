/*
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

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import lombok.NonNull;

public class WildflyBootableJarImageOpenShiftProvisioner extends BootableJarImageOpenShiftProvisioner {
	public WildflyBootableJarImageOpenShiftProvisioner(@NonNull BootableJarOpenShiftApplication bootableApplication) {
		super(bootableApplication);
	}

	@Override
	protected void configureAppBuilder(ContainerBuilder containerBuilder) {
		containerBuilder
				.withLivenessProbe(new ProbeBuilder()
						.withInitialDelaySeconds(45)
						.withFailureThreshold(6)
						.withNewHttpGet()
						.withPath("/health/live")
						.withPort(new IntOrString(9990))
						.endHttpGet()
						.build())
				.withReadinessProbe(new ProbeBuilder()
						.withInitialDelaySeconds(10)
						.withFailureThreshold(6)
						.withNewHttpGet()
						.withPath("/health/ready")
						.withPort(new IntOrString(9990))
						.endHttpGet()
						.build());
	}
}
