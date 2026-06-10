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
package org.jboss.intersmash.tools.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jboss.intersmash.tools.config.IntersmashProperties;
import org.jboss.intersmash.tools.config.WaitingConfig;
import org.jboss.intersmash.tools.waiting.SimpleWaiter;
import org.jboss.intersmash.tools.waiting.Waiter;
import org.jboss.intersmash.tools.waiting.failfast.FailFastCheck;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import io.fabric8.kubernetes.api.builder.Visitor;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerList;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccountList;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.kubernetes.client.dsl.ApiextensionsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.AppsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.AutoscalingAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.BatchAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NamespaceableResource;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.ParameterMixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.RbacAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import io.fabric8.kubernetes.client.dsl.ServiceAccountResource;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import io.fabric8.kubernetes.client.dsl.V1APIGroupDSL;
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.HasMetadataOperationsImpl;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigList;
import io.fabric8.openshift.api.model.BuildList;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigList;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamList;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteList;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.api.model.TemplateList;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.dsl.BuildConfigResource;
import io.fabric8.openshift.client.dsl.BuildResource;
import io.fabric8.openshift.client.dsl.DeployableScalableResource;
import io.fabric8.openshift.client.dsl.TemplateResource;
import lombok.extern.slf4j.Slf4j;

/**
 * An OpenShift client wrapper that provides additional convenience methods on top of the Fabric8
 * {@link NamespacedOpenShiftClient}. This class replaces XTF's {@code cz.xtf.core.openshift.OpenShift}
 * with an Intersmash-native implementation.
 *
 * <p>The class delegates all Fabric8 operations to a wrapped {@link NamespacedOpenShiftClient} instance,
 * which can be obtained via {@link #getClient()} for direct Fabric8 API calls.</p>
 *
 * <p>Instances are cached per namespace using a synchronized multimap, so repeated calls to
 * {@link #get(String)} with the same configuration return the same instance.</p>
 */
@Slf4j
public class OpenShift {

	/**
	 * SPI interface for custom resource definition context providers used by {@link #clean()} and
	 * {@link #isNamespaceClean()} to discover and delete CRD instances.
	 *
	 * <p>Implementations should be registered via {@code META-INF/services}.</p>
	 */
	public interface CustomResourceDefinitionContextProvider {
		ResourceDefinitionContext getContext();
	}

	private static ServiceLoader<CustomResourceDefinitionContextProvider> crdContextProviderLoader;

	/**
	 * This label is supposed to be used for any resource created by Intersmash to easily distinguish which resources have
	 * been created by Intersmash automation.
	 */
	public static final String INTERSMASH_MANAGED_LABEL = "intersmash/managed";

	/**
	 * Property key for the OpenShift route domain.
	 */
	private static final String OPENSHIFT_ROUTE_DOMAIN = "intersmash.openshift.route_domain";
	private static final String XTF_ROUTE_DOMAIN = "xtf.openshift.route_domain";

	private static volatile String routeSuffix;

	/**
	 * Used to cache created OpenShift clients for given test case.
	 */
	public static final Multimap<String, OpenShift> namespaceToClientMap = Multimaps
			.synchronizedListMultimap(ArrayListMultimap.create());

	public static final String KEEP_LABEL = "intersmash/keep";

	private final NamespacedOpenShiftClient client;

	/**
	 * Creates a new OpenShift wrapper around the given client.
	 *
	 * @param client the Fabric8 {@link NamespacedOpenShiftClient} to wrap
	 */
	public OpenShift(NamespacedOpenShiftClient client) {
		this.client = client;
	}

	/**
	 * Returns the underlying Fabric8 {@link NamespacedOpenShiftClient} for direct API calls.
	 *
	 * @return the wrapped OpenShift client
	 */
	public NamespacedOpenShiftClient getClient() {
		return client;
	}

	/**
	 * Returns the namespace this client operates in.
	 *
	 * @return the client namespace
	 */
	public String getNamespace() {
		return client.getNamespace();
	}

	/**
	 * Returns the Fabric8 configuration for this client.
	 *
	 * @return the client configuration
	 */
	public Config getConfiguration() {
		return client.getConfiguration();
	}

	// ========================
	// Fabric8 KubernetesClient delegation methods
	// ========================

	/**
	 * Returns version information about the Kubernetes cluster.
	 *
	 * @return {@link VersionInfo} with cluster version details
	 */
	public VersionInfo getVersion() {
		return client.getKubernetesVersion();
	}

