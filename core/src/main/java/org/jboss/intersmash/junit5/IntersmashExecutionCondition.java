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
package org.jboss.intersmash.junit5;

import java.util.Arrays;
import java.util.function.Predicate;

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.annotations.Intersmash;
import org.jboss.intersmash.annotations.Service;
import org.jboss.intersmash.application.openshift.OperatorApplication;
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
		Intersmash[] intersmashes = context.getRequiredTestClass().getAnnotationsByType(Intersmash.class);
		Intersmash intersmash;
		if (intersmashes.length > 0) {
			intersmash = intersmashes[0];
			log.debug("Running: {}", context.getRequiredTestClass().getSimpleName());
			if (IntersmashConfig.isOcp3x(OpenShifts.admin())
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
