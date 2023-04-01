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
package org.jboss.intersmash.tools.provision;

import java.util.ServiceLoader;

import org.jboss.intersmash.tools.application.Application;

public class ProvisionerManager {

	/**
	 * Get application provisioner based on interfaces the application does implement.
	 *
	 * @param application  interoperability application to be provisioned
	 * @return provisioner fit for the application provisioning
	 * @throws UnsupportedOperationException in case that give application type is unsupported, or in case an exception is thrown while
	 *                   gathering the data required for proper provisioner selection
	 */
	public static Provisioner getProvisioner(Application application) {
		ServiceLoader<ProvisionerFactory> provisionerFactories = ServiceLoader.load(ProvisionerFactory.class);
		for (ProvisionerFactory pf : provisionerFactories) {
			Provisioner p = pf.getProvisioner(application);
			if (p != null)
				return p;
		}
		throw new UnsupportedOperationException("No suitable Provisioner found for application " + application.getClass());
	}
}
