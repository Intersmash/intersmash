package org.jboss.intersmash.deployments;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IntersmashSharedDeploymentsPropertiesTest {
	@Test
	public void versionTest() {
		Assertions.assertNotNull(IntersmashSharedDeploymentsProperties.version());
	}

	@Test
	public void groupIdTest() {
		Assertions.assertNotNull(IntersmashSharedDeploymentsProperties.groupID());
	}
}
