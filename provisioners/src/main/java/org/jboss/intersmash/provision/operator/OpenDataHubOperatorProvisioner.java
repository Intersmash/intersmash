/*
 * Copyright (C) 2024 Red Hat, Inc.
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
package org.jboss.intersmash.provision.operator;

import java.util.List;

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.operator.OpenDataHubOperatorApplication;
import org.jboss.intersmash.provision.Provisioner;
import org.jboss.intersmash.provision.operator.model.odh.DSCInitializationList;
import org.jboss.intersmash.provision.operator.model.odh.DataScienceClusterList;
import org.jboss.intersmash.provision.operator.model.odh.FeatureTrackerList;
import org.jboss.intersmash.provision.operator.model.odh.MonitoringList;
import org.slf4j.event.Level;

import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.opendatahub.datasciencecluster.v1.DataScienceCluster;
import io.opendatahub.dscinitialization.v1.DSCInitialization;
import io.opendatahub.features.v1.FeatureTracker;
import io.opendatahub.platform.services.v1alpha1.Monitoring;
import lombok.extern.slf4j.Slf4j;

/**
 * Defines the contract and default behavior of an Operator based provisioner for the Open Data Hub Operator
 */
@Slf4j
public abstract class OpenDataHubOperatorProvisioner<C extends NamespacedKubernetesClient> extends
		OperatorProvisioner<OpenDataHubOperatorApplication, C> implements Provisioner<OpenDataHubOperatorApplication> {

	public OpenDataHubOperatorProvisioner(OpenDataHubOperatorApplication application) {
		super(application, OpenDataHubOperatorProvisioner.OPERATOR_ID);
	}

	// =================================================================================================================
	// Related to generic provisioning behavior
	// =================================================================================================================
	@Override
	public String getOperatorCatalogSource() {
		return IntersmashConfig.openDataHubOperatorCatalogSource();
	}

	@Override
	public String getOperatorIndexImage() {
		return IntersmashConfig.openDataHubOperatorIndexImage();
	}

	@Override
	public String getOperatorChannel() {
		return IntersmashConfig.openDataHubOperatorChannel();
	}

	@Override
	public void deploy() {
		FailFastCheck ffCheck = getFailFastCheck();

		subscribe();

		dataScienceClusterClient().createOrReplace(getApplication().getDataScienceCluster());
		new SimpleWaiter(() -> dataScienceCluster().get().getStatus() != null)
				.failFast(ffCheck)
				.reason("Wait for status field to be initialized.")
				.level(Level.DEBUG)
				.waitFor();

		dscInitializationClient().createOrReplace(getApplication().getDSCInitialization());
	}

	@Override
	public void undeploy() {
		// remove the CRs
		List<StatusDetails> deletionDetails = dataScienceCluster().delete();
		boolean deleted = deletionDetails.stream().allMatch(d -> d.getCauses().isEmpty());
		if (!deleted) {
			log.warn("Wasn't able to remove the 'DataScienceCluster' resource");
		}
		new SimpleWaiter(() -> dataScienceCluster().get() == null)
				.reason("Waiting for the the 'DataScienceCluster' resource to be removed.")
				.level(Level.DEBUG)
				.waitFor();

		if (getApplication().getDSCInitialization() != null) {
			final String appName = getApplication().getName();
			deletionDetails = dscInitializationClient().withName(appName).delete();
			deleted = deletionDetails.stream().allMatch(d -> d.getCauses().isEmpty());
			if (!deleted) {
				log.warn("Wasn't able to remove the 'DSCInitialization' resources created for '{}' instance!",
						appName);
			}
			new SimpleWaiter(() -> dscInitializationClient().list().getItems().isEmpty()).level(Level.DEBUG).waitFor();
		}
		// delete the OLM subscription
		unsubscribe();
		new SimpleWaiter(() -> getPods().isEmpty())
				.reason("Waiting for the all the HyperFoil pods to be stopped.")
				.level(Level.DEBUG)
				.waitFor();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		throw new UnsupportedOperationException("Scaling is not implemented by Open Data Hub operator based provisioning");
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	// this is the packagemanifest for the operator;
	// you can get it with command:
	// oc get packagemanifest <bundle> -o template --template='{{ .metadata.name }}'
	public static String OPERATOR_ID = IntersmashConfig.openDataHubOperatorPackageManifest();

	// this is the name of the CustomResourceDefinition(s)
	// you can get it with command:
	// oc get crd <group>> -o template --template='{{ .metadata.name }}'
	protected static String ODH_DATA_SCIENCE_CLUSTER_CRD_NAME = "datascienceclusters.datasciencecluster.opendatahub.io";

	protected static String ODH_DSC_INITIALIZATION_CRD_NAME = "dscinitializations.dscinitialization.opendatahub.io";

	protected static String ODH_FEATURE_TRACKER_CRD_NAME = "featuretrackers.features.opendatahub.io";

	protected static String ODH_MONITORING_CRD_NAME = "monitorings.services.platform.opendatahub.io";

	/**
	 * Generic CRD client which is used by client builders default implementation to build the CRDs client
	 *
	 * @return A {@link NonNamespaceOperation} instance that represents a
	 */
	protected abstract NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient();

	// datascienceclusters.datasciencecluster.opendatahub.io
	protected abstract HasMetadataOperationsImpl<DataScienceCluster, DataScienceClusterList> dataScienceClusterCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	// dscinitializations.dscinitialization.opendatahub.io
	protected abstract HasMetadataOperationsImpl<DSCInitialization, DSCInitializationList> dscInitializationCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	// featuretrackers.features.opendatahub.io
	protected abstract HasMetadataOperationsImpl<FeatureTracker, FeatureTrackerList> featureTrackerCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	// monitorings.services.platform.opendatahub.io
	protected abstract HasMetadataOperationsImpl<Monitoring, MonitoringList> monitoringCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	private static NonNamespaceOperation<DataScienceCluster, DataScienceClusterList, Resource<DataScienceCluster>> ODH_DATA_SCIENCE_CLUSTER_CLIENT;
	private static NonNamespaceOperation<DSCInitialization, DSCInitializationList, Resource<DSCInitialization>> ODH_DSC_INITIALIZATION_CLIENT;
	private static NonNamespaceOperation<FeatureTracker, FeatureTrackerList, Resource<FeatureTracker>> ODH_FEATURE_TRACKER_CLIENT;
	private static NonNamespaceOperation<Monitoring, MonitoringList, Resource<Monitoring>> ODH_MONITORING_CLIENT;

	/**
	 * Get a client capable of working with {@link OpenDataHubOperatorProvisioner#ODH_DATA_SCIENCE_CLUSTER_CLIENT} custom resource.
	 *
	 * @return client for operations with {@link OpenDataHubOperatorProvisioner#ODH_DATA_SCIENCE_CLUSTER_CLIENT} custom resource
	 */
	public NonNamespaceOperation<DataScienceCluster, DataScienceClusterList, Resource<DataScienceCluster>> dataScienceClusterClient() {
		if (ODH_DATA_SCIENCE_CLUSTER_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(ODH_DATA_SCIENCE_CLUSTER_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						ODH_DATA_SCIENCE_CLUSTER_CRD_NAME, OPERATOR_ID));
			}
			ODH_DATA_SCIENCE_CLUSTER_CLIENT = dataScienceClusterCustomResourcesClient(
					CustomResourceDefinitionContext.fromCrd(crd));
		}
		return ODH_DATA_SCIENCE_CLUSTER_CLIENT;
	}

	/**
	 * Get a client capable of working with {@link OpenDataHubOperatorProvisioner#ODH_DSC_INITIALIZATION_CLIENT} custom resource.
	 *
	 * @return client for operations with {@link OpenDataHubOperatorProvisioner#ODH_DSC_INITIALIZATION_CLIENT} custom resource
	 */
	public NonNamespaceOperation<DSCInitialization, DSCInitializationList, Resource<DSCInitialization>> dscInitializationClient() {
		if (ODH_DSC_INITIALIZATION_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(ODH_DSC_INITIALIZATION_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						ODH_DSC_INITIALIZATION_CRD_NAME, OPERATOR_ID));
			}
			ODH_DSC_INITIALIZATION_CLIENT = dscInitializationCustomResourcesClient(
					CustomResourceDefinitionContext.fromCrd(crd));
		}
		return ODH_DSC_INITIALIZATION_CLIENT;
	}

	/**
	 * Get a client capable of working with {@link OpenDataHubOperatorProvisioner#ODH_FEATURE_TRACKER_CLIENT} custom resource.
	 *
	 * @return client for operations with {@link OpenDataHubOperatorProvisioner#ODH_FEATURE_TRACKER_CLIENT} custom resource
	 */
	public NonNamespaceOperation<FeatureTracker, FeatureTrackerList, Resource<FeatureTracker>> featureTrackerClient() {
		if (ODH_FEATURE_TRACKER_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(ODH_FEATURE_TRACKER_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						ODH_FEATURE_TRACKER_CRD_NAME, OPERATOR_ID));
			}
			ODH_FEATURE_TRACKER_CLIENT = featureTrackerCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return ODH_FEATURE_TRACKER_CLIENT;
	}

	/**
	 * Get a client capable of working with {@link OpenDataHubOperatorProvisioner#ODH_MONITORING_CLIENT} custom resource.
	 *
	 * @return client for operations with {@link OpenDataHubOperatorProvisioner#ODH_MONITORING_CLIENT} custom resource
	 */
	public NonNamespaceOperation<Monitoring, MonitoringList, Resource<Monitoring>> monitoringClient() {
		if (ODH_MONITORING_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(ODH_MONITORING_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						ODH_MONITORING_CRD_NAME, OPERATOR_ID));
			}
			ODH_MONITORING_CLIENT = monitoringCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return ODH_MONITORING_CLIENT;
	}

	/**
	 * Get a reference to an {@link DataScienceCluster} instance.
	 * Use get() to obtain the actual object, or null in case it does not exist on tested cluster.
	 *
	 * @return A concrete {@link Resource} instance representing the {@link DataScienceCluster} resource definition
	 */
	public Resource<DataScienceCluster> dataScienceCluster() {
		return dataScienceClusterClient().withName(getApplication().getName());
	}

}
