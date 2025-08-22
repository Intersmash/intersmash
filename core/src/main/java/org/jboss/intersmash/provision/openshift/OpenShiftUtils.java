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
package org.jboss.intersmash.provision.openshift;

import java.util.Map;

import cz.xtf.core.openshift.OpenShift;

/**
 * Helper class that leverages the XTF library {@link OpenShift} APIs in methods that can be used to control the
 * Intersmash provisioning workflow.
 */
public class OpenShiftUtils {
	public static void deleteResourcesWithLabel(OpenShift openShift, String labelKey, String labelValue) {
		openShift.deploymentConfigs().withLabel(labelKey, labelValue).delete();

		openShift.templates().withLabel(labelKey, labelValue).delete();
		openShift.apps().deployments().withLabel(labelKey, labelValue).delete();
		openShift.apps().replicaSets().withLabel(labelKey, labelValue).delete();
		openShift.apps().statefulSets().withLabel(labelKey, labelValue).delete();
		openShift.batch().jobs().withLabel(labelKey, labelValue).delete();
		openShift.deploymentConfigs().withLabel(labelKey, labelValue).delete();
		openShift.replicationControllers().withLabel(labelKey, labelValue).delete();
		openShift.buildConfigs().withLabel(labelKey, labelValue).delete();
		openShift.imageStreams().withLabel(labelKey, labelValue).delete();
		openShift.endpoints().withLabel(labelKey, labelValue).delete();
		openShift.services().withLabel(labelKey, labelValue).delete();
		openShift.builds().withLabel(labelKey, labelValue).delete();
		openShift.routes().withLabel(labelKey, labelValue).delete();
		openShift.pods().withLabel(labelKey, labelValue).delete();
		openShift.persistentVolumeClaims().withLabel(labelKey, labelValue).delete();
		openShift.autoscaling().v1().horizontalPodAutoscalers().withLabel(labelKey, labelValue).delete();
		openShift.configMaps().withLabel(labelKey, labelValue).delete();
		openShift.rbac().roles().withLabel(labelKey, labelValue).delete();
	}

	public static void deleteResourcesWithLabels(OpenShift openShift, Map<String, String> labels) {
		labels.forEach((key, value) -> deleteResourcesWithLabel(openShift, key, value));
	}
}
