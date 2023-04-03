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
package org.jboss.intersmash.tools.provision.openshift.auto;

import java.net.URL;
import java.util.List;

import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.auto.AutoProvisioningExecutionException;
import org.jboss.intersmash.tools.application.openshift.AutoProvisioningOpenShiftApplication;
import org.jboss.intersmash.tools.provision.auto.AutoProvisioningManagedException;
import org.jboss.intersmash.tools.provision.openshift.OpenShiftProvisioner;

import io.fabric8.kubernetes.api.model.Pod;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements an OpenShift provisioner that delegates the real job of any phase in the testing workflow to the
 * {@link Application} class, which must be implement {@link AutoProvisioningOpenShiftApplication}
 */
@Slf4j
public final class OpenShiftAutoProvisioner implements OpenShiftProvisioner<AutoProvisioningOpenShiftApplication> {

	private final AutoProvisioningOpenShiftApplication configuredApplication;

	public OpenShiftAutoProvisioner(AutoProvisioningOpenShiftApplication application) {
		configuredApplication = application;
	}

	/**
	 * Access the {@link AutoProvisioningOpenShiftApplication} instance provisioned by this
	 * {@link OpenShiftAutoProvisioner}.
	 */
	public AutoProvisioningOpenShiftApplication getApplication() {
		return configuredApplication;
	}

	/**
	 * Task which will be performed by a provisioner prior to {@link #deploy()} operation.
	 */
	@Override
	public void preDeploy() {
		try {
			configuredApplication.preDeploy();
		} catch (AutoProvisioningExecutionException e) {
			log.debug(String.format("\"PreDeploy\" method failed: %s", e.getMessage()));
			throw new AutoProvisioningManagedException(e);
		}
	}

	/**
	 * Deploy the application.
	 */
	public void deploy() {
		try {
			configuredApplication.deploy();
		} catch (AutoProvisioningExecutionException e) {
			log.debug(String.format("\"Deploy\" method failed: %s", e.getMessage()));
			throw new AutoProvisioningManagedException(e);
		}
	}

	/**
	 * Undeploy the application.
	 */
	public void undeploy() {
		try {
			configuredApplication.undeploy();
		} catch (AutoProvisioningExecutionException e) {
			log.debug(String.format("\"Undeploy\" method failed: %s", e.getMessage()));
			throw new AutoProvisioningManagedException(e);
		}
	}

	/**
	 * Task which will be performed by a provisioner after the {@link #undeploy()} operation.
	 */
	public void postUndeploy() {
		try {
			configuredApplication.postUndeploy();
		} catch (AutoProvisioningExecutionException e) {
			log.debug(String.format("\"PostUndeploy\" method failed: %s", e.getMessage()));
			throw new AutoProvisioningManagedException(e);
		}
	}

	/**
	 * Get the URL for the application endpoint - depending on the provisioner, this could be BareMetal process,
	 * OpenShift route, ...
	 *
	 * The more specific we go in provisioner hierarchy, the more specific the implementation will be.
	 *
	 * @return Application endpoint
	 */
	public URL getURL() {
		try {
			return configuredApplication.getURL();
		} catch (AutoProvisioningExecutionException e) {
			log.debug(String.format("\"GetUrl\" method failed: %s", e.getMessage()));
			throw new AutoProvisioningManagedException(e);
		}
	}

	/**
	 * Returns a list of {@link Pod} instances, related to the application service which is being deployed
	 * <b>Currently, the method is not implemented and will throw {@link UnsupportedOperationException} </b>.
	 * For this reason it has been marked as {@code Deprecated}, although that is not accurate in this case - so that
	 * developers are aware of it.
	 *
	 * This could change in the future, by hooking somehow to the OpenShiftApplication logic as well.
	 * Currently, the feature is not affected by any limitation because of it, e.g. the test class could have its own
	 * methods for retrieving the pods
	 *
	 * @return {@code List<Pod>} belonging to the application service scope
	 * @throws UnsupportedOperationException Not implemented yet.
	 */
	@Deprecated
	@Override
	public List<Pod> getPods() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		try {
			configuredApplication.scale(replicas, wait);
		} catch (AutoProvisioningExecutionException e) {
			log.debug(String.format("\"Scale\" method failed: %s", e.getMessage()));
			throw new AutoProvisioningManagedException(e);
		}
	}
}
