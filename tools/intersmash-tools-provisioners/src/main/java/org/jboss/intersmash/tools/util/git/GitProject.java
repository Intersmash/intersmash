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
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Git project in the relevant terms for the {@link GitUtil} utilities, which can be used when
 * automating Git related operations, as for instance when cloning Helm Charts repositories.
 *
 */
public class GitProject {
	private static final Logger LOGGER = LoggerFactory.getLogger(GitProject.class);

	private final Git repo;
	private final Path path;
	private CredentialsProvider credentials;
	private String httpUrl;

	public GitProject(Path location) throws IOException {
		this(location, Git.open(location.toFile()), null);
	}

	public GitProject(Path path, Git repo, CredentialsProvider credentials) {
		this.path = path;
		this.repo = repo;
		this.credentials = credentials;
	}

	public static GitProject init(Path path) {
		InitCommand initCommand = Git.init().setDirectory(path.toFile());
		try {
			final Git git = initCommand.call();

			return new GitProject(path, git, null);
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	public static GitProject init(Path path, boolean bare) {
		InitCommand initCommand = Git.init().setDirectory(path.toFile()).setBare(bare);
		try {
			final Git git = initCommand.call();

			return new GitProject(path, git, null);
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	public String getName() {
		return path.getFileName().toString();
	}

	public String getUrl() {
		return getUrl("origin");
	}

	public void setUrl(String url) {
		setUrl(url, "origin");
	}

	public Path getPath() {
		return path;
	}

	public void setCredentials(CredentialsProvider credentials) {
		this.credentials = credentials;
	}

	public String getUrl(String remote) {
		return repo.getRepository().getConfig().getString("remote", remote, "url");
	}

	public String getRevHash() {
		try {
			return repo.getRepository().findRef("HEAD").getObjectId().getName();
		} catch (IOException ex) {
			LOGGER.error("Error retrieving HEAD revision", ex);
		}
		return null;
	}

	void setUrl(String url, String remote) {
		if (url != null && url.length() > 0) {
			StoredConfig config = repo.getRepository().getConfig();
			config.setString("remote", remote, "url", url);
			try {
				config.save();
			} catch (IOException ex) {
				throw new RuntimeException("Unable to save repository configuration", ex);
			}
		}
	}

	void setHttpUrl(String httpUrl) {
		this.httpUrl = httpUrl;
	}

	public String getHttpUrl() {
		return httpUrl;
	}

	public void pull() {
		try {
			if (credentials != null) {
				repo.pull().setCredentialsProvider(credentials).setRebase(true).call();
			} else {
				repo.pull().setRebase(true).call();
			}
		} catch (GitAPIException ex) {
			LOGGER.error("Error during pull.", ex);
		}
	}

	public void add(Path file) {
		try {
			repo.add().addFilepattern(path.relativize(file).toString()).call();
		} catch (GitAPIException ex) {
			LOGGER.error("Unable to add files", ex);
		}
	}

	public void addAll() {
		try {
			repo.add().addFilepattern(".").call();
		} catch (GitAPIException ex) {
			LOGGER.error("Unable to add all files", ex);
		}
	}

	public void commit(String message) {
		try {
			repo.commit().setAll(true).setMessage(message).call();
		} catch (GitAPIException ex) {
			LOGGER.error("Unable to commit changes.", ex);
		}
	}

	public void push() {
		try {
			if (credentials != null) {
				repo.push().setCredentialsProvider(credentials).call();
			} else {
				repo.push().call();
			}
		} catch (GitAPIException ex) {
			LOGGER.error("Error during push.", ex);
		}
	}

	public void checkout(String ref) {
		try {
			repo.checkout().setName(ref).call();
		} catch (GitAPIException ex) {
			LOGGER.error("Failed to checkout ref '" + ref + "'", ex);
		}
	}

	public void delete() {
		repo.close();
		try {
			FileUtils.deleteDirectory(path.toFile());
		} catch (IOException ex) {
			throw new RuntimeException("Failed to delete project '" + getName() + "'", ex);
		}
	}

	public void submodulesInitialization() {
		try {
			repo.submoduleInit().call();
			repo.submoduleUpdate().call();
		} catch (GitAPIException e) {
			throw new IllegalArgumentException("Unable to update submodules");
		}
	}

	public void close() {
		repo.close();
	}
}