	/**
	 * Returns a client scoped to the given namespace.
	 *
	 * @param namespace the target namespace
	 * @return a namespace-scoped {@link NamespacedOpenShiftClient}
	 */
	public NamespacedOpenShiftClient inNamespace(String namespace) {
		return client.inNamespace(namespace);
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#v1()} for core v1 API access (e.g. Events).
	 */
	public V1APIGroupDSL v1() {
		return client.v1();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#apiextensions()} for CRD API access.
	 */
	public ApiextensionsAPIGroupDSL apiextensions() {
		return client.apiextensions();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#apps()} for Deployments, StatefulSets, ReplicaSets.
	 */
	public AppsAPIGroupDSL apps() {
		return client.apps();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#batch()} for Jobs, CronJobs.
	 */
	public BatchAPIGroupDSL batch() {
		return client.batch();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#autoscaling()} for HorizontalPodAutoscalers.
	 */
	public AutoscalingAPIGroupDSL autoscaling() {
		return client.autoscaling();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#rbac()} for Roles, RoleBindings, ClusterRoles, ClusterRoleBindings.
	 */
	public RbacAPIGroupDSL rbac() {
		return client.rbac();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#secrets()}.
	 */
	public MixedOperation<Secret, SecretList, Resource<Secret>> secrets() {
		return client.secrets();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#services()}.
	 */
	public MixedOperation<Service, ServiceList, ServiceResource<Service>> services() {
		return client.services();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#pods()}.
	 */
	public MixedOperation<Pod, PodList, PodResource> pods() {
		return client.pods();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#endpoints()}.
	 */
	public MixedOperation<Endpoints, EndpointsList, Resource<Endpoints>> endpoints() {
		return client.endpoints();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#persistentVolumeClaims()}.
	 */
	public MixedOperation<PersistentVolumeClaim, PersistentVolumeClaimList, Resource<PersistentVolumeClaim>> persistentVolumeClaims() {
		return client.persistentVolumeClaims();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#replicationControllers()}.
	 */
	public MixedOperation<ReplicationController, ReplicationControllerList, RollableScalableResource<ReplicationController>> replicationControllers() {
		return client.replicationControllers();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#namespaces()}.
	 */
	public NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespaces() {
		return client.namespaces();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#serviceAccounts()}.
	 */
	public MixedOperation<ServiceAccount, ServiceAccountList, ServiceAccountResource> serviceAccounts() {
		return client.serviceAccounts();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#configMaps()}.
	 */
	public MixedOperation<ConfigMap, ConfigMapList, Resource<ConfigMap>> configMaps() {
		return client.configMaps();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#resource(HasMetadata)} for resource operations.
	 */
	public <T extends HasMetadata> NamespaceableResource<T> resource(T item) {
		return client.resource(item);
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#genericKubernetesResources(ResourceDefinitionContext)} for CRD access.
	 */
	public MixedOperation<GenericKubernetesResource, GenericKubernetesResourceList, Resource<GenericKubernetesResource>> genericKubernetesResources(
			ResourceDefinitionContext context) {
		return client.genericKubernetesResources(context);
	}

	// ========================
	// Fabric8 OpenShift-specific delegation methods
	// ========================

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#deploymentConfigs()}.
	 */
	public MixedOperation<DeploymentConfig, DeploymentConfigList, DeployableScalableResource<DeploymentConfig>> deploymentConfigs() {
		return client.deploymentConfigs();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#buildConfigs()}.
	 */
	public MixedOperation<BuildConfig, BuildConfigList, BuildConfigResource<BuildConfig, Void, Build>> buildConfigs() {
		return client.buildConfigs();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#imageStreams()}.
	 */
	public MixedOperation<ImageStream, ImageStreamList, Resource<ImageStream>> imageStreams() {
		return client.imageStreams();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#builds()}.
	 */
	public MixedOperation<Build, BuildList, BuildResource> builds() {
		return client.builds();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#routes()}.
	 */
	public MixedOperation<Route, RouteList, Resource<Route>> routes() {
		return client.routes();
	}

	/**
	 * Delegates to {@link NamespacedOpenShiftClient#templates()}.
	 */
	public ParameterMixedOperation<Template, TemplateList, TemplateResource> templates() {
		return client.templates();
	}

	// ========================
	// Convenience helper methods (matching XTF's OpenShift API)
	// ========================

	/**
	 * Creates a Secret resource.
	 */
	public Secret createSecret(Secret secret) {
		return client.secrets().resource(secret).create();
	}

	/**
	 * Deletes a Secret resource.
	 */
	public boolean deleteSecret(Secret secret) {
		return !client.secrets().resource(secret).delete().isEmpty();
	}

	/**
	 * Creates a ConfigMap resource.
	 */
	public ConfigMap createConfigMap(ConfigMap configMap) {
		return client.configMaps().resource(configMap).create();
	}

	/**
	 * Deletes a ConfigMap resource.
	 */
	public boolean deleteConfigMap(ConfigMap configMap) {
		return !client.configMaps().resource(configMap).delete().isEmpty();
	}

	/**
	 * Creates a Template resource.
	 */
	public Template createTemplate(Template template) {
		return client.templates().resource(template).create();
	}

	/**
	 * Returns a Template by name, or null if not found.
	 */
	public Template getTemplate(String name) {
		return client.templates().withName(name).get();
	}

	/**
	 * Deletes a Template by name.
	 */
	public boolean deleteTemplate(String name) {
		return !client.templates().withName(name).delete().isEmpty();
	}

	/**
	 * Deletes a Template resource.
	 */
	public boolean deleteTemplate(Template template) {
		return !client.templates().resource(template).delete().isEmpty();
	}

	/**
	 * Processes a template with the given parameters and returns the resulting resources.
	 */
	public KubernetesList processTemplate(String name, Map<String, String> parameters) {
		io.fabric8.openshift.client.ParameterValue[] values = parameters.entrySet().stream()
				.map(entry -> new io.fabric8.openshift.client.ParameterValue(entry.getKey(), entry.getValue()))
				.toArray(io.fabric8.openshift.client.ParameterValue[]::new);
		return client.templates().withName(name).process(values);
	}

	/**
	 * Processes a template and deploys (creates) the resulting resources.
	 */
	public KubernetesList processAndDeployTemplate(String name, Map<String, String> parameters) {
		return createResources(processTemplate(name, parameters));
	}

	/**
	 * Creates all resources from a KubernetesList.
	 */
	public KubernetesList createResources(KubernetesList resources) {
		KubernetesList result = new KubernetesList();
		result.setItems(client.resourceList(resources.getItems()).create());
		return result;
	}

	/**
	 * Creates all resources from a list of HasMetadata.
	 */
	public KubernetesList createResources(List<HasMetadata> resources) {
		KubernetesList list = new KubernetesList();
		list.setItems(resources);
		return createResources(list);
	}

	/**
	 * Creates all resources from varargs.
	 */
	public KubernetesList createResources(HasMetadata... resources) {
		return createResources(Arrays.asList(resources));
	}

	/**
	 * Deletes all resources from a KubernetesList.
	 */
	public boolean deleteResources(KubernetesList resources) {
		return resources.getItems().size() == client.resourceList(resources.getItems()).delete().size();
	}

	/**
	 * Returns all endpoints in the current namespace.
	 */
	public List<Endpoints> getEndpoints() {
		return client.endpoints().list().getItems();
	}

	/**
	 * Returns all pods in the current namespace.
	 */
	public List<Pod> getPods() {
		return client.pods().list().getItems();
	}

	/**
	 * Returns all pods associated with the given deployment config.
	 */
	public List<Pod> getPods(String deploymentConfigName) {
		return getLabeledPods("deploymentconfig", deploymentConfigName);
	}

	/**
	 * Returns any single pod matching the given deployment config name.
	 *
	 * @param dcName the name of the deployment config
	 * @return a Pod instance, or null if no pods match
	 */
	public Pod getAnyPod(String dcName) {
		List<Pod> pods = getPods(dcName);
		return pods.isEmpty() ? null : pods.get(0);
	}

	/**
	 * Returns pods matching a label key/value pair.
	 */
	public List<Pod> getLabeledPods(String key, String value) {
		return client.pods().withLabel(key, value).list().getItems();
	}

	/**
	 * Returns pods matching a set of labels.
	 */
	public List<Pod> getLabeledPods(Map<String, String> labels) {
		return client.pods().withLabels(labels).list().getItems();
	}

	/**
	 * Scales a deployment config to the specified number of replicas.
	 */
	public void scale(String name, int replicas) {
		client.deploymentConfigs().withName(name).scale(replicas);
	}

	/**
	 * Returns all ConfigMaps in the current namespace.
	 */
	public List<ConfigMap> getConfigMaps() {
		return client.configMaps().list().getItems();
	}

	/**
	 * Returns a ConfigMap by name, or null if not found.
	 */
	public ConfigMap getConfigMap(String name) {
		return client.configMaps().withName(name).get();
	}

	/**
	 * Returns a Service by name, or null if not found.
	 */
	public Service getService(String name) {
		return client.services().withName(name).get();
	}

	/**
	 * Returns a Secret by name, or null if not found.
	 */
	public Secret getSecret(String name) {
		return client.secrets().withName(name).get();
	}

	/**
	 * Returns the environment variables of the first container of a DeploymentConfig as a Map.
	 */
	public Map<String, String> getDeploymentConfigEnvVars(String dcName) {
		DeploymentConfig dc = getDeploymentConfig(dcName);
		if (dc == null || dc.getSpec() == null || dc.getSpec().getTemplate() == null
				|| dc.getSpec().getTemplate().getSpec() == null
				|| dc.getSpec().getTemplate().getSpec().getContainers() == null
				|| dc.getSpec().getTemplate().getSpec().getContainers().isEmpty()) {
			return Collections.emptyMap();
		}
		return dc.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().stream()
				.filter(env -> env.getValue() != null)
				.collect(Collectors.toMap(
						io.fabric8.kubernetes.api.model.EnvVar::getName,
						io.fabric8.kubernetes.api.model.EnvVar::getValue));
	}

	/**
	 * Returns a DeploymentConfig by name, or null if not found.
	 */
	public DeploymentConfig getDeploymentConfig(String name) {
		return client.deploymentConfigs().withName(name).get();
	}

	/**
	 * Returns all DeploymentConfigs in the current namespace.
	 */
	public List<DeploymentConfig> getDeploymentConfigs() {
		return client.deploymentConfigs().list().getItems();
	}

	/**
	 * Returns a Route by name, or null if not found.
	 */
	public Route getRoute(String name) {
		return client.routes().withName(name).get();
	}

	/**
	 * Returns all Routes in the current namespace.
	 */
	public List<Route> getRoutes() {
		return client.routes().list().getItems();
	}

	/**
	 * Returns a StatefulSet by name, or null if not found.
	 */
	public io.fabric8.kubernetes.api.model.apps.StatefulSet getStatefulSet(String name) {
		return client.apps().statefulSets().withName(name).get();
	}

	/**
	 * Returns all ImageStreams in the current namespace.
	 */
	public List<ImageStream> getImageStreams() {
		return client.imageStreams().list().getItems();
	}

	/**
	 * Deletes an ImageStream resource.
	 */
	public boolean deleteImageStream(ImageStream imageStream) {
		return !client.imageStreams().resource(imageStream).delete().isEmpty();
	}

	/**
	 * Returns all Builds in the current namespace.
	 */
	public List<Build> getBuilds() {
		return client.builds().list().getItems();
	}

	/**
	 * Deletes a Build resource.
	 */
	public boolean deleteBuild(Build build) {
		return !client.builds().resource(build).delete().isEmpty();
	}

	/**
	 * Returns all BuildConfigs in the current namespace.
	 */
	public List<BuildConfig> getBuildConfigs() {
		return client.buildConfigs().list().getItems();
	}

	/**
	 * Returns all Secrets in the current namespace.
	 */
	public List<Secret> getSecrets() {
		return client.secrets().list().getItems();
	}

	/**
	 * Returns all Services in the current namespace.
	 */
	public List<Service> getServices() {
		return client.services().list().getItems();
	}

	/**
	 * Returns all Events in the current namespace.
	 */
	public List<io.fabric8.kubernetes.api.model.Event> getEvents() {
		return client.v1().events().list().getItems();
	}

	/**
	 * Returns all StatefulSets in the current namespace.
	 */
	public List<io.fabric8.kubernetes.api.model.apps.StatefulSet> getStatefulSets() {
		return client.apps().statefulSets().list().getItems();
	}

	/**
	 * Stores a pod's log to a file.
	 */
	public void storePodLog(Pod pod, java.nio.file.Path dir, String filename) throws java.io.IOException {
		String logContent = getPodLog(pod);
		java.nio.file.Files.createDirectories(dir);
		java.nio.file.Files.writeString(dir.resolve(filename), logContent != null ? logContent : "");
	}

	/**
	 * Stores a build's log to a file.
	 */
	public void storeBuildLog(Build build, java.nio.file.Path dir, String filename) throws java.io.IOException {
		String logContent = client.builds().withName(build.getMetadata().getName()).getLog();
		java.nio.file.Files.createDirectories(dir);
		java.nio.file.Files.writeString(dir.resolve(filename), logContent != null ? logContent : "");
	}

	/**
	 * Deletes a BuildConfig resource.
	 */
	public boolean deleteBuildConfig(BuildConfig buildConfig) {
		return !client.buildConfigs().resource(buildConfig).delete().isEmpty();
	}

	/**
	 * Deletes a Pod resource.
	 */
	public boolean deletePod(Pod pod) {
		return deletePod(pod, 0L);
	}

	/**
	 * Deletes a Pod resource with the given grace period.
	 */
	public boolean deletePod(Pod pod, long gracePeriod) {
		return !client.pods().withName(pod.getMetadata().getName()).withGracePeriod(gracePeriod).delete().isEmpty();
	}

	/**
	 * Creates a PersistentVolumeClaim resource.
	 */
	public PersistentVolumeClaim createPersistentVolumeClaim(PersistentVolumeClaim pvc) {
		return client.persistentVolumeClaims().resource(pvc).create();
	}

	/**
	 * Creates an ImageStream resource.
	 */
	public ImageStream createImageStream(ImageStream imageStream) {
		return client.imageStreams().resource(imageStream).create();
	}

	/**
	 * Returns an ImageStreamTag by name and tag, or null if not found.
	 */
	public io.fabric8.openshift.api.model.ImageStreamTag getImageStreamTag(String name, String tag) {
		return client.imageStreamTags().withName(name + ":" + tag).get();
	}

	/**
	 * Loads resources from an InputStream.
	 */
	public io.fabric8.kubernetes.client.dsl.NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> load(
			java.io.InputStream is) {
		return client.load(is);
	}

	/**
	 * Returns an ImageStream by name, or null if not found.
	 */
	public ImageStream getImageStream(String name) {
		return client.imageStreams().withName(name).get();
	}

	/**
	 * Returns a BuildConfig by name, or null if not found.
	 */
	public BuildConfig getBuildConfig(String name) {
		return client.buildConfigs().withName(name).get();
	}

	/**
	 * Returns a Build by name, or null if not found.
	 */
	public Build getBuild(String name) {
		return client.builds().withName(name).get();
	}

	/**
	 * Returns a Project by name, or null if not found.
	 */
	public io.fabric8.openshift.api.model.Project getProject(String name) {
		return client.projects().withName(name).get();
	}

	/**
	 * Creates a ProjectRequest for the current namespace.
	 */
	public void createProjectRequest() {
		createProjectRequest(getNamespace());
	}

	/**
	 * Creates a ProjectRequest for the specified namespace.
	 */
	public void createProjectRequest(String namespace) {
		client.projectrequests().create(
				new io.fabric8.openshift.api.model.ProjectRequestBuilder()
						.withNewMetadata().withName(namespace).endMetadata()
						.build());
	}

	/**
	 * Returns a ResourceQuota by name, or null if not found.
	 */
	public io.fabric8.kubernetes.api.model.ResourceQuota getResourceQuota(String name) {
		return client.resourceQuotas().withName(name).get();
	}

	/**
	 * Creates a ResourceQuota resource.
	 */
	public io.fabric8.kubernetes.api.model.ResourceQuota createResourceQuota(io.fabric8.kubernetes.api.model.ResourceQuota rq) {
		return client.resourceQuotas().resource(rq).create();
	}

	/**
	 * Adds a role to a group in the current namespace.
	 */
	public void addRoleToGroup(String roleName, String roleKind, String groupName) {
		io.fabric8.kubernetes.api.model.rbac.RoleBinding roleBinding = new io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder()
				.withNewMetadata().withName(roleName + "-" + groupName).endMetadata()
				.withNewRoleRef().withApiGroup("rbac.authorization.k8s.io").withName(roleName).withKind(roleKind).endRoleRef()
				.addNewSubject().withKind("Group").withName(groupName).withApiGroup("rbac.authorization.k8s.io").endSubject()
				.build();
		client.rbac().roleBindings().resource(roleBinding).createOrReplace();
	}

	/**
	 * Adds a role to a service account in the current namespace.
	 */
	public void addRoleToServiceAccount(String roleName, String serviceAccountName) {
		RoleBinding roleBinding = new io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder()
				.withNewMetadata().withName(roleName + "-" + serviceAccountName).endMetadata()
				.withNewRoleRef().withApiGroup("rbac.authorization.k8s.io").withName(roleName).withKind("ClusterRole")
				.endRoleRef()
				.addNewSubject().withKind("ServiceAccount").withName(serviceAccountName).withNamespace(getNamespace())
				.endSubject()
				.build();
		client.rbac().roleBindings().resource(roleBinding).createOrReplace();
	}

	/**
	 * Returns the log output of a pod.
	 */
	public String getPodLog(Pod pod) {
		return client.pods().withName(pod.getMetadata().getName()).getLog();
	}

	/**
	 * Returns the log output of the first pod matching the given deployment config name.
	 */
	public String getPodLog(String dcName) {
		Pod pod = getAnyPod(dcName);
		if (pod == null) {
			throw new IllegalStateException("No pod found for deployment config: " + dcName);
		}
		return getPodLog(pod);
	}

	/**
	 * Returns an Endpoints resource by name, or null if not found.
	 */
	public Endpoints getEndpoint(String name) {
		return client.endpoints().withName(name).get();
	}

	/**
	 * Returns a Pod by name, or null if not found.
	 */
	public Pod getPod(String name) {
		return client.pods().withName(name).get();
	}

	/**
	 * Returns a {@link PodShell} for a pod associated with the given deployment config name.
	 */
	public PodShell podShell(String dcName) {
		return new PodShell(this, dcName);
	}

	/**
	 * Returns a {@link PodShell} for the given pod.
	 */
	public PodShell podShell(Pod pod) {
		return new PodShell(this, pod);
	}

	/**
	 * Returns an {@link OpenShiftWaiters} instance for this client.
	 */
	public OpenShiftWaiters waiters() {
		return OpenShiftWaiters.get(this, () -> false);
	}

	// ========================
	// Static factory methods
	// ========================

	/**
	 * Autoconfigures the client with the default Fabric8 client rules.
	 *
	 * @param namespace set namespace to the OpenShift client instance
	 * @return an OpenShift client instance
	 */
	public static OpenShift get(String namespace) {
		Config kubeconfig = Config.autoConfigure(null);
		setupTimeouts(kubeconfig);
		if (StringUtils.isNotEmpty(namespace)) {
			kubeconfig.setNamespace(namespace);
		}
		return get(kubeconfig);
	}

	/**
	 * Creates a client from a kubeconfig file.
	 *
	 * @param kubeconfigPath path to the kubeconfig file
	 * @param namespace namespace to use
	 * @return an OpenShift client instance
	 */
	public static OpenShift get(Path kubeconfigPath, String namespace) {
		try {
			String kubeconfigContents = new String(Files.readAllBytes(kubeconfigPath), StandardCharsets.UTF_8);
			Config kubeconfig = Config.fromKubeconfig(null, kubeconfigContents,
					kubeconfigPath.toAbsolutePath().toString());
			setupTimeouts(kubeconfig);
			if (StringUtils.isNotEmpty(namespace)) {
				kubeconfig.setNamespace(namespace);
			}
			return get(kubeconfig);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a client from a URL, namespace, and OAuth token.
	 *
	 * @param masterUrl the OpenShift API URL
	 * @param namespace namespace to use
	 * @param token the OAuth token
	 * @return an OpenShift client instance
	 */
	public static OpenShift get(String masterUrl, String namespace, String token) {
		Config kubeconfig = Config.empty();
		kubeconfig.setMasterUrl(masterUrl);
		kubeconfig.setTrustCerts(true);
		kubeconfig.setNamespace(namespace);
		kubeconfig.setOauthToken(token);
		setupTimeouts(kubeconfig);
		return get(kubeconfig);
	}

	/**
	 * Creates a client from a URL, namespace, username, and password.
	 *
	 * @param masterUrl the OpenShift API URL
	 * @param namespace namespace to use
	 * @param username the username
	 * @param password the password
	 * @return an OpenShift client instance
	 */
	public static OpenShift get(String masterUrl, String namespace, String username, String password) {
		Config kubeconfig = new ConfigBuilder()
				.withMasterUrl(masterUrl)
				.withTrustCerts(true)
				.withNamespace(namespace)
				.withUsername(username)
				.withPassword(password).build();
		setupTimeouts(kubeconfig);
		return get(kubeconfig);
	}

	private static OpenShift get(Config kubeconfig) {
		// check whether such a client already exists
		Optional<OpenShift> existing = namespaceToClientMap
				.get(kubeconfig.getNamespace()).stream()
				.filter(os -> isEqualConfig(kubeconfig, os.getConfiguration()))
				.findFirst();

		if (existing.isPresent()) {
			return existing.get();
		} else {
			NamespacedOpenShiftClient fabricClient = new KubernetesClientBuilder()
					.withConfig(kubeconfig)
					.build()
					.adapt(NamespacedOpenShiftClient.class);
			OpenShift openShift = new OpenShift(fabricClient);
			namespaceToClientMap.put(kubeconfig.getNamespace(), openShift);
			return openShift;
		}
	}

	private static void setupTimeouts(Config config) {
		config.setRequestTimeout(120_000);
		config.setConnectionTimeout(120_000);
	}

	private static boolean isEqualConfig(Config newConfig, Config existingConfig) {
		return new EqualsBuilder()
				.append(newConfig.getMasterUrl(), existingConfig.getMasterUrl())
				.append(newConfig.getNamespace(), existingConfig.getNamespace())
				.append(newConfig.getUsername(), existingConfig.getUsername())
				.append(newConfig.getPassword(), existingConfig.getPassword())
				.append(newConfig.getOauthToken(), existingConfig.getOauthToken())
				.append(newConfig.isTrustCerts(), existingConfig.isTrustCerts())
				.isEquals();
	}

	// ========================
	// Pull secret setup
	// ========================

	/**
	 * Sets up a pull secret with the default name "intersmash-pull-secret".
	 *
	 * @param secret the dockerconfigjson content
	 */
	public void setupPullSecret(String secret) {
		setupPullSecret("intersmash-pull-secret", secret);
	}

	/**
	 * Creates a pull secret for authenticated image registries.
	 * The secret content must be provided in "dockerconfigjson" format.
	 *
	 * <p>E.g.: {@code {"auths":{"registry.redhat.io":{"auth":"<REDACTED_TOKEN>"}}}}</p>
	 *
	 * @param name name of the Secret to be created
	 * @param secret content of Secret in JSON format
	 */
	public void setupPullSecret(String name, String secret) {
		Secret pullSecret = new SecretBuilder()
				.withNewMetadata()
				.withName(name)
				.addToLabels(KEEP_LABEL, "true")
				.endMetadata()
				.withType("kubernetes.io/dockerconfigjson")
				.withData(Collections.singletonMap(".dockerconfigjson",
						Base64.getEncoder().encodeToString(secret.getBytes())))
				.build();
		client.secrets().createOrReplace(pullSecret);
		client.serviceAccounts().withName("default").edit(new Visitor<ServiceAccountBuilder>() {
			@Override
			public void visit(ServiceAccountBuilder builder) {
				builder.addToImagePullSecrets(
						new LocalObjectReferenceBuilder().withName(pullSecret.getMetadata().getName()).build());
			}
		});
		client.serviceAccounts().withName("builder").edit(new Visitor<ServiceAccountBuilder>() {
			@Override
			public void visit(ServiceAccountBuilder builder) {
				builder.addToSecrets(
						new ObjectReferenceBuilder().withName(pullSecret.getMetadata().getName()).build());
			}
		});
	}

	// ========================
	// Hostname generation
	// ========================

	/**
	 * Generates a hostname for a route in the format {@code {routeName}-{namespace}.{routeDomain}}.
	 *
	 * <p>The route domain is resolved from the {@code intersmash.openshift.route_domain} property
	 * (with XTF fallback {@code xtf.openshift.route_domain}). If neither is set, the domain is
	 * auto-detected by creating a temporary probe route on the cluster.</p>
	 *
	 * @param routeName the name of the route
	 * @return the generated hostname
	 */
	public String generateHostname(String routeName) {
		if (routeSuffix == null) {
			synchronized (OpenShift.class) {
				if (routeSuffix == null) {
					String routeDomain = IntersmashProperties.get(OPENSHIFT_ROUTE_DOMAIN);
					if (routeDomain == null) {
						routeDomain = IntersmashProperties.get(XTF_ROUTE_DOMAIN);
					}
					if (routeDomain != null) {
						routeSuffix = routeDomain;
					} else {
						routeSuffix = retrieveRouteSuffix();
					}
				}
			}
		}
		return routeName + "-" + getNamespace() + "." + routeSuffix;
	}

	private String retrieveRouteSuffix() {
		Route route = new Route();
		route.setMetadata(new io.fabric8.kubernetes.api.model.ObjectMetaBuilder().withName("probing-route").build());
		route.setSpec(new io.fabric8.openshift.api.model.RouteSpecBuilder()
				.withNewTo().withKind("Service").withName("imaginary-service").endTo().build());

		route = client.routes().resource(route).create();
		String host = route.getSpec().getHost();
		client.routes().resource(route).delete();

		log.info("Auto-detected route domain suffix: {}", host.replaceAll("^[^\\.]*\\.(.*)", "$1"));
		return host.replaceAll("^[^\\.]*\\.(.*)", "$1");
	}

	// ========================
	// Filtered resource retrieval (for clean())
	// ========================

	/**
	 * Retrieves all configmaps but "kube-root-ca.crt" and "openshift-service-ca.crt" which are created out of the box.
	 *
	 * @return List of configmaps created by user
	 */
	public List<ConfigMap> getUserConfigMaps() {
		return client.configMaps().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems().stream()
				.filter(cm -> !cm.getMetadata().getName().equals("kube-root-ca.crt"))
				.filter(cm -> !cm.getMetadata().getName().equals("openshift-service-ca.crt"))
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves secrets that are not considered default. Secrets with type starting with 'kubernetes.io/' are excluded.
	 *
	 * @return List of secrets that are not considered default.
	 */
	public List<Secret> getUserSecrets() {
		return client.secrets().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems().stream()
				.filter(s -> !s.getType().startsWith("kubernetes.io/"))
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves service accounts that are not considered default.
	 * Excludes: builder, default, deployer.
	 *
	 * @return List of service accounts that are not considered default.
	 */
	public List<ServiceAccount> getUserServiceAccounts() {
		return client.serviceAccounts().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems().stream()
				.filter(sa -> !sa.getMetadata().getName().matches("builder|default|deployer"))
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves role bindings that are not considered default.
	 * Excludes: admin, system:deployers, system:image-builders, system:image-pullers.
	 *
	 * @return List of role bindings that are not considered default.
	 */
	public List<RoleBinding> getUserRoleBindings() {
		return client.rbac().roleBindings().withLabelNotIn(KEEP_LABEL, "", "true")
				.withLabelNotIn("olm.owner.kind", "ClusterServiceVersion").list().getItems().stream()
				.filter(rb -> !rb.getMetadata().getName()
						.matches("admin|system:deployers|system:image-builders|system:image-pullers"))
				.collect(Collectors.toList());
	}

	// ========================
	// Clean
	// ========================

	/**
	 * Orchestrates ordered deletion of all user-created resources in the namespace, including
	 * OpenShift-specific resource types (templates, deploymentConfigs, buildConfigs, imageStreams,
	 * builds, routes) and standard Kubernetes types.
	 *
	 * @return a {@link Waiter} that waits until the namespace is clean
	 */
	public Waiter clean() {
		// Delete CRD instances
		for (CustomResourceDefinitionContextProvider crdContextProvider : getCRDContextProviders()) {
			try {
				client.genericKubernetesResources(crdContextProvider.getContext())
						.inNamespace(getNamespace()).delete();
				log.debug("DELETE :: {} instances", crdContextProvider.getContext().getPlural());
			} catch (KubernetesClientException kce) {
				log.debug("{} might not be installed on the cluster.",
						crdContextProvider.getContext().getPlural(), kce);
			}
		}

		// Delete OpenShift-specific resources
		client.templates().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.deploymentConfigs().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.buildConfigs().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.imageStreams().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.builds().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.routes().withLabelNotIn(KEEP_LABEL, "", "true").delete();

		// Delete Kubernetes resources in order to prevent K8s from recreating them
		client.apps().deployments().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.apps().replicaSets().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.apps().statefulSets().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.batch().jobs().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.replicationControllers().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.endpoints().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.services().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.pods().withLabelNotIn(KEEP_LABEL, "", "true").withGracePeriod(0).delete();
		client.persistentVolumeClaims().withLabelNotIn(KEEP_LABEL, "", "true").delete();
		client.autoscaling().v1().horizontalPodAutoscalers().withLabelNotIn(KEEP_LABEL, "", "true").delete();

		getUserConfigMaps().forEach(c -> client.configMaps().delete(c));
		getUserSecrets().forEach(s -> client.secrets().delete(s));
		getUserServiceAccounts().forEach(sa -> client.serviceAccounts().delete(sa));
		getUserRoleBindings().forEach(r -> client.rbac().roleBindings().delete(r));
		client.rbac().roles().withLabelNotIn(KEEP_LABEL, "", "true")
				.withLabelNotIn("olm.owner.kind", "ClusterServiceVersion")
				.delete();

		for (HasMetadata hasMetadata : listRemovableResources()) {
			log.warn("DELETE LEFTOVER :: {}/{}", hasMetadata.getKind(), hasMetadata.getMetadata().getName());
			client.resource(hasMetadata).cascading(true).withGracePeriod(0).delete();
		}

		FailFastCheck failFastCheck = () -> false;
		return new SimpleWaiter(
				() -> isNamespaceClean(),
				TimeUnit.MILLISECONDS, WaitingConfig.timeoutCleanup(),
				"Cleaning project - " + getNamespace())
				.onTimeout(() -> log.info("Cleaning namespace: {} - timed out.", getNamespace()))
				.onFailure(() -> log.info("Cleaning namespace: {} - failed.", getNamespace()))
				.onSuccess(() -> log.info("Cleaning namespace: {} - finished.", getNamespace()))
				.failFast(failFastCheck);
	}

	// ========================
	// Internal helpers for clean()
	// ========================

	List<HasMetadata> listRemovableResources() {
		List<HasMetadata> removables = new ArrayList<>();

		// OpenShift-specific resources
		removables.addAll(client.templates().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(client.deploymentConfigs().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(client.buildConfigs().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(client.imageStreams().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(client.builds().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(client.routes().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());

		// Kubernetes resources in deletion order
		removables.addAll(client.apps().deployments().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(client.apps().replicaSets().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(client.batch().jobs().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(client.apps().statefulSets().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(
				client.replicationControllers().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(client.endpoints().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(client.services().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(client.pods().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(
				client.persistentVolumeClaims().withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(client.autoscaling().v1().horizontalPodAutoscalers()
				.withLabelNotIn(KEEP_LABEL, "", "true").list().getItems());
		removables.addAll(getUserConfigMaps());
		removables.addAll(getUserSecrets());
		removables.addAll(getUserServiceAccounts());
		removables.addAll(getUserRoleBindings());
		removables.addAll(client.rbac().roles().withLabelNotIn(KEEP_LABEL, "", "true")
				.withLabelNotIn("olm.owner.kind", "ClusterServiceVersion").list().getItems());

		return removables;
	}

	private boolean isNamespaceClean() {
		int crdInstances = 0;
		List<GenericKubernetesResource> customResourceDefinitionList = null;
		for (CustomResourceDefinitionContextProvider crdContextProvider : getCRDContextProviders()) {
			try {
				customResourceDefinitionList = client
						.genericKubernetesResources(crdContextProvider.getContext())
						.inNamespace(getNamespace())
						.list().getItems();
				crdInstances += customResourceDefinitionList.size();
			} catch (KubernetesClientException kce) {
				// CRD might not be installed on the cluster
			}
		}

		List<HasMetadata> listRemovableResources = listRemovableResources();
		if (crdInstances == 0 && listRemovableResources.isEmpty()) {
			return true;
		}

		StringBuilder strBuilderResourcesToDelete = new StringBuilder(
				"Cleaning project - " + getNamespace()
						+ " Waiting for following resources to be deleted: \n");
		if (customResourceDefinitionList != null && !customResourceDefinitionList.isEmpty()) {
			customResourceDefinitionList.forEach(r -> strBuilderResourcesToDelete.append(r).append("\n"));
		}
		if (!listRemovableResources.isEmpty()) {
			listRemovableResources.forEach(r -> strBuilderResourcesToDelete.append(r).append("\n"));
		}
		log.debug(strBuilderResourcesToDelete.toString());
		return false;
	}

	// ========================
	// CRD context providers
	// ========================

	protected static synchronized ServiceLoader<CustomResourceDefinitionContextProvider> getCRDContextProviders() {
		if (crdContextProviderLoader == null) {
			crdContextProviderLoader = ServiceLoader.load(CustomResourceDefinitionContextProvider.class);
		}
		return crdContextProviderLoader;
	}

	// ========================
	// HasMetadata operations helper
	// ========================

	/**
	 * Creates a new {@link HasMetadataOperationsImpl} for working with custom resource types.
	 *
	 * @param <T> the resource type
	 * @param <L> the resource list type
	 * @param rdContext the resource definition context
	 * @param resourceType the resource class
	 * @param listClass the resource list class
	 * @return a new {@link HasMetadataOperationsImpl}
	 */
	public <T extends HasMetadata, L extends KubernetesResourceList<T>> HasMetadataOperationsImpl<T, L> newHasMetadataOperation(
			ResourceDefinitionContext rdContext, Class<T> resourceType, Class<L> listClass) {
		return new HasMetadataOperationsImpl(client, rdContext, resourceType, listClass);
	}
}
