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
package org.jboss.intersmash.tools.application.openshift;

import org.jboss.intersmash.tools.application.openshift.template.Eap7Template;
import org.jboss.intersmash.tools.provision.openshift.Eap7TemplateOpenShiftProvisioner;

/**
 * End user Application descriptor interface which presents EAP 7 template application on OpenShift Container Platform.
 *
 * See {@link Eap7Template} for available templates the
 * application can represent.
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link Eap7TemplateOpenShiftProvisioner}</li>
 * </ul>
 */
public interface Eap7TemplateOpenShiftApplication
		extends WildflyOpenShiftApplication, TemplateApplication<Eap7Template> {

}
