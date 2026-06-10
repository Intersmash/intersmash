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
package org.jboss.intersmash.tools.build;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigSpecBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Binary build from source directory. The sources are uploaded and built inside the builder image.
 */
@Slf4j
public class BinarySourceBuild extends BinaryBuildFromSources {

	public BinarySourceBuild(String builderImage, Path path, Map<String, String> envProperties, String id) {
		super(builderImage, path, envProperties, id);
	}

	@Override
	protected void configureBuildStrategy(BuildConfigSpecBuilder builder, String builderImage, List<EnvVar> env) {
		builder.withNewStrategy().withType("Source").withNewSourceStrategy().withEnv(env).withForcePull(true).withNewFrom()
				.withKind("DockerImage").withName(builderImage).endFrom().endSourceStrategy().endStrategy();
	}

	@Override
	protected String getImage(BuildConfig bc) {
		return bc.getSpec().getStrategy().getSourceStrategy().getFrom().getName();
	}

	@Override
	protected List<EnvVar> getEnv(BuildConfig buildConfig) {
		return buildConfig.getSpec().getStrategy().getSourceStrategy().getEnv();
	}
}
