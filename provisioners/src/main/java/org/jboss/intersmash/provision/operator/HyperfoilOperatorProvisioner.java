/*
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
package org.jboss.intersmash.provision.operator;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.k8s.HasPods;
import org.jboss.intersmash.application.operator.HyperfoilOperatorApplication;
import org.jboss.intersmash.provision.Provisioner;
import org.slf4j.event.Level;

import cz.xtf.core.http.Https;
import cz.xtf.core.http.HttpsException;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.hyperfoil.v1alpha2.Hyperfoil;
import io.hyperfoil.v1alpha2.HyperfoilList;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class HyperfoilOperatorProvisioner<C extends NamespacedKubernetesClient> extends
		OperatorProvisioner<HyperfoilOperatorApplication, C> implements Provisioner<HyperfoilOperatorApplication>, HasPods {

	public HyperfoilOperatorProvisioner(@NonNull HyperfoilOperatorApplication application) {
		super(application, HyperfoilOperatorProvisioner.OPERATOR_ID);
	}

	// =================================================================================================================
	// Hyperfoil related
	// =================================================================================================================
	public Pod getControllerPod() {
		return getPods().stream()
				.filter(p -> p.getMetadata().getName().equals(String.format("%s-controller", getApplication().getName()))
						&& "controller".equals(p.getMetadata().getLabels().get("role")))
				.findFirst().orElse(null);
	}

	public List<Pod> getAgentPods() {
		return getPods().stream().filter(p -> p.getMetadata().getName().startsWith(
				"agent-") && "agent".equals(p.getMetadata().getLabels().get("role"))).collect(Collectors.toList());
	}

	/**
	 * This method checks if the Operator's POD is actually running;
	 * It's been tailored on the community-operators Cluster Service version format which is missing label
	 * <code>spec.install.spec.deployments.spec.template.metadata.labels."app.kubernetes.io/name"</code> which is used
	 * in @see OperatorProvisioner#waitForOperatorPod() (see
	 * https://github.com/operator-framework/community-operators/tree/master/community-operators/hyperfoil-bundle)
	 */
	@Override
	protected void waitForOperatorPod() {
		String[] operatorSpecs = this.execute("get", "csvs", getCurrentCSV(), "-o", "template", "--template",
				"{{range .spec.install.spec.deployments}}{{printf \"%d|%s\\n\" .spec.replicas .name}}{{end}}")
				.split(System.lineSeparator());
		for (String spec : operatorSpecs) {
			String[] operatorSpec = spec.split("\\|");
			if (operatorSpec.length != 2) {
				throw new RuntimeException("Failed to get operator deployment spec from csvs!");
			}
			new SimpleWaiter(() -> getPods().stream().filter(
					pod -> (pod.getMetadata()
							.getName()
							.startsWith(operatorSpec[1])
							&& pod.getStatus().getPhase().equalsIgnoreCase("Running")))
					.count() == Integer.parseInt(operatorSpec[0]))
					.failFast(() -> false)
					.reason("Wait for expected number of replicas to be active.")
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	// =================================================================================================================
	// Related to generic provisioning behavior
	// =================================================================================================================
	@Override
	public String getOperatorCatalogSource() {
		return IntersmashConfig.hyperfoilOperatorCatalogSource();
	}

	@Override
	public String getOperatorIndexImage() {
		return IntersmashConfig.hyperfoilOperatorIndexImage();
	}

	@Override
	public String getOperatorChannel() {
		return IntersmashConfig.hyperfoilOperatorChannel();
	}

	@Override
	public void deploy() {
		FailFastCheck ffCheck = getFailFastCheck();

		subscribe();

		hyperfoilClient().createOrReplace(getApplication().getHyperfoil());
		new SimpleWaiter(() -> hyperfoil().get().getStatus() != null)
				.failFast(ffCheck)
				.reason("Wait for status field to be initialized.")
				.level(Level.DEBUG)
				.waitFor();
		new SimpleWaiter(() -> getControllerPod() != null && getControllerPod().getStatus().getConditions().stream()
				.anyMatch(c -> "Ready".equals(c.getType()) && "True".equals(c.getStatus())))
				.failFast(ffCheck)
				.reason("Wait for expected number of replicas to be active.")
				.level(Level.DEBUG)
				.waitFor();
		new SimpleWaiter(
				this::routeIsReadyAndUp)
				.reason("Wait until the route is ready to serve.")
				.level(Level.DEBUG)
				.waitFor();
	}

	private boolean routeIsReadyAndUp() {
		try {
			return Https.getCode(getURL().toExternalForm()) == HttpStatus.SC_OK;
		} catch (HttpsException ex) {
			// we need to tolerate connection refused while the service nodeport networking is set up,
			// rather than just checking response code, which simpleWaiter
			// doesn't seem to support, i.e. if the above fails, then the waiter blows up, no waiting at all
			log.debug(
					"Exception thrown while trying to connect to the Hyperfoil controller: {}. This might be temporary, as the service node port is set up",
					ex.getMessage());
			return false;
		}
	}

	@Override
	public void undeploy() {
		// remove agents, since it seems they actually survive a stopped run...
		getAgentPods().stream().forEach(p -> client().resource(p).delete());
		new SimpleWaiter(() -> getAgentPods().isEmpty())
				.reason("Waiting for the the HyperFoil agents to be stopped.")
				.level(Level.DEBUG)
				.waitFor();
		// remove the CR
		hyperfoil().delete();
		new SimpleWaiter(() -> hyperfoil().get() == null)
				.reason("Waiting for the the HyperFoil controller to be stopped.")
				.level(Level.DEBUG)
				.waitFor();
		// delete the OLM subscription
		unsubscribe();
		new SimpleWaiter(() -> getPods().isEmpty())
				.reason("Waiting for the all the HyperFoil pods to be stopped.")
				.level(Level.DEBUG)
				.waitFor();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		throw new UnsupportedOperationException("Scaling is not implemented by Hyperfoil operator based provisioning");
	}

	// =================================================================================================================
	// Client related
	// =================================================================================================================
	// this is the packagemanifest for the hyperfoil operator;
	// you can get it with command:
	// oc get packagemanifest hyperfoil-bundle -o template --template='{{ .metadata.name }}'
	public static String OPERATOR_ID = IntersmashConfig.hyperfoilOperatorPackageManifest();

	// this is the name of the Hyperfoil CustomResourceDefinition
	// you can get it with command:
	// oc get crd hyperfoils.hyperfoil.io -o template --template='{{ .metadata.name }}'
	protected static String HYPERFOIL_CRD_NAME = "hyperfoils.hyperfoil.io";

	/**
	 * Generic CRD client which is used by client builders default implementation to build the CRDs client
	 *
	 * @return A {@link NonNamespaceOperation} instance that represents a
	 */
	protected abstract NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient();

	// hyperfoils.hyperfoil.io
	protected abstract HasMetadataOperationsImpl<Hyperfoil, HyperfoilList> hyperfoilCustomResourcesClient(
			CustomResourceDefinitionContext crdc);

	protected static NonNamespaceOperation<Hyperfoil, HyperfoilList, Resource<Hyperfoil>> HYPERFOIL_CUSTOM_RESOURCE_CLIENT;

	/**
	 * Get a client capable of working with {@link HyperfoilOperatorProvisioner#HYPERFOIL_CRD_NAME} custom resource.
	 *
	 * @return client for operations with {@link HyperfoilOperatorProvisioner#HYPERFOIL_CRD_NAME} custom resource
	 */
	public NonNamespaceOperation<Hyperfoil, HyperfoilList, Resource<Hyperfoil>> hyperfoilClient() {
		if (HYPERFOIL_CUSTOM_RESOURCE_CLIENT == null) {
			CustomResourceDefinition crd = customResourceDefinitionsClient()
					.withName(HYPERFOIL_CRD_NAME).get();
			if (crd == null) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						HYPERFOIL_CRD_NAME, OPERATOR_ID));
			}
			HYPERFOIL_CUSTOM_RESOURCE_CLIENT = hyperfoilCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
		}
		return HYPERFOIL_CUSTOM_RESOURCE_CLIENT;
	}

	/**
	 * Get a reference to Hyperfoil object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @return A concrete {@link Resource} instance representing the {@link Hyperfoil} resource definition
	 */
	public Resource<Hyperfoil> hyperfoil() {
		return hyperfoilClient().withName(getApplication().getName());
	}
}
