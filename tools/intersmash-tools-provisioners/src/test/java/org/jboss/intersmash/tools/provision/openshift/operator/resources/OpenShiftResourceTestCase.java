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
package org.jboss.intersmash.tools.provision.openshift.operator.resources;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import org.jboss.intersmash.tools.provision.openshift.operator.OperatorProvisioner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verify the functionality provided by {@link OpenShiftResource} interface.
 */
public class OpenShiftResourceTestCase {

	private static Stream<OpenShiftResource> resourceProvider() {
		return Stream.of(
				new OperatorGroup("my-namepace"),
				new Subscription(
						"openshift-marketplace",
						"my-namespace",
						"redhat-operators",
						"dummy-operator",
						"alpha",
						OperatorProvisioner.INSTALLPLAN_APPROVAL_MANUAL),
				new CatalogSource(
						"my-custom-cs",
						"my-namespace",
						"grpc",
						"quay.io/operatorhubio/catalog:latest",
						"My Custom Operators",
						"Intersmash"));
	}

	/**
	 * Verify that object equals after serialization to file and deserialization back to object.
	 */

	@ParameterizedTest(name = "{displayName}#class({0})")
	@MethodSource("resourceProvider")
	public void writeReadEqualsTest(OpenShiftResource resource) throws IOException, NoSuchMethodException,
			InvocationTargetException, InstantiationException, IllegalAccessException {
		// write test
		File yaml = resource.save();
		// read test

		OpenShiftResource loaded = resource.getClass().getDeclaredConstructor().newInstance();
		loaded.load(yaml);
		//
		Assertions.assertEquals(resource, loaded,
				"OpenShift resource (" + resource.getClass().getSimpleName()
						+ ") does not equal after serialization into yaml file and deserialization back to an object.");
	}
}
