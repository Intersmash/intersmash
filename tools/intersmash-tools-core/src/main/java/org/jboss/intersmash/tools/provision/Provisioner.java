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

import java.net.URL;

import org.jboss.intersmash.tools.application.Application;

/**
 * Application provisioner for deploying and undeploying the application.
 */
public interface Provisioner<T extends Application> {

	/**
	 * Get the {@link Application} provisioned by this {@link Provisioner}.
	 * @return a concrete instance of type {@link Application}
	 */
	T getApplication();

	/**
	 * Task which will be performed by a provisioner prior to {@link #deploy()} operation.
	 */
	void preDeploy();

	/**
	 * Deploy the application.
	 */
	void deploy();

	/**
	 * Undeploy the application.
	 */
	void undeploy();

	/**
	 * Task which will be performed by a provisioner after the {@link #undeploy()} operation.
	 */
	void postUndeploy();

	/**
	 * Get the URL for the application endpoint - depending on the provisioner, this could be BareMetal process,
	 * OpenShift route, ...
	 *
	 * The more specific we go in provisioner hierarchy, the more specific the implementation will be.
	 *
	 * @return Application endpoint
	 */
	URL getURL();

	default void configure() {
		;
	}
}
