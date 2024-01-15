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
package org.jboss.intersmash.provision.helm.wildfly;

import org.jboss.intersmash.application.Application;
import org.jboss.intersmash.application.openshift.helm.WildflyHelmChartOpenShiftApplication;
import org.jboss.intersmash.provision.ProvisionerFactory;
import org.jboss.intersmash.provision.helm.HelmChartOpenShiftProvisioner;

import lombok.extern.slf4j.Slf4j;

/**
 * A provisioner factory that will return a Helm Charts based provisioner for Wildfly, see
 * {@link WildflyHelmChartOpenShiftProvisioner}
 */
@Slf4j
public class WildflyHelmChartOpenShiftProvisionerFactory implements ProvisionerFactory<HelmChartOpenShiftProvisioner> {

	@Override
	public HelmChartOpenShiftProvisioner getProvisioner(Application application) {
		if (WildflyHelmChartOpenShiftApplication.class.isAssignableFrom(application.getClass())) {
			return new WildflyHelmChartOpenShiftProvisioner((WildflyHelmChartOpenShiftApplication) application);
		}
		return null;
	}
}
