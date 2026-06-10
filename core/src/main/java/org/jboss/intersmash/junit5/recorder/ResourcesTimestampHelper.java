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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.jboss.intersmash.tools.client.OpenShift;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.Route;

public class ResourcesTimestampHelper {

	static ZonedDateTime lastCreationTime(List<? extends HasMetadata> items) {
		ZonedDateTime result = null;
		for (HasMetadata item : items) {
			ZonedDateTime time = parseZonedDateTime(item.getMetadata().getCreationTimestamp());
			if (result == null || time.isAfter(result)) {
				result = time;
			}
		}
		return result == null ? ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.UTC) : result;
	}

	static ZonedDateTime parseZonedDateTime(String time) {
		return ZonedDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
	}

	public static ZonedDateTime timeOfLastEvent(OpenShift openShift) {
		ZonedDateTime result = null;
		for (Event event : openShift.getEvents()) {
			if (event.getLastTimestamp() == null) {
				continue;
			}
			ZonedDateTime time = parseZonedDateTime(event.getLastTimestamp());
			if (result == null || (time != null && time.isAfter(result))) {
				result = time;
			}
		}
		return result == null ? ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.UTC) : result;
	}

	public static ZonedDateTime timeOfLastResourceOf(OpenShift openShift, Class<? extends HasMetadata> resourceClass) {
		if (resourceClass.equals(Pod.class)) {
			return lastCreationTime(openShift.getPods());
		} else if (resourceClass.equals(DeploymentConfig.class)) {
			return lastCreationTime(openShift.getDeploymentConfigs());
		} else if (resourceClass.equals(BuildConfig.class)) {
			return lastCreationTime(openShift.getBuildConfigs());
		} else if (resourceClass.equals(Build.class)) {
			return lastCreationTime(openShift.getBuilds());
		} else if (resourceClass.equals(ImageStream.class)) {
			return lastCreationTime(openShift.getImageStreams());
		} else if (resourceClass.equals(StatefulSet.class)) {
			return lastCreationTime(openShift.getStatefulSets());
		} else if (resourceClass.equals(Route.class)) {
			return lastCreationTime(openShift.getRoutes());
		} else if (resourceClass.equals(ConfigMap.class)) {
			return lastCreationTime(openShift.getConfigMaps());
		} else if (resourceClass.equals(Service.class)) {
			return lastCreationTime(openShift.getServices());
		} else if (resourceClass.equals(Event.class)) {
			return timeOfLastEvent(openShift);
		} else {
			throw new RuntimeException("Unsupported resource - " + resourceClass.getName());
		}
	}
}
