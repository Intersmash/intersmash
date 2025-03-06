/**
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
package org.jboss.intersmash.k8s.junit5;

import java.util.Arrays;

import org.jboss.intersmash.k8s.KubernetesConfig;
import org.jboss.intersmash.k8s.client.Kuberneteses;
import org.jboss.intersmash.k8s.client.NamespaceManager;
import org.jboss.intersmash.k8s.client.TestCaseContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.descriptor.MethodBasedTestDescriptor;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KubernetesNamespaceCreator
		implements TestExecutionListener, BeforeAllCallback, AfterAllCallback, PostDiscoveryFilter {

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		NamespaceManager.createIfDoesNotExistsProject(KubernetesConfig.namespace());
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		// todo this can be removed once TestCaseContextExtension is called always before ProjectCreator extension
		setTestExecutionContext(context);

		log.debug("BeforeAll - Test case: " + context.getTestClass().get().getName() + " running in thread name: "
				+ Thread.currentThread().getName()
				+ " will use namespace: " + Kuberneteses.master().getNamespace() + " - thread context is: "
				+ TestCaseContext.getRunningTestCaseName());
		NamespaceManager.createIfDoesNotExistsProject();
	}

	private void setTestExecutionContext(ExtensionContext context) {
		TestCaseContext.setRunningTestCase(context.getTestClass().get().getName());
	}

	@Override
	public void afterAll(ExtensionContext context) {
		if (KubernetesConfig.cleanKubernetes()) {
			NamespaceManager.deleteProjectIfUsedNamespacePerTestCase(false);
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		if (KubernetesConfig.cleanKubernetes()) {
			NamespaceManager.deleteProject(KubernetesConfig.namespace(), true);
		}
	}

	@Override
	public FilterResult apply(TestDescriptor testDescriptor) {
		if (testDescriptor instanceof MethodBasedTestDescriptor) {
			boolean disabled = Arrays.stream(((MethodBasedTestDescriptor) testDescriptor).getTestClass().getAnnotations())
					.filter(annotation -> annotation instanceof Disabled).count() > 0;
			if (!disabled) {
				NamespaceManager.addTestCaseToNamespaceEntryIfAbsent(testDescriptor);
			}
		}
		return FilterResult.included(testDescriptor.getDisplayName());
	}
}
