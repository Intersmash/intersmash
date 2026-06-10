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
package org.jboss.intersmash.tools.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

/**
 * Holds the output (stdout and stderr) of a command execution in a pod shell.
 * This replaces XTF's {@code cz.xtf.core.openshift.PodShellOutput}.
 */
@Getter
public class PodShellOutput {
	private final String output;
	private final String error;

	PodShellOutput(String output, String error) {
		this.output = output;
		this.error = error;
	}

	public List<String> getOutputAsList() {
		return getOutputAsList("\n");
	}

	public List<String> getOutputAsList(String delimiter) {
		return Arrays.asList(StringUtils.split(getOutput(), delimiter));
	}

	public Map<String, String> getOutputAsMap(String keyValueDelimiter) {
		return getOutputAsMap(keyValueDelimiter, "\n");
	}

	public Map<String, String> getOutputAsMap(String keyValueDelimiter, String entryDelimiter) {
		Map<String, String> map = new HashMap<>();

		getOutputAsList(entryDelimiter).forEach(entry -> {
			String[] parsedEntry = StringUtils.split(entry, keyValueDelimiter, 2);
			map.put(parsedEntry[0], parsedEntry.length > 1 ? parsedEntry[1] : null);
		});

		return map;
	}
}
