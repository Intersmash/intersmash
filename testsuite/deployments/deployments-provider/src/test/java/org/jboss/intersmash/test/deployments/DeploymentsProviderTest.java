/*
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
package org.jboss.intersmash.test.deployments;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeploymentsProviderTest {

	@Test
	public void testWildflyBootableJarIsAvailable() {
		assertBootableJarIsAvailable(DeploymentsProvider.wildflyBootableJarOpenShiftDeployment());
	}

	@Test
	public void testEap7BootableJarIsAvailable() {
		assertBootableJarIsAvailable(DeploymentsProvider.wildflyBootableJarOpenShiftDeployment());
	}

	private static void assertBootableJarIsAvailable(final Path openShiftApp) {
		Assertions.assertNotNull(openShiftApp);
		Assertions.assertTrue(Files.exists(openShiftApp));
	}
}
