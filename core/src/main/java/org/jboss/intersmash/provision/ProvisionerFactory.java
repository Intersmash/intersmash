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
package org.jboss.intersmash.provision;

import org.jboss.intersmash.application.Application;

/**
 * Define the contract for classes that return a suitable {@link Provisioner} for a given {@link Application} class.
 * @param <T> The type of the {@link Provisioner} that the factory implementation shall return.
 */
public interface ProvisionerFactory<T extends Provisioner> {

	/**
	 * @param application A concrete {@link Application} instance that represents the application/service to be
	 *                       provisioned
	 * @return provisioner for {@link Application} or null
	 */
	T getProvisioner(Application application);
}
