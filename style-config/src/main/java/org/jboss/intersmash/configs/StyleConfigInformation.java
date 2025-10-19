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
package org.jboss.intersmash.configs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Provides methods to access checkstyle configuration.
 */
public class StyleConfigInformation {

	/**
	 * Get the contents of the {@code eclipse-formatter.xml} file
	 * @return a string representing the contents of the {@code eclipse-formatter.xml} file
	 * @throws IOException If the {@code eclipse-formatter.xml} resource is not available
	 */
	public String getEclipseFormatterContents() throws IOException {
		InputStream resourceAsStream = StyleConfigInformation.class.getClassLoader().getResourceAsStream(
				"org/jboss/intersmash/configs/eclipse-formatter.xml");
		if (resourceAsStream == null) {
			throw new IllegalStateException("\"eclipse-formatter.xml\" contents not found.");
		}
		try (BufferedReader bufferedReadr = new BufferedReader(
				new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
			return bufferedReadr.lines().toString();
		}
	}
}
