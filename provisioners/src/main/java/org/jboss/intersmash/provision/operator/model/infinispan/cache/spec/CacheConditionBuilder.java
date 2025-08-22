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
package org.jboss.intersmash.provision.operator.model.infinispan.cache.spec;

import org.infinispan.v2alpha1.cachestatus.Conditions;

public final class CacheConditionBuilder {
	private String type;
	private String status;
	private String message;

	/**
	 * Set the type of the condition.
	 *
	 * @param type Type of the condition.
	 * @return this
	 */
	public CacheConditionBuilder type(String type) {
		this.type = type;
		return this;
	}

	/**
	 * Set the status of the condition.
	 *
	 * @param status Status of the condition.
	 * @return this
	 */
	public CacheConditionBuilder status(String status) {
		this.status = status;
		return this;
	}

	/**
	 * Set a human-readable message indicating details about last transition.
	 *
	 * @param message Human-readable message indicating details about last transition.
	 * @return this
	 */
	public CacheConditionBuilder message(String message) {
		this.message = message;
		return this;
	}

	public Conditions build() {
		Conditions cacheCondition = new Conditions();
		cacheCondition.setType(type);
		cacheCondition.setStatus(status);
		cacheCondition.setMessage(message);
		return cacheCondition;
	}
}
