package org.jboss.intersmash.tools.application.openshift;

import java.util.List;

import org.jboss.intersmash.tools.provision.openshift.ActiveMQOperatorProvisioner;

import io.amq.broker.v1beta1.ActiveMQArtemis;
import io.amq.broker.v1beta1.ActiveMQArtemisAddress;

/**
 * End user Application interface which presents ActiveMQ operator application on OpenShift Container Platform.
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link ActiveMQOperatorProvisioner}</li>
 * </ul>
 */
public interface ActiveMQOperatorApplication extends OperatorApplication {

	ActiveMQArtemis getActiveMQArtemis();

	List<ActiveMQArtemisAddress> getActiveMQArtemisAddresses();

	//	ActiveMQArtemisScaledowns getActiveMQArtemisScaledowns(); // TODO add on demand
}
