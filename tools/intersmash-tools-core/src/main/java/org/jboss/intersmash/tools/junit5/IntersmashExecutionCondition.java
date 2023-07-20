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
package org.jboss.intersmash.tools.junit5;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.annotations.Intersmash;
import org.jboss.intersmash.tools.annotations.Service;
import org.jboss.intersmash.tools.application.operator.OperatorApplication;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import cz.xtf.core.openshift.OpenShifts;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntersmashExecutionCondition implements ExecutionCondition {
	private static final Predicate<Service> isOperatorApplication = (application) -> OperatorApplication.class
			.isAssignableFrom(application.value());

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		final List<String> targets = Arrays.stream(IntersmashConfig.getJunit5ExecutionTargets())
				.collect(Collectors.toList());
		// log what the configured JUnit 5 target execution environment names are
		log.debug("Configured JUnit 5 execution environments: {}", targets.stream().collect(Collectors.joining(",")));

		Intersmash[] intersmashes = context.getRequiredTestClass().getAnnotationsByType(Intersmash.class);
		Intersmash intersmash;
		if (intersmashes.length > 0) {
			intersmash = intersmashes[0];

			// Skip tests that don't fit the environments supported by the actual test execution environment
			if (IntersmashExtensionHelper.isIntersmashTargetingOpenShift(context)
					&& !IntersmashConfig.testEnvironmentSupportsOpenShift()) {
				return ConditionEvaluationResult.disabled(
						"An @Intersmash service is set to target OpenShift which has not been configured for the current execution.");
			}
			if (IntersmashExtensionHelper.isIntersmashTargetingKubernetes(context)
					&& !IntersmashConfig.testEnvironmentSupportsKubernetes()) {
				return ConditionEvaluationResult.disabled(
						"An @Intersmash service is set to target Kubernetes which has not been configured for the current execution.");
			}
			log.debug("Running: {}", context.getRequiredTestClass().getSimpleName());

			// evaluate peculiar OpenShift/Kubernetes requirements
			if (IntersmashExtensionHelper.isIntersmashTargetingOpenShift(context)
					&& IntersmashConfig.isOcp3x(OpenShifts.admin())
					&& Arrays.stream(intersmash.value()).anyMatch(isOperatorApplication)) {
				return ConditionEvaluationResult.disabled("OLM is not available on OCP 3.x clusters, " +
						"skip the tests due to OperatorApplication(s) involvement.");
			}
			log.debug("Test enabled.");
			return ConditionEvaluationResult.enabled("@Intersmash present.");
		} else {
			log.debug("Intersmash disabled - @Intersmash annotation not present.");
			return ConditionEvaluationResult.enabled("Regular test - missing @Intersmash annotation.");
		}
	}
}
