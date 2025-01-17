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

import org.assertj.core.util.Strings;
import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.openshift.helm.HelmChartRelease;
import org.jboss.intersmash.application.openshift.helm.WildflyHelmChartOpenShiftApplication;
import org.jboss.intersmash.model.helm.charts.values.eap8.HelmEap8Release;
import org.jboss.intersmash.model.helm.charts.values.eap81.HelmEap81Release;
import org.jboss.intersmash.model.helm.charts.values.wildfly.HelmWildflyRelease;
import org.jboss.intersmash.model.helm.charts.values.xp5.HelmXp5Release;
import org.jboss.intersmash.model.helm.charts.values.xp6.HelmXp6Release;
import org.jboss.intersmash.provision.helm.HelmChartReleaseAdapter;
import org.jboss.intersmash.provision.helm.wildfly.WildFlyHelmChartReleaseAdapter;
import org.jboss.intersmash.provision.helm.wildfly.WildflyHelmChartRelease;
import org.jboss.intersmash.provision.helm.wildfly.eap8.Eap8HelmChartReleaseAdapter;
import org.jboss.intersmash.provision.helm.wildfly.eap81.Eap81HelmChartReleaseAdapter;
import org.jboss.intersmash.provision.helm.wildfly.xp5.EapXp5HelmChartReleaseAdapter;
import org.jboss.intersmash.provision.helm.wildfly.xp6.EapXp6HelmChartReleaseAdapter;
import org.jboss.intersmash.test.deployments.TestDeploymentProperties;
import org.jboss.intersmash.test.deployments.WildflyDeploymentApplicationConfiguration;
import org.jboss.intersmash.testsuite.IntersmashTestsuiteProperties;

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
		WildflyHelmChartRelease wildflyHelmChartRelease;
		if (IntersmashTestsuiteProperties.isCommunityTestExecutionProfileEnabled()) {
			HelmWildflyRelease helmRelease = HelmChartReleaseAdapter.<HelmWildflyRelease> fromValuesFile(
					this.getClass().getResource("wildfly-helm-values.yaml"), HelmWildflyRelease.class);
			wildflyHelmChartRelease = new WildFlyHelmChartReleaseAdapter(helmRelease)
					.withJdk17BuilderImage(IntersmashConfig.wildflyImageURL())
					.withJdk17RuntimeImage(IntersmashConfig.wildflyRuntimeImageURL());
		} else if (IntersmashTestsuiteProperties.isProductizedTestExecutionProfileEnabled()) {
			// EAP 8.0
			if (TestDeploymentProperties.isEap80DeploymentsBuildStreamEnabled()) {
				HelmEap8Release helmRelease = HelmChartReleaseAdapter.<HelmEap8Release> fromValuesFile(
						this.getClass().getResource("eap8-helm-values.yaml"), HelmEap8Release.class);
				wildflyHelmChartRelease = new Eap8HelmChartReleaseAdapter(helmRelease)
						.withJdk17BuilderImage(IntersmashConfig.wildflyImageURL())
						.withJdk17RuntimeImage(IntersmashConfig.wildflyRuntimeImageURL());
			} // EAP 8.1
			else if (TestDeploymentProperties.isEap81DeploymentsBuildStreamEnabled()) {
				HelmEap81Release helmRelease = HelmChartReleaseAdapter.<HelmEap81Release> fromValuesFile(
						this.getClass().getResource("eap81-helm-values.yaml"), HelmEap81Release.class);
				wildflyHelmChartRelease = new Eap81HelmChartReleaseAdapter(helmRelease)
						.withJdk17BuilderImage(IntersmashConfig.wildflyImageURL())
						.withJdk17RuntimeImage(IntersmashConfig.wildflyRuntimeImageURL());
			} // EAP XP 5
			else if (TestDeploymentProperties.isEapXp5DeploymentsBuildStreamEnabled()) {
				HelmXp5Release helmRelease = HelmChartReleaseAdapter.<HelmXp5Release> fromValuesFile(
						this.getClass().getResource("xp5-helm-values.yaml"), HelmXp5Release.class);
				wildflyHelmChartRelease = new EapXp5HelmChartReleaseAdapter(helmRelease)
						.withJdk17BuilderImage(IntersmashConfig.wildflyImageURL())
						.withJdk17RuntimeImage(IntersmashConfig.wildflyRuntimeImageURL());
			} // EAP XP 6
			else if (TestDeploymentProperties.isEapXp6DeploymentsBuildStreamEnabled()) {
				HelmXp6Release helmRelease = HelmChartReleaseAdapter.<HelmXp6Release> fromValuesFile(
						this.getClass().getResource("xp6-helm-values.yaml"), HelmXp6Release.class);
				wildflyHelmChartRelease = new EapXp6HelmChartReleaseAdapter(helmRelease)
						.withJdk17BuilderImage(IntersmashConfig.wildflyImageURL())
						.withJdk17RuntimeImage(IntersmashConfig.wildflyRuntimeImageURL());
			} else
				throw new IllegalStateException(String.format("Not a valid WildFly deployments stream! (%s)",
						TestDeploymentProperties.getWildflyDeploymentsBuildStream()));
		} else
			throw new IllegalStateException(String.format("Not a valid testing profile! (%s)",
					IntersmashTestsuiteProperties.getTestExecutionProfile()));
		// let's compute some additional maven args for our s2i build to happen on a Pod
		String mavenAdditionalArgs = "-Denforcer.skip=true";
		// let's add configurable deployment additional args:
		mavenAdditionalArgs = mavenAdditionalArgs.concat(generateAdditionalMavenArgs());
		// let's pass the profile for building the deployment too...
		mavenAdditionalArgs = mavenAdditionalArgs.concat(
				(Strings.isNullOrEmpty(TestDeploymentProperties.getWildflyDeploymentsBuildProfile()) ? ""
						: " -Pts.wildfly.target-distribution."
								+ TestDeploymentProperties.getWildflyDeploymentsBuildProfile()));
		wildflyHelmChartRelease.setBuildEnvironmentVariables(Map.of("MAVEN_ARGS_APPEND", mavenAdditionalArgs));
		// let's pass the stream for building the deployment too...
		final String deploymentStream = TestDeploymentProperties.getWildflyDeploymentsBuildStream();
		if (!TestDeploymentProperties.WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_COMMUNITY.equals(deploymentStream)) {
			mavenAdditionalArgs = mavenAdditionalArgs.concat(
					(Strings.isNullOrEmpty(deploymentStream) ? ""
							: String.format(" -Pts.%s-stream.", getWildflyDeploymentVariantFromStream(deploymentStream))
							+ deploymentStream));
		}
		return wildflyHelmChartRelease;
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
