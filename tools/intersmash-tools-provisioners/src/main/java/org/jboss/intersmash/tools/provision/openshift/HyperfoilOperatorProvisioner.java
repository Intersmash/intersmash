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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.HyperfoilOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.operator.OperatorProvisioner;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
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
public class HyperfoilOperatorProvisioner extends OperatorProvisioner<HyperfoilOperatorApplication> {
	// this is the name of the Hyperfoil CustomResourceDefinition
	// you can get it with command:
	// oc get crd hyperfoils.hyperfoil.io -o template --template='{{ .metadata.name }}'
	private final static String HYPERFOIL_CUSTOM_RESOURCE_DEFINITION = "hyperfoils.hyperfoil.io";
	private static NonNamespaceOperation<Hyperfoil, HyperfoilList, Resource<Hyperfoil>> HYPERFOIL_CUSTOM_RESOURCE_CLIENT;
	// this is the packagemanifest for the hyperfoil operator;
	// you can get it with command:
	// oc get packagemanifest hyperfoil-bundle -o template --template='{{ .metadata.name }}'
	private static final String OPERATOR_ID = IntersmashConfig.hyperfoilOperatorPackageManifest();

	public HyperfoilOperatorProvisioner(@NonNull HyperfoilOperatorApplication hyperfoilOperatorApplication) {
		super(hyperfoilOperatorApplication, OPERATOR_ID, null);
	}

	public static String getOperatorId() {
		return OPERATOR_ID;
	}

	/**
	 * Get a client capable of working with {@link #HYPERFOIL_CUSTOM_RESOURCE_DEFINITION} custom resource.
	 *
	 * @return client for operations with {@link #HYPERFOIL_CUSTOM_RESOURCE_DEFINITION} custom resource
	 */
	NonNamespaceOperation<Hyperfoil, HyperfoilList, Resource<Hyperfoil>> hyperfoilClient() {
		if (HYPERFOIL_CUSTOM_RESOURCE_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(HYPERFOIL_CUSTOM_RESOURCE_DEFINITION).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(HYPERFOIL_CUSTOM_RESOURCE_DEFINITION)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						HYPERFOIL_CUSTOM_RESOURCE_DEFINITION, OPERATOR_ID));
			}
			MixedOperation<Hyperfoil, HyperfoilList, Resource<Hyperfoil>> hyperfoilCrClient = OpenShifts
					.master().customResources(crdc, Hyperfoil.class, HyperfoilList.class);
			HYPERFOIL_CUSTOM_RESOURCE_CLIENT = hyperfoilCrClient.inNamespace(OpenShiftConfig.namespace());
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

	@Override
	public void deploy() {
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				getApplication().getName());
		if (!isSubscribed()) {
			subscribe();
		}
		hyperfoilClient().createOrReplace(getApplication().getHyperfoil());
		new SimpleWaiter(() -> hyperfoil().get().getStatus() != null)
				.failFast(ffCheck)
				.reason("Wait for status field to be initialized.")
				.level(Level.DEBUG)
				.waitFor();
		new SimpleWaiter(() -> getPods().size() == 1)
				.failFast(ffCheck)
				.reason("Wait for expected number of replicas to be active.")
				.level(Level.DEBUG)
				.waitFor();
		WaitersUtil.routeIsUp(getURL().toExternalForm())
				.level(Level.DEBUG)
				.waitFor();
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

	@Override
	public void undeploy() {
		undeploy(true);
	}

	public void undeploy(boolean unsubscribe) {
		hyperfoil().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck).areExactlyNPodsReady(0, "app", getApplication().getName())
				.level(Level.DEBUG).waitFor();
		if (unsubscribe) {
			unsubscribe();
		}
	}

	@Override
	public List<Pod> getPods() {
		List<Pod> pods = new ArrayList<>();
		Pod hyperfoilControllerPod = OpenShiftProvisioner.openShift
				.getPod(String.format("%s-controller", getApplication().getName()));
		if (isContainerReady(hyperfoilControllerPod, "controller")) {
			pods.add(hyperfoilControllerPod);
		}
		return pods;
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
		String[] operatorSpecs = getAdminBinary().execute("get", "csvs", getCurrentCSV(), "-o", "template", "--template",
				"{{range .spec.install.spec.deployments}}{{printf \"%d|%s\\n\" .spec.replicas .name}}{{end}}")
				.split(System.lineSeparator());
		for (String spec : operatorSpecs) {
			String[] operatorSpec = spec.split("\\|");
			if (operatorSpec.length != 2) {
				throw new RuntimeException("Failed to get operator deployment spec from csvs!");
			}
			new SimpleWaiter(() -> OpenShiftProvisioner.openShift.getPods().stream().filter(
					pod -> (pod.getMetadata()
							.getName()
							.startsWith(operatorSpec[1])
							&& pod.getStatus().getPhase().equalsIgnoreCase("Running")))
					.count() == Integer.valueOf(operatorSpec[0]))
					.failFast(ffCheck)
					.reason("Wait for expected number of replicas to be active.")
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	/**
	 * Tells if a specific container inside the pod is ready
	 *
	 * @param pod
	 * @param containerName: name of the container
	 * @return
	 */
	private boolean isContainerReady(Pod pod, String containerName) {
		if (Objects.nonNull(pod)) {
			return pod.getStatus().getContainerStatuses().stream()
					.filter(containerStatus -> containerStatus.getName().equalsIgnoreCase(containerName)
							&& containerStatus.getReady())
					.count() > 0;
		}
		return false;
	}

	@Override
	protected String getOperatorCatalogSource() {
		return IntersmashConfig.hyperfoilOperatorCatalogSource();
	}

	@Override
	protected String getOperatorIndexImage() {
		return IntersmashConfig.hyperfoilOperatorIndexImage();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		throw new UnsupportedOperationException("To be implemented!");
	}
}
