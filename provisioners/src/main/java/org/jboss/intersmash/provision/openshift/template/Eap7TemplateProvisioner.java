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
package org.jboss.intersmash.provision.openshift.template;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.intersmash.IntersmashConfig;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.TagImportPolicyBuilder;

public class Eap7TemplateProvisioner implements OpenShiftTemplateProvisioner {

	// supported product versions
	// EAP
	public static final String SUPPORTED_EAP_VERSION_74 = "eap74";
	public static final String SUPPORTED_EAP_VERSION_XP4 = "eap-xp4";
	public static final String SUPPORTED_JDK_OPENJDK_8 = "openjdk8";
	public static final String SUPPORTED_JDK_OPENJDK_11 = "openjdk11";
	public static final String SUPPORTED_JDK_OPENJDK_17 = "openjdk17";

	public static final String[] SUPPORTED_EAP_VERSIONS = { SUPPORTED_EAP_VERSION_74, SUPPORTED_EAP_VERSION_XP4 };
	public static final String[] SUPPORTED_JDK_VERSIONS = { SUPPORTED_JDK_OPENJDK_8, SUPPORTED_JDK_OPENJDK_11,
			SUPPORTED_JDK_OPENJDK_17 };

	@Override
	public String getTemplatesUrl() {
		return IntersmashConfig.eap7Templates();
	}

	@Override
	public String getTemplateFileName(OpenShiftTemplate openShiftTemplate) {
		final String eapProductCode = getProductCode();
		final String eapJdk = getEapJdk();
		// validate used EAP image product code is allowed
		if (!List.of(SUPPORTED_EAP_VERSIONS).stream().anyMatch(allowed -> allowed.equals(eapProductCode))) {
			throw new IllegalStateException(String.format("Unsupported EAP product code: %s", eapProductCode));
		}

		// validate used eap image JDK is allowed
		if (!List.of(SUPPORTED_JDK_VERSIONS).stream().anyMatch(allowed -> allowed.equals(eapJdk))) {
			throw new IllegalStateException("Unsupported JDK version: " + eapJdk);
		}
		// E.g.:
		// https://raw.githubusercontent.com/jboss-container-images/jboss-eap-openshift-templates/eap74/templates/eap74-basic-s2i.json
		// https://raw.githubusercontent.com/jboss-container-images/jboss-eap-openshift-templates/eap74/templates/eap74-https-s2i.json
		return String.format("%s-%s-s2i", eapProductCode, openShiftTemplate.getLabel());
	}

	/**
	 * This is a bit different for EAP since the image stream template contains two image streams, one for builder
	 * image, the other for runtime image. Create both image streams here, but return the list of builder image references
	 * in order to be aligned with implementations for other products.
	 * <p>
	 * Update only DockerImage based tags, as the others are just tag references.
	 *
	 * @return list of builder image stream references
	 */
	@Override
	public List<ImageStream> deployImageStreams() {
		List<ImageStream> streams = new ArrayList<>(2);
		String url = getUsedImageStreamUrl();
		try (InputStream is = new URL(url).openStream()) {
			List<HasMetadata> kubernetesList = openShift.load(is).items();
			for (HasMetadata item : kubernetesList) {
				if (item.getMetadata().getName().contains("runtime")) {
					ImageStream runtimeImageStream = (ImageStream) item;
					// update the DockerImage based tags with EAP runtime image set by configuration
					runtimeImageStream.getSpec().getTags().stream()
							.filter(tagReference -> tagReference.getFrom().getKind().equals("DockerImage"))
							.forEach(tagReference -> {
								tagReference.getFrom().setName(IntersmashConfig.eap7ImageURL());
								tagReference.setImportPolicy(new TagImportPolicyBuilder().withInsecure(true).build());
							});
					streams.add(openShift.imageStreams().createOrReplace(runtimeImageStream));
				} else {
					ImageStream imageStream = (ImageStream) item;
					// update the DockerImage based tags with EAP builder image set by configuration
					imageStream.getSpec().getTags().stream()
							.filter(tagReference -> tagReference.getFrom().getKind().equals("DockerImage"))
							.forEach(tagReference -> {
								tagReference.getFrom().setName(IntersmashConfig.eap7ImageURL());
								tagReference.setImportPolicy(new TagImportPolicyBuilder().withInsecure(true).build());
							});
					streams.add(openShift.imageStreams().createOrReplace((ImageStream) item));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to deploy EAP 7 image streams from " + url, e);
		}
		return streams;
	}

	@Override
	public String getProductCode() {
		return IntersmashConfig.eap7ProductCode();
	}

	/**
	 * Here the actual URL for the used EAP 7 image streams files, depending on the supported EAP 7 version
	 *
	 * @return String that represents the URL to the actual EAP 7 image streams files, depending on the supported EAP 7
	 * version
	 */
	private String getUsedImageStreamUrl() {

		final String imageStreamsFileNameSuffix = "-image-stream.json";
		final String eapProductCode = getProductCode();
		final String eapJdk = getEapJdk();

		// validate used EAP image product code is allowed
		if (!List.of(SUPPORTED_EAP_VERSIONS).stream().anyMatch(allowed -> allowed.equals(eapProductCode))) {
			throw new IllegalStateException(String.format("Unsupported EAP product code: %s", eapProductCode));
		}

		// validate used eap image JDK is allowed
		if (!List.of(SUPPORTED_JDK_VERSIONS).stream().anyMatch(allowed -> allowed.equals(eapJdk))) {
			throw new IllegalStateException("Unsupported JDK version: " + eapJdk);
		}

		return getImageStreamsUrl() + eapProductCode
				+ String.format("-%s", eapJdk) + imageStreamsFileNameSuffix;
	}

	private String getImageStreamsUrl() {
		return IntersmashConfig.eap7ImageStreams();
	}

	private String getEapJdk() {
		final String image = IntersmashConfig.eap7ImageURL();
		final String eapProductCode = getProductCode();

		if (image.matches(".*" + eapProductCode + "-openjdk8.*")) {
			return SUPPORTED_JDK_OPENJDK_8;
		} else if (image.matches(".*" + eapProductCode + "-openjdk\\d\\d.*")) {
			return image.replaceFirst(".*" + eapProductCode + "-openjdk(\\d\\d).*", "openjdk$1");
		} else {
			throw new IllegalStateException(String.format("Unsupported JDK: %s", image));
		}
	}
}
