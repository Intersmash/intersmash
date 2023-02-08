package org.jboss.intersmash.tools.provision.openshift;

import java.util.Map;

import cz.xtf.core.openshift.OpenShift;

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
