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
package org.jboss.intersmash.application.auto;

import org.jboss.intersmash.application.openshift.AutoProvisioningOpenShiftApplication;

/**
 * Exceptions that happen during the auto provisioning workflow execution
 *
 * {@link AutoProvisioningOpenShiftApplication} provides a way to plug
 * in methods that will implement the provisioning workflow steps.
 * Any code could be executed this way and any error could happen.
 * By defining that {@link AutoProvisioningOpenShiftApplication}
 * methods can throw this exception type, the framework requires for implementations to wrap any original error with
 * the {@code AutoProvisioningException} type.
 * This should allow the for caller code (e.g. test classes, provisioners) to have a common logical interface to handle
 * and interpret errors thrown by implementation code appropriately.
 */
public class AutoProvisioningExecutionException extends Exception {
	public AutoProvisioningExecutionException(Throwable cause) {
		super(cause);
	}
}
