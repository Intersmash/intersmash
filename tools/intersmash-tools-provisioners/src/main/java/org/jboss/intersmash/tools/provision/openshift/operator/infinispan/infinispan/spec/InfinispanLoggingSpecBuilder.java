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
package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

import java.util.Map;

public final class InfinispanLoggingSpecBuilder {
	private Map<String, String> categories;

	/**
	 * Names logging categories and levels
	 *
	 * @param categories Map storing logging categories and levels
	 * @return this
	 */
	public InfinispanLoggingSpecBuilder categories(Map<String, String> categories) {
		this.categories = categories;
		return this;
	}

	public InfinispanLoggingSpec build() {
		InfinispanLoggingSpec infinispanLoggingSpec = new InfinispanLoggingSpec();
		infinispanLoggingSpec.setCategories(categories);
		return infinispanLoggingSpec;
	}
}
