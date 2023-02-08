package org.jboss.intersmash.tools.provision.openshift.operator.resources;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CatalogSourceTest {

	/**
	 * Verifies serialization/deserialization preserve variables
	 */
	@Test
	public void testSerialization() throws IOException {
		String name = "redhat-operator-index-alias";
		String namespace = "dummy-ns";
		String sourceType = "grpc";
		String indexImage = "some.registry/some-user/some-index:v4.9";
		String displayName = "Dummy alias";
		String publisher = "jboss-tests@redhat.com";
		CatalogSource catalogSource = new CatalogSource(
				name,
				namespace,
				sourceType,
				indexImage,
				displayName,
				publisher);
		File catalogSourceFile = catalogSource.save();
		CatalogSource catalogSourceLoaded = new CatalogSource();
		catalogSourceLoaded.load(catalogSourceFile);
		Assertions.assertEquals(catalogSourceLoaded.getMetadata().getName(), name);
		Assertions.assertEquals(catalogSourceLoaded.getMetadata().getNamespace(), namespace);
		Assertions.assertEquals(catalogSourceLoaded.getSpec().getSourceType(), sourceType);
		Assertions.assertEquals(catalogSourceLoaded.getSpec().getImage(), indexImage);
		Assertions.assertEquals(catalogSourceLoaded.getSpec().getDisplayName(), displayName);
		Assertions.assertEquals(catalogSourceLoaded.getSpec().getPublisher(), publisher);
	}
}
