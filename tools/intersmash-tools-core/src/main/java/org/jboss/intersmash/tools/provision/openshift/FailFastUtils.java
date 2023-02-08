package org.jboss.intersmash.tools.provision.openshift;

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
