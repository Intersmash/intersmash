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
import org.jboss.intersmash.tools.waiting.Waiter;

import io.fabric8.kubernetes.api.builder.Visitor;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages builds in a dedicated namespace, handling build lifecycle (deploy, update, delete)
 * and providing references to completed build output images.
 */
@Slf4j
public class BuildManager {

	private static final String INTERSMASH_MANAGED_LABEL = "intersmash/managed";

	private final OpenShift openShift;

	public BuildManager(OpenShift openShift) {
		this.openShift = openShift;

		if (openShift.getProject(openShift.getNamespace()) == null) {
			openShift.createProjectRequest();
			openShift.waiters().isProjectReady().waitFor();

			try {
				OpenShifts.admin(openShift.getNamespace()).namespaces().withName(openShift.getNamespace())
						.edit(new Visitor<NamespaceBuilder>() {
							@Override
							public void visit(NamespaceBuilder builder) {
								builder.editMetadata()
										.addToLabels(INTERSMASH_MANAGED_LABEL, "true");
							}
						});
			} catch (KubernetesClientException e) {
				log.warn("Couldn't assign label '{}' to the new project '{}'. Possible cause: insufficient permissions.",
						INTERSMASH_MANAGED_LABEL, openShift.getNamespace());
				log.debug(e.getMessage());
			}
		}

		String pullSecret = IntersmashProperties.get("intersmash.openshift.pullsecret",
				IntersmashProperties.get("xtf.openshift.pullsecret"));
		if (pullSecret != null) {
			openShift.setupPullSecret(pullSecret);
		}

		String mainNamespace = IntersmashProperties.get("intersmash.openshift.namespace",
				IntersmashProperties.get("xtf.openshift.namespace", ""));
		if (!BuildManagerConfig.namespace().equals(mainNamespace)) {
			openShift.addRoleToGroup("system:image-puller", "ClusterRole", "system:authenticated");
		}
	}

	public ManagedBuildReference deploy(ManagedBuild managedBuild) {
		if (BuildManagerConfig.skipRebuild()) {
			log.info("Skip rebuilding is enabled... Skipping rebuild of '{}'.", managedBuild.getId());
		} else if (BuildManagerConfig.forceRebuild()) {
			log.info("Force rebuilding is enabled... Building '{}' ...", managedBuild.getId());
			managedBuild.delete(openShift);
			managedBuild.build(openShift);
		} else if (!managedBuild.isPresent(openShift)) {
			log.info("Managed build '{}' is not present... Building...", managedBuild.getId());
			managedBuild.build(openShift);
		} else if (managedBuild.needsUpdate(openShift)) {
			log.info("Managed build '{}' is not up to date... Building...", managedBuild.getId());
			managedBuild.update(openShift);
		} else {
			log.info("Managed build '{}' is up to date.", managedBuild.getId());
		}

		return getBuildReference(managedBuild);
	}

	public Waiter hasBuildCompleted(ManagedBuild managedBuild) {
		return managedBuild.hasCompleted(openShift);
	}

	public ManagedBuildReference getBuildReference(ManagedBuild managedBuild) {
		return new ManagedBuildReference(managedBuild.getId(), "latest", openShift.getNamespace());
	}

	public OpenShift openShift() {
		return openShift;
	}
}
