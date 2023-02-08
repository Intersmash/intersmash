package org.jboss.intersmash.tools.application.openshift;

import java.util.Collections;
import java.util.List;

import org.jboss.intersmash.tools.application.openshift.input.BinarySource;
import org.jboss.intersmash.tools.provision.openshift.WildflyBootableJarImageOpenShiftProvisioner;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Secret;

/**
 * End user Application interface which presents WILDFLY bootable jar application on OpenShift Container Platform.
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link WildflyBootableJarImageOpenShiftProvisioner}</li>
 * </ul>
 */
public interface BootableJarOpenShiftApplication extends OpenShiftApplication, HasSecrets, HasEnvVars {

	BinarySource getBuildInput();

	@Override
	default List<Secret> getSecrets() {
		return Collections.emptyList();
	}

	@Override
	default List<EnvVar> getEnvVars() {
		return Collections.emptyList();
	}
}
