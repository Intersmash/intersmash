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
package org.jboss.intersmash.tools.application.openshift;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.intersmash.tools.application.openshift.input.BinarySourceBuilder;
import org.jboss.intersmash.tools.application.openshift.input.BuildInput;
import org.jboss.intersmash.tools.application.openshift.input.BuildInputBuilder;
import org.jboss.intersmash.tools.application.openshift.input.GitSourceBuilder;

import cz.xtf.builder.builders.pod.PersistentVolumeClaim;
import cz.xtf.builder.builders.pod.VolumeMount;
import io.fabric8.kubernetes.api.model.EnvVar;

public interface WildflyImageOpenShiftApplication extends WildflyOpenShiftApplication, HasEnvVars {

	/**
	 * Use the {@link BuildInputBuilder} to get instances implementing the {@link BuildInput} interface.
	 *
	 * @see GitSourceBuilder
	 * @see BinarySourceBuilder
	 * @return {@link BuildInput} instance for application
	 */
	BuildInput getBuildInput();

	/**
	 * Setup mount points to WILDFLY pod and persistent volume claims to be created.
	 * @return A {@link Map} instance storing PVCs needed by the WILDFLY application service
	 */
	default Map<PersistentVolumeClaim, Set<VolumeMount>> getPersistentVolumeClaimMounts() {
		return Collections.emptyMap();
	}

	@Override
	default List<EnvVar> getEnvVars() {
		return Collections.emptyList();
	}

	/**
	 * Override this in the implementation class to return a non-null value in case you want to create a ping service
	 * to be used for DNS_PING clustering. The provisioner will take care of setting up all the ENV variables needed
	 * for clustering to work.
	 * If this method returns null, no service will be created.
	 *
	 * @return The name of the ping-service that will be created.
	 */
	default String getPingServiceName() {
		return null;
	}

	/**
	 * Gives a chance at modifying local sources before they are uploaded to the builder image; only works when
	 * performing a binary build from local sources
	 * @param mavenProjectRoot path to maven project root
	 * @return
	 */
	default Path prepareProjectSources(Path mavenProjectRoot) {
		return mavenProjectRoot;
	};
}
