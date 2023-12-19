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
package org.jboss.intersmash.testsuite.provision.openshift;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.deployments.WildflyDeploymentApplicationConfiguration;
import org.jboss.intersmash.model.helm.charts.values.eap8.HelmEap8Release;
import org.jboss.intersmash.model.helm.charts.values.wildfly.HelmWildflyRelease;
import org.jboss.intersmash.testsuite.IntersmashTestsuiteProperties;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.helm.HelmChartRelease;
import org.jboss.intersmash.tools.application.openshift.helm.WildflyHelmChartOpenShiftApplication;
import org.jboss.intersmash.tools.provision.helm.HelmChartReleaseAdapter;
import org.jboss.intersmash.tools.provision.helm.wildfly.WildFlyHelmChartReleaseAdapter;
import org.jboss.intersmash.tools.provision.helm.wildfly.eap8.Eap8HelmChartReleaseAdapter;

import io.fabric8.kubernetes.api.model.Secret;

public class WildflyHelmChartExistingValuesOpenShiftExampleApplication
		implements WildflyHelmChartOpenShiftApplication, WildflyDeploymentApplicationConfiguration {
	private static final String APP_NAME = "wildfly-helm-helloworld-qs";

	private final HelmChartRelease release;
	private final Map<String, String> setOverrides = new HashMap<>();

	public WildflyHelmChartExistingValuesOpenShiftExampleApplication() {
		this.release = loadRelease();
	}

	WildflyHelmChartExistingValuesOpenShiftExampleApplication addSetOverride(String name, String value) {
		setOverrides.put(name, value);
		return this;
	}

	@Override
	public Map<String, String> getSetOverrides() {
		return setOverrides;
	}

	private HelmChartRelease loadRelease() {
		if (IntersmashTestsuiteProperties.isCommunityTestExecutionProfileEnabled()) {
			HelmWildflyRelease helmRelease = HelmChartReleaseAdapter.<HelmWildflyRelease> fromValuesFile(
					this.getClass().getResource("wildfly-helm-values.yaml"), HelmWildflyRelease.class);
			return new WildFlyHelmChartReleaseAdapter(helmRelease)
					.withJdk17BuilderImage(IntersmashConfig.wildflyImageURL())
					.withJdk17RuntimeImage(IntersmashConfig.wildflyRuntimeImageURL());
		} else if (IntersmashTestsuiteProperties.isProductizedTestExecutionProfileEnabled()) {
			HelmEap8Release helmRelease = HelmChartReleaseAdapter.<HelmEap8Release> fromValuesFile(
					this.getClass().getResource("eap8-helm-values.yaml"), HelmEap8Release.class);
			return new Eap8HelmChartReleaseAdapter(helmRelease)
					.withJdk17BuilderImage(IntersmashConfig.wildflyImageURL())
					.withJdk17RuntimeImage(IntersmashConfig.wildflyRuntimeImageURL());
		} else
			throw new IllegalStateException("Not a valid testing profile!");
	}

	@Override
	public HelmChartRelease getRelease() {
		return release;
	}

	@Override
	public List<Secret> getSecrets() {
		return Collections.emptyList();
	}

	@Override
	public String getName() {
		return APP_NAME;
	}

	@Override
	public String getHelmChartsRepositoryUrl() {
		return IntersmashConfig.getWildflyHelmChartsRepo();
	}

	@Override
	public String getHelmChartsRepositoryRef() {
		return IntersmashConfig.getWildflyHelmChartsBranch();
	}

	@Override
	public String getHelmChartsRepositoryName() {
		return IntersmashConfig.getWildflyHelmChartsName();
	}

	@Override
	public String getBuilderImage() {
		return IntersmashConfig.wildflyImageURL();
	}

	@Override
	public String getRuntimeImage() {
		return IntersmashConfig.wildflyRuntimeImageURL();
	}
}
