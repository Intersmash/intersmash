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

import java.net.URL;

import org.jboss.intersmash.tools.application.auto.AutoProvisioningExecutionException;

/**
 * Interface representing a self provisioning capable Application on OpenShift Container Platform.
 *
 * Concrete implementations of this interface implement the various steps of the provisioning workflow through their
 * own methods.
 */
public interface AutoProvisioningOpenShiftApplication extends OpenShiftApplication {

	/**
	 * Perform tasks before deploying the application/service (e.g.: create secrets, config maps etc.)
	 * @throws AutoProvisioningExecutionException when something goes wrong during this phase
	 */
	default void preDeploy() throws AutoProvisioningExecutionException {
	}

	/**
	 * Deploy the application.
	 * @throws AutoProvisioningExecutionException when something goes wrong during this phase
	 */
	default void deploy() throws AutoProvisioningExecutionException {
	}

	/**
	 * Undeploy the application.
	 * @throws AutoProvisioningExecutionException when something goes wrong during this phase
	 */
	default void undeploy() throws AutoProvisioningExecutionException {
	}

	/**
	 * Task which will be performed by a provisioner after the {@link #undeploy()} operation.
	 * @throws AutoProvisioningExecutionException when something goes wrong during this phase
	 */
	default void postUndeploy() throws AutoProvisioningExecutionException {
	}

	/**
	 * Get the URL for the application endpoint - depending on the provisioner, this could be BareMetal process,
	 * OpenShift route, ...
	 *
	 * The more specific we go in provisioner hierarchy, the more specific the implementation will be.
	 *
	 * @return Application endpoint
	 * @throws AutoProvisioningExecutionException when something goes wrong during this phase
	 */
	URL getURL() throws AutoProvisioningExecutionException;

	/**
	 * Scales the application/service pods
	 *
	 * @param replicas Integer, the number of desired replicas after the scaling operation has completed
	 * @param wait Boolean, whether to wait for the scaling process to be finished
	 * @throws AutoProvisioningExecutionException when something goes wrong during this phase
	 */
	default void scale(int replicas, boolean wait) throws AutoProvisioningExecutionException {
	}
}
