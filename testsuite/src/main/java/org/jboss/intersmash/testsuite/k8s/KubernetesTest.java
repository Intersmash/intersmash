package org.jboss.intersmash.testsuite.k8s;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;

/**
 * Mark test that runs against Kubernetes.
 * Used per class.
 */
@Tag("ts.k8s")
@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.TYPE })
public @interface KubernetesTest {
}
