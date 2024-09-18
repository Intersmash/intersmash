/**
 * Copyright (C) 2024 Red Hat, Inc.
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
package org.jboss.intersmash.provision.ai;

/**
 * Provisioner for deploying and undeploying Open Data Hub prerequisites
 */
public interface OpenDataHubPrerequisitesProvisioner {
	/**
	 * Task which will be performed by a provisioner prior to {@link #deploy()} operation.
	 */
	default void preDeploy() {
	}

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
	default void postUndeploy() {
	}

	/**
	 * Defines an operation for configuring the provisioner, before any deployment related tasks are executed, e.g.:
	 * for Operator based provisioners this allows for configuring custom catalog sources etc.
	 */
	default void configure() {
	}

	/**
	 * Defines an operation for configuring the provisioner, after all deploy related tasks are executed, e.g.:
	 * for Operator based provisioners this allows for removing custom catalog sources etc.
	 */
	default void dismiss() {
	}
}
