/**
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
package org.jboss.intersmash.tools.application.openshift.helm;

import java.nio.file.Path;

/**
 * Defines the contract for implementors that represent a Helm Chart release values data structure which can be
 * serialized to a file.
 */
public interface SerializableHelmChartRelease {
	/**
	 * Implementors must generate the file by serializing the data at each call and reflect the information held by
	 * the concrete instance
	 * @return Instance of {@link Path} that represents the generated file location
	 */
	Path toValuesFile();
}
