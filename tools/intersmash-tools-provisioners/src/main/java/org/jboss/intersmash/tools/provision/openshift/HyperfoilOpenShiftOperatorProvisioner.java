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
package org.jboss.intersmash.tools.provision.openshift;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import org.assertj.core.util.Strings;
import org.jboss.intersmash.tools.application.operator.HyperfoilOperatorApplication;
import org.jboss.intersmash.tools.provision.operator.HyperfoilOperatorProvisioner;
import org.jboss.intersmash.tools.provision.operator.OperatorProvisioner;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.fabric8.openshift.api.model.Route;
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
		// default Operator based provisioner behavior, which implements OLM workflow contract and leverage lifecycle as well
		extends OperatorProvisioner<HyperfoilOperatorApplication>
		// leverage Hyperfoil common Operator based provisioner behavior
		implements HyperfoilOperatorProvisioner,
		// leverage common OpenShift provisioning logic
		OpenShiftProvisioner<HyperfoilOperatorApplication> {

	protected static NonNamespaceOperation<Hyperfoil, HyperfoilList, Resource<Hyperfoil>> HYPERFOIL_CUSTOM_RESOURCE_CLIENT;

	public HyperfoilOpenShiftOperatorProvisioner(@NonNull HyperfoilOperatorApplication hyperfoilOperatorApplication) {
		super(hyperfoilOperatorApplication, HyperfoilOperatorProvisioner.operatorId());
	}

	public HasMetadataOperationsImpl<Hyperfoil, HyperfoilList> hyperfoilCustomResourcesClient(
			CustomResourceDefinitionContext crdc) {
		return OpenShifts
				.master().customResources(crdc, Hyperfoil.class, HyperfoilList.class);
	}

	/**
	 * Get a client capable of working with {@link HyperfoilOperatorProvisioner#hyperfoilCustomResourceDefinitionName()} custom resource.
	 *
	 * @return client for operations with {@link HyperfoilOperatorProvisioner#hyperfoilCustomResourceDefinitionName()} custom resource
	 */
	public NonNamespaceOperation<Hyperfoil, HyperfoilList, Resource<Hyperfoil>> hyperfoilClient() {
		if (HYPERFOIL_CUSTOM_RESOURCE_CLIENT == null) {
			HYPERFOIL_CUSTOM_RESOURCE_CLIENT = buildHyperfoilClient().inNamespace(OpenShiftConfig.namespace());
		}
		return HYPERFOIL_CUSTOM_RESOURCE_CLIENT;
	}

	@Override
	public Ingress retrieveNamedIngress(final String ingressName) {
		return OpenShiftProvisioner.openShift.network().v1().ingresses().withName(ingressName).get();
	}

	@Override
	protected String getCatalogSourceNamespace() {
		String namespace = super.getCatalogSourceNamespace();
		if (!Strings.isNullOrEmpty(getOperatorIndexImage())) {
			namespace = OpenShiftConfig.namespace();
		}
		return namespace;
	}

	@Override
	public List<Pod> getPods() {
		return HyperfoilOperatorProvisioner.super.getPods();
	}

	@Override
	public void waitForOperatorPod() {
		HyperfoilOperatorProvisioner.super.waitForOperatorPod();
	}

	@Override
	public String getOperatorCatalogSource() {
		return HyperfoilOperatorProvisioner.super.getOperatorCatalogSource();
	}

	@Override
	public String getOperatorIndexImage() {
		return HyperfoilOperatorProvisioner.super.getOperatorIndexImage();
	}

	@Override
	public String getOperatorChannel() {
		return HyperfoilOperatorProvisioner.super.getOperatorChannel();
	}

	@Override
	public List<Pod> retrieveNamespacePods() {
		return OpenShiftProvisioner.openShift.getPods();
	}

	@Override
	public Pod retrieveNamedPod(final String podName) {
		return OpenShiftProvisioner.openShift.pods().withName(podName).get();
	}

	public List<Pod> retrievePods() {
		return OpenShiftProvisioner.openShift.getPods();
	}

	@Override
	public String execute(String... args) {
		return OpenShifts.adminBinary().execute(args);
	}

	@Override
	protected String getOperatorNamespace() {
		return "openshift-marketplace";
	}

	@Override
	protected String getTargetNamespace() {
		return OpenShiftConfig.namespace();
	}

	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> retrieveCustomResourceDefinitions() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		throw new UnsupportedOperationException("To be implemented!");
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
}
