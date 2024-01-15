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
package org.jboss.intersmash.provision.openshift;

import java.time.ZonedDateTime;

import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.failfast.FailFastBuilder;
import cz.xtf.core.waiting.failfast.FailFastCheck;

public class FailFastUtils {
	private static String[] failFastEventMessages = new String[] {
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
		String[] appNamesRegex = new String[appNames.length];
		for (int i = 0; i < appNames.length; i++) {
			appNamesRegex[i] = appNames[i].concat(".*");
		}
		return FailFastBuilder
				.ofTestAndBuildNamespace()
				.events()
				.ofNames(appNamesRegex)
				.after(after)
				.ofMessages(failFastEventMessages)
				.atLeastOneExists()
				.build();
	}
}
