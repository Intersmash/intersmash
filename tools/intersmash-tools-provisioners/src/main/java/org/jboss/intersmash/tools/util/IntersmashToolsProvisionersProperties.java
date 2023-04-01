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
