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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.assertj.core.util.Strings;
import org.jboss.intersmash.deployments.IntersmashSharedDeploymentsProperties;
import org.jboss.intersmash.deployments.WildflyDeploymentApplicationConfiguration;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Build;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Deploy;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Env;
import org.jboss.intersmash.model.helm.charts.values.wildfly.HelmWildflyRelease;
import org.jboss.intersmash.model.helm.charts.values.wildfly.S2i;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.helm.HelmChartRelease;
import org.jboss.intersmash.tools.application.openshift.helm.WildflyHelmChartOpenShiftApplication;

import io.fabric8.kubernetes.api.model.Secret;

public class WildflyHelmChartOpenShiftExampleApplication
		implements WildflyHelmChartOpenShiftApplication, WildflyDeploymentApplicationConfiguration {
	private static final String APP_NAME = "wildfly-helm-helloworld-qs";

	private final HelmChartRelease release;

	public WildflyHelmChartOpenShiftExampleApplication() {
		this.release = new HelmChartRelease(loadRelease());
	}

	private HelmWildflyRelease loadRelease() {
		HelmWildflyRelease release = new HelmWildflyRelease();
		// let's compute some additional maven args for our s2i build to happen on a Pod
		String mavenAdditionalArgs = "-Denforcer.skip=true";
		// let's add configurable deployment additional args:
		mavenAdditionalArgs = mavenAdditionalArgs.concat(generateAdditionalMavenArgs());
		// let's pass the profile for building the deployment too...
		mavenAdditionalArgs = mavenAdditionalArgs.concat(
				(Strings.isNullOrEmpty(IntersmashSharedDeploymentsProperties.getWildflyDeploymentsBuildProfile()) ? ""
						: " -Pwildfly-deployments-build."
								+ IntersmashSharedDeploymentsProperties.getWildflyDeploymentsBuildProfile()));
		// ok, let's configure the release via the fluent(-ish) API
		List<Env> environmentVariables = new ArrayList();
		release
				.withBuild(
						new Build()
								.withUri(IntersmashConfig.deploymentsRepositoryUrl())
								.withRef(IntersmashConfig.deploymentsRepositoryRef())
								.withContextDir("deployments/openshift-jakarta-sample-standalone")
								.withEnv(
										Arrays.asList(
												new Env()
														.withName("MAVEN_ARGS_APPEND")
														.withValue(mavenAdditionalArgs)))
								.withS2i(
										new S2i()
												.withBuilderImage(IntersmashConfig.wildflyImageURL())
												.withRuntimeImage(IntersmashConfig.wildflyRuntimeImageURL())))
				.withDeploy(new Deploy().withReplicas(1));
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
