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
package org.jboss.intersmash.provision.olm.wildfly;

import java.io.File;
import java.io.IOException;

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.provision.olm.SerializationCapableResource;
import org.jboss.intersmash.provision.operator.model.wildfly.WildFlyServerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.v1alpha1.WildFlyServer;

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
		File yaml = SerializationCapableResource.save(wildFlyServer);
		// read test
		WildFlyServer testServer = new WildFlyServer();
		SerializationCapableResource.load(yaml, WildFlyServer.class, testServer);
		//
		Assertions.assertEquals(wildFlyServer, testServer,
				"OpenShift resource (WildflyServer) does not equal after serialization into yaml file and deserialization back to an object.");
	}
}
