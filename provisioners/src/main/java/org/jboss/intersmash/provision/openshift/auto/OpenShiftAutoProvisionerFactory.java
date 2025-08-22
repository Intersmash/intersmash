/*
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
package org.jboss.intersmash.provision.openshift.auto;

import org.jboss.intersmash.application.Application;
import org.jboss.intersmash.application.openshift.AutoProvisioningOpenShiftApplication;
import org.jboss.intersmash.provision.ProvisionerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * This factory is responsible for recognizing test classes that use auto (or application dictated, or declarative)
 * provisioning, i.e. classes that implement
 * {@link AutoProvisioningOpenShiftApplication}
 */
@Slf4j
public class OpenShiftAutoProvisionerFactory implements ProvisionerFactory<OpenShiftAutoProvisioner> {

	@Override
	public OpenShiftAutoProvisioner getProvisioner(Application application) {
		if (AutoProvisioningOpenShiftApplication.class.isAssignableFrom(application.getClass()))
			return new OpenShiftAutoProvisioner((AutoProvisioningOpenShiftApplication) application);
		return null;
	}
}
