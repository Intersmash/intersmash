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
package org.jboss.intersmash.provision.operator;

import org.jboss.intersmash.application.Application;
import org.jboss.intersmash.application.openshift.OpenShiftApplication;
import org.jboss.intersmash.application.operator.HyperfoilOperatorApplication;
import org.jboss.intersmash.provision.ProvisionerFactory;
import org.jboss.intersmash.provision.k8s.HyperfoilKubernetesOperatorProvisioner;
import org.jboss.intersmash.provision.openshift.HyperfoilOpenShiftOperatorProvisioner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HyperfoilOperatorProvisionerFactory implements ProvisionerFactory<HyperfoilOperatorProvisioner> {

	@Override
	public HyperfoilOperatorProvisioner getProvisioner(Application application) {
		if (HyperfoilOperatorApplication.class.isAssignableFrom(application.getClass())) {
			if (OpenShiftApplication.class.isAssignableFrom(application.getClass())) {
				return new HyperfoilOpenShiftOperatorProvisioner((HyperfoilOperatorApplication) application);
			} else {
				return new HyperfoilKubernetesOperatorProvisioner((HyperfoilOperatorApplication) application);
			}
		}
		return null;
	}
}
