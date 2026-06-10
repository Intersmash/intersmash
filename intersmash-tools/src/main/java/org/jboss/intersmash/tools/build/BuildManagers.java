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

import org.jboss.intersmash.tools.client.OpenShift;
import org.jboss.intersmash.tools.client.OpenShifts;
import org.jboss.intersmash.tools.config.IntersmashProperties;

import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.ResourceQuotaBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;

/**
 * Singleton access point for the {@link BuildManager}.
 */
@Slf4j
public class BuildManagers {

	private static BuildManager bm;

	public static BuildManager get() {
		if (bm == null) {
			synchronized (BuildManager.class) {
				if (bm == null) {
					String buildNamespace = BuildManagerConfig.namespace();

					bm = new BuildManager(OpenShifts.master(buildNamespace));

					// If the build namespace is in a separate namespace to "master" namespace, we assume it is a shared namespace
					String mainNamespace = IntersmashProperties.get("intersmash.openshift.namespace",
							IntersmashProperties.get("xtf.openshift.namespace", ""));
					if (!BuildManagerConfig.namespace().equals(mainNamespace)) {
						OpenShift admin = OpenShifts.admin(buildNamespace);

						try {
							if (admin.getResourceQuota("max-running-builds") == null) {
								ResourceQuota rq = new ResourceQuotaBuilder()
										.withNewMetadata().withName("max-running-builds").endMetadata()
										.withNewSpec()
										.addToHard("pods",
												new Quantity(String.format("%d", BuildManagerConfig.maxRunningBuilds())))
										.endSpec()
										.build();
								admin.createResourceQuota(rq);
							}
						} catch (KubernetesClientException e) {
							log.warn("Attempt to add hard resource quota on {} namespace failed!", buildNamespace);
						}
					}
				}
			}
		}
		return bm;
	}
}
