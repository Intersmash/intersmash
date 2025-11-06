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
package org.jboss.intersmash.provision.operator;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.operator.ActiveMQOperatorApplication;
import org.jboss.intersmash.provision.Provisioner;
import org.jboss.intersmash.provision.operator.model.activemq.address.ActiveMQArtemisAddressList;
import org.jboss.intersmash.provision.operator.model.activemq.broker.ActiveMQArtemisList;
import org.slf4j.event.Level;

import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.amq.broker.v1beta1.ActiveMQArtemis;
import io.amq.broker.v1beta1.ActiveMQArtemisAddress;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import lombok.NonNull;

/**
 * Defines the contract and default behavior of an Operator based provisioner for the ActiveMQ Artemis Operator
 */
public abstract class ActiveMQOperatorProvisioner<C extends NamespacedKubernetesClient> extends
		OperatorProvisioner<ActiveMQOperatorApplication, C> implements Provisioner<ActiveMQOperatorApplication> {

	public ActiveMQOperatorProvisioner(@NonNull ActiveMQOperatorApplication application) {
		super(application, ActiveMQOperatorProvisioner.OPERATOR_ID);
	}

	// =================================================================================================================
	// ActiveMQ Artemis related
	// =================================================================================================================
	protected List<Pod> getLabeledPods(final String labelName, final String labelValue) {
		return this.getPods().stream()
				.filter(p -> p.getMetadata().getLabels().entrySet().stream()
						.anyMatch(l -> l.getKey().equals(labelName) && l.getValue().equals(labelValue)))
				.collect(Collectors.toList());
	}

	// =================================================================================================================
	// Related to generic provisioning behavior
	// =================================================================================================================
	@Override
	public String getOperatorCatalogSource() {
		return IntersmashConfig.activeMQOperatorCatalogSource();
	}

	@Override
	public String getOperatorIndexImage() {
		return IntersmashConfig.activeMQOperatorIndexImage();
	}

	@Override
	public String getOperatorChannel() {
		return IntersmashConfig.activeMQOperatorChannel();
	}

	@Override
	public String getVersion() {
		return IntersmashConfig.activeMQOperatorVersion();
	}

	@Override
	public void deploy() {
		FailFastCheck ffCheck = () -> false;
		subscribe();

		int replicas = getApplication().getActiveMQArtemis().getSpec().getDeploymentPlan().getSize();
		// deploy broker
		activeMQArtemisesClient().createOrReplace(getApplication().getActiveMQArtemis());
		// deploy addresses
		getApplication().getActiveMQArtemisAddresses().stream()
				.forEach(activeMQArtemisAddress -> activeMQArtemisAddressesClient().createOrReplace(activeMQArtemisAddress));
		// wait for all resources to be ready
		activeMQArtemisAddresses()
				.forEach(address -> new SimpleWaiter(() -> address.get() != null)
						.level(Level.DEBUG)
						.waitFor());
		new SimpleWaiter(() -> activeMQArtemis().get() != null)
				.failFast(ffCheck)
				.level(Level.DEBUG)
				.waitFor();
		new SimpleWaiter(() -> getLabeledPods(activeMQArtemis().get().getKind(),
				getApplication().getActiveMQArtemis().getMetadata().getName())
				.size() == replicas)
				.failFast(ffCheck)
				.level(Level.DEBUG)
				.waitFor();

		/*
		 * The following wait condition has been suppressed temporarily since:
		 * 1. doesn't allow for the ActiveMQ CR `.podStatus` to be
		 * checked
		 * 2. the condition above waits already for the expected (i.e. representing ActiveMQArtemis CRs) number of CR
		 * pods to be ready
		 */
		//		new SimpleWaiter(() -> activeMQArtemis().get().getStatus().getPodStatus().getReady().size() == replicas)
		//				.failFast(ffCheck)
		//				.level(Level.DEBUG)
		//				.waitFor();
	}

	@Override
	public void undeploy() {
		FailFastCheck ffCheck = () -> false;
		// delete the resources
		activeMQArtemisAddresses().forEach(address -> address.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());

		// scaling down to 0 (graceful shutdown) is required since ActiveMQ Operator 7.10
		this.scale(0, Boolean.TRUE);
		activeMQArtemis().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();

		// wait
		new SimpleWaiter(() -> activeMQArtemisesClient().list().getItems().isEmpty()).failFast(ffCheck).level(Level.DEBUG)
				.waitFor();
		activeMQArtemisAddresses().forEach(
				address -> new SimpleWaiter(() -> address.get() == null).level(Level.DEBUG).failFast(ffCheck).waitFor());

		unsubscribe();
	}

	public void scale(int replicas, boolean wait) {
		ActiveMQArtemis tmpBroker = activeMQArtemis().get();
		tmpBroker.getSpec().getDeploymentPlan().setSize(replicas);
		activeMQArtemis().replace(tmpBroker);
		if (wait) {
			new SimpleWaiter(() -> getLabeledPods(tmpBroker.getKind(),
					tmpBroker.getMetadata().getName())
					.size() == replicas)
					.level(Level.DEBUG)
					.waitFor();
			/*
			 * The following wait condition has been suppressed temporarily since:
			 * 1. doesn't allow for the AMQ Broker CR `.podStatus` to be checked
			 * 2. the condition above waits already for the expected (i.e. representing ActiveMQArtemis CRs) number of CR
			 * pods to be ready
			 */
			//			new SimpleWaiter(() -> activeMQArtemis().get().getStatus().getPodStatus().getReady().size() == replicas)
			//					.failFast(ffCheck)
			//					.level(Level.DEBUG)
			//					.waitFor();
		}
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	// this is the packagemanifest for the operator;
	// you can get it with command:
	// oc get packagemanifest <bundle> -o template --template='{{ .metadata.name }}'
	public static String OPERATOR_ID = IntersmashConfig.activeMQOperatorPackageManifest();

	// this is the name of the CustomResourceDefinition(s)
	// you can get it with command:
	// oc get crd <group>> -o template --template='{{ .metadata.name }}'
	protected static String ACTIVEMQ_ARTEMIS_CRD_NAME = "activemqartemises.broker.amq.io";

	protected static String ACTIVEMQ_ARTEMIS_ADDRESS_CRD_NAME = "activemqartemisaddresses.broker.amq.io";

	/**
	 * Generic CRD client which is used by client builders default implementation to build the CRDs client
	 *
	 * @return A {@link NonNamespaceOperation} instance that represents a
	 */
	protected abstract NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient();

	// activemqartemises.broker.amq.io
	protected abstract HasMetadataOperationsImpl<ActiveMQArtemis, ActiveMQArtemisList> activeMQArtemisCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	// activemqartemisaddresses.broker.amq.io
	protected abstract HasMetadataOperationsImpl<ActiveMQArtemisAddress, ActiveMQArtemisAddressList> activeMQArtemisAddressesCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	private static NonNamespaceOperation<ActiveMQArtemis, ActiveMQArtemisList, Resource<ActiveMQArtemis>> ACTIVE_MQ_ARTEMISES_CLIENT;

	/**
	 * Get a client capable of working with {@link {@link ActiveMQOperatorProvisioner#ACTIVEMQ_ARTEMIS_CRD_NAME}} custom resource.
	 *
	 * @return client for operations with {@link {@link ActiveMQOperatorProvisioner#ACTIVEMQ_ARTEMIS_CRD_NAME}} custom resource
	 */
	public NonNamespaceOperation<ActiveMQArtemis, ActiveMQArtemisList, Resource<ActiveMQArtemis>> activeMQArtemisesClient() {
		if (ACTIVE_MQ_ARTEMISES_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(ACTIVEMQ_ARTEMIS_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						ACTIVEMQ_ARTEMIS_CRD_NAME, OPERATOR_ID));
			}
			ACTIVE_MQ_ARTEMISES_CLIENT = activeMQArtemisCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return ACTIVE_MQ_ARTEMISES_CLIENT;
	}

	private static NonNamespaceOperation<ActiveMQArtemisAddress, ActiveMQArtemisAddressList, Resource<ActiveMQArtemisAddress>> ACTIVE_MQ_ARTEMIS_ADDRESSES_CLIENT;

	/**
	 * Get a client capable of working with {@link ActiveMQOperatorProvisioner#ACTIVEMQ_ARTEMIS_ADDRESS_CRD_NAME} custom resource.
	 *
	 * @return client for operations with {@link ActiveMQOperatorProvisioner#ACTIVEMQ_ARTEMIS_ADDRESS_CRD_NAME} custom resource
	 */
	public NonNamespaceOperation<ActiveMQArtemisAddress, ActiveMQArtemisAddressList, Resource<ActiveMQArtemisAddress>> activeMQArtemisAddressesClient() {
		if (ACTIVE_MQ_ARTEMIS_ADDRESSES_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(ACTIVEMQ_ARTEMIS_ADDRESS_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						ACTIVEMQ_ARTEMIS_ADDRESS_CRD_NAME, OPERATOR_ID));
			}
			ACTIVE_MQ_ARTEMIS_ADDRESSES_CLIENT = activeMQArtemisAddressesCustomResourcesClient(
					CustomResourceDefinitionContext.fromCrd(crd));
		}
		return ACTIVE_MQ_ARTEMIS_ADDRESSES_CLIENT;
	}

	/**
	 * Get a reference to activeMQArtemis object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link ActiveMQArtemis} resource definition
	 */
	public Resource<ActiveMQArtemis> activeMQArtemis() {
		return activeMQArtemisesClient().withName(getApplication().getActiveMQArtemis().getMetadata().getName());
	}

	/**
	 * Get a reference to activeMQArtemisAddress object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the activeMQArtemisAddress custom resource
	 * @return A concrete {@link Resource} instance representing the {@link ActiveMQArtemisAddress} resource definition
	 */
	public Resource<ActiveMQArtemisAddress> activeMQArtemisAddress(String name) {
		return activeMQArtemisAddressesClient().withName(name);
	}

	/**
	 * Get all activeMQArtemisAddresses maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link ActiveMQArtemisAddress} resource definitions
	 */
	public List<Resource<ActiveMQArtemisAddress>> activeMQArtemisAddresses() {
		ActiveMQOperatorApplication activeMqOperatorApplication = getApplication();
		return activeMqOperatorApplication.getActiveMQArtemisAddresses().stream()
				.map(activeMQArtemisAddress -> activeMQArtemisAddress.getMetadata().getName())
				.map(this::activeMQArtemisAddress)
				.collect(Collectors.toList());
	}
}
