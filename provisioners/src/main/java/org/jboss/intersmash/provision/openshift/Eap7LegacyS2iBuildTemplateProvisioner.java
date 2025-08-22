/*
 * Copyright (C) 2025 Red Hat, Inc.
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
package org.jboss.intersmash.provision.openshift;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jboss.intersmash.application.openshift.Eap7LegacyS2iBuildTemplateApplication;
import org.slf4j.event.Level;

import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.Template;
import lombok.extern.slf4j.Slf4j;

/**
 * Provisioner used process and deploy eap-s2i-build template to OpenShift Container Platform cluster.
 *
 * @see Eap7LegacyS2iBuildTemplateApplication
 */
@Slf4j
public class Eap7LegacyS2iBuildTemplateProvisioner implements OpenShiftProvisioner<Eap7LegacyS2iBuildTemplateApplication> {
	// we can parametrize this one once required, but its location should be static
	private String EAP_S2I_BUILD = "https://raw.githubusercontent.com/jboss-container-images/jboss-eap-openshift-templates/master/eap-s2i-build.yaml";
	private KubernetesList deployedResources;
	private Eap7LegacyS2iBuildTemplateApplication application;
	private FailFastCheck ffCheck = () -> false;
	private Template template;

	public Eap7LegacyS2iBuildTemplateProvisioner(Eap7LegacyS2iBuildTemplateApplication application) {
		this.application = application;
	}

	@Override
	public Eap7LegacyS2iBuildTemplateApplication getApplication() {
		return application;
	}

	@Override
	public void deploy() {
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				application.getName());

		if (application.getParameters().getOrDefault("APPLICATION_IMAGE", "null") != application.getName()) {
			throw new IllegalArgumentException("APPLICATION_IMAGE template parameters has to match the application name!");
		}
		try {
			template = openShift.templates().load(new URL(EAP_S2I_BUILD)).item();
		} catch (IOException e) {
			throw new RuntimeException("Failed to load eap-s2i-build template from " + EAP_S2I_BUILD, e);
		}
		template.setApiVersion("template.openshift.io/v1");
		openShift.createTemplate(template);
		deployedResources = openShift.processTemplate(template.getMetadata().getName(),
				application.getParameters());
		// add additional environment variables to the build config resources
		deployedResources.getItems().stream()
				.filter(hasMetadata -> hasMetadata.getKind().equals(BuildConfig.class.getSimpleName()))
				.forEach(hasMetadata -> setEnvToBuildConfig((BuildConfig) hasMetadata));
		openShift.createResources(deployedResources);

		// two build configs has to be built (builder/runtime) in order to proceed
		waitForBuilds();
	}

	private void setEnvToBuildConfig(BuildConfig buildConfig) {
		// Update Git based build config
		if (buildConfig.getSpec().getStrategy().getSourceStrategy() != null) {
			if (buildConfig.getSpec().getStrategy().getSourceStrategy().getEnv() == null) {
				buildConfig.getSpec().getStrategy().getSourceStrategy().setEnv(application.getEnvVars());
			} else {
				buildConfig.getSpec().getStrategy().getSourceStrategy().getEnv().addAll(application.getEnvVars());
			}
		}
		// Update dockerile based build config
		if (buildConfig.getSpec().getStrategy().getDockerStrategy() != null) {
			if (buildConfig.getSpec().getStrategy().getDockerStrategy().getEnv() == null) {
				buildConfig.getSpec().getStrategy().getDockerStrategy().setEnv(application.getEnvVars());
			} else {
				buildConfig.getSpec().getStrategy().getDockerStrategy().getEnv().addAll(application.getEnvVars());
			}
		}
	}

	private void waitForBuilds() {
		OpenShiftWaiters.get(openShift, ffCheck).hasBuildCompleted(application.getName() + "-build-artifacts")
				.level(Level.DEBUG)
				.waitFor();
		OpenShiftWaiters.get(openShift, ffCheck).hasBuildCompleted(application.getName())
				.level(Level.DEBUG)
				.waitFor();
	}

	@Override
	public void undeploy() {
		openShift.deleteResources(deployedResources);
		openShift.deleteTemplate(template);
	}

	@Override
	public URL getURL() {
		throw new UnsupportedOperationException("No route for eap-s2i-build template instance.");
	}

	@Override
	public List<Pod> getPods() {
		throw new UnsupportedOperationException(
				"eap-s2i-build does not provide any running pods - all pods are completed once builds are done.");
	}

	@Override
	public void scale(int replicas, boolean wait) {
		throw new UnsupportedOperationException("eap-s2i-build template is not suppose to be scaled - no running pods.");
	}
}
