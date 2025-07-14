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
package org.jboss.intersmash.provision.openshift;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.jboss.intersmash.application.operator.HyperfoilOperatorApplication;
import org.jboss.intersmash.provision.operator.HyperfoilOperatorProvisioner;

import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClientAdapter;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.hyperfoil.v1alpha2.Hyperfoil;
import io.hyperfoil.v1alpha2.HyperfoilList;
import lombok.NonNull;

/**
 * <p> @see io.hyperfoil.v1alpha2 <code>package-info.java</code> file, for details about how to create/update/delete an
 *     <b>Hyperfoil Custom Resource</b></p>
 * <p> @see org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.release021 <code>package-info.java</code>
 *     file, for details about how to interact with the <b>Hyperfoil Server</b> which is started by the <b>Hyperfoil Operator</b>
 *     when an <b>Hyperfoil Custom Resource</b> is created</p>
 */
public class HyperfoilOpenShiftOperatorProvisioner
		// leverage Hyperfoil common Operator based provisioner behavior
		extends HyperfoilOperatorProvisioner<NamespacedOpenShiftClient>
		// ... and common OpenShift provisioning logic, too
		implements OpenShiftProvisioner<HyperfoilOperatorApplication> {

	private FailFastCheck ffCheck;

	public HyperfoilOpenShiftOperatorProvisioner(@NonNull HyperfoilOperatorApplication application) {
		super(application);
	}

	@Override
	public NamespacedKubernetesClientAdapter<NamespacedOpenShiftClient> client() {
		return OpenShiftProvisioner.super.client();
	}

	@Override
	public String execute(String... args) {
		return OpenShiftProvisioner.super.execute(args);
	}

	@Override
	protected FailFastCheck getFailFastCheck() {
		if (ffCheck == null) {
			ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
					getApplication().getName());
		}
		return ffCheck;
	}

	@Override
	public URL getURL() {
		Route route = OpenShiftProvisioner.openShift.getRoute(getApplication().getName());
		if (Objects.nonNull(route)) {
			String host = route.getSpec().getHost() != null ? route.getSpec().getHost()
					: route.getStatus().getIngress().get(0).getHost();
			String url = String.format("https://%s", host);
			try {
				return new URL(
						url);
			} catch (MalformedURLException e) {
				throw new RuntimeException(String.format("Hyperfoil operator route \"%s\"is malformed.", url), e);
			}
		}
		return null;
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	public HasMetadataOperationsImpl<Hyperfoil, HyperfoilList> hyperfoilCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().newHasMetadataOperation(crdc, Hyperfoil.class, HyperfoilList.class);
	}

	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}
}
