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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.intersmash.tools.client.OpenShift;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigSpecBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Binary build that expects a file on path that shall be uploaded as {@code ROOT.(suffix)}.
 * {@code suffix} is war, jar, etc. and is extracted from the filename.
 */
@Slf4j
public class BinaryBuildFromFile extends BinaryBuild {

	public BinaryBuildFromFile(String builderImage, Path path, Map<String, String> envProperties, String id) {
		super(builderImage, path, envProperties, id);
	}

	protected void configureBuildStrategy(BuildConfigSpecBuilder builder, String builderImage, List<EnvVar> env) {
		builder.withNewStrategy().withType("Source").withNewSourceStrategy().withEnv(env).withForcePull(true).withNewFrom()
				.withKind("DockerImage").withName(builderImage).endFrom().endSourceStrategy().endStrategy();
	}

	protected String getImage(BuildConfig bc) {
		return bc.getSpec().getStrategy().getSourceStrategy().getFrom().getName();
	}

	protected List<EnvVar> getEnv(BuildConfig buildConfig) {
		return buildConfig.getSpec().getStrategy().getSourceStrategy().getEnv();
	}

	@Override
	public void build(OpenShift openShift) {
		openShift.imageStreams().create(is);
		openShift.buildConfigs().create(bc);
		String fileName = getPath().getFileName().toString();
		if (fileName.matches(".*(\\.\\w+)$")) {
			fileName = "ROOT" + fileName.replaceFirst(".*(\\.\\w+)$", "$1");
		}
		openShift.buildConfigs().withName(bc.getMetadata().getName())
				.instantiateBinary()
				.asFile(fileName)
				.fromFile(getPath().toFile());
	}

	protected String getContentHash() {
		if (!isCached() || contentHash == null) {
			try (InputStream i = Files.newInputStream(getPath())) {
				contentHash = Hex.encodeHexString(DigestUtils.sha256(i)).substring(0, 63);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return contentHash;
	}
}
