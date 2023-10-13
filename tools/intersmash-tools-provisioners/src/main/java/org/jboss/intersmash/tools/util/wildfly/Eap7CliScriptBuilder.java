package org.jboss.intersmash.tools.util.wildfly;

import java.util.List;

/**
 * Class provide supports for building CLI commands that are used to configure an EAP 7.z (i.e. Jakarta EE 8 based
 * WildFly) application on OpenShift via custom scripts and config map
 */
public class Eap7CliScriptBuilder extends WildflyAbstractCliScriptBuilder {
	public List<String> build() {
		return build("standalone-openshift.xml");
	}
}
