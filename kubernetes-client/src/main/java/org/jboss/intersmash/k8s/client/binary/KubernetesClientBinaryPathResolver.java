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
package org.jboss.intersmash.k8s.client.binary;

/**
 * Defines a contract for resolving the path of a Kubernetes client binary, which concrete implementations will
 * base upon different criteria.
 */
public interface KubernetesClientBinaryPathResolver {
	String BINARY_NAME = "kubectl";
	String LOCAL_BINARY_CLIENT_TMP_DIR = "tmp/kubectl";

	/**
	 * Resolves Kubernetes client binary path
	 *
	 * @return Kubernetes client binary path
	 */
	String resolve();
}
