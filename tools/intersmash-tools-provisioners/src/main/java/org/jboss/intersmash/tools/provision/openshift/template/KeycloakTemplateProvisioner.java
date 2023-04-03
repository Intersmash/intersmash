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
package org.jboss.intersmash.tools.provision.openshift.template;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.intersmash.tools.IntersmashConfig;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.TagImportPolicyBuilder;

public class KeycloakTemplateProvisioner implements OpenShiftTemplateProvisioner {
	@Override
	public String getTemplatesUrl() {
		return IntersmashConfig.keycloakTemplates();
	}

	@Override
	public String getProductCode() {
		return IntersmashConfig.keycloakProductCode();
	}

	@Override
	public List<ImageStream> deployImageStreams() {
		String url = getTemplatesUrl() + getProductCode() + "-image-stream.json";
		try (InputStream is = new URL(url).openStream()) {
			// since RH-SSO 76, an additional PostgreSQL image has been added to the template
			KubernetesList imageStreams = openShift.lists().load(is).get();
			// get a reference to the actual RH-SSO ImageStream definition (by skipping the PostgreSQL one)
			ImageStream ssoImageStream = imageStreams.getItems().stream()
					.filter(item -> !"PostgreSQL".equals(item.getMetadata().getAnnotations().get("openshift.io/display-name")))
					.map(item -> (ImageStream) item)
					.findFirst().orElseThrow(
							() -> new IllegalStateException(
									String.format(
											"The template provisioner didn't find a suitable image stream in the provided image streams definition: %s",
											url)));
			// update the tags with the RH-SSO image set via configuration mechanism
			ssoImageStream.getSpec().getTags().stream()
					.filter(tagReference -> tagReference.getFrom().getKind().equals("DockerImage"))
					.forEach(tagReference -> {
						tagReference.getFrom().setName(IntersmashConfig.keycloakImageURL());
						tagReference.setImportPolicy(new TagImportPolicyBuilder().withInsecure(true).build());
					});
			return imageStreams.getItems().stream()
					.map(imageStream -> openShift.imageStreams().createOrReplace((ImageStream) imageStream))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException("Failed to deploy image streams from " + url, e);
		}
	}

	@Override
	public String getTemplateFileName(OpenShiftTemplate openShiftTemplate) {
		switch (KeycloakTemplate.valueOfLabel(openShiftTemplate.getLabel())) {
			case HTTPS:
			case POSTGRESQL:
			case POSTGRESQL_PERSISTENT:
				return OpenShiftTemplateProvisioner.super.getTemplateFileName(openShiftTemplate);
			case X509_HTTPS:
			case X509_POSTGRESQL_PERSISTENT:
				return String.format("%s-ocp4-%s", getProductCode(), openShiftTemplate.getLabel());
			default:
				throw new IllegalArgumentException(String.format("Unknown template name: %s", openShiftTemplate.getLabel()));
		}
	}

	@Override
	public String getTemplateFileUrl(OpenShiftTemplate openShiftTemplate) {
		switch (KeycloakTemplate.valueOfLabel(openShiftTemplate.getLabel())) {
			case HTTPS:
			case POSTGRESQL:
			case POSTGRESQL_PERSISTENT:
				return String.format("%s/%s/%s.json", getTemplatesUrl(), "passthrough", getTemplateFileName(openShiftTemplate));
			case X509_HTTPS:
			case X509_POSTGRESQL_PERSISTENT:
				return String.format("%s/%s/%s.json", getTemplatesUrl(), "reencrypt/ocp-4.x",
						getTemplateFileName(openShiftTemplate));
			default:
				throw new IllegalArgumentException(String.format("Unknown template name: %s", openShiftTemplate.getLabel()));
		}
	}
}
