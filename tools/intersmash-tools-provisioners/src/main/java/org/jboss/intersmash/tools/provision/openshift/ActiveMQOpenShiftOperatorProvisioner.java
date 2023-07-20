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
package org.jboss.intersmash.tools.provision.openshift;

import java.util.List;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.operator.ActiveMQOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.operator.activemq.address.ActiveMQArtemisAddressList;
import org.jboss.intersmash.tools.provision.openshift.operator.activemq.broker.ActiveMQArtemisList;
import org.jboss.intersmash.tools.provision.operator.ActiveMQOperatorProvisioner;
import org.jboss.intersmash.tools.provision.operator.OperatorProvisioner;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import io.amq.broker.v1beta1.ActiveMQArtemis;
import io.amq.broker.v1beta1.ActiveMQArtemisAddress;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import lombok.NonNull;

/**
 * ActiveMQ Operator based provisioner
 */
public class ActiveMQOpenShiftOperatorProvisioner
		// default Operator based provisioner behavior, which implements OLM workflow contract and leverage lifecycle as well
		extends OperatorProvisioner<ActiveMQOperatorApplication>
		// leverage ActiveMQ Artemis common Operator based provisioner behavior
		implements ActiveMQOperatorProvisioner,
		// leverage common OpenShift provisioning logic
		OpenShiftProvisioner<ActiveMQOperatorApplication> {
	private static NonNamespaceOperation<ActiveMQArtemis, ActiveMQArtemisList, Resource<ActiveMQArtemis>> ACTIVE_MQ_ARTEMISES_CLIENT;
	private static NonNamespaceOperation<ActiveMQArtemisAddress, ActiveMQArtemisAddressList, Resource<ActiveMQArtemisAddress>> ACTIVE_MQ_ARTEMIS_ADDRESSES_CLIENT;

	//	private final static String ACTIVE_MQ_ARTEMIS_SCALEDOWN_RESOURCE = "activemqartemisscaledowns.broker.amq.io"; // TODO add on demand

	private static final String OPERATOR_ID = IntersmashConfig.activeMQOperatorPackageManifest();

	public ActiveMQOpenShiftOperatorProvisioner(@NonNull ActiveMQOperatorApplication activeMqOperatorApplication) {
		super(activeMqOperatorApplication, ActiveMQOperatorProvisioner.operatorId());
	}

	@Override
	public HasMetadataOperationsImpl<ActiveMQArtemisAddress, ActiveMQArtemisAddressList> activeMQArtemisAddressesCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().customResources(crdc, ActiveMQArtemisAddress.class, ActiveMQArtemisAddressList.class);
	}

	@Override
	public HasMetadataOperationsImpl<ActiveMQArtemis, ActiveMQArtemisList> activeMQArtemisCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().customResources(crdc, ActiveMQArtemis.class, ActiveMQArtemisList.class);
	}

	/**
	 * Get a client capable of working with {@link ActiveMQOperatorProvisioner#activeMQCustomResourceDefinitionName()} custom resource.
	 *
	 * @return client for operations with {@link ActiveMQOperatorProvisioner#activeMQCustomResourceDefinitionName()} custom resource
	 */
	public NonNamespaceOperation<ActiveMQArtemisAddress, ActiveMQArtemisAddressList, Resource<ActiveMQArtemisAddress>> activeMQArtemisAddressesClient() {
		if (ACTIVE_MQ_ARTEMIS_ADDRESSES_CLIENT == null) {
			ACTIVE_MQ_ARTEMIS_ADDRESSES_CLIENT = buildActiveMQArtemisAddressesClient().inNamespace(OpenShiftConfig.namespace());
		}
		return ACTIVE_MQ_ARTEMIS_ADDRESSES_CLIENT;
	}

	/**
	 * Get a client capable of working with {@link {@link ActiveMQOperatorProvisioner#activeMQCustomResourceDefinitionName()}} custom resource.
	 *
	 * @return client for operations with {@link {@link ActiveMQOperatorProvisioner#activeMQCustomResourceDefinitionName()}} custom resource
	 */
	public NonNamespaceOperation<ActiveMQArtemis, ActiveMQArtemisList, Resource<ActiveMQArtemis>> activeMQArtemisesClient() {
		if (ACTIVE_MQ_ARTEMISES_CLIENT == null) {
			ACTIVE_MQ_ARTEMISES_CLIENT = buildActiveMQArtemisesClient();
		}
		return ACTIVE_MQ_ARTEMISES_CLIENT;
	}

	/**
	 * Get a reference to activeMQArtemis object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link ActiveMQArtemis} resource definition
	 */
	public Resource<ActiveMQArtemis> activeMQArtemis() {
		return activeMQArtemisesClient().withName(getApplication().getActiveMQArtemis().getMetadata().getName());
	}

	@Override
	public String execute(String... args) {
		return OpenShifts.adminBinary().execute(args);
	}

	@Override
	public void scale(int replicas, boolean wait) {
		ActiveMQOperatorProvisioner.super.scale(replicas, wait);
	}

	@Override
	public List<Pod> retrievePods() {
		return OpenShiftProvisioner.openShift.pods().inNamespace(OpenShiftProvisioner.openShift.getNamespace()).list()
				.getItems();
	}

	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> retrieveCustomResourceDefinitions() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}

	/**
	 * Get the provisioned application service related Pods
	 * <p>
	 * Currently blocked by the fact that Pod Status pod names do not reflect the reality
	 * <p>
	 * Once these issues are resolved, we can use the ready pod names returned by
	 * {@code ActiveMQArtemisStatus.getPodStatus()} to create the List with pods maintained by the provisioner.
	 *
	 * @return A list of related {@link Pod} instances
	 */
	@Override
	public List<Pod> getPods() {
		throw new UnsupportedOperationException("To be implemented!");
	}

	@Override
	public String getOperatorCatalogSource() {
		return ActiveMQOperatorProvisioner.super.getOperatorCatalogSource();
	}

	@Override
	public String getOperatorIndexImage() {
		return ActiveMQOperatorProvisioner.super.getOperatorIndexImage();
	}

	@Override
	public String getOperatorChannel() {
		return ActiveMQOperatorProvisioner.super.getOperatorChannel();
	}

	@Override
	protected String getOperatorNamespace() {
		return "openshift-marketplace";
	}

	@Override
	protected String getTargetNamespace() {
		return OpenShiftConfig.namespace();
	}
}
