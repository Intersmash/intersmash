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
package org.jboss.intersmash.util.openshift;

import java.util.concurrent.TimeUnit;

import org.jboss.intersmash.tools.client.OpenShifts;
import org.jboss.intersmash.tools.waiting.SimpleWaiter;

import io.fabric8.openshift.api.model.ImageStreamBuilder;
import io.fabric8.openshift.api.model.ImageStreamTag;
import io.fabric8.openshift.api.model.TagReferenceBuilder;

/**
 * The OpenShift Container Platform utilities related to WILDFLY services.
 *
 * The class should follow the Wildfly Applications and Provisioners.
 *
 * TODO Move the class, with Wildfly Application and Provisioner classes, to separate module in case of refactoring
 */
public class WildflyOpenShiftUtils {

	/**
	 * Import tested WILDFLY builder image into OCP, create a new image stream in test namespace and return image stream
	 * tag reference.
	 *
	 * @return builder image stream tag reference
	 */
	public static ImageStreamTag importBuilderImage(final String imageUrl) {
		return createImageStream(imageUrl, "wildfly-openshift", "latest");
	}

	/**
	 * Import tested WILDFLY runtime image into OCP, create a new image stream in test namespace and return image stream
	 * tag reference.
	 *
	 * @return runtime image stream tag reference
	 */
	public static ImageStreamTag importRuntimeImage(final String imageUrl) {
		return createImageStream(imageUrl, "wildfly-runtime-openshift", "latest");
	}

	public static ImageStreamTag createImageStream(String imageUrl, String name, String tag) {
		OpenShifts.master().createImageStream(
				new ImageStreamBuilder()
						.withNewMetadata().withName(name)
						.addToAnnotations("openshift.io/image.insecureRepository", "true")
						.endMetadata()
						.withNewSpec()
						.withTags(new TagReferenceBuilder()
								.withName(tag)
								.withNewImportPolicy().withInsecure(true).endImportPolicy()
								.withNewFrom().withKind("DockerImage").withName(imageUrl).endFrom()
								.build())
						.endSpec()
						.build());
		new SimpleWaiter(() -> OpenShifts.master().getImageStreamTag(name, tag) != null, TimeUnit.SECONDS, 10,
				String.format("Waiting for %s:%s image stream tag to be created", name, tag)).waitFor();
		return OpenShifts.master().getImageStreamTag(name, tag);
	}
}
