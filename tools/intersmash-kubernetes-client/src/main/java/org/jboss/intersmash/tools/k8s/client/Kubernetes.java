package org.jboss.intersmash.tools.k8s.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import io.fabric8.kubernetes.api.builder.Visitor;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

public class Kubernetes extends DefaultKubernetesClient {
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
		Config kubeconfig = Config.empty();
		kubeconfig.setMasterUrl(masterUrl);
		kubeconfig.setTrustCerts(true);
		kubeconfig.setNamespace(namespace);
		kubeconfig.setUsername(username);
		kubeconfig.setPassword(password);

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
				.addToLabels(KEEP_LABEL, "true")
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

}
