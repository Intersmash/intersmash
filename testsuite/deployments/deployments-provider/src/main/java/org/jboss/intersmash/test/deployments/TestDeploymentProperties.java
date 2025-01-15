/**
 * Copyright (C) 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.intersmash.test.deployments;

import java.io.IOException;
import java.util.Properties;

/**
 * Class loads .properties from resources and provides method to get properties.
 */
public class TestDeploymentProperties {
	static Properties properties;
	private static final String VERSION = "version";
	private static final String GROUPD_ID = "groupID";
	private static final String DEPLOYMENTS_PROVIDER_PATH = "deploymentsProviderPath";
	private static final String WILDFLY_DEPLOYMENTS_BUILD_PROFILE = "wildflyDeploymentsBuildProfile";
	public static final String WILDFLY_DEPLOYMENTS_BUILD_PROFILE_VALUE_EAP = "eap";
	public static final String WILDFLY_DEPLOYMENTS_BUILD_PROFILE_VALUE_EAP_XP = "eapxp";
	private static final String WILDFLY_DEPLOYMENTS_BUILD_STREAM = "wildflyDeploymentsBuildStream";
	public static final String WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_COMMUNITY = "community";
	public static final String WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_EAP_80 = "eap80";
	public static final String WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_EAP_81 = "eap81";
	public static final String WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_EAP_XP5 = "eapxp5";
	public static final String WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_EAP_XP6 = "eapxp6";

	static {
		properties = new Properties();
		try {
			properties.load(TestDeploymentProperties.class.getClassLoader().getResourceAsStream(".properties"));
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

	public static String getWildflyDeploymentsBuildStream() {
		return properties.getProperty(WILDFLY_DEPLOYMENTS_BUILD_STREAM);
	}

	public static Boolean isWildFlyDeploymentsBuildProfileEnabled() {
		return "".equals(getWildflyDeploymentsBuildProfile());
	}

	public static Boolean isCommunityDeploymentsBuildStreamEnabled() {
		return WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_COMMUNITY.equals(getWildflyDeploymentsBuildStream());
	}

	public static Boolean isEapDeploymentsBuildProfileEnabled() {
		return WILDFLY_DEPLOYMENTS_BUILD_PROFILE_VALUE_EAP.equals(getWildflyDeploymentsBuildProfile());
	}

	public static Boolean isEap80DeploymentsBuildStreamEnabled() {
		return WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_EAP_80.equals(getWildflyDeploymentsBuildStream());
	}

	public static Boolean isEap81DeploymentsBuildStreamEnabled() {
		return WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_EAP_81.equals(getWildflyDeploymentsBuildStream());
	}

	public static Boolean isEapXpDeploymentsBuildProfileEnabled() {
		return WILDFLY_DEPLOYMENTS_BUILD_PROFILE_VALUE_EAP_XP.equals(getWildflyDeploymentsBuildProfile());
	}

	public static Boolean isEapXp5DeploymentsBuildStreamEnabled() {
		return WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_EAP_XP5.equals(getWildflyDeploymentsBuildStream());
	}

	public static Boolean isEapXp6DeploymentsBuildStreamEnabled() {
		return WILDFLY_DEPLOYMENTS_BUILD_STREAM_VALUE_EAP_XP6.equals(getWildflyDeploymentsBuildStream());
	}
}
