package org.jboss.intersmash.tools.provision.openshift.operator.resources;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Verify the functionality provided by {@link OpenShiftResource} interface.
 */
public class OpenShiftResourceTestCase {

	/**
	 * Verify that object equals after serialization to file and deserialization back to object.
	 */
	@Test
	public void writeReadEqualsTest() throws IOException {
		// write test
		File yaml = OperatorGroup.SINGLE_NAMESPACE.save();
		// read test
		OpenShiftResource testGroup = new OperatorGroup();
		testGroup.load(yaml);
		//
		Assertions.assertEquals(OperatorGroup.SINGLE_NAMESPACE, testGroup,
				"OpenShift resource (OperatorGroup) does not equal after serialization into yaml file and deserialization back to an object.");
	}
}
