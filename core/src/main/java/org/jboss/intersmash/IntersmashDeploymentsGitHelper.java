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
package org.jboss.intersmash;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RemoteConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * Use the JGit to read the current remote and branch so we can set it dynamically in the test job without need to
 * explicitly change to test remote/branch via additional parameters (this might be required once we split everything
 * into a multiple git repositories, but we can utilize that for now).
 *
 * The class is not supposed to be used outside the {@link IntersmashConfig#deploymentsRepositoryUrl()} and
 * {@link IntersmashConfig#deploymentsRepositoryRef()} methods!
 *
 * WARNING: This one becomes obsolete once we split the current repository structure (move the deployments into different repository).
 *
 * See https://www.vogella.com/tutorials/JGit/article.html for more examples with JGit.
 */
@Slf4j
class IntersmashDeploymentsGitHelper {
	private static final String URL_TEMPLATE = "https://%s/%s/%s";
	private final static String DEFAULT_HOST = "github.com";
	private static final String DEFAULT_REMOTE = "Intersmash";
	private final static String DEFAULT_REPOSITORY = "intersmash.git";
	private static final String DEFAULT_URL = getRepositoryUrl(DEFAULT_HOST, DEFAULT_REMOTE, DEFAULT_REPOSITORY);
	private static final String DEFAULT_BRANCH = "main";
	private static String reference;
	private static String url;

	private static String getRepositoryUrl(String host, String remote, String repository) {
		return String.format(URL_TEMPLATE, host, remote, repository);
	}

	// TODO
	// WARNING: This one becomes obsolete once we split the current repository structure (move the deployments into different repository).
	static {
		// Try to find a git repository located in Intersmash root folder
		Optional<File> gitDir = findGit();
		if (gitDir.isPresent()) {
			try (Git git = Git.open(gitDir.get())) {
				// Jenkins git plugin creates a detached state, try to read the remote from FETCH_HEAD file
				if (git.getRepository().getBranch().equals(git.getRepository().getFullBranch())) {
					getRepoFromFetchHeadFile(gitDir.get(), git.getRepository().getFullBranch());
				} else {
					url = getRepositoryUrl(git.getRepository());
					if (url != null) {
						reference = git.getRepository().getBranch();
						log.debug("Dynamic deployments repository setup: {} at {}", reference, url);
					} else {
						setDefaults();
					}
				}
			} catch (IOException e) {
				log.debug("Failed to dynamically set the tested repository url and reference", e);
				setDefaults();
			} catch (URISyntaxException e) {
				log.debug("Failed to dynamically set the tested repository url and reference: invalid remote URI", e);
				setDefaults();
			}
		} else {
			log.debug("Failed to find a git config");
			setDefaults();
		}
	}

	// Jenkins git plugin creates a detached state, try to read the remote from FETCH_HEAD file
	private static void getRepoFromFetchHeadFile(File gitDir, String branch) throws IOException {
		File fetchHeadFile = new File(gitDir, "FETCH_HEAD");
		Optional<String> fetchHead = Arrays
				.stream(FileUtils.readFileToString(fetchHeadFile, Charset.defaultCharset())
						.split(System.lineSeparator()))
				.filter(s -> s.startsWith(branch)).findFirst();
		if (fetchHead.isPresent()) {
			// fetchHead example: "10d5068820c8ee2fc4eb51fa72b31a641cf1d6c0		branch 'xtf-0.18' of github.com:Intersmash/intersmash"
			String regex = "'(.+)' of .+:(.+)/intersmash";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(fetchHead.get());
			if (matcher.find() && matcher.groupCount() == 2) {
				reference = matcher.group(1);
				url = getRepositoryUrl(DEFAULT_HOST, matcher.group(2), DEFAULT_REPOSITORY);
				log.debug("Dynamic deployments repository setup from FETCH_HEAD file: {} at {}", reference,
						url);
			} else {
				log.debug("Failed to parse the FETCH_HEAD file");
				setDefaults();
			}
		} else {
			log.debug("Failed to locate fetch head for current commit");
			setDefaults();
		}
	}

	private static Optional<File> findGit() {
		boolean foundIntersmashRoot = false; // do not go above intersmash root directory
		int depth = 0; // do not go over 3 depth
		Path current = Paths.get(".").normalize().toAbsolutePath().normalize();
		while (!foundIntersmashRoot && depth++ < 3) {
			// look into a parent directory
			current = Paths.get(current.toString(), "..").normalize();
			foundIntersmashRoot = current.endsWith("intersmash");
			File[] gitConfig = current.toFile().listFiles((dir, name) -> name.equals(".git"));
			if (gitConfig != null && gitConfig.length == 1) {
				return Optional.of(gitConfig[0]);
			}
		}
		return Optional.empty();
	}

	private static void setDefaults() {
		log.warn("Fallback to default deployments repository configuration: {} at {}", DEFAULT_BRANCH,
				DEFAULT_URL);
		reference = DEFAULT_BRANCH;
		url = DEFAULT_URL;
	}

	/**
	 * Look for a remote branch reference. These should not be a problem for CI, but users could use have some crazy
	 * stuff locally, lets try to handle that as well.
	 *
	 * @return {@code null} in case we cannot determine a unambiguous remote reference for the current local repository
	 * state (current branch).
	 */
	private static String getRepositoryUrl(Repository repository) throws IOException, URISyntaxException {
		String branch = repository.getBranch();
		List<String> references = repository.getRefDatabase().getRefs().stream()
				.map(Ref::getName)
				.filter(reference -> reference.startsWith("refs/remotes/"))
				.filter(reference -> reference.endsWith("/" + branch))
				.collect(Collectors.toList());
		// current branch exist only locally, now way templates can use it
		if (references.size() == 0) {
			log.warn("Cannot find a remote branch references for the current branch \"{}\". You need to push it " +
					"to remote otherwise it cannot be used with templates.", branch);
			return null;
			// multiple remote references for the current branch (e.g. origin/main, user/main), cannot decide, fallback
		} else if (references.size() > 1) {
			log.warn("More than one remote branch references exist ({}) for the current branch \"{}\". Intersmash cannot " +
					"determine which one to use. See intersmash.deployments.repository.* configuration properties to set " +
					"the deployments repository manually.",
					references.toString(), branch);
			if (branch.equals("main")) {
				log.info("Please do not use the main branch for extensive development. See set of " +
						"intersmash.deployments.repository.* configuration properties in case you need to.");
			}
			return null;
		} else {
			// just a single remote reference has to exist at this point in the list
			String remote = repository.getRemoteName(references.get(0));
			RemoteConfig remoteConfig = new RemoteConfig(repository.getConfig(), remote);
			if (remoteConfig.getURIs() == null || remoteConfig.getURIs().isEmpty()) {
				throw new RuntimeException(String.format("Missing URI in git remote ref '%s'", remote));
			}
			// we expect a single URI
			String[] pathTokens = remoteConfig.getURIs().get(0).getPath().split("/");
			if (pathTokens.length != 2) {
				throw new RuntimeException(String.format("Unexpected path '%s' in URI '%s' of git remote ref '%s'",
						remoteConfig.getURIs().get(0).getPath(), remoteConfig.getURIs().get(0), remote));
			}
			// the URI must be converted in the HTTPS format
			String repositoryUrl = getRepositoryUrl(remoteConfig.getURIs().get(0).getHost(), pathTokens[0], pathTokens[1]);
			log.info("Repository Url for remote {}: {}", remote, repositoryUrl);
			return repositoryUrl;
		}
	}

	public static String repositoryReference() {
		return reference;
	}

	public static String repositoryUrl() {
		return url;
	}
}
