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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.util.Strings;
import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.openshift.helm.HelmChartRelease;
import org.jboss.intersmash.application.openshift.helm.WildflyHelmChartOpenShiftApplication;
import org.jboss.intersmash.model.helm.charts.values.eap8.HelmEap8Release;
import org.jboss.intersmash.model.helm.charts.values.eap81.HelmEap81Release;
import org.jboss.intersmash.model.helm.charts.values.wildfly.HelmWildflyRelease;
import org.jboss.intersmash.model.helm.charts.values.xp5.HelmXp5Release;
import org.jboss.intersmash.model.helm.charts.values.xp6.HelmXp6Release;
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

public class WildflyHelmChartOpenShiftExampleApplication
		implements WildflyHelmChartOpenShiftApplication, WildflyDeploymentApplicationConfiguration {
	private static final String APP_NAME = "wildfly-helm-helloworld-qs";

	private final HelmChartRelease release;

	public WildflyHelmChartOpenShiftExampleApplication() {
		if (IntersmashTestsuiteProperties.isCommunityTestExecutionProfileEnabled()) {
			release = loadRelease(new WildFlyHelmChartReleaseAdapter(new HelmWildflyRelease()));
		} else if (IntersmashTestsuiteProperties.isProductizedTestExecutionProfileEnabled()) {
			if (TestDeploymentProperties.isEap80DeploymentsBuildStreamEnabled()) {
				release = loadRelease(new Eap8HelmChartReleaseAdapter(new HelmEap8Release()));
			} else if (TestDeploymentProperties.isEap81DeploymentsBuildStreamEnabled()) {
				release = loadRelease(new Eap81HelmChartReleaseAdapter(new HelmEap81Release()));
			} else if (TestDeploymentProperties.isEapXp5DeploymentsBuildStreamEnabled()) {
				release = loadRelease(new EapXp5HelmChartReleaseAdapter(new HelmXp5Release()));
			} else if (TestDeploymentProperties.isEapXp6DeploymentsBuildStreamEnabled()) {
				release = loadRelease(new EapXp6HelmChartReleaseAdapter(new HelmXp6Release()));
			} else
				throw new IllegalStateException("Not a valid WildFly deployments stream!");
		} else
			throw new IllegalStateException("Not a valid testing profile!");
	}

	private HelmChartRelease loadRelease(final WildflyHelmChartRelease release) {
		// let's compute some additional maven args for our s2i build to happen on a Pod
		String mavenAdditionalArgs = "-Denforcer.skip=true";
		// let's add configurable deployment additional args:
		mavenAdditionalArgs = mavenAdditionalArgs.concat(generateAdditionalMavenArgs());
		// let's pass the profile for building the deployment too...
		mavenAdditionalArgs = mavenAdditionalArgs.concat(
				(Strings.isNullOrEmpty(TestDeploymentProperties.getWildflyDeploymentsBuildProfile()) ? ""
						: " -Pts.wildfly.target-distribution."
								+ TestDeploymentProperties.getWildflyDeploymentsBuildProfile()));
		// ok, let's configure the release via the WildflyHelmChartRelease fluent(-ish) API,
		// which offers a common reference for both WildFly and EAP (latest)
		release
				.withSourceRepositoryUrl(IntersmashConfig.deploymentsRepositoryUrl())
				.withSourceRepositoryRef(IntersmashConfig.deploymentsRepositoryRef())
				.withContextDir("testsuite/deployments/openshift-jakarta-sample-standalone")
				.withJdk17BuilderImage(IntersmashConfig.wildflyImageURL())
				.withJdk17RuntimeImage(IntersmashConfig.wildflyRuntimeImageURL())
				.withBuildEnvironmentVariable("MAVEN_ARGS_APPEND", mavenAdditionalArgs);
		List<String> channelDefinition = Arrays.asList(this.eeChannelGroupId(), this.eeChannelArtifactId(),
				this.eeChannelVersion());
		if (!channelDefinition.isEmpty()) {
			// an example of EAP channel usage, not working with EAP 7.4.x or WildFly
			release.withS2iChannel(channelDefinition.stream().collect(Collectors.joining(":")));
		}
		return release;
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
