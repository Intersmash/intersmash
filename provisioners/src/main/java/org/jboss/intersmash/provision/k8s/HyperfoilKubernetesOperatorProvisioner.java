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
package org.jboss.intersmash.provision.k8s;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.jboss.intersmash.application.operator.HyperfoilOperatorApplication;
import org.jboss.intersmash.k8s.KubernetesConfig;
import org.jboss.intersmash.k8s.client.Kuberneteses;
import org.jboss.intersmash.provision.operator.HyperfoilOperatorProvisioner;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.NamespacedKubernetesClientAdapter;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.hyperfoil.v1alpha2.Hyperfoil;
import io.hyperfoil.v1alpha2.HyperfoilList;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p> @see io.hyperfoil.v1alpha2 <code>package-info.java</code> file, for details about how to create/update/delete an
 *     <b>Hyperfoil Custom Resource</b></p>
 * <p> @see org.jboss.intersmash.tools.provision.openshift.operator.hyperfoil.client.release021 <code>package-info.java</code>
 *     file, for details about how to interact with the <b>Hyperfoil Server</b> which is started by the <b>Hyperfoil Operator</b>
 *     when an <b>Hyperfoil Custom Resource</b> is created</p>
 */
@Slf4j
public class HyperfoilKubernetesOperatorProvisioner
		// leverage Hyperfoil common Operator based provisioner behavior
		extends HyperfoilOperatorProvisioner<NamespacedKubernetesClient>
		// ... and common K8s provisioning logic, too
		implements KubernetesProvisioner<HyperfoilOperatorApplication> {

	public HyperfoilKubernetesOperatorProvisioner(@NonNull HyperfoilOperatorApplication application) {
		super(application);
	}

	@Override
	public NamespacedKubernetesClientAdapter<NamespacedKubernetesClient> client() {
		return KubernetesProvisioner.super.client();
	}

	@Override
	public String execute(String... args) {
		return KubernetesProvisioner.super.execute(args);
	}

	@Override
	public String executeInNamespace(String namespace, String... args) {
		return KubernetesProvisioner.super.executeInNamespace(namespace, args);
	}

	/**
	 * On Kubernetes, the HyperFoil operator will expose the controller service externally via a {@code NodePort} type {@link Service}
	 * @return A {@link URL} instance that represents the HyperFoil controller service external URL
	 */
	@Override
	public URL getURL() {
		Service service = this.client().services().inNamespace(this.client().getNamespace()).list().getItems()
				.stream().filter(s -> getApplication().getName().equals(s.getMetadata().getName())
						&& "NodePort".equals(s.getSpec().getType()))
				.findFirst().orElse(null);
		if (Objects.nonNull(service)) {
			final String host = KubernetesConfig.getKubernetesHostname();
			final Integer nodePort = service.getSpec().getPorts().get(0).getNodePort();
			String url = String.format("http://%s:%s", host, nodePort);
			try {
				return new URL(url);
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
		return Kuberneteses.master().newHasMetadataOperation(crdc, Hyperfoil.class, HyperfoilList.class);
	}

	@Override
	public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient() {
		return Kuberneteses.admin().apiextensions().v1().customResourceDefinitions();
	}
}
