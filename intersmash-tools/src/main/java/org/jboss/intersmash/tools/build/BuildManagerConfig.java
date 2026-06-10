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
package org.jboss.intersmash.tools.build;

import org.jboss.intersmash.tools.config.IntersmashProperties;

/**
 * Configuration for the build manager, controlling build namespace, rebuild policies, and resource limits.
 */
public class BuildManagerConfig {

	public static final String BUILD_NAMESPACE = "intersmash.bm.namespace";
	public static final String FORCE_REBUILD = "intersmash.bm.force_rebuild";
	public static final String SKIP_REBUILD = "intersmash.bm.skip_rebuild";
	public static final String MAX_RUNNING_BUILDS = "intersmash.bm.max_running_builds";
	public static final String MEMORY_REQUEST = "intersmash.bm.memory.request";
	public static final String MEMORY_LIMIT = "intersmash.bm.memory.limit";

	private static String getWithXtfFallback(String intersmashKey, String xtfKey) {
		String value = IntersmashProperties.get(intersmashKey);
		if (value == null && xtfKey != null) {
			value = IntersmashProperties.get(xtfKey);
		}
		return value;
	}

	private static String getWithXtfFallback(String intersmashKey, String xtfKey, String defaultValue) {
		String value = getWithXtfFallback(intersmashKey, xtfKey);
		return value != null ? value : defaultValue;
	}

	public static String namespace() {
		return getWithXtfFallback(BUILD_NAMESPACE, "xtf.bm.namespace", "intersmash-builds");
	}

	public static boolean forceRebuild() {
		return Boolean.parseBoolean(getWithXtfFallback(FORCE_REBUILD, "xtf.bm.force_rebuild", "false"));
	}

	public static boolean skipRebuild() {
		return Boolean.parseBoolean(getWithXtfFallback(SKIP_REBUILD, "xtf.bm.skip_rebuild", "false"));
	}

	public static int maxRunningBuilds() {
		return Integer.parseInt(getWithXtfFallback(MAX_RUNNING_BUILDS, "xtf.bm.max_running_builds", "5"));
	}

	/**
	 * Memory request for builds (e.g., "2Gi", "512Mi")
	 *
	 * @return memory request string or null if not configured
	 */
	public static String memoryRequest() {
		return getWithXtfFallback(MEMORY_REQUEST, "xtf.bm.memory.request");
	}

	/**
	 * Memory limit for builds (e.g., "4Gi", "1Gi")
	 *
	 * @return memory limit string or null if not configured
	 */
	public static String memoryLimit() {
		return getWithXtfFallback(MEMORY_LIMIT, "xtf.bm.memory.limit");
	}
}
