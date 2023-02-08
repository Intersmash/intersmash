package org.jboss.intersmash.tools.application.openshift;

import org.jboss.intersmash.tools.provision.openshift.WildflyOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.WildFlyServer;

/**
 * End user Application interface which presents WILDFLY operator application on OpenShift Container Platform.
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link WildflyOperatorProvisioner}</li>
 * </ul>
 */
public interface WildflyOperatorApplication extends OperatorApplication {
	String NAME = "wildfly-operator";

	WildFlyServer getWildflyServer();
}
