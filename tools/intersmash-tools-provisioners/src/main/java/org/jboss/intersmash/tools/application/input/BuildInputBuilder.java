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
package org.jboss.intersmash.tools.application.input;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class BuildInputBuilder implements GitSourceBuilder, BinarySourceBuilder {
	private static Path DEPLOYMENTS_DIRECTORY;

	private String uri;
	private String ref;
	private String contextDir;
	private Path archive;

	static {
		try {
			DEPLOYMENTS_DIRECTORY = Files.createDirectories(Paths.get("target", "deployments"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public GitSourceBuilder uri(String uri) {
		this.uri = uri;
		return this;
	}

	@Override
	public GitSourceBuilder ref(String ref) {
		this.ref = ref;
		return this;
	}

	@Override
	public GitSourceBuilder contextDir(String contextDir) {
		this.contextDir = contextDir;
		return this;
	}

	@Override
	public BinarySourceBuilder archive(Path archive) {
		this.archive = archive;
		return this;
	}

	@Override
	public BinarySourceBuilder archive(WebArchive webArchive) {
		final File war = new File(DEPLOYMENTS_DIRECTORY.toFile(), webArchive.getName());
		webArchive.as(ZipExporter.class).exportTo(war, true);
		this.archive = war.toPath();
		return this;
	}

	@Override
	public BuildInput build() {
		if (uri != null) {
			return new GitSource() {
				@Override
				public String getUri() {
					return uri;
				}

				@Override
				public String getRef() {
					return ref;
				}

				@Override
				public String getContextDir() {
					return contextDir;
				}
			};
		} else {
			return (BinarySource) () -> archive;
		}
	}
}
