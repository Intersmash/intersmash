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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.util.Strings;
import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.input.BinarySource;
import org.jboss.intersmash.application.input.BuildInput;
import org.jboss.intersmash.application.input.GitSource;
import org.jboss.intersmash.application.openshift.Eap7ImageOpenShiftApplication;
import org.jboss.intersmash.tools.build.BinaryBuildFromFile;
import org.jboss.intersmash.tools.build.BuildManagers;
import org.jboss.intersmash.tools.build.ManagedBuildReference;
import org.jboss.intersmash.tools.client.OpenShiftWaiters;
import org.jboss.intersmash.tools.waiting.failfast.FailFastCheck;
import org.slf4j.event.Level;

import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildTriggerPolicyBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.DeploymentTriggerPolicyBuilder;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Class deploys an EAP 7.z (i.e. Jakarta EE 8 based Wildfly) application based on
 * {@link Eap7ImageOpenShiftApplication}
 */
@Slf4j
public class Eap7ImageOpenShiftProvisioner implements OpenShiftProvisioner<Eap7ImageOpenShiftApplication> {

	private final Eap7ImageOpenShiftApplication application;
	private FailFastCheck ffCheck = () -> false;

	public Eap7ImageOpenShiftProvisioner(@NonNull Eap7ImageOpenShiftApplication application) {
		this.application = application;
	}

	@Override
	public Eap7ImageOpenShiftApplication getApplication() {
		return application;
	}

	@Override
	public void deploy() {
		deployImage();
	}

	@Override
	public void undeploy() {
		OpenShiftUtils.deleteResourcesWithLabel(openShift, APP_LABEL_KEY, application.getName());
		// when using git repo S2I process creates some custom maps and build pods
		openShift.getConfigMaps()
				.stream()
				.filter(cfMap -> cfMap.getMetadata().getName().startsWith(application.getName()))
				.forEach(openShift::deleteConfigMap);
		openShift.getPods()
				.stream()
				.filter(pod -> pod.getMetadata().getName().startsWith(application.getName()))
				.forEach(openShift::deletePod);
	}

	@Override
	public void scale(int replicas, boolean wait) {
		openShift.scale(application.getName(), replicas);
		if (wait) {
			waitForReplicas(replicas);
		}
	}

