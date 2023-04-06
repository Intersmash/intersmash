package cz.xtf.core.openshift;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.xtf.core.config.WaitingConfig;
import cz.xtf.core.openshift.crd.CustomResourceDefinitionContextProvider;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.Waiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.RoleBinding;

/**
 * TEMP - it seems that the systsem:image-puller rolebinding is not handled by XTF, hence this is a temp
 * class to perform the isProjectClean.waitFor, BUT MUST be investigated.
 */
public class IntersmashOpenShiftWaiters {
	private static final Logger log = LoggerFactory.getLogger(IntersmashOpenShiftWaiters.class);
	private OpenShift openShift;
	private FailFastCheck failFast;

	IntersmashOpenShiftWaiters(OpenShift openShift) {
		this(openShift, () -> {
			return false;
		});
	}

	IntersmashOpenShiftWaiters(OpenShift openShift, FailFastCheck failFast) {
		this.openShift = openShift;
		this.failFast = failFast;
	}

	public static IntersmashOpenShiftWaiters get(OpenShift openShift, FailFastCheck failFast) {
		return new IntersmashOpenShiftWaiters(openShift, failFast);
	}

	public Waiter isProjectClean() {
		return new SimpleWaiter(() -> {
			int crdInstances = 0;
			List<GenericKubernetesResource> customResourceDefinitionList = null;
			for (CustomResourceDefinitionContextProvider crdContextProvider : OpenShift.getCRDContextProviders()) {
				try {
					customResourceDefinitionList = openShift.customResources(crdContextProvider.getContext(),
							GenericKubernetesResource.class, GenericKubernetesResourceList.class)
							.inNamespace(openShift.getNamespace())
							.list().getItems();
					crdInstances += customResourceDefinitionList.size();
				} catch (KubernetesClientException kce) {
					// CRD might not be installed on the cluster
				}
			}

			boolean isClean = false;
			List<HasMetadata> listRemovableResources = openShift.listRemovableResources();

			// it seems that XTF doesn't manage the system:image-puller rolebinding
			listRemovableResources = listRemovableResources.stream()
					.filter(r -> !(RoleBinding.class.getSimpleName().equals(r.getKind())
							&& ("system:image-puller".equals(r.getMetadata().getName()))))
					.collect(Collectors.toList());

			if (crdInstances == 0 & listRemovableResources.isEmpty()) {
				isClean = true;
			} else {
				StringBuilder strBuilderResourcesToDelete = new StringBuilder(
						"Cleaning project - " + openShift.getNamespace()
								+ " Waiting for following resources to be deleted: \n");
				if (customResourceDefinitionList != null && !customResourceDefinitionList.isEmpty()) {
					customResourceDefinitionList.stream().forEach((r) -> {
						strBuilderResourcesToDelete.append(r + "\n");
					});
				}
				if (!listRemovableResources.isEmpty()) {
					listRemovableResources.stream().forEach((r) -> {
						strBuilderResourcesToDelete.append(r + "\n");
					});
				}
				log.debug(strBuilderResourcesToDelete.toString());
			}
			return isClean;
		}, TimeUnit.MILLISECONDS, WaitingConfig.timeoutCleanup(), "Cleaning project - " + openShift.getNamespace())
				.onTimeout(() -> log.info("Cleaning namespace: " + openShift.getNamespace() + " - timed out."))
				.onFailure(() -> log.info("Cleaning namespace: " + openShift.getNamespace() + " - failed."))
				.onSuccess(() -> log.info("Cleaning namespace: " + openShift.getNamespace() + " - finished."))
				.failFast(failFast);
	}
}
