package org.jboss.intersmash.testsuite.junit5.categories;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a test that does not support community deliverables.
 * Used per class.
 */
@Tag("ts.not-for-community")
@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.TYPE })
public @interface NotForCommunityExecutionProfile {
}
