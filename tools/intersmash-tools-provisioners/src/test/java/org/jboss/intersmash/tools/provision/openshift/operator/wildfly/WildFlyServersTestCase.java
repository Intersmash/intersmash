package org.jboss.intersmash.tools.provision.openshift.operator.wildfly;

import java.io.File;
import java.io.IOException;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Basic verification of wildflyservers.wildfly.org resource.
 */
public class WildFlyServersTestCase {

	/**
	 * Verify that object equals after serialization to file and deserialization back to object.
	 */
	@Test
	public void writeReadEqualsTest() throws IOException {
		WildFlyServer wildFlyServer = new WildFlyServerBuilder("wildfly-operator-test")
				.applicationImage(IntersmashConfig.wildflyImageURL())
				.replicas(1)
				.build();

		// write test
		File yaml = wildFlyServer.save();
		// read test
		WildFlyServer testServer = new WildFlyServer();
		testServer.load(yaml);
		//
		Assertions.assertEquals(wildFlyServer, testServer,
				"OpenShift resource (WildflyServer) does not equal after serialization into yaml file and deserialization back to an object.");
	}
}
