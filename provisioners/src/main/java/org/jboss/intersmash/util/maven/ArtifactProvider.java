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
package org.jboss.intersmash.util.maven;

import java.io.File;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;

/**
 * Class provides support to get artifact by GAV from local repo (considered as remote).
 * Local repositories:
 * <ul>
 *     <li>
 *         local repository from maven settings
 *         <ul>
 *             <li>M2_HOME/conf/settings.xml as global settings</li>
 *             <li>user.home/.m2/settings.xml as local settings</li>
 *         </ul>
 *         if not available, user.home/.m2/repository is considered
 *     </li>
 *     <li>sys property localRepository if given (result from -Dmaven.repo.local)</li>
 * </ul>
 * maven central and such repositories are not supported.
 */
public class ArtifactProvider {

	/**
	 * @param groupId - required
	 * @param artifactId - required
	 * @param version - required
	 * @param type - (jar/war/..) required
	 * @param classifier optional - might be null
	 * @return artifact file
	 */
	public static File resolveArtifact(String groupId, String artifactId, String version, String type, String classifier)
			throws SettingsBuildingException, ArtifactResolutionException {
		LocalRepository localRepository = MavenSettingsUtil.getLocalRepository(MavenSettingsUtil.loadSettings());
		RepositorySystem system = newRepositorySystem();
		RepositorySystemSession session = newRepositorySystemSession(system, localRepository.getBasedir().getAbsolutePath());

		Artifact artifact = new DefaultArtifact(groupId, artifactId, classifier, type, version);
		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(artifact);

		artifactRequest.setRepositories(MavenSettingsUtil.getRemoteRepositories(MavenSettingsUtil.loadSettings()));
		ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
		return artifactResult.getArtifact().getFile();
	}

	public static RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);

		locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
			@Override
			public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
				throw new RuntimeException(exception);
			}
		});

		return locator.getService(RepositorySystem.class);
	}

	public static RepositorySystemSession newRepositorySystemSession(RepositorySystem system, String localRepositoryPath) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepo = new LocalRepository(localRepositoryPath);
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

		return session;
	}
}
