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
package org.jboss.intersmash.tools.provision.openshift.operator.activemq.address;

import java.util.Map;

import io.amq.broker.v1beta1.ActiveMQArtemisAddress;
import io.amq.broker.v1beta1.ActiveMQArtemisAddressSpec;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class ActiveMQArtemisAddressBuilder {

	private String name;
	private Map<String, String> labels;
	private String addressName;
	private String queueName;
	private Boolean removeFromBrokerOnDelete;
	private String routingType;

	/**
	 * Initialize the {@link ActiveMQArtemisAddressBuilder} with given resource name.
	 *
	 * @param name resource object name
	 */
	public ActiveMQArtemisAddressBuilder(String name) {
		this.name = name;
	}

	/**
	 * Initialize the {@link ActiveMQArtemisAddressBuilder} with given resource name and labels.
	 *
	 * @param name resource object name
	 * @param labels key/value pairs that are attached to objects
	 */
	public ActiveMQArtemisAddressBuilder(String name, Map<String, String> labels) {
		this.name = name;
		this.labels = labels;
	}

	/**
	 * Address name to be created on broker.
	 *
	 * @param addressName The name for the address to be created
	 * @return this
	 */
	public ActiveMQArtemisAddressBuilder addressName(String addressName) {
		this.addressName = addressName;
		return this;
	}

	/**
	 * Queue name to be created on broker.
	 *
	 * @param queueName The name for the new queue to be created
	 * @return this
	 */
	public ActiveMQArtemisAddressBuilder queueName(String queueName) {
		this.queueName = queueName;
		return this;
	}

	/**
	 * Routing type to be used; see {@link RoutingType} values.
	 *
	 * @param routingType Desired value for the broker routing type
	 * @return this
	 */
	public ActiveMQArtemisAddressBuilder routingType(RoutingType routingType) {
		this.routingType = routingType.routingType;
		return this;
	}

	/**
	 * Specify whether the Operator removes existing addresses for all brokers in a deployment when you remove the
	 * address CR instance for that deployment. The default value is false, which means the Operator does not delete
	 * existing addresses when you remove the CR.
	 *
	 * @param removeFromBrokerOnDelete Boolean to state whether addresses should be removed when deleting the broker
	 * @return this
	 */
	private ActiveMQArtemisAddressBuilder removeFromBrokerOnDelete(boolean removeFromBrokerOnDelete) {
		this.removeFromBrokerOnDelete = removeFromBrokerOnDelete;
		return this;
	}

	public ActiveMQArtemisAddress build() {
		ActiveMQArtemisAddress activeMQArtemisAddress = new ActiveMQArtemisAddress();
		activeMQArtemisAddress.setMetadata(new ObjectMeta());
		activeMQArtemisAddress.getMetadata().setName(name);
		activeMQArtemisAddress.getMetadata().setLabels(labels);
		activeMQArtemisAddress.setSpec(new ActiveMQArtemisAddressSpec());
		activeMQArtemisAddress.getSpec().setAddressName(addressName);
		activeMQArtemisAddress.getSpec().setQueueName(queueName);
		activeMQArtemisAddress.getSpec().setRoutingType(routingType);
		activeMQArtemisAddress.getSpec().setRemoveFromBrokerOnDelete(removeFromBrokerOnDelete);
		return activeMQArtemisAddress;
	}

	public enum RoutingType {
		ANYCAST("anycast"),
		MULTICAST("multicast");

		private String routingType;

		RoutingType(String routingType) {
			this.routingType = routingType;
		}
	}
}
