package org.jboss.intersmash.tools.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Class loads intersmash-tools-provisioners.properties from resources and provides method to get properties.
 */
public class IntersmashToolsProvisionersProperties {
	static Properties properties;
	private static String WILDFLY_DEPLOYMENTS_BUILD_PROFILE = "wildflyDeploymentsBuildProfile";
	public static String WILDFLY_DEPLOYMENTS_BUILD_PROFILE_VALUE_WILDFLY = "wildfly";
	public static String WILDFLY_DEPLOYMENTS_BUILD_PROFILE_VALUE_EAP = "eap";
	public static String WILDFLY_DEPLOYMENTS_BUILD_PROFILE_VALUE_EAP_XP = "eapxp";

	static {
		properties = new Properties();
		try {
			properties
					.load(IntersmashToolsProvisionersProperties.class.getClassLoader()
							.getResourceAsStream("intersmash-tools-provisioners.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getWildflyDeploymentsBuildProfile() {
		return properties.getProperty(WILDFLY_DEPLOYMENTS_BUILD_PROFILE);
	}

}
