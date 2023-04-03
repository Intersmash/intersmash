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
package org.jboss.intersmash.tools.util.git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.xtf.core.waiting.Waiters;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilities that can be used when automating Git related operations, as for instance when cloning Helm Charts
 * repositories.
 */
@Slf4j
public final class GitUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(GitUtil.class);
	public static final Path REPOSITORIES = Paths.get("tmp").toAbsolutePath().resolve("git");

	private GitUtil() {
		// prevent instantiation
	}

	public static GitProject cloneRepository(Path target, String url) {
		return cloneRepository(target, url, null, null, "master");
	}

	public static GitProject cloneRepository(Path target, String url, String branch) {
		return cloneRepository(target, url, null, null, branch);
	}

	public static GitProject cloneRepository(Path target, String url, String username, String password) {
		return cloneRepository(target, url, username, password, "master");
	}

	public static GitProject cloneRepository(Path target, String url, String username, String password, String branch) {
		if (Files.exists(target)) {
			try {
				FileUtils.deleteDirectory(target.toFile());
			} catch (IOException e) {
				LOGGER.warn("Unable to delete directory", e);
			}
		}

		//CloneCommand cloneCommand = Git.cloneRepository().setURI(url).setDirectory(target.toFile());
		CloneCommand cloneCommand = Git.cloneRepository().setBranch(branch).setURI(url).setDirectory(target.toFile())
				.setTimeout(120_000);

		CredentialsProvider credentials = null;
		if (username != null && password != null) {
			credentials = new UsernamePasswordCredentialsProvider(username, password);
			cloneCommand = cloneCommand.setCredentialsProvider(credentials);
		}
		try {
			Git git = null;
			int RETRIES = 5;
			int RETRY_INTERVAL = 10;
			for (int i = 0; i < RETRIES; i++) {
				try {
					git = cloneCommand.call();
					break;
				} catch (Exception e) {
					log.error("Cloning git repo failed. Retry " + i, e);
				}
				Waiters.sleep(TimeUnit.SECONDS, RETRY_INTERVAL);
			}
			Assertions.assertThat(git).as("Unable to clone the repo").isNotNull();

			if (StringUtils.isNotBlank(branch)) {
				git.checkout().setName(branch).call();
			}

			return new GitProject(target, git, credentials);
		} catch (GitAPIException ex) {
			throw new RuntimeException("Unable to clone git repository", ex);
		}
	}

	public static GitProject initRepository(Path target) {
		return initRepository(target, null, null, null);
	}

	public static GitProject initRepository(Path target, String url) {
		return initRepository(target, url, null, null);
	}

	public static GitProject initRepository(Path target, String url, String username, String password) {
		InitCommand initCommand = Git.init().setDirectory(target.toFile());

		CredentialsProvider credentials = null;
		if (username != null && password != null) {
			credentials = new UsernamePasswordCredentialsProvider(username, password);
		}

		try {
			Git repo = initCommand.call();

			GitProject project = new GitProject(target, repo, credentials);
			project.setUrl(url);

			return project;
		} catch (GitAPIException ex) {
			throw new RuntimeException("Unable to init git repository", ex);
		}
	}

	public static boolean checkIfRepositoryExist(String url) {
		try {
			Git.lsRemoteRepository().setRemote(url).call();
		} catch (GitAPIException e) {
			LOGGER.debug("Repository \"" + url + "\" does not exist.");
			return false;
		}

		return true;
	}
}
