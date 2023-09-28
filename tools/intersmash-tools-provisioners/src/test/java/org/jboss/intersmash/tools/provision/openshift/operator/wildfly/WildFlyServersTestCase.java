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
package org.jboss.intersmash.tools.provision.openshift.operator.wildfly;

import java.io.File;
import java.io.IOException;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.OpenShiftResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.v1alpha1.WildFlyServer;

import io.fabric8.kubernetes.client.CustomResource;

/**
 * Basic verification of wildflyservers.wildfly.org resource.
 */
public class WildFlyServersTestCase {

	class WildFlyServerSerializableResource
			extends CustomResource<org.wildfly.v1alpha1.WildFlyServerSpec, org.wildfly.v1alpha1.WildFlyServerStatus>
			implements OpenShiftResource<WildFlyServerSerializableResource> {

		private final WildFlyServer wildFlyServer;

		WildFlyServerSerializableResource(WildFlyServer wildFlyServer) {
			this.wildFlyServer = wildFlyServer;
		}

		@Override
		public WildFlyServerSerializableResource load(WildFlyServerSerializableResource loaded) {
			this.wildFlyServer.setMetadata(loaded.getMetadata());
			this.wildFlyServer.setSpec(loaded.getSpec());
			return this;
		}
	}

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
		final WildFlyServerSerializableResource serde = new WildFlyServerSerializableResource(wildFlyServer);
		File yaml = serde.save();
		// read test
		WildFlyServer testServer = new WildFlyServer();
		serde.load(yaml);
		//
		Assertions.assertEquals(wildFlyServer, testServer,
				"OpenShift resource (WildflyServer) does not equal after serialization into yaml file and deserialization back to an object.");
	}
}
