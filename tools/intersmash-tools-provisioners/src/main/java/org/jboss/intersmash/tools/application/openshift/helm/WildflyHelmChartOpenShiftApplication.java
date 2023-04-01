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

/**
 * A contract that defines data that an WILDFLY Helm Chart provisioned application service should expose
 */
public interface WildflyHelmChartOpenShiftApplication extends HelmChartOpenShiftApplication {

	/**
	 * The builder image that should be used by the WILDFLY Helm Charts release
	 * @return A string representing the builder image that will be used by the WILDFLY Helm Charts release
	 */
	String getBuilderImage();

	/**
	 * The runtime image that should be used by the WILDFLY Helm Charts release
	 * @return A string representing the runtime image that will be used by the WILDFLY Helm Charts release
	 */
	String getRuntimeImage();
}
