/**
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
package org.jboss.intersmash.provision.helm;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Image {

	private final From from;
	private final List<Path> paths;

	@Getter
	@Setter
	@AllArgsConstructor
	public static class From {
		private final String kind;
		private final String namespace;
		private final String name;

	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class Path {
		private final String sourcePath;
		private final String destinationDir;
	}
}
