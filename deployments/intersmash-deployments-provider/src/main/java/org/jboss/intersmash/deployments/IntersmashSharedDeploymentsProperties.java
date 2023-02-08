package org.jboss.intersmash.deployments;

import java.io.IOException;
import java.util.Properties;

/**
 * Class loads .properties from resources and provides method to get properties.
 */
public class IntersmashSharedDeploymentsProperties {
	static Properties properties;
	private static String VERSION = "version";
	private static String GROUPD_ID = "groupID";
	private static String DEPLOYMENTS_PROVIDER_PATH = "deploymentsProviderPath";
	private static String WILDFLY_DEPLOYMENTS_BUILD_PROFILE = "wildflyDeploymentsBuildProfile";
	public static String WILDFLY_DEPLOYMENTS_BUILD_PROFILE_VALUE_WILDFLY = "wildfly";
	public static String WILDFLY_DEPLOYMENTS_BUILD_PROFILE_VALUE_EAP = "eap";
	public static String WILDFLY_DEPLOYMENTS_BUILD_PROFILE_VALUE_EAP_XP = "eapxp";

	static {
		properties = new Properties();
		try {
			properties.load(IntersmashSharedDeploymentsProperties.class.getClassLoader().getResourceAsStream(".properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static String version() {
		return properties.getProperty(VERSION);
	}

	static String groupID() {
		return properties.getProperty(GROUPD_ID);
	}

	static String getDeploymentsProviderPath() {
		return properties.getProperty(DEPLOYMENTS_PROVIDER_PATH);
	}

	public static String getWildflyDeploymentsBuildProfile() {
		return properties.getProperty(WILDFLY_DEPLOYMENTS_BUILD_PROFILE);
	}
}
