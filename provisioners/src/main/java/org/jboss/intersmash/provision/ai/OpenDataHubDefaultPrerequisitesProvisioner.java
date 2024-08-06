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
package org.jboss.intersmash.provision.ai;

import java.util.List;

import org.jboss.intersmash.application.openshift.AuthorinoOperatorApplication;
import org.jboss.intersmash.provision.Provisioner;
import org.jboss.intersmash.provision.ProvisionerManager;

import io.kuadrant.authorino.operator.v1beta1.Authorino;
import io.kuadrant.authorino.v1beta2.AuthConfig;

/**
 *
 */
public class OpenDataHubDefaultPrerequisitesProvisioner implements OpenDataHubPrerequisitesProvisioner {
	static final String OPENSHIFT_AI_DEFAULT_PREREQUISITES_AUTHORINO_APP_NAME = "intersmash-odh-authorino";
	private static final AuthorinoOperatorApplication authorinoOperatorApplication = new AuthorinoOperatorApplication() {

		@Override
		public Authorino getAuthorino() {
			return null;
		}

		@Override
		public List<AuthConfig> getAuthConfigs() {
			return List.of();
		}

		@Override
		public String getName() {
			return OPENSHIFT_AI_DEFAULT_PREREQUISITES_AUTHORINO_APP_NAME;
		}
	};
	private final Provisioner authorinoOperatorProvisioner;

	public OpenDataHubDefaultPrerequisitesProvisioner() {
		this.authorinoOperatorProvisioner = ProvisionerManager.getProvisioner(authorinoOperatorApplication);
	}

	@Override
	public void deploy() {
		authorinoOperatorProvisioner.deploy();
	}

	@Override
	public void undeploy() {
		authorinoOperatorProvisioner.undeploy();
	}
}
