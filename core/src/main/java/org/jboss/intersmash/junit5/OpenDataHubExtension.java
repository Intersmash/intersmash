/**
 * Copyright (C) 2024 Red Hat, Inc.
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
package org.jboss.intersmash.junit5;

import java.io.IOException;

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.provision.ai.OpenDataHubSetupManager;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

import cz.xtf.core.config.OpenShiftConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenDataHubExtension implements TestExecutionListener {
	protected static boolean handleOpenDataHubSetup = IntersmashConfig.OPEN_DATA_HUB_INSTALL_MODE_UNMANAGED.equals(
			IntersmashConfig.openDataHubUnmanagedInstallClass());

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		if (handleOpenDataHubSetup) {
			// Let the ODH Manager place a subscription to track that this session is handled.
			// Note: the configured test namespace is used as identifier, so this might not be reliable in case
			// a namespace is reused
			try {
				OpenDataHubSetupManager.getInstance().subscribe(OpenShiftConfig.namespace());
			} catch (IOException e) {
				throw new IllegalStateException("Cannot subscribe to the Intersmash Open Data Hub manager: " + e);
			}
			// deploy pre-requisites
			try {
				if (!OpenDataHubSetupManager.getInstance().isReady() && !OpenDataHubSetupManager.getInstance().isInstalling()) {
					OpenDataHubSetupManager.getInstance().setup();
				}
			} catch (IOException e) {
				throw new IllegalStateException("Cannot deploy Open Data Hub prerequisites: " + e);
			}
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		if (handleOpenDataHubSetup) {
			// Let the ODH Manager remove the subscription that tracks that this session is handled.
			// Note: the configured test namespace is used as identifier, so this might not be reliable in case
			// a namespace is reused
			try {
				OpenDataHubSetupManager.getInstance().unsubscribe(OpenShiftConfig.namespace());
			} catch (IOException e) {
				throw new IllegalStateException(
						"Cannot remove a subscription from the Intersmash the Open Data Hub manager: " + e);
			}
			// undeploy pre-requisites, as long as there are no more subscribers
			try {
				if (OpenDataHubSetupManager.getInstance().getSubscribers().isEmpty()
						&& !OpenDataHubSetupManager.getInstance().isInstalling()) {
					OpenDataHubSetupManager.getInstance().tearDown();
				}
			} catch (IOException e) {
				throw new IllegalStateException("Cannot undeploy Open Data Hub prerequisites: " + e);
			}

		}
	}
}
