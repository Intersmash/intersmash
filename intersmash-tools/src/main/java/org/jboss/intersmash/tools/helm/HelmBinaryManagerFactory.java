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

import java.util.Arrays;

public enum HelmBinaryManagerFactory {

	INSTANCE;

	private volatile String resolvedPath;

	public String getHelmBinaryPath() {
		String localRef = resolvedPath;
		if (localRef == null) {
			synchronized (HelmBinaryManagerFactory.class) {
				localRef = resolvedPath;
				if (localRef == null) {
					for (HelmBinaryPathResolver resolver : Arrays.asList(
							new ConfiguredPathHelmBinaryResolver(),
							new ClusterVersionBasedHelmBinaryPathResolver())) {
						String path = resolver.resolve();
						if (path != null) {
							resolvedPath = localRef = path;
							break;
						}
					}
				}
			}
		}
		return localRef;
	}
}