	public void waitForReplicas(int replicas) {
		OpenShiftWaiters.get(openShift, ffCheck).areExactlyNPodsReady(replicas, application.getName()).level(Level.DEBUG)
				.waitFor();
		WaitersUtil.serviceEndpointsAreReady(openShift, getApplication().getName(), replicas, 8080)
				.level(Level.DEBUG)
				.waitFor();
		if (replicas > 0) {
			WaitersUtil.routeIsUp(getUrl(application.getName(), false))
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	/**
	 * Holds the collected resources and configuration from the build input resolution.
	 */
	private static class AppResources {
		String containerImageName;
		String containerImageNamespace;
		boolean imageChangeTrigger;
		BuildConfig buildConfig;
		ImageStream imageStream;
	}

	private AppResources resolveAppResources() {
		BuildInput buildInput = application.getBuildInput();
		Objects.requireNonNull(buildInput);

		AppResources res = new AppResources();

		if (BinarySource.class.isAssignableFrom(buildInput.getClass())) {
			BinarySource binarySource = (BinarySource) buildInput;
			log.debug("Create application from artifact (path: {}).", binarySource.getArchive().toString());

			List<EnvVar> environmentVariables = new ArrayList<>(application.getEnvVars());
			BinaryBuildFromFile build = new BinaryBuildFromFile(
					IntersmashConfig.eap7ImageURL(),
					binarySource.getArchive(),
					environmentVariables.stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue)),
					application.getName() + "-" + IntersmashConfig.eap7ProductCode());
			ManagedBuildReference reference = BuildManagers.get().deploy(build);
			BuildManagers.get().hasBuildCompleted(build).level(Level.DEBUG).waitFor();

			res.containerImageName = reference.getStreamName();
			res.containerImageNamespace = reference.getNamespace();
			res.imageChangeTrigger = true;
		} else if (GitSource.class.isAssignableFrom(buildInput.getClass())) {
			GitSource gitSource = (GitSource) buildInput;
			log.debug("Create application from git reference (repo: {}, ref: {}).",
					gitSource.getUri(), gitSource.getRef());

			String name = application.getName();
			Map<String, String> labels = Collections.singletonMap(APP_LABEL_KEY, name);

			// Create ImageStream
			res.imageStream = new ImageStreamBuilder()
					.withNewMetadata()
					.withName(name)
					.withLabels(labels)
					.endMetadata()
					.build();

			// Collect build env vars from the application
			List<EnvVar> buildEnvVarList = new ArrayList<>();
			application.getEnvVars().stream()
					.forEach(entry -> buildEnvVarList.add(new EnvVar(entry.getName(), entry.getValue(), null)));

			// Build the source object separately
			io.fabric8.openshift.api.model.BuildSourceBuilder sourceBuilder = new io.fabric8.openshift.api.model.BuildSourceBuilder()
					.withType("Git")
					.withNewGit()
					.withUri(gitSource.getUri())
					.withRef(gitSource.getRef())
					.endGit();

			if (!Strings.isNullOrEmpty(gitSource.getContextDir())) {
				sourceBuilder.withContextDir(gitSource.getContextDir());
			}

			// Create BuildConfig with git source, S2I strategy
			io.fabric8.openshift.api.model.BuildConfigBuilder bcBuilder = new io.fabric8.openshift.api.model.BuildConfigBuilder()
					.withNewMetadata()
					.withName(name)
					.withLabels(labels)
					.endMetadata()
					.withNewSpec()
					.addToTriggers(new BuildTriggerPolicyBuilder()
							.withType("Generic")
							.withNewGeneric().withAllowEnv(true).withSecret("secret101").endGeneric()
							.build())
					.addToTriggers(new BuildTriggerPolicyBuilder()
							.withType("ConfigChange")
							.build())
					.withSource(sourceBuilder.build())
					.withNewStrategy()
					.withType("Source")
					.withNewSourceStrategy()
					.withForcePull(true)
					.withNewFrom()
					.withKind("DockerImage")
					.withName(IntersmashConfig.eap7ImageURL())
					.endFrom()
					.withEnv(buildEnvVarList)
					.endSourceStrategy()
					.endStrategy()
					.withNewOutput()
					.withNewTo()
					.withKind("ImageStreamTag")
					.withName(name + ":latest")
					.endTo()
					.endOutput()
					.endSpec();

			res.buildConfig = bcBuilder.build();
			res.containerImageName = name;
			res.containerImageNamespace = null;
			res.imageChangeTrigger = true;
		} else {
			throw new RuntimeException("Application artifact path or git reference has to be specified");
		}
		return res;
	}

