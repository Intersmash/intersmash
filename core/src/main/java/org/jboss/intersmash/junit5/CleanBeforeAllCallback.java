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
package org.jboss.intersmash.junit5;

import org.jboss.intersmash.tools.client.OpenShifts;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 callback that cleans the OpenShift namespace before all tests in a class.
 * Activated by the {@link CleanBeforeAll} annotation.
 */
public class CleanBeforeAllCallback implements BeforeAllCallback {
	@Override
	public void beforeAll(ExtensionContext context) {
		if (context.getRequiredTestClass().isAnnotationPresent(CleanBeforeAll.class)) {
			OpenShifts.master().clean().waitFor();
		}
	}
}
