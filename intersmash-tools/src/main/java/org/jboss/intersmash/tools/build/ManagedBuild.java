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

import org.jboss.intersmash.tools.client.OpenShift;
import org.jboss.intersmash.tools.waiting.Waiter;

/**
 * Interface for builds managed by the {@link BuildManager}.
 */
public interface ManagedBuild {

	String getId();

	void build(OpenShift openShift);

	void update(OpenShift openShift);

	void delete(OpenShift openShift);

	boolean isPresent(OpenShift openShift);

	boolean needsUpdate(OpenShift openShift);

	Waiter hasCompleted(OpenShift openShift);
}
