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
package org.jboss.intersmash.provision.openshift;

import org.jboss.intersmash.application.operator.OpenDataHubOperatorApplication;
import org.jboss.intersmash.provision.operator.OpenDataHubOperatorProvisioner;
import org.jboss.intersmash.provision.operator.model.odh.DSCInitializationList;
import org.jboss.intersmash.provision.operator.model.odh.DataScienceClusterList;
import org.jboss.intersmash.provision.operator.model.odh.FeatureTrackerList;
import org.jboss.intersmash.provision.operator.model.odh.MonitoringList;

import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClientAdapter;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.opendatahub.datasciencecluster.v1.DataScienceCluster;
import io.opendatahub.dscinitialization.v1.DSCInitialization;
import io.opendatahub.features.v1.FeatureTracker;
import io.opendatahub.platform.services.v1alpha1.Monitoring;
import lombok.NonNull;

/**
 * A {@link org.jboss.intersmash.provision.Provisioner} that extends {@link OpenDataHubOperatorProvisioner} and
 * is able to deploy the Open Data Hub Operator on OpenShift specifically.
 */
public class OpenDataHubOpenShiftOperatorProvisioner
		// leverage Open Data Hub common Operator based provisioner behavior
		extends OpenDataHubOperatorProvisioner<NamespacedOpenShiftClient>
		// ... and common OpenShift provisioning logic, too
		implements OpenShiftProvisioner<OpenDataHubOperatorApplication> {

	private static final String OPENSHIFT_OPERATORS_NAMESPACE = "openshift-operators";

	public OpenDataHubOpenShiftOperatorProvisioner(
			@NonNull OpenDataHubOperatorApplication application) {
		super(application);
	}

	@Override
	protected String getTargetNamespace() {
		return OPENSHIFT_OPERATORS_NAMESPACE;
	}

	@Override
	public NamespacedKubernetesClientAdapter<NamespacedOpenShiftClient> client() {
		return OpenShifts.admin();
	}

	@Override
	public String execute(String... args) {
		return OpenShiftProvisioner.super.execute(args);
	}

	// =================================================================================================================
	// Related to generic provisioning behavior
	// =================================================================================================================
	@Override
	protected void removeClusterServiceVersion() {
		this.execute("delete", "csvs", currentCSV, "-n", this.getTargetNamespace(), "--ignore-not-found");
	}

	@Override
	protected void removeSubscription() {
		this.execute("delete", "subscription", packageManifestName, "-n", this.getTargetNamespace(), "--ignore-not-found");
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}

	@Override
	protected HasMetadataOperationsImpl<DataScienceCluster, DataScienceClusterList> dataScienceClusterCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts.admin().newHasMetadataOperation(crdc, DataScienceCluster.class, DataScienceClusterList.class);
	}

	@Override
	protected HasMetadataOperationsImpl<DSCInitialization, DSCInitializationList> dscInitializationCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts.admin().newHasMetadataOperation(crdc, DSCInitialization.class, DSCInitializationList.class);
	}

	@Override
	protected HasMetadataOperationsImpl<FeatureTracker, FeatureTrackerList> featureTrackerCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts.admin().newHasMetadataOperation(crdc, FeatureTracker.class, FeatureTrackerList.class);
	}

	@Override
	protected HasMetadataOperationsImpl<Monitoring, MonitoringList> monitoringCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts.admin().newHasMetadataOperation(crdc, Monitoring.class, MonitoringList.class);
	}
}
