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
package org.jboss.intersmash.application.operator;

import io.opendatahub.datasciencecluster.v1.DataScienceCluster;
import io.opendatahub.dscinitialization.v1.DSCInitialization;

/**
 * End user Application interface which presents an Open Data Hub/OpenShift AI operator application.
 * Only relevant model APIs are currently exposed, more would be added on demand
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link org.jboss.intersmash.provision.operator.OpenDataHubOperatorProvisioner}</li>
 * </ul>
 */
public interface OpenDataHubOperatorApplication extends OperatorApplication {

	/**
	 * Provides read access to the deployed {@link DataScienceCluster} instance.
	 * @return A {@link DataScienceCluster} instance that is deployed.
	 */
	DataScienceCluster getDataScienceCluster();

	/**
	 * Provides read access to the {@link DSCInitialization} instance that should be used to initialize the
	 * deployed {@link DataScienceCluster}, represented by {@link #getDataScienceCluster} getter.
	 * @return A {@link DSCInitialization} instance that configures the deployed {@link DataScienceCluster}
	 */
	DSCInitialization getDSCInitialization();
}
