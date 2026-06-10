/*
 * Copyright (C) 2025 Red Hat, Inc.
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
package org.jboss.intersmash.tools.helm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.jboss.intersmash.tools.cli.CLIUtils;
import org.jboss.intersmash.tools.config.IntersmashProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelmBinary {

	private final String path;
	private final String helmConfigPath;
	private final String kubeToken;
	private final String namespace;

	public HelmBinary(String path, String kubeToken, String namespace) {
		this.path = path;
		this.kubeToken = kubeToken;
		Path helmConfigFile = Paths.get(path).getParent().resolve(".config");
		try {
			helmConfigFile = Files.createDirectories(helmConfigFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.helmConfigPath = helmConfigFile.toAbsolutePath().toString();
		this.namespace = namespace;
	}

	public HelmBinary(String path, String helmConfigPath, String kubeUsername, String kubeToken, String namespace) {
		this.path = path;
		this.helmConfigPath = helmConfigPath;
		this.kubeToken = kubeToken;
		this.namespace = namespace;
	}

	public String execute(String... args) {
		String url = IntersmashProperties.get("intersmash.openshift.url",
				IntersmashProperties.get("xtf.openshift.url", ""));
		Map<String, String> environmentVariables = new HashMap<>();
		environmentVariables.put("HELM_CONFIG_HOME", helmConfigPath);
		environmentVariables.put("HELM_KUBEAPISERVER", url);
		environmentVariables.put("HELM_KUBETOKEN", kubeToken);
		environmentVariables.put("HELM_NAMESPACE", namespace);
		environmentVariables.put("HELM_KUBEINSECURE_SKIP_TLS_VERIFY", "true");
		return CLIUtils.executeCommand(environmentVariables, ArrayUtils.addAll(new String[] { path }, args));
	}
}
