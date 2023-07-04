package org.jboss.intersmash.tools.k8s.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jboss.intersmash.tools.k8s.KubernetesConfig;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import cz.xtf.core.config.WaitingConfig;
import cz.xtf.core.openshift.crd.CustomResourceDefinitionContextProvider;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.Waiter;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.builder.Visitor;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Kubernetes extends DefaultKubernetesClient {

	private static ServiceLoader<CustomResourceDefinitionContextProvider> crdContextProviderLoader;

	/**
	 * This label is supposed to be used for any resource created by Intersmash to easily distinguish which resources have
	 * been created by Intersmash automation.
	 * NOTE: at the moment only place where this is used is for labeling namespaces. Other usages may be added in the future.
	 */
	public static final String INTERSMASH_MANAGED_LABEL = "intersmash/managed";
	/**
	 * Used to cache created Kubernetes clients for given test case.
	 */
	public static final Multimap<String, Kubernetes> namespaceToKubernetesClientMap = Multimaps
			.synchronizedListMultimap(ArrayListMultimap.create());
	private static final String KEEP_LABEL = "intersmash/keep";

	/**
	 * Autoconfigures the client with the default fabric8 client rules
	 *
	 * @param namespace set namespace to the Kubernetes client instance
	 * @return this Kubernetes client instance
	 */
	public static Kubernetes get(String namespace) {
		Config kubeconfig = Config.autoConfigure(null);

		setupTimeouts(kubeconfig);

		if (StringUtils.isNotEmpty(namespace)) {
			kubeconfig.setNamespace(namespace);
		}

		return get(kubeconfig);
	}

	public static Kubernetes get(Path kubeconfigPath, String namespace) {
		try {
			String kubeconfigContents = new String(Files.readAllBytes(kubeconfigPath), StandardCharsets.UTF_8);
			Config kubeconfig = Config.fromKubeconfig(null, kubeconfigContents, kubeconfigPath.toAbsolutePath().toString());

			setupTimeouts(kubeconfig);

			if (StringUtils.isNotEmpty(namespace)) {
				kubeconfig.setNamespace(namespace);
			}

			return get(kubeconfig);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Kubernetes get(String masterUrl, String namespace, String username, String password) {
		Config kubeconfig = new ConfigBuilder()
				.withMasterUrl(masterUrl)
				.withTrustCerts(true)
				.withNamespace(namespace)
				.withUsername(username)
				.withPassword(password).build();

		setupTimeouts(kubeconfig);

		return get(kubeconfig);
	}

	public static Kubernetes get(String masterUrl, String namespace, String token) {
		Config kubeconfig = Config.empty();
		kubeconfig.setMasterUrl(masterUrl);
		kubeconfig.setTrustCerts(true);
		kubeconfig.setNamespace(namespace);
		kubeconfig.setOauthToken(token);

		setupTimeouts(kubeconfig);

		return get(kubeconfig);
	}

	public Kubernetes(Config kubeconfig) {
		super(kubeconfig);
	}

	public void setupPullSecret(String secret) {
		setupPullSecret("xtf-pull-secret", secret);
	}

	/**
	 * Convenient method to create pull secret for authenticated image registries.
	 * The secret content must be provided in "dockerconfigjson" formar.
	 *
	 * E.g.: {@code {"auths":{"registry.redhat.io":{"auth":"<REDACTED_TOKEN>"}}}}
	 *
	 * TODO - Check Linking Secret to ServiceAccount
	 *
	 * @param name of the Secret to be created
	 * @param secret content of Secret in json format
	 */
	public void setupPullSecret(String name, String secret) {
		Secret pullSecret = new SecretBuilder()
				.withNewMetadata()
				.withNewName(name)
				.addToLabels(Kubernetes.KEEP_LABEL, "true")
				.endMetadata()
				.withNewType("kubernetes.io/dockerconfigjson")
				.withData(Collections.singletonMap(".dockerconfigjson", Base64.getEncoder().encodeToString(secret.getBytes())))
				.build();
		secrets().createOrReplace(pullSecret);
		serviceAccounts().withName("default").edit(new Visitor<ServiceAccountBuilder>() {
			@Override
			public void visit(ServiceAccountBuilder builder) {
				builder.addToImagePullSecrets(
						new LocalObjectReferenceBuilder().withName(pullSecret.getMetadata().getName()).build());
			}
		});

		serviceAccounts().withName("builder").edit(new Visitor<ServiceAccountBuilder>() {
			@Override
			public void visit(ServiceAccountBuilder builder) {
				builder.addToSecrets(new ObjectReferenceBuilder().withName(pullSecret.getMetadata().getName()).build());
			}
		});
	}

	private static Kubernetes get(Config kubeconfig) {
		Kubernetes kubernetes;

		// check whether such a client already exists
		Optional<Kubernetes> optionalKubernetes = namespaceToKubernetesClientMap
				.get(kubeconfig.getNamespace()).stream()
				.filter(kc -> isEqualConfig(kubeconfig, kc.getConfiguration()))
				.findFirst();

		if (optionalKubernetes.isPresent()) {
			return optionalKubernetes.get();
		} else {
			kubernetes = new Kubernetes(kubeconfig);
			namespaceToKubernetesClientMap.put(kubeconfig.getNamespace(), kubernetes);
		}
		return kubernetes;
	}

	private static void setupTimeouts(Config config) {
		//___*** (ShipWright?)config.setBuildTimeout(10 * 60 * 1000);
		config.setRequestTimeout(120_000);
		config.setConnectionTimeout(120_000);
	}

	protected static synchronized ServiceLoader<CustomResourceDefinitionContextProvider> getCRDContextProviders() {
		if (crdContextProviderLoader == null) {
			crdContextProviderLoader = ServiceLoader.load(CustomResourceDefinitionContextProvider.class);
		}
		return crdContextProviderLoader;
	}

	private static boolean isEqualConfig(Config newConfig, Config existingConfig) {
		return new EqualsBuilder()
				.append(newConfig.getMasterUrl(), existingConfig.getMasterUrl())
				.append(newConfig.getNamespace(), existingConfig.getNamespace())
				.append(newConfig.getUsername(), existingConfig.getUsername())
				.append(newConfig.getPassword(), existingConfig.getPassword())
				.append(newConfig.getRequestConfig().getOauthToken(), existingConfig.getRequestConfig().getOauthToken())
				.append(newConfig.isTrustCerts(), existingConfig.isTrustCerts())
				.isEquals();
	}

	/**
	 * Retrieves all configmaps but "kube-root-ca.crt" and "openshift-service-ca.crt" which are created out of the box.
	 *
	 * @return List of configmaps created by user
	 */
	public List<ConfigMap> getUserConfigMaps() {
		return configMaps().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems().stream()
				.filter(cm -> !cm.getMetadata().getName().equals("kube-root-ca.crt"))
				//.filter(cm -> !cm.getMetadata().getName().equals("openshift-service-ca.crt"))
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves secrets that aren't considered default. Secrets that are left out contain type starting with 'kubernetes.io/'.
	 *
	 * @return List of secrets that aren't considered default.
	 */
	public List<Secret> getUserSecrets() {
		return secrets().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems().stream()
				.filter(s -> !s.getType().startsWith("kubernetes.io/"))
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves service accounts that aren't considered default.
	 * Service accounts that are left out from list:
	 * <ul>
	 * <li>builder</li>
	 * <li>default</li>
	 * <li>deployer</li>
	 * </ul>
	 *
	 * @return List of service accounts that aren't considered default.
	 */
	public List<ServiceAccount> getUserServiceAccounts() {
		return serviceAccounts().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems().stream()
				.filter(sa -> !sa.getMetadata().getName().matches("builder|default|deployer"))
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves role bindings that aren't considered default.
	 * Role bindings that are left out from list:
	 * <ul>
	 * <li>admin</li>
	 * <li>system:deployers</li>
	 * <li>system:image-builders</li>
	 * <li>system:image-pullers</li>
	 * </ul>
	 *
	 * @return List of role bindings that aren't considered default.
	 */
	public List<RoleBinding> getUserRoleBindings() {
		return rbac().roleBindings().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true")
				.withLabelNotIn("olm.owner.kind", "ClusterServiceVersion").list().getItems().stream()
				.filter(rb -> !rb.getMetadata().getName()
						.matches("admin|system:deployers|system:image-builders|system:image-pullers"))
				.collect(Collectors.toList());
	}

	public Waiter clean() {
		for (CustomResourceDefinitionContextProvider crdContextProvider : Kubernetes.getCRDContextProviders()) {
			try {
				customResources(crdContextProvider.getContext(), GenericKubernetesResource.class,
						GenericKubernetesResourceList.class)
						.inNamespace(getNamespace()).delete();
				log.debug("DELETE :: " + crdContextProvider.getContext().getName() + " instances");
			} catch (KubernetesClientException kce) {
				log.debug(crdContextProvider.getContext().getName() + " might not be installed on the cluster.", kce);
			}
		}

		/* Only OpenShift has the following ones, which are missing from k8s
		templates().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		deploymentConfigs().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		buildConfigs().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		imageStreams().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		builds().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		routes().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		*/

		// keep the order for deletion to prevent K8s creating resources again
		apps().deployments().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").delete();
		apps().replicaSets().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").delete();
		apps().statefulSets().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").delete();
		batch().jobs().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").delete();
		replicationControllers().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").delete();
		endpoints().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").delete();
		services().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").delete();
		pods().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").withGracePeriod(0).delete();
		persistentVolumeClaims().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").delete();
		autoscaling().v1().horizontalPodAutoscalers().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").delete();

		getUserConfigMaps().forEach(c -> configMaps().delete(c));
		getUserSecrets().forEach(s -> secrets().delete(s));
		getUserServiceAccounts().forEach((sa) -> {
			serviceAccounts().delete(sa);
		});
		getUserRoleBindings().forEach(r -> rbac().roleBindings().delete(r));
		rbac().roles().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true")
				.withLabelNotIn("olm.owner.kind", "ClusterServiceVersion")
				.delete();

		for (HasMetadata hasMetadata : listRemovableResources()) {
			log.warn("DELETE LEFTOVER :: " + hasMetadata.getKind() + "/" + hasMetadata.getMetadata().getName());
			resource(hasMetadata).withGracePeriod(0).cascading(true).delete();
		}

		FailFastCheck failFastCheck = () -> false;
		return new SimpleWaiter(
				() -> isNamespaceClean(),
				TimeUnit.MILLISECONDS, WaitingConfig.timeoutCleanup(), "Cleaning project - " + getNamespace())
				.onTimeout(() -> log.info("Cleaning namespace: " + getNamespace() + " - timed out."))
				.onFailure(() -> log.info("Cleaning namespace: " + getNamespace() + " - failed."))
				.onSuccess(() -> log.info("Cleaning namespace: " + getNamespace() + " - finished."))
				.failFast(failFastCheck);
	}

	List<HasMetadata> listRemovableResources() {
		/* Only OpenShift has the following ones, which are missing from k8s
		removables.addAll(templates().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(deploymentConfigs().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(buildConfigs().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(imageStreams().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(builds().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(routes().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		*/

		// keep the order for deletion to prevent K8s creating resources again
		List<HasMetadata> removables = new ArrayList<>();
		removables.addAll(apps().deployments().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(apps().replicaSets().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(batch().jobs().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(apps().statefulSets().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(replicationControllers().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(endpoints().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(services().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(pods().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(persistentVolumeClaims().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(autoscaling().v1().horizontalPodAutoscalers().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true").list()
				.getItems());
		removables.addAll(getUserConfigMaps());
		removables.addAll(getUserSecrets());
		removables.addAll(getUserServiceAccounts());
		removables.addAll(getUserRoleBindings());
		removables.addAll(rbac().roles().withLabelNotIn(Kubernetes.KEEP_LABEL, "", "true")
				.withLabelNotIn("olm.owner.kind", "ClusterServiceVersion").list().getItems());

		return removables;
	}

	private boolean isNamespaceClean() {
		int crdInstances = 0;
		List<GenericKubernetesResource> customResourceDefinitionList = null;
		for (CustomResourceDefinitionContextProvider crdContextProvider : Kubernetes.getCRDContextProviders()) {
			try {
				customResourceDefinitionList = customResources(crdContextProvider.getContext(),
						GenericKubernetesResource.class, GenericKubernetesResourceList.class)
						.inNamespace(getNamespace())
						.list().getItems();
				crdInstances += customResourceDefinitionList.size();
			} catch (KubernetesClientException kce) {
				// CRD might not be installed on the cluster
			}
		}

		boolean isClean = false;
		List<HasMetadata> listRemovableResources = listRemovableResources();
		if (crdInstances == 0 & listRemovableResources.isEmpty()) {
			isClean = true;
		} else {
			StringBuilder strBuilderResourcesToDelete = new StringBuilder(
					"Cleaning project - " + getNamespace()
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
	}

	public String generateHostname() {
		log.info("Kubernetes generateHostname returns: " + KubernetesConfig.getKubernetesHostname());
		return KubernetesConfig.getKubernetesHostname();
	}

}
