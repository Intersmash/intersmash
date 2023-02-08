package org.jboss.intersmash.tools.application.openshift;

import java.util.List;

import org.jboss.intersmash.tools.provision.openshift.InfinispanOperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.Cache;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.Infinispan;

/**
 * End user Application interface which presents Infinispan operator application on OpenShift Container Platform.
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link InfinispanOperatorProvisioner}</li>
 * </ul>
 */
public interface InfinispanOperatorApplication extends OperatorApplication {

	Infinispan getInfinispan();

	List<Cache> getCaches();
}
