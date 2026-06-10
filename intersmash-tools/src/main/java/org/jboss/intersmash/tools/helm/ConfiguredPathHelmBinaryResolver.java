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

import org.jboss.intersmash.tools.config.IntersmashProperties;

class ConfiguredPathHelmBinaryResolver implements HelmBinaryPathResolver {

	private static final String HELM_BINARY_PATH = "intersmash.helm.binary.path";
	private static final String XTF_HELM_BINARY_PATH = "xtf.helm.binary.path";

	@Override
	public String resolve() {
		String path = IntersmashProperties.get(HELM_BINARY_PATH);
		if (path == null) {
			path = IntersmashProperties.get(XTF_HELM_BINARY_PATH);
		}
		return path;
	}
}
