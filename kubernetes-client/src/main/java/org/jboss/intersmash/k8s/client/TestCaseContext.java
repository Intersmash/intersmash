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
package org.jboss.intersmash.k8s.client;

public class TestCaseContext {

	/**
	 *
	 * This allows to track currently running test case for correct namespace mapping. This is used to automatically find
	 * namespace for running test case when
	 * creating {@link Kubernetes} instances.
	 */
	private static String runningTestCaseName;

	/**
	 * @return test case name associated with current thread or null if not such mapping exists, for example for com.SmokeTest
	 *         returns SmokeTest
	 */
	public static String getRunningTestCaseName() {
		return runningTestCaseName;
	}

	public static void setRunningTestCase(String currentlyRunningTestCaseName) {
		runningTestCaseName = currentlyRunningTestCaseName;
	}
}
