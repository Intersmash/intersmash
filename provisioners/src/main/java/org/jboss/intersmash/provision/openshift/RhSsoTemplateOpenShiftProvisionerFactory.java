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
package org.jboss.intersmash.provision.openshift;

import org.jboss.intersmash.application.Application;
import org.jboss.intersmash.application.openshift.RhSsoTemplateOpenShiftApplication;
import org.jboss.intersmash.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Deprecated(since = "0.0.2")
public class RhSsoTemplateOpenShiftProvisionerFactory implements ProvisionerFactory<RhSsoTemplateOpenShiftProvisioner> {

	@Override
	public RhSsoTemplateOpenShiftProvisioner getProvisioner(Application application) {
		if (RhSsoTemplateOpenShiftApplication.class.isAssignableFrom(application.getClass()))
			return new RhSsoTemplateOpenShiftProvisioner((RhSsoTemplateOpenShiftApplication) application);
		return null;
	}
}
