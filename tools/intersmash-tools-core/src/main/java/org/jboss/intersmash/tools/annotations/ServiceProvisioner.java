package org.jboss.intersmash.tools.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.intersmash.tools.provision.openshift.OpenShiftProvisioner;

/**
 * Inject {@link OpenShiftProvisioner} into a field based on the Application FQCN.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ServiceProvisioner {
	Class<?> value();
}
