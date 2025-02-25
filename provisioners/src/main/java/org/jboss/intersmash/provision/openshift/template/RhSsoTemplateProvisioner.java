/**
 * Copyright (C) 2025 Red Hat, Inc.
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
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.openshift.template.RhSsoTemplate;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.TagImportPolicyBuilder;

public class RhSsoTemplateProvisioner implements OpenShiftTemplateProvisioner {
	@Override
	public String getTemplatesUrl() {
		return IntersmashConfig.rhSsoTemplates();
	}

	@Override
	public String getProductCode() {
		return IntersmashConfig.rhSsoProductCode();
	}

	@Override
	public List<ImageStream> deployImageStreams() {
		String url = getTemplatesUrl() + getProductCode() + "-image-stream.json";
		try (InputStream is = new URL(url).openStream()) {
			// since RH-SSO 76, an additional PostgreSQL image has been added to the template
			List<HasMetadata> imageStreams = openShift.load(is).items();
			// get a reference to the actual RH-SSO ImageStream definition (by skipping the PostgreSQL one)
			ImageStream ssoImageStream = imageStreams.stream()
					.filter(item -> !"PostgreSQL".equals(item.getMetadata().getAnnotations().get("openshift.io/display-name")))
					.map(item -> (ImageStream) item)
					.findFirst().orElseThrow(
							() -> new IllegalStateException(
									String.format(
											"The RH-SSO template provisioner didn't find a suitable RH-SSO image stream in the provided image streams definition: %s",
											url)));
			// update the tags with the RH-SSO image set via configuration mechanism
			ssoImageStream.getSpec().getTags().stream()
					.filter(tagReference -> tagReference.getFrom().getKind().equals("DockerImage"))
					.forEach(tagReference -> {
						tagReference.getFrom().setName(IntersmashConfig.rhSsoImageURL());
						tagReference.setImportPolicy(new TagImportPolicyBuilder().withInsecure(true).build());
					});
			return imageStreams.stream()
					.map(imageStream -> openShift.imageStreams().createOrReplace((ImageStream) imageStream))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException("Failed to deploy RH-SSO image streams from " + url, e);
		}
	}

	@Override
	public String getTemplateFileName(OpenShiftTemplate openShiftTemplate) {
		return String.format("%s-ocp4-%s", getProductCode(), openShiftTemplate.getLabel());
	}

	@Override
	public String getTemplateFileUrl(OpenShiftTemplate openShiftTemplate) {
		switch (RhSsoTemplate.valueOfLabel(openShiftTemplate.getLabel())) {
			case HTTPS:
			case POSTGRESQL:
			case POSTGRESQL_PERSISTENT:
				return String.format("%s/%s/%s.json", getTemplatesUrl(), "passthrough/ocp-4.x",
						getTemplateFileName(openShiftTemplate));
			case X509_HTTPS:
			case X509_POSTGRESQL_PERSISTENT:
				return String.format("%s/%s/%s.json", getTemplatesUrl(), "reencrypt/ocp-4.x",
						getTemplateFileName(openShiftTemplate));
			default:
				throw new IllegalArgumentException(String.format("Unknown template name: %s", openShiftTemplate.getLabel()));
		}
	}
}
