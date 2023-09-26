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

package org.jboss.jaxws;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.settings.building.SettingsBuildingException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jboss.intersmash.deployments.util.maven.ArtifactProvider;
import org.jboss.intersmash.tools.application.openshift.BootableJarOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.input.BinarySource;

import cz.xtf.builder.builders.SecretBuilder;
import cz.xtf.builder.builders.secret.SecretType;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Secret;

public class SoapWildflyBootableOpenShiftJarApplication implements BootableJarOpenShiftApplication {
	private String GROUPID = "org.jboss.intersmash";
	private String ARTIFACTID = "jaxws";
	private String VERSION = "0.0.1-SNAPSHOT";
	static final String BOOTABLE_JAR_ARTIFACT_PACKAGING = "jar";
	static final String ARTIFACT_CLASSIFIER = "bootable-openshift";

	static final EnvVar TEST_ENV_VAR = new EnvVarBuilder().withName("test-evn-key").withValue("test-evn-value").build();
	static final String TEST_SECRET_FOO = "foo";
	static final String TEST_SECRET_BAR = "bar";
	static final Secret TEST_SECRET = new SecretBuilder("test-secret")
			.setType(SecretType.OPAQUE).addData(TEST_SECRET_FOO, TEST_SECRET_BAR.getBytes()).build();

	@Override
	public BinarySource getBuildInput() {
		Path file = null;
		try {
			file = ArtifactProvider.resolveArtifact(
					GROUPID,
					ARTIFACTID,
					VERSION,
					BOOTABLE_JAR_ARTIFACT_PACKAGING,
					ARTIFACT_CLASSIFIER).toPath();
		} catch (SettingsBuildingException | ArtifactResolutionException e) {
			throw new RuntimeException("Can not get artifact", e);
		}
		return new BinarySourceImpl(file);
	}

	@Override
	public List<Secret> getSecrets() {
		List<Secret> secrets = new ArrayList<>();
		secrets.add(TEST_SECRET);
		return Collections.unmodifiableList(secrets);
	}

	@Override
	public List<EnvVar> getEnvVars() {
		List<EnvVar> list = new ArrayList<>();
		list.add(new EnvVarBuilder().withName(TEST_ENV_VAR.getName())
				.withValue(TEST_ENV_VAR.getValue()).build());
		return Collections.unmodifiableList(list);
	}

	@Override
	public String getName() {
		return "ws-bootable-openshift-jar";
	}

	// todo remove local class impl once intersmash issue #85 is resolved
	class BinarySourceImpl implements BinarySource {
		Path f;

		public BinarySourceImpl(Path f) {
			this.f = f;
		}

		public Path getArchive() {
			return f;
		}
	}

}
