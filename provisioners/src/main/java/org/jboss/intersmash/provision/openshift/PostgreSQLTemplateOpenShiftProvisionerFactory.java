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
import org.jboss.intersmash.application.openshift.PostgreSQLTemplateOpenShiftApplication;
import org.jboss.intersmash.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides logic to obtain a {@link PostgreSQLTemplateOpenShiftProvisioner} instance that is initialized with a
 * given {@link PostgreSQLTemplateOpenShiftApplication} instance.
 */
@Slf4j
public class PostgreSQLTemplateOpenShiftProvisionerFactory
		implements ProvisionerFactory<PostgreSQLTemplateOpenShiftProvisioner> {

	@Override
	public PostgreSQLTemplateOpenShiftProvisioner getProvisioner(Application application) {
		if (PostgreSQLTemplateOpenShiftApplication.class.isAssignableFrom(application.getClass()))
			return new PostgreSQLTemplateOpenShiftProvisioner((PostgreSQLTemplateOpenShiftApplication) application);
		return null;
	}
}