	private void deployImage() {
		ffCheck = FailFastUtils.getFailFastCheck(ZonedDateTime.now(),
				application.getName());

		AppResources appRes = resolveAppResources();
		String name = application.getName();
		final String serverHomeDirectory = "eap";
		Map<String, String> labels = new HashMap<>();
		labels.put(APP_LABEL_KEY, name);
		labels.put("name", name);

		// Collect all resources
		List<HasMetadata> resources = new ArrayList<>();

		// Add ImageStream and BuildConfig if present (git-based builds)
		if (appRes.imageStream != null) {
			resources.add(appRes.imageStream);
		}
		if (appRes.buildConfig != null) {
			resources.add(appRes.buildConfig);
		}

		// ---- Build the Container ----
		List<EnvVar> envVars = new ArrayList<>(
				application.getEnvVars().stream()
						.map(ev -> new EnvVar(ev.getName(), ev.getValue(), null))
						.collect(Collectors.toList()));

		ContainerBuilder containerBuilder = new ContainerBuilder()
				.withName(name)
				.withImage(appRes.containerImageName)
				.withImagePullPolicy("Always")
				.withPorts(new ContainerPortBuilder().withContainerPort(8080).build())
				.withLivenessProbe(new ProbeBuilder()
						.withInitialDelaySeconds(60)
						.withNewExec()
						.withCommand("/bin/bash", "-c",
								String.format("/opt/%s/bin/livenessProbe.sh", serverHomeDirectory))
						.endExec()
						.build())
				.withReadinessProbe(new ProbeBuilder()
						.withNewExec()
						.withCommand("/bin/bash", "-c",
								String.format("/opt/%s/bin/readinessProbe.sh", serverHomeDirectory))
						.endExec()
						.build());

		// ---- Volumes ----
		List<io.fabric8.kubernetes.api.model.Volume> volumes = new ArrayList<>();
		List<io.fabric8.kubernetes.api.model.VolumeMount> volumeMounts = new ArrayList<>();

		// ---- Clustering via DNS_PING ----
		List<io.fabric8.kubernetes.api.model.Service> services = new ArrayList<>();

		// Main service
		services.add(new ServiceBuilder()
				.withNewMetadata()
				.withName(name)
				.withLabels(labels)
				.endMetadata()
				.withNewSpec()
				.withSelector(Collections.singletonMap("deploymentconfig", name))
				.withPorts(new ServicePortBuilder()
						.withName("8080-tcp")
						.withPort(8080)
						.withNewTargetPort(8080)
						.withProtocol("TCP")
						.build())
				.withSessionAffinity("None")
				.endSpec()
				.build());

		if (application.getPingServiceName() != null) {
			String pingServiceName = application.getPingServiceName();
			int pingServicePort = 8888;

			services.add(new ServiceBuilder()
					.withNewMetadata()
					.withName(pingServiceName)
					.withLabels(labels)
					.addToAnnotations("service.alpha.kubernetes.io/tolerate-unready-endpoints", "true")
					.endMetadata()
					.withNewSpec()
					.withClusterIP("None")
					.withSelector(Collections.singletonMap("deploymentconfig", name))
					.withPorts(new ServicePortBuilder()
							.withName("ping")
							.withPort(pingServicePort)
							.withNewTargetPort(pingServicePort)
							.withProtocol("TCP")
							.build())
					.withSessionAffinity("None")
					.endSpec()
					.build());

			envVars.add(new EnvVar("JGROUPS_PING_PROTOCOL", "dns.DNS_PING", null));
			envVars.add(new EnvVar("OPENSHIFT_DNS_PING_SERVICE_NAME", pingServiceName, null));
			envVars.add(new EnvVar("OPENSHIFT_DNS_PING_SERVICE_PORT", String.valueOf(pingServicePort), null));
		}

		// ---- CLI script as ConfigMap ----
		if (!application.getCliScript().isEmpty()) {
			String postconfigure = "#!/usr/bin/env bash\n"
					+ "echo \"Executing postconfigure.sh\"\n"
					+ "$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/extensions/configure.cli\n";

			Map<String, String> configMapData = new HashMap<>();
			configMapData.put("postconfigure.sh", postconfigure);
			configMapData.put("configure.cli", String.join("\n", application.getCliScript()));

			resources.add(new ConfigMapBuilder()
					.withNewMetadata()
					.withName("jboss-cli")
					.withLabels(labels)
					.endMetadata()
					.withData(configMapData)
					.build());

			volumes.add(new VolumeBuilder()
					.withName("jboss-cli")
					.withConfigMap(new ConfigMapVolumeSourceBuilder()
							.withName("jboss-cli")
							.withDefaultMode(0755)
							.build())
					.build());

			volumeMounts.add(new VolumeMountBuilder()
					.withName("jboss-cli")
					.withMountPath(String.format("/opt/%s/extensions", serverHomeDirectory))
					.withReadOnly(false)
					.build());
		}

		// ---- Secret volumes ----
		for (Secret secret : application.getSecrets()) {
			String secretName = secret.getMetadata().getName();
			volumes.add(new VolumeBuilder()
					.withName(secretName)
					.withSecret(new SecretVolumeSourceBuilder()
							.withSecretName(secretName)
							.build())
					.build());
			volumeMounts.add(new VolumeMountBuilder()
					.withName(secretName)
					.withMountPath("/etc/secrets")
					.withReadOnly(false)
					.build());
		}

		// ---- Script debugging ----
		if (application.getEnvVars().stream().noneMatch((envVar -> envVar.getName().equals("SCRIPT_DEBUG")))) {
			if (IntersmashConfig.scriptDebug() != null) {
				envVars.add(new EnvVar(SCRIPT_DEBUG, IntersmashConfig.scriptDebug(), null));
				if (!BinarySource.class.isAssignableFrom(application.getBuildInput().getClass())
						&& appRes.buildConfig != null) {
					addEnvToBuildConfig(appRes, SCRIPT_DEBUG, IntersmashConfig.scriptDebug());
				}
			}
		}

		// ---- PVC mounts ----
		if (!application.getPersistentVolumeClaimMounts().isEmpty()) {
			application.getPersistentVolumeClaimMounts().entrySet().stream()
					.forEach(entry -> {
						Volume pvc = entry.getKey();
						String volumeName = pvc.getName();
						String claimName = pvc.getPersistentVolumeClaim().getClaimName();
						Set<VolumeMount> vms = entry.getValue();

						volumes.add(new VolumeBuilder()
								.withName(volumeName)
								.withNewPersistentVolumeClaim(claimName, false)
								.build());

						vms.forEach(vm -> volumeMounts.add(new VolumeMountBuilder()
								.withName(volumeName)
								.withMountPath(vm.getMountPath())
								.withReadOnly(vm.getReadOnly())
								.withSubPath(vm.getSubPath())
								.build()));

						openShift.createPersistentVolumeClaim(
								new PersistentVolumeClaimBuilder()
										.withNewMetadata().withName(claimName).endMetadata()
										.withNewSpec()
										.withAccessModes("ReadWriteMany")
										.withNewResources()
										.addToRequests("storage", new Quantity("100Mi"))
										.endResources()
										.endSpec()
										.build());
					});
		}

		// Set env vars and volume mounts on container
		containerBuilder.withEnv(envVars);
		containerBuilder.withVolumeMounts(volumeMounts);

		// ---- Build DeploymentConfig ----
		List<io.fabric8.openshift.api.model.DeploymentTriggerPolicy> dcTriggers = new ArrayList<>();
		dcTriggers.add(new DeploymentTriggerPolicyBuilder()
				.withType("ConfigChange")
				.build());
		if (appRes.imageChangeTrigger) {
			io.fabric8.kubernetes.api.model.ObjectReferenceBuilder imageRef = new io.fabric8.kubernetes.api.model.ObjectReferenceBuilder()
					.withKind("ImageStreamTag")
					.withName(appRes.containerImageName + ":latest");
			if (appRes.containerImageNamespace != null) {
				imageRef.withNamespace(appRes.containerImageNamespace);
			}
			dcTriggers.add(new DeploymentTriggerPolicyBuilder()
					.withType("ImageChange")
					.withNewImageChangeParams()
					.withAutomatic(true)
					.withContainerNames(name)
					.withFrom(imageRef.build())
					.endImageChangeParams()
					.build());
		}

		DeploymentConfig dc = new DeploymentConfigBuilder()
				.withNewMetadata()
				.withName(name)
				.withLabels(labels)
				.endMetadata()
				.withNewSpec()
				.withReplicas(1)
				.withSelector(Collections.singletonMap("name", name))
				.withTriggers(dcTriggers)
				.withNewStrategy().withType("Recreate").endStrategy()
				.withNewTemplate()
				.withNewMetadata()
				.withLabels(labels)
				.endMetadata()
				.withNewSpec()
				.withDnsPolicy("ClusterFirst")
				.withRestartPolicy("Always")
				.withContainers(containerBuilder.build())
				.withVolumes(volumes)
				.endSpec()
				.endTemplate()
				.endSpec()
				.build();

		resources.add(dc);

		// ---- Route ----
		Route route = new RouteBuilder()
				.withNewMetadata()
				.withName(name)
				.withLabels(labels)
				.endMetadata()
				.withNewSpec()
				.withNewTo()
				.withKind("Service")
				.withName(name)
				.endTo()
				.withNewPort()
				.withNewTargetPort(8080)
				.endPort()
				.endSpec()
				.build();
		resources.add(route);

		// Add services
		resources.addAll(services);

		openShift.createResources(resources);
		OpenShiftWaiters.get(openShift, ffCheck).isDcReady(name).level(Level.DEBUG).waitFor();
		// 1 by default
		waitForReplicas(1);
	}

	/**
	 * Adds an env var to the build config's source strategy env list.
	 */
	private void addEnvToBuildConfig(AppResources appRes, String key, String value) {
		if (appRes.buildConfig != null
				&& appRes.buildConfig.getSpec() != null
				&& appRes.buildConfig.getSpec().getStrategy() != null
				&& appRes.buildConfig.getSpec().getStrategy().getSourceStrategy() != null) {
			List<EnvVar> env = appRes.buildConfig.getSpec().getStrategy().getSourceStrategy().getEnv();
			if (env == null) {
				env = new ArrayList<>();
				appRes.buildConfig.getSpec().getStrategy().getSourceStrategy().setEnv(env);
			}
			env.add(new EnvVar(key, value, null));
		}
	}

	@Override
	public List<Pod> getPods() {
		return openShift.getPods(getApplication().getName());
	}
}
