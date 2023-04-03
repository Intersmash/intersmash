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
package org.jboss.intersmash.tools.util.wildfly;

import java.util.List;

/**
 * Class provide supports for building CLI commands that are used to configure WILDFLY on OpenShift via custom scripts and
 * config map or on Baremetal
 */
public class WildflyCliScriptBuilder extends WildflyAbstractCliScriptBuilder {
	public List<String> build() {
		return build(null);
	}
}
