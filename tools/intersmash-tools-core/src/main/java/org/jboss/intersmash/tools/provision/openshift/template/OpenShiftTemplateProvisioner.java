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

import org.slf4j.LoggerFactory;

import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.client.dsl.TemplateResource;

/**
 * Provisioner for OpenShift templates. Its goal is to unify the basic operations we need to do with OpenShift templates.
 */
public interface OpenShiftTemplateProvisioner {

	OpenShift openShift = OpenShifts.master();

	/**
	 * Get a <b>base</b> URL of templates location for given product.
	 *
	 * @return URL of templates
	 */
	String getTemplatesUrl();

	/**
	 * Get the full URL to a template file.
	 *
	 * @param openShiftTemplate the used template instance
	 * @return A string representing the full URL to a given template file
	 */
	default String getTemplateFileUrl(OpenShiftTemplate openShiftTemplate) {
		return String.format("%s/%s.json", getTemplatesUrl(), getTemplateFileName(openShiftTemplate));
	}

	/**
	 * Get a product code for given product. Product code is usually used as a prefix in template file name - it's used
	 * here to construct the filename for {@link OpenShiftTemplate} deployment.
	 *
	 * @return product code of given product the provisioner is build for
	 */
	String getProductCode();

	/**
	 * Get a template file name format. Having a template name (e.g. "basic") is not sufficient, as the actual template
	 * file name could differ between products (e.g. "datagrid73-basic.json" VS "eap73-basic-s2i.json").
	 *
	 * @param openShiftTemplate template
	 * @return file name of template source file
	 */
	default String getTemplateFileName(OpenShiftTemplate openShiftTemplate) {
		return String.format("%s-%s", getProductCode(), openShiftTemplate.getLabel());
	}

	/**
	 * Deploy image streams for product the provisioner is build for. Use the image urls set by configuration mechanism -
	 * do not use the template defaults, but update to the configured images in order to run the tests against the
	 * tested images as required.
	 *
	 * @return deployed image stream instances
	 */
	List<ImageStream> deployImageStreams();

	/**
	 * Deploy the template for product the provisioner is build for.
	 *
	 * @param openShiftTemplate to be deployed
	 * @return deployed template instance
	 */
	default Template deployTemplate(OpenShiftTemplate openShiftTemplate) {
		Template template;
		String url = getTemplateFileUrl(openShiftTemplate);
		try (InputStream is = new URL(url).openStream()) {
			// workaround for the API version in the data (v1) does not match the expected API version (image.template.io/v1)
			template = (Template) ((TemplateResource) openShift.templates().load(is)).get();
			template.setApiVersion("template.openshift.io/v1");
			if (openShift.getTemplate(template.getMetadata().getName()) == null) {
				openShift.createTemplate(template);
			} else {
				LoggerFactory.getLogger(OpenShiftTemplateProvisioner.class).warn(
						"Template \"{}\" creation skipped: template already exists", template.getMetadata().getName());
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to deploy openshift template from " + url, e);
		}
		return template;
	}
}
