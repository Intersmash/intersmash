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
package org.jboss.intersmash.tools.provision.operator;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.operator.ActiveMQOperatorApplication;
import org.jboss.intersmash.tools.provision.Provisioner;
import org.jboss.intersmash.tools.provision.openshift.OpenShiftProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.activemq.address.ActiveMQArtemisAddressList;
import org.jboss.intersmash.tools.provision.openshift.operator.activemq.broker.ActiveMQArtemisList;
import org.slf4j.event.Level;

import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.amq.broker.v1beta1.ActiveMQArtemis;
import io.amq.broker.v1beta1.ActiveMQArtemisAddress;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;

/**
 * ActiveMQ Operator based provisioner
 */
public interface ActiveMQOperatorProvisioner extends
		OlmOperatorProvisioner<ActiveMQOperatorApplication>, Provisioner<ActiveMQOperatorApplication> {

	// this is the packagemanifest for the operator;
	// you can get it with command:
	// oc get packagemanifest <bundle> -o template --template='{{ .metadata.name }}'
	static String operatorId() {
		return IntersmashConfig.activeMQOperatorPackageManifest();
	}

	// this is the name of the CustomResourceDefinition(s)
	// you can get it with command:
	// oc get crd <group>> -o template --template='{{ .metadata.name }}'
	default String activeMQCustomResourceDefinitionName() {
		return "activemqartemises.broker.amq.io";
	}

	HasMetadataOperationsImpl<ActiveMQArtemisAddress, ActiveMQArtemisAddressList> activeMQArtemisAddressesCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	HasMetadataOperationsImpl<ActiveMQArtemis, ActiveMQArtemisList> activeMQArtemisCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	NonNamespaceOperation<ActiveMQArtemisAddress, ActiveMQArtemisAddressList, Resource<ActiveMQArtemisAddress>> activeMQArtemisAddressesClient();

	NonNamespaceOperation<ActiveMQArtemis, ActiveMQArtemisList, Resource<ActiveMQArtemis>> activeMQArtemisesClient();

	/**
	 * Get a client capable of working with {@link #activeMQCustomResourceDefinitionName()} custom resource.
	 *
	 * @return client for operations with {@link #activeMQCustomResourceDefinitionName()} custom resource
	 */
	default MixedOperation<ActiveMQArtemisAddress, ActiveMQArtemisAddressList, Resource<ActiveMQArtemisAddress>> buildActiveMQArtemisAddressesClient() {
		CustomResourceDefinition crd = retrieveCustomResourceDefinitions()
				.withName(activeMQCustomResourceDefinitionName()).get();
		CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
		if (!retrieveCustomResourceDefinitions().list().getItems().contains(activeMQCustomResourceDefinitionName())) {
			throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
					activeMQCustomResourceDefinitionName(), operatorId()));
		}
		return activeMQArtemisAddressesCustomResourcesClient(crdc);
	}

	/**
	 * Get a reference to activeMQArtemisAddress object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the activeMQArtemisAddress custom resource
	 * @return A concrete {@link Resource} instance representing the {@link ActiveMQArtemisAddress} resource definition
	 */
	default Resource<ActiveMQArtemisAddress> activeMQArtemisAddress(String name) {
		return activeMQArtemisAddressesClient().withName(name);
	}

	/**
	 * Get all activeMQArtemisAddresses maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link ActiveMQArtemisAddress} resource definitions
	 */
	default List<Resource<ActiveMQArtemisAddress>> activeMQArtemisAddresses() {
		ActiveMQOperatorApplication activeMqOperatorApplication = getApplication();
		return activeMqOperatorApplication.getActiveMQArtemisAddresses().stream()
				.map(activeMQArtemisAddress -> activeMQArtemisAddress.getMetadata().getName())
				.map(this::activeMQArtemisAddress)
				.collect(Collectors.toList());
	}

	/**
	 * Get a client capable of working with {@link #activeMQCustomResourceDefinitionName()} custom resource.
	 *
	 * @return client for operations with {@link #activeMQCustomResourceDefinitionName()} custom resource
	 */
	default MixedOperation<ActiveMQArtemis, ActiveMQArtemisList, Resource<ActiveMQArtemis>> buildActiveMQArtemisesClient() {
		CustomResourceDefinition crd = retrieveCustomResourceDefinitions()
				.withName(activeMQCustomResourceDefinitionName()).get();
		CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
		if (!retrieveCustomResourceDefinitions().list().getItems().contains(activeMQCustomResourceDefinitionName())) {
			throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
					activeMQCustomResourceDefinitionName(), operatorId()));
		}
		return activeMQArtemisCustomResourcesClient(crdc);

	}

	/**
	 * Get a reference to activeMQArtemis object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link ActiveMQArtemis} resource definition
	 */
	default Resource<ActiveMQArtemis> activeMQArtemis() {
		return activeMQArtemisesClient().withName(getApplication().getActiveMQArtemis().getMetadata().getName());
	}

	@Override
	default void deploy() {
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
				.forEach(address -> new SimpleWaiter(() -> address.get() != null).level(Level.DEBUG).waitFor());
		new SimpleWaiter(() -> activeMQArtemis().get() != null)
				.failFast(ffCheck)
				.level(Level.DEBUG)
				.waitFor();
		OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck).areExactlyNPodsReady(replicas,
				activeMQArtemis().get().getKind(), getApplication().getActiveMQArtemis().getMetadata().getName())
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
	default void undeploy() {
		FailFastCheck ffCheck = () -> false;
		// delete the resources
		activeMQArtemisAddresses().forEach(address -> address.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());

		// scaling down to 0 (graceful shutdown) is required since ActiveMQ Operator 7.10
		this.scale(0, Boolean.TRUE);
		activeMQArtemis().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();

		// wait
		new SimpleWaiter(() -> activeMQArtemisesClient().list().getItems().size() == 0).failFast(ffCheck).level(Level.DEBUG)
				.waitFor();
		activeMQArtemisAddresses().forEach(
				address -> new SimpleWaiter(() -> address.get() == null).level(Level.DEBUG).failFast(ffCheck).waitFor());

		unsubscribe();
	}

	default void scale(int replicas, boolean wait) {
		FailFastCheck ffCheck = () -> false;
		ActiveMQArtemis tmpBroker = activeMQArtemis().get();
		tmpBroker.getSpec().getDeploymentPlan().setSize(replicas);
		activeMQArtemis().replace(tmpBroker);
		if (wait) {
			OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
					.areExactlyNPodsReady(replicas, tmpBroker.getKind(), tmpBroker.getMetadata().getName())
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

	List<Pod> getPods();

	default String getOperatorCatalogSource() {
		return IntersmashConfig.activeMQOperatorCatalogSource();
	}

	default String getOperatorIndexImage() {
		return IntersmashConfig.activeMQOperatorIndexImage();
	}

	default String getOperatorChannel() {
		return IntersmashConfig.activeMQOperatorChannel();
	}
}
