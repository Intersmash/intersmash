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
package org.jboss.intersmash.provision.operator.model.infinispan.infinispan.spec;

import org.infinispan.v1.infinispanstatus.Conditions;

public final class InfinispanConditionBuilder {
	private String type;
	private String status;
	private String message;

	/**
	 * Set the type of the condition.
	 *
	 * @param type Type of the condition.
	 * @return this
	 */
	public InfinispanConditionBuilder type(ConditionType type) {
		if (type != ConditionType.None) {
			this.type = type.getValue();
		}
		return this;
	}

	/**
	 * Set the status of the condition.
	 *
	 * @param status Status of the condition.
	 * @return this
	 */
	public InfinispanConditionBuilder status(String status) {
		this.status = status;
		return this;
	}

	/**
	 * Set a human-readable message indicating details about last transition.
	 *
	 * @param message Human-readable message indicating details about last transition.
	 * @return this
	 */
	public InfinispanConditionBuilder message(String message) {
		this.message = message;
		return this;
	}

	public Conditions build() {
		Conditions infinispanCondition = new Conditions();
		infinispanCondition.setType(type);
		infinispanCondition.setStatus(status);
		infinispanCondition.setMessage(message);
		return infinispanCondition;
	}

	/**
	 * Describe the type of the condition
	 */
	public enum ConditionType {
		None(""),
		ConditionPrelimChecksPassed("PreliminaryChecksPassed"),
		ConditionGracefulShutdown("GracefulShutdown"),
		ConditionStopping("Stopping"),
		ConditionUpgrade("Upgrade"),
		ConditionWellFormed("WellFormed");

		private String value;

		ConditionType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
