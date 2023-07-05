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

import io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSourceBuilder;

public class CatalogSource extends io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSource
		implements OpenShiftResource<CatalogSource> {

	public CatalogSource() {
		super();
	}

	public CatalogSource(
			String name,
			String namespace,
			String sourceType,
			String indexImage,
			String displayName,
			String publisher) {
		this();
		io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSource catalogSource = new CatalogSourceBuilder()
				.withNewMetadata()
				.withName(name)
				.withNamespace(namespace)
				.endMetadata()
				.withNewSpec()
				.withSourceType(sourceType)
				.withImage(indexImage)
				.withDisplayName(displayName)
				.withPublisher(publisher)
				.endSpec()
				.build();
		this.setMetadata(catalogSource.getMetadata());
		this.setSpec(catalogSource.getSpec());
	}

	@Override
	public CatalogSource load(CatalogSource loaded) {
		this.setMetadata(loaded.getMetadata());
		this.setSpec(loaded.getSpec());
		return this;
	}

	public CatalogSource load(io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSource loaded) {
		this.setMetadata(loaded.getMetadata());
		this.setSpec(loaded.getSpec());
		return this;
	}
}
