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
import java.util.Collections;
import java.util.List;

import org.jboss.intersmash.tools.IntersmashConfig;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.TagImportPolicyBuilder;

public class InfinispanTemplateProvisioner implements OpenShiftTemplateProvisioner {
	@Override
	public String getTemplatesUrl() {
		return IntersmashConfig.infinispanTemplates();
	}

	@Override
	public String getProductCode() {
		return IntersmashConfig.infinispanProductCode();
	}

	@Override
	public List<ImageStream> deployImageStreams() {
		String url = getTemplatesUrl() + getProductCode() + "-image-stream.json";
		try (InputStream is = new URL(url).openStream()) {
			KubernetesList kubernetesList = openShift.lists().load(is).get();
			ImageStream imageStream = (ImageStream) kubernetesList.getItems().get(0);
			// workaround for the API version in the data (v1) does not match the expected API version (image.openshift.io/v1)
			imageStream.setApiVersion("image.openshift.io/v1");
			// update the tags with the Infinispan image set via configuration mechanism
			imageStream.getSpec().getTags().stream()
					.forEach(tagReference -> {
						tagReference.getFrom().setName(IntersmashConfig.infinispanImageURL());
						tagReference.setImportPolicy(new TagImportPolicyBuilder().withInsecure(true).build());
					});
			return Collections.singletonList(openShift.imageStreams().createOrReplace(imageStream));
		} catch (IOException e) {
			throw new RuntimeException("Failed to deploy Infinispan image streams from " + url, e);
		}
	}
}
