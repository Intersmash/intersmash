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
package org.jboss.intersmash.provision.operator;

import org.jboss.intersmash.application.Application;
import org.jboss.intersmash.application.openshift.OpenShiftApplication;
import org.jboss.intersmash.application.operator.OpenDataHubOperatorApplication;
import org.jboss.intersmash.provision.ProvisionerFactory;
import org.jboss.intersmash.provision.openshift.OpenDataHubOpenShiftOperatorProvisioner;

import lombok.extern.slf4j.Slf4j;

/**
 * This factory is loaded by the Service loader via SPI and will release a concrete instance of
 * {@link OpenDataHubOperatorProvisioner} that is responsible for deploying an Open Data Hub application service
 * represented by {@link OpenDataHubOperatorApplication} instances.
 */
@Slf4j
public class OpenDataHubOperatorProvisionerFactory
		implements ProvisionerFactory<OpenDataHubOperatorProvisioner> {

	@Override
	public OpenDataHubOperatorProvisioner getProvisioner(Application application) {
		if (OpenDataHubOperatorApplication.class.isAssignableFrom(application.getClass())) {
			if (OpenShiftApplication.class.isAssignableFrom(application.getClass())) {
				return new OpenDataHubOpenShiftOperatorProvisioner((OpenDataHubOperatorApplication) application);
			}
			throw new UnsupportedOperationException(
					"Open Data Hub operator based provisioner requires OpenShift");
		}
		return null;
	}
}
