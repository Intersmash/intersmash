package org.jboss.intersmash.tools.application.openshift;

import java.util.Collections;
import java.util.List;

import org.jboss.intersmash.tools.application.openshift.input.BuildInput;
import org.jboss.intersmash.tools.provision.openshift.Eap7ImageOpenShiftProvisioner;

import io.fabric8.kubernetes.api.model.EnvVar;

/**
 * End user Application descriptor interface which presents a AP 7.z (i.e. Jakarta EE 8 based WildFly) application on
 * OpenShift Container Platform.
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link Eap7ImageOpenShiftProvisioner}</li>
 * </ul>
 */
public interface Eap7ImageOpenShiftApplication extends WildflyImageOpenShiftApplication, HasEnvVars {

	/**
	 * Use the {@link org.jboss.intersmash.tools.application.openshift.input.BuildInputBuilder} to get instances
	 * implementing the {@link org.jboss.intersmash.tools.application.openshift.input.BuildInput} interface.
	 *
	 * @see org.jboss.intersmash.tools.application.openshift.input.GitSourceBuilder
	 * @see org.jboss.intersmash.tools.application.openshift.input.BinarySourceBuilder
	 * @return {@link org.jboss.intersmash.tools.application.openshift.input.BuildInput} instance for application
	 */
	BuildInput getBuildInput();

	@Override
	default List<EnvVar> getEnvVars() {
		return Collections.emptyList();
	}

	/**
	 * Override this in the implementation class to return a non-null value in case you want to create a ping service
	 * to be used for DNS_PING clustering. The provisioner will take care of setting up all the ENV variables needed
	 * for clustering to work.
	 * If this method returns null, no service will be created.
	 *
	 * @return The name of the ping-service that will be created.
	 */
	default String getPingServiceName() {
		return null;
	}
}
