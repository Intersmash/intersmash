package org.jboss.intersmash.deployments;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IntersmashSharedDeploymentsTest {
	@Test
	public void BootableJarBareMetalAvailable() {
		Path bareMetalApp = IntersmashSharedDeployments.bootableJarDemoBareMetal();
		Assertions.assertNotNull(bareMetalApp);
		Assertions.assertTrue(Files.exists(bareMetalApp));
	}

	@Test
	public void BootableJarOpenShiftAvailable() {
		Path openShiftApp = IntersmashSharedDeployments.bootableJarDemoOpenShift();
		Assertions.assertNotNull(openShiftApp);
		Assertions.assertTrue(Files.exists(openShiftApp));
	}

	@Test
	public void BootableJarNotSame() {
		Path bareMetalApp = IntersmashSharedDeployments.bootableJarDemoBareMetal();
		Path openShiftApp = IntersmashSharedDeployments.bootableJarDemoOpenShift();
		Assertions.assertNotEquals(bareMetalApp.toAbsolutePath().toString(), openShiftApp.toAbsolutePath().toString());
	}
}
