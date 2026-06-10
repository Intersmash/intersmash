/*
 * Copyright (C) 2025 Red Hat, Inc.
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
package org.jboss.intersmash.junit5.recorder;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.function.Predicate;

import io.fabric8.kubernetes.api.model.HasMetadata;

public class ResourcesFilterBuilder<E extends HasMetadata> implements Cloneable {

	FilterMethod method;
	String[] resourceNames;
	ZonedDateTime excludeUntil;
	ZonedDateTime includeAlwaysAfter;
	ZonedDateTime includeAlwaysUntil;

	public Predicate<E> build() {
		switch (method) {
			case RESOURCE_NAMES:
				return resource -> resourceNameMatches(getResourceName(resource), resourceNames);
			case PREVIOUSLY_SEEN_RESOURCES:
				return resource -> {
					ZonedDateTime time = getResourceTime(resource);
					return (includeAlwaysAfter != null && includeAlwaysUntil != null && time.isAfter(includeAlwaysAfter)
							&& !time.isAfter(includeAlwaysUntil))
							|| (excludeUntil != null && time.isAfter(excludeUntil));
				};
			default:
				return resource -> true;
		}
	}

	String getResourceName(E item) {
		return item.getMetadata().getName();
	}

	ZonedDateTime getResourceTime(E item) {
		return ResourcesTimestampHelper.parseZonedDateTime(item.getMetadata().getCreationTimestamp());
	}

	public ZonedDateTime getExcludedUntil() {
		return excludeUntil;
	}

	public ResourcesFilterBuilder<E> setExcludedUntil(ZonedDateTime until) {
		this.excludeUntil = until;
		return this;
	}

	public ResourcesFilterBuilder<E> setIncludedAlwaysWindow(ZonedDateTime after, ZonedDateTime until) {
		this.includeAlwaysAfter = after;
		this.includeAlwaysUntil = until;
		return this;
	}

	public ResourcesFilterBuilder<E> filterByResourceNames() {
		method = FilterMethod.RESOURCE_NAMES;
		return this;
	}

	public ResourcesFilterBuilder<E> filterByLastSeenResources() {
		method = FilterMethod.PREVIOUSLY_SEEN_RESOURCES;
		return this;
	}

	public ResourcesFilterBuilder<E> setResourceNames(String... resourceNames) {
		this.resourceNames = Arrays.copyOf(resourceNames, resourceNames.length);
		return this;
	}

	@Override
	public ResourcesFilterBuilder<E> clone() throws CloneNotSupportedException {
		return (ResourcesFilterBuilder<E>) super.clone();
	}

	boolean resourceNameMatches(String resourceName, String[] resourceNamesLookup) {
		for (String resourceNameLookup : getRegexResourceNames(resourceNamesLookup)) {
			if (resourceName.matches(resourceNameLookup)) {
				return true;
			}
		}
		return false;
	}

	String[] getRegexResourceNames(String[] resourceNames) {
		String[] result = new String[resourceNames.length];
		for (int i = 0; i < resourceNames.length; i++) {
			result[i] = resourceNames[i] + ".*";
		}
		return result;
	}

	enum FilterMethod {
		RESOURCE_NAMES,
		PREVIOUSLY_SEEN_RESOURCES
	}
}
