/**
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
package org.jboss.intersmash.application.openshift;

import java.util.Collections;
import java.util.Map;

import org.jboss.intersmash.provision.openshift.template.OpenShiftTemplate;

/**
 * This interface is not supposed to be implemented by user Applications. See the "Mapping of implemented provisioners"
 * section of Appsint README.md file for the up-to-date list of supported end users Applications.
 */
public interface DBTemplateOpenShiftApplication<TT extends OpenShiftTemplate>
		extends TemplateApplication<TT> {

	@Override
	default Map<String, String> getParameters() {
		return Collections.emptyMap();
	}
}
