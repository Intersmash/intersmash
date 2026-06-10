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
package org.jboss.intersmash.provision.openshift;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jboss.intersmash.tools.client.OpenShiftWaiters;
import org.jboss.intersmash.tools.client.OpenShifts;
import org.jboss.intersmash.tools.waiting.failfast.FailFastCheck;

import io.fabric8.kubernetes.api.model.Event;

/**
 * Helper class that provides fail-fast APIs based on OpenShift events, used to control the
 * Intersmash provisioning workflow.
 *
 * <p>When a fail-fast check detects error events (e.g. failed image pulls, failed builds, mount errors),
 * the associated waiter will fail immediately rather than waiting for a timeout.</p>
 */
public class FailFastUtils {
	private static final String[] failFastEventMessages = new String[] {
			"Failed to pull image.*",
			"Build.*failed",
			"Error syncing pod, skipping: failed to.*",
			"FailedMount MountVolume.SetUp.*",
			"Error creating: pods.*is forbidden:.*",
			".*failed to fit in any node.*",
			"Failed to attach volume.*"
	};

	public static OpenShiftWaiters failFastWaitersFor(ZonedDateTime after, String... appNames) {
		return OpenShiftWaiters.get(OpenShifts.master(), getFailFastCheck(after, appNames));
	}

	public static FailFastCheck getFailFastCheck(ZonedDateTime after, String... appNames) {
		final Pattern[] namePatterns = Arrays.stream(appNames)
				.map(name -> Pattern.compile(name + ".*"))
				.toArray(Pattern[]::new);
		final Pattern[] messagePatterns = Arrays.stream(failFastEventMessages)
				.map(Pattern::compile)
				.toArray(Pattern[]::new);

		return new FailFastCheck() {
			private String reason;

			@Override
			public boolean hasFailed() {
				List<Event> events = OpenShifts.master().v1().events().list().getItems();
				List<Event> matchingEvents = events.stream()
						.filter(event -> matchesAnyName(event, namePatterns))
						.filter(event -> isAfter(event, after))
						.filter(event -> matchesAnyMessage(event, messagePatterns))
						.collect(Collectors.toList());
				if (!matchingEvents.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					sb.append("Fail-fast triggered by matching events:\n");
					for (Event e : matchingEvents) {
						sb.append("\t")
								.append(e.getLastTimestamp())
								.append("\t")
								.append(e.getInvolvedObject().getKind())
								.append("/")
								.append(e.getInvolvedObject().getName())
								.append("\t")
								.append(e.getMessage())
								.append("\n");
					}
					reason = sb.toString();
					return true;
				}
				return false;
			}

			@Override
			public String reason() {
				return reason != null ? reason : "reason not specified";
			}
		};
	}

	private static boolean matchesAnyName(Event event, Pattern[] namePatterns) {
		if (event.getInvolvedObject() == null || event.getInvolvedObject().getName() == null) {
			return false;
		}
		String name = event.getInvolvedObject().getName();
		return Arrays.stream(namePatterns).anyMatch(p -> p.matcher(name).matches());
	}

	private static boolean isAfter(Event event, ZonedDateTime after) {
		if (after == null) {
			return true;
		}
		String timestamp = event.getLastTimestamp();
		if (timestamp == null || timestamp.isEmpty()) {
			return false;
		}
		try {
			ZonedDateTime eventTime = ZonedDateTime.parse(timestamp);
			return eventTime.isAfter(after);
		} catch (DateTimeParseException e) {
			// If the timestamp format doesn't parse directly, try ISO format
			return false;
		}
	}

	private static boolean matchesAnyMessage(Event event, Pattern[] messagePatterns) {
		if (event.getMessage() == null) {
			return false;
		}
		String message = event.getMessage();
		return Arrays.stream(messagePatterns).anyMatch(p -> p.matcher(message).matches());
	}
}
