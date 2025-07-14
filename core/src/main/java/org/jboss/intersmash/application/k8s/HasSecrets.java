/*
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
package org.jboss.intersmash.application.k8s;

import java.util.List;

import io.fabric8.kubernetes.api.model.Secret;

/**
 * Providers serving Application implementing this interface are capable of managing the Secret resources.
 *
 * This interface is not supposed to be implemented by user Applications. See the "Mapping of implemented provisioners"
 * section of Intersmash README.md file for the up-to-date list of supported end users Applications.
 */
public interface HasSecrets {

	/**
	 * Get {@link Secret} objects associated with an application. It's a task for provider to deploy and undeploy the
	 * resource on tested cluster as a part of standard application lifecycle.
	 *
	 * @return Secret resources associated with Application
	 */
	List<Secret> getSecrets();
}
