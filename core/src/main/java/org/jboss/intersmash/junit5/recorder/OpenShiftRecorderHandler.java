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
package org.jboss.intersmash.junit5.recorder;

import java.io.IOException;

import org.jboss.intersmash.tools.config.IntersmashProperties;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestWatcher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenShiftRecorderHandler implements TestWatcher, TestExecutionExceptionHandler, BeforeAllCallback,
		BeforeEachCallback, LifecycleMethodExecutionExceptionHandler {

	private final OpenShiftRecorderService openShiftRecorderService;

	public OpenShiftRecorderHandler() {
		openShiftRecorderService = new OpenShiftRecorderService();
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		openShiftRecorderService.initFilters(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		openShiftRecorderService.updateFilters(context);
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		try {
			openShiftRecorderService.recordState(context);
		} catch (Throwable t) {
			log.error("Error recording OpenShift state: ", t);
		} finally {
			throw throwable;
		}
	}

	@Override
	public void handleBeforeAllMethodExecutionException(final ExtensionContext context, final Throwable throwable)
			throws Throwable {
		try {
			if (recordBefore()) {
				openShiftRecorderService.recordState(context);
			}
		} catch (Throwable t) {
			log.error("Error recording OpenShift state: ", t);
		} finally {
			throw throwable;
		}
	}

	@Override
	public void handleBeforeEachMethodExecutionException(final ExtensionContext context, final Throwable throwable)
			throws Throwable {
		try {
			if (recordBefore()) {
				openShiftRecorderService.recordState(context);
			}
		} catch (Throwable t) {
			log.error("Error recording OpenShift state: ", t);
		} finally {
			throw throwable;
		}
	}

	@Override
	public void testSuccessful(ExtensionContext context) {
		if (recordAlways()) {
			try {
				openShiftRecorderService.recordState(context);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static boolean recordAlways() {
		return Boolean.parseBoolean(getProperty("intersmash.record.always", "xtf.record.always", "false"));
	}

	private static boolean recordBefore() {
		return Boolean.parseBoolean(getProperty("intersmash.record.before", "xtf.record.before", "false"));
	}

	private static String getProperty(String intersmashKey, String xtfKey, String defaultValue) {
		String value = IntersmashProperties.get(intersmashKey);
		if (value == null) {
			value = IntersmashProperties.get(xtfKey);
		}
		return value != null ? value : defaultValue;
	}
}
