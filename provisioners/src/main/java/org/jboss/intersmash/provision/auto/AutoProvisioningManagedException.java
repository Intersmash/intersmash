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
package org.jboss.intersmash.provision.auto;

import org.jboss.intersmash.application.openshift.AutoProvisioningOpenShiftApplication;
import org.jboss.intersmash.provision.Provisioner;
import org.jboss.intersmash.provision.openshift.auto.OpenShiftAutoProvisioner;

/**
 * {@link RuntimeException} class that is meant to wrap any exception thrown by the
 * {@link AutoProvisioningOpenShiftApplication} which is delegated
 * for the provisioning workflow execution.
 *
 * Since this is a {@code RuntimeException}, it is not needed for the caller - e.g.: provisioner - methods signature to
 * declare it in their {@code throws} clause, thus keeping the implementation compliant with the
 * {@link Provisioner} contract.
 *
 * {@link OpenShiftAutoProvisioner} implementation uses this class in order to wrap the exceptions which are bubbling
 * up from the delegated application code execution in a common way, so that the call code (e.g.: test classes) can
 * handle them appropriately.
 */
public class AutoProvisioningManagedException extends RuntimeException {
	public AutoProvisioningManagedException(Throwable cause) {
		super(cause);
	}
}
