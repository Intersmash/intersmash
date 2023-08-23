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

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.deployments.IntersmashDelpoyableWildflyApplication;
import org.jboss.intersmash.model.helm.charts.values.wildfly.HelmWildflyRelease;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.helm.HelmChartRelease;
import org.jboss.intersmash.tools.application.openshift.helm.WildflyHelmChartOpenShiftApplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import io.fabric8.kubernetes.api.model.Secret;

public class WildflyHelmChartExistingValuesOpenShiftExampleApplicaton
		implements WildflyHelmChartOpenShiftApplication, IntersmashDelpoyableWildflyApplication {
	private static final String APP_NAME = "wildfly-helm-helloworld-qs";

	private final HelmChartRelease release;
	private final Map<String, String> setOverrides = new HashMap<>();

	public WildflyHelmChartExistingValuesOpenShiftExampleApplicaton() {
		this.release = new HelmChartRelease(loadRelease());
	}

	WildflyHelmChartExistingValuesOpenShiftExampleApplicaton addSetOverride(String name, String value) {
		setOverrides.put(name, value);
		return this;
	}

	@Override
	public Map<String, String> getSetOverrides() {
		return setOverrides;
	}

	private HelmWildflyRelease loadRelease() {
		URL url = this.getClass().getResource("wildfly-helm-values.yaml");
		if (url == null) {
			throw new IllegalStateException("No wildfly-helm-values.yaml found");
		}
		try {
			Path valuePath = Path.of(url.toURI());
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
			return mapper.readValue(valuePath.toFile(), HelmWildflyRelease.class);
		} catch (Error | RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	@Override
	public String bomServerVersionPropertyValue() {
		return IntersmashConfig.getWildflyBomsEeServerVersion();
	}

	@Override
	public String eeFeaturePackLocation() {
		// this value is supposed to be overridden by the CI Jenkins job by passing e.g.
		// "mvn ... -Dwildfly.ee-feature-pack.location="
		return IntersmashConfig.getWildflyEeFeaturePackLocation();
	}

	@Override
	public String cloudFeaturePackLocation() {
		// this value is supposed to be overridden by the CI Jenkins job by passing e.g.
		// "mvn ... -Dwildfly.cloud-feature-pack.location="
		return IntersmashConfig.getWildflyCloudFeaturePackLocation();
	}

	@Override
	public String eeChannelLocation() {
		// this value is supposed to be overridden by the CI Jenkins job by passing e.g.
		// "mvn ... -Dwildfly.ee-channel.location="
		return IntersmashConfig.getWildflyEeChannelLocation();
	}

	@Override
	public String wildflyMavenPluginGroupId() {
		return IntersmashConfig.getWildflyMavenPluginGroupId();
	}

	@Override
	public String wildflyMavenPluginArtifactId() {
		return IntersmashConfig.getWildflyMavenPluginArtifactId();
	}

	@Override
	public String wildflyMavenPluginVersion() {
		return IntersmashConfig.getWildflyMavenPluginVersion();
	}
}
