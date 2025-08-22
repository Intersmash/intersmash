/*
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
package org.jboss.intersmash.testsuite;

import java.io.IOException;
import java.util.Properties;

public class IntersmashTestsuiteProperties {
	static Properties properties;
	private static String TEST_EXECUTION_PROFILE = "testExecutionProfile";
	public static String TEST_EXECUTION_PROFILE_VALUE_COMMUNITY = "community";
	public static String TEST_EXECUTION_PROFILE_VALUE_PRODUCTIZED = "prod";

	static {
		properties = new Properties();
		try {
			properties
					.load(IntersmashTestsuiteProperties.class.getClassLoader()
							.getResourceAsStream("intersmash-testsuite.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getTestExecutionProfile() {
		return properties.getProperty(TEST_EXECUTION_PROFILE);
	}

	public static Boolean isCommunityTestExecutionProfileEnabled() {
		return TEST_EXECUTION_PROFILE_VALUE_COMMUNITY.equals(getTestExecutionProfile());
	}

	public static Boolean isProductizedTestExecutionProfileEnabled() {
		return TEST_EXECUTION_PROFILE_VALUE_PRODUCTIZED.equals(getTestExecutionProfile());
	}
}
