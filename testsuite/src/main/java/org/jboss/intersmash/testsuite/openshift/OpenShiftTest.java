package org.jboss.intersmash.testsuite.openshift;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark test that runs against OpenShift.
 * Used per class.
 */
@Tag("ts.openshift")
@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.TYPE })
public @interface OpenShiftTest {
}
