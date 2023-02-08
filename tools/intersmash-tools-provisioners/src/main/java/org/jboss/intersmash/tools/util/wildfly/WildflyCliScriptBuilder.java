package org.jboss.intersmash.tools.util.wildfly;

import java.util.List;

/**
 * Class provide supports for building CLI commands that are used to configure WILDFLY on OpenShift via custom scripts and
 * config map or on Baremetal
 */
public class WildflyCliScriptBuilder extends WildflyAbstractCliScriptBuilder {
	public List<String> build() {
		return build(null);
	}
}
