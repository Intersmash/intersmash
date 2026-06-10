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
package org.jboss.intersmash.tools.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class IntersmashProperties {

	private static final Properties properties = new Properties();

	private static final String TEST_PROPERTIES_PATH = "intersmash.test_properties.path";
	private static final String GLOBAL_TEST_PROPERTIES_PATH = "intersmash.global_test_properties.path";

	static {
		loadConfig();
	}

	public static void loadConfig() {
		properties.clear();
		Path globalPropertiesPath = resolvePropertiesPath(
				System.getProperty(GLOBAL_TEST_PROPERTIES_PATH,
						System.getProperty("xtf.global_test_properties.path", "global-test.properties")));
		Path testPropertiesPath = resolvePropertiesPath(
				System.getProperty(TEST_PROPERTIES_PATH,
						System.getProperty("xtf.test_properties.path", "test.properties")));
		properties.putAll(getPropertiesFromPath(globalPropertiesPath));
		properties.putAll(getPropertiesFromPath(testPropertiesPath));
		properties.putAll(System.getenv().entrySet().stream()
				.collect(Collectors.toMap(
						e -> "intersmash." + e.getKey().replaceAll("_", ".").toLowerCase(),
						Map.Entry::getValue)));
		properties.putAll(System.getProperties());
	}

	public static String get(String property) {
		return properties.getProperty(property);
	}

	public static String get(String property, String fallbackValue) {
		return properties.getProperty(property, fallbackValue);
	}

	static void setProperty(String property, String value) {
		properties.setProperty(property, value);
	}

	private static Path getProjectRoot() {
		Path dir = Paths.get("").toAbsolutePath();
		while (dir.getParent() != null && dir.getParent().resolve("pom.xml").toFile().exists())
			dir = dir.getParent();
		return dir;
	}

	private static Path resolvePropertiesPath(String path) {
		if (Paths.get(path).toFile().exists()) {
			return Paths.get(path);
		}
		return getProjectRoot().resolve(path);
	}

	private static Properties getPropertiesFromPath(Path path) {
		Properties props = new Properties();
		if (Files.isReadable(path)) {
			try (InputStream is = Files.newInputStream(path)) {
				props.load(is);
			} catch (final IOException ex) {
				log.warn("Unable to read properties from '{}'", path, ex);
			}
		}
		return props;
	}
}
