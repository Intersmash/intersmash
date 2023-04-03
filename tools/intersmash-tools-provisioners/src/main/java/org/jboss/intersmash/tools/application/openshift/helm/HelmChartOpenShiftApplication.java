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
package org.jboss.intersmash.tools.application.openshift.helm;

import org.jboss.intersmash.tools.application.openshift.HasSecrets;
import org.jboss.intersmash.tools.application.openshift.OpenShiftApplication;
import org.jboss.intersmash.tools.provision.helm.HelmChartOpenShiftProvisioner;

/**
 * Contract to implement descriptors for any application services that will be provisioned through Helm Charts by
 * {@link HelmChartOpenShiftProvisioner}
 */
public interface HelmChartOpenShiftApplication extends OpenShiftApplication, HasSecrets {

	/**
	 * Instance of {@link HelmChartRelease} that defines the actual release
	 * @return Instance of {@link HelmChartRelease} that defines the actual release
	 */
	HelmChartRelease getRelease();

	/**
	 * The Helm Charts repo URL
	 * @return A string that identifies the used Helm Charts repository URL
	 */
	String getHelmChartsRepositoryUrl();

	/**
	 * The Helm Charts repo URL
	 * @return A string that identifies the used Helm Charts repository branch
	 */
	String getHelmChartsRepositoryRef();

	/**
	 * The Helm Charts repo URL
	 * @return A string that identifies the used Helm Charts repository name
	 */
	String getHelmChartsRepositoryName();
}
