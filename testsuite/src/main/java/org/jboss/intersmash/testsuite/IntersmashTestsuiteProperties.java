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
