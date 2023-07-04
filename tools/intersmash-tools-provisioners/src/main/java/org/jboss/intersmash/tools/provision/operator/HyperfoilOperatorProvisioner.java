package org.jboss.intersmash.tools.provision.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.operator.HyperfoilOperatorApplication;
import org.jboss.intersmash.tools.provision.Provisioner;
import org.jboss.intersmash.tools.provision.openshift.WaitersUtil;
import org.slf4j.event.Level;

import com.google.common.base.Strings;

import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.hyperfoil.v1alpha2.Hyperfoil;
import io.hyperfoil.v1alpha2.HyperfoilList;

public interface HyperfoilOperatorProvisioner extends
		OlmOperatorProvisioner<HyperfoilOperatorApplication>, Provisioner<HyperfoilOperatorApplication>,
		OlmOperatorProvisionerClientBinary {

	// this is the packagemanifest for the hyperfoil operator;
	// you can get it with command:
	// oc get packagemanifest hyperfoil-bundle -o template --template='{{ .metadata.name }}'
	static String operatorId() {
		return IntersmashConfig.hyperfoilOperatorPackageManifest();
	}

	// this is the name of the Hyperfoil CustomResourceDefinition
	// you can get it with command:
	// oc get crd hyperfoils.hyperfoil.io -o template --template='{{ .metadata.name }}'
	default String hyperfoilCustomResourceDefinitionName() {
		return "hyperfoils.hyperfoil.io";
	}

	HasMetadataOperationsImpl<Hyperfoil, HyperfoilList> hyperfoilCustomResourcesClient(CustomResourceDefinitionContext crdc);

	/**
	 * Get a client capable of working with {@link #hyperfoilCustomResourceDefinitionName} custom resource.
	 *
	 * @return client for operations with {@link #hyperfoilCustomResourceDefinitionName} custom resource
	 */
	default MixedOperation<Hyperfoil, HyperfoilList, Resource<Hyperfoil>> buildHyperfoilClient() {
		CustomResourceDefinition crd = retrieveCustomResourceDefinitions()
				.withName(hyperfoilCustomResourceDefinitionName()).get();
		if (crd == null) {
			throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
					hyperfoilCustomResourceDefinitionName(), operatorId()));
		}
		return hyperfoilCustomResourcesClient(CustomResourceDefinitionContext.fromCrd(crd));
	}

	NonNamespaceOperation<Hyperfoil, HyperfoilList, Resource<Hyperfoil>> hyperfoilClient();

	/**
	 * Get a reference to Hyperfoil object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @return A concrete {@link Resource} instance representing the {@link Hyperfoil} resource definition
	 */
	default Resource<Hyperfoil> hyperfoil() {
		return hyperfoilClient().withName(getApplication().getName());
	}

	default void deploy() {
		FailFastCheck ffCheck = () -> false;
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

	String getCurrentCSV();

	List<Pod> retrieveNamespacePods();

	Pod retrieveNamedPod(final String podName);

	Ingress retrieveNamedIngress(final String ingressName);

	default List<Pod> getPods() {
		List<Pod> pods = new ArrayList<>();
		Pod hyperfoilControllerPod = retrieveNamedPod(String.format("%s-controller", getApplication().getName()));
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
	default void waitForOperatorPod() {
		String[] operatorSpecs = this.execute("get", "csvs", getCurrentCSV(), "-o", "template", "--template",
				"{{range .spec.install.spec.deployments}}{{printf \"%d|%s\\n\" .spec.replicas .name}}{{end}}")
				.split(System.lineSeparator());
		for (String spec : operatorSpecs) {
			String[] operatorSpec = spec.split("\\|");
			if (operatorSpec.length != 2) {
				throw new RuntimeException("Failed to get operator deployment spec from csvs!");
			}
			new SimpleWaiter(() -> retrievePods().stream().filter(
					pod -> (pod.getMetadata()
							.getName()
							.startsWith(operatorSpec[1])
							&& pod.getStatus().getPhase().equalsIgnoreCase("Running")))
					.count() == Integer.valueOf(operatorSpec[0]))
					.failFast(() -> false)
					.reason("Wait for expected number of replicas to be active.")
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	@Override
	default void undeploy() {
		undeploy(true);
	}

	default void undeploy(boolean unsubscribe) {
		hyperfoil().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		BooleanSupplier bs = () -> retrieveNamespacePods().stream()
				.filter(p -> !Strings.isNullOrEmpty(p.getMetadata().getLabels().get("app"))
						&& p.getMetadata().getLabels().get("app").equals(getApplication().getName()))
				.collect(Collectors.toList()).size() == 0;
		String reason = "Waiting for exactly 0 pods with label \"app\"=" + getApplication().getName() + " to be ready.";
		new SimpleWaiter(bs, TimeUnit.MINUTES, 2, reason)
				.level(Level.DEBUG)
				.waitFor();
		if (unsubscribe) {
			unsubscribe();
		}
	}

	/**
	 * Tells if a specific container inside the pod is ready
	 *
	 * @param pod
	 * @param containerName: name of the container
	 * @return
	 */
	default boolean isContainerReady(Pod pod, String containerName) {
		if (Objects.nonNull(pod)) {
			return pod.getStatus().getContainerStatuses().stream()
					.filter(containerStatus -> containerStatus.getName().equalsIgnoreCase(containerName)
							&& containerStatus.getReady())
					.count() > 0;
		}
		return false;
	}

	default String getOperatorCatalogSource() {
		return IntersmashConfig.hyperfoilOperatorCatalogSource();
	}

	default String getOperatorIndexImage() {
		return IntersmashConfig.hyperfoilOperatorIndexImage();
	}

	default String getOperatorChannel() {
		return IntersmashConfig.hyperfoilOperatorChannel();
	}
}
