package org.jboss.intersmash.tools.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inject URL into a field based on the Application FQCN. URL points to endpoint defined by a provisioner.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ServiceUrl {
	Class<?> value();
}
