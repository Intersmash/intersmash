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
package org.jboss.intersmash.deployments;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.settings.building.SettingsBuildingException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jboss.intersmash.deployments.util.maven.ArtifactProvider;

/**
 * A class which is expected to provide access to shared (e.g.: used by both demos or testsuite) deployment
 * applications. They need to be installed in local repository.
 */
public class IntersmashSharedDeployments {
	static final String WILDFLY_BOOTABLE_JAR = "wildfly-bootable-jar";
	static final String EAP_7_BOOTABLE_JAR = "eap7-bootable-jar";
	static final String BOOTABLE_JAR_ARTIFACT_PACKAGING = "jar";

	public static Path wildflyBootableJarOpenShiftDeployment() {
		Path file = null;
		try {
			file = ArtifactProvider.resolveArtifact(
					IntersmashSharedDeploymentsProperties.groupID(),
					WILDFLY_BOOTABLE_JAR,
					IntersmashSharedDeploymentsProperties.version(),
					BOOTABLE_JAR_ARTIFACT_PACKAGING,
					"bootable-openshift").toPath();
		} catch (SettingsBuildingException | ArtifactResolutionException e) {
			throw new RuntimeException("Can not get artifact", e);
		}
		return file;
	}

	public static Path wildflyBootableJarBareMetalDeployment() {
		Path file = null;
		try {
			file = ArtifactProvider.resolveArtifact(
					IntersmashSharedDeploymentsProperties.groupID(),
					WILDFLY_BOOTABLE_JAR,
					IntersmashSharedDeploymentsProperties.version(),
					BOOTABLE_JAR_ARTIFACT_PACKAGING,
					"bootable-baremetal").toPath();
		} catch (SettingsBuildingException | ArtifactResolutionException e) {
			throw new RuntimeException("Can not get artifact", e);
		}
		return file;
	}

	public static Path eap7BootableJarOpenShiftDeployment() {
		Path file = null;
		try {
			file = ArtifactProvider.resolveArtifact(
					IntersmashSharedDeploymentsProperties.groupID(),
					EAP_7_BOOTABLE_JAR,
					IntersmashSharedDeploymentsProperties.version(),
					BOOTABLE_JAR_ARTIFACT_PACKAGING,
					"bootable-openshift").toPath();
		} catch (SettingsBuildingException | ArtifactResolutionException e) {
			throw new RuntimeException("Can not get artifact", e);
		}
		return file;
	}

	public static Path eap7BootableJarBareMetalDeployment() {
		Path file = null;
		try {
			file = ArtifactProvider.resolveArtifact(
					IntersmashSharedDeploymentsProperties.groupID(),
					EAP_7_BOOTABLE_JAR,
					IntersmashSharedDeploymentsProperties.version(),
					BOOTABLE_JAR_ARTIFACT_PACKAGING,
					"bootable-baremetal").toPath();
		} catch (SettingsBuildingException | ArtifactResolutionException e) {
			throw new RuntimeException("Can not get artifact", e);
		}
		return file;
	}

	public static Path findStandaloneDeploymentPath(String deployment) {
		File deploymentsBaseDir = new File(IntersmashSharedDeploymentsProperties.getDeploymentsProviderPath())
				.getParentFile();
		Path path = Paths.get(deploymentsBaseDir.getAbsolutePath(), deployment);
		if (path.toFile().exists() && path.toFile().isDirectory()) {
			return path;
		}
		throw new RuntimeException("Cannot find sources root directory: " + path.toFile().getAbsolutePath());
	}
}
