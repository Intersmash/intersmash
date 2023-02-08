package org.jboss.intersmash.tools.util.openshift;

import java.util.concurrent.TimeUnit;

import cz.xtf.core.image.Image;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.openshift.api.model.ImageStreamTag;

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

	public static ImageStreamTag createImageStream(String image, String name, String tag) {
		OpenShifts.master().createImageStream(Image.from(image).getImageStream(name, tag));
		new SimpleWaiter(() -> OpenShifts.master().getImageStreamTag(name, tag) != null, TimeUnit.SECONDS, 10,
				String.format("Waiting for %s:%s image stream tag to be created", name, tag)).waitFor();
		return OpenShifts.master().getImageStreamTag(name, tag);
	}
}
