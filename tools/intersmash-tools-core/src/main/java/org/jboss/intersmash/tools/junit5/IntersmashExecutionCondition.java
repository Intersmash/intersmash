package org.jboss.intersmash.tools.junit5;

import java.util.Arrays;
import java.util.function.Predicate;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.annotations.Intersmash;
import org.jboss.intersmash.tools.annotations.Service;
import org.jboss.intersmash.tools.application.openshift.OperatorApplication;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

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
			if (IntersmashConfig.isOcp3x() && Arrays.stream(intersmash.value()).anyMatch(isOperatorApplication)) {
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
