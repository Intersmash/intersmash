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
package org.jboss.intersmash.application.openshift;

import org.jboss.intersmash.provision.openshift.MysqlImageOpenShiftProvisioner;

/**
 * End user Application interface which presents MYSQL image application on OpenShift Container Platform.
 *
 * MYSQL application that is supposed to run on OpenShift needs to implement this interface.
 * Usage:
 * <pre>
 *     &#064;Intersmash(
 *           &#064;Service(MysqlApp.class)
 * )
 * </pre>
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link MysqlImageOpenShiftProvisioner}</li>
 * </ul>
 */
public interface MysqlImageOpenShiftApplication extends DBImageOpenShiftApplication {

	default String getName() {
		return "mysql";
	}

}
