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

import org.slf4j.event.Level;

public class WaitingConfig {
	public static final String WAITING_TIMEOUT = "intersmash.waiting.timeout";
	public static final String WAITING_LOG_LEVEL = "intersmash.waiting.log.level";
	public static final String WAITING_TIMEOUT_CLEANUP = "intersmash.waiting.timeout.cleanup";
	public static final String WAITING_BUILD_TIMEOUT = "intersmash.waiting.build.timeout";

	private static final String WAITING_TIMEOUT_DEFAULT = "180000";
	private static final String WAITING_LOG_LEVEL_DEFAULT = "INFO";
	private static final String WAITING_TIMEOUT_CLEANUP_DEFAULT = "20000";
	private static final String WAITING_BUILD_TIMEOUT_DEFAULT = "600000";

	public static long timeout() {
		return Long.parseLong(IntersmashProperties.get(WAITING_TIMEOUT,
				IntersmashProperties.get("xtf.waiting.timeout", WAITING_TIMEOUT_DEFAULT)));
	}

	public static Level level() {
		return Level.valueOf(IntersmashProperties.get(WAITING_LOG_LEVEL,
				IntersmashProperties.get("xtf.waiting.log.level", WAITING_LOG_LEVEL_DEFAULT)).toUpperCase());
	}

	public static long timeoutCleanup() {
		return Long.parseLong(IntersmashProperties.get(WAITING_TIMEOUT_CLEANUP,
				IntersmashProperties.get("xtf.waiting.timeout.cleanup", WAITING_TIMEOUT_CLEANUP_DEFAULT)));
	}

	public static long buildTimeout() {
		return Long.parseLong(IntersmashProperties.get(WAITING_BUILD_TIMEOUT,
				IntersmashProperties.get("xtf.waiting.build.timeout", WAITING_BUILD_TIMEOUT_DEFAULT)));
	}
}
