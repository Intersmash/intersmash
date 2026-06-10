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

import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.input.BinarySource;
import org.jboss.intersmash.application.input.BuildInput;
import org.jboss.intersmash.application.openshift.BootableJarOpenShiftApplication;
import org.jboss.intersmash.tools.build.BinaryBuild;
import org.jboss.intersmash.tools.build.BinaryBuildFromFile;
import org.jboss.intersmash.tools.build.BinarySourceBuild;
import org.jboss.intersmash.tools.build.BuildManagers;
import org.jboss.intersmash.tools.build.ManagedBuildReference;
import org.jboss.intersmash.tools.client.OpenShiftWaiters;
import org.jboss.intersmash.tools.waiting.failfast.FailFastCheck;
import org.slf4j.event.Level;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.DeploymentTriggerPolicyBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Class deploys an Wildfly application based on {@link BootableJarImageOpenShiftProvisioner}
 */
@Slf4j
public abstract class BootableJarImageOpenShiftProvisioner
		implements OpenShiftProvisioner<BootableJarOpenShiftApplication> {

	private final BootableJarOpenShiftApplication bootableApplication;
	private FailFastCheck ffCheck = () -> false;

	public BootableJarImageOpenShiftProvisioner(@NonNull BootableJarOpenShiftApplication bootableApplication) {
		this.bootableApplication = bootableApplication;
	}

	/**
	 * Hook for subclasses to configure the container (e.g., add probes).
	 *
	 * @param containerBuilder the Fabric8 ContainerBuilder to configure
	 */
	protected void configureAppBuilder(ContainerBuilder containerBuilder) {
	}

	@Override
	public BootableJarOpenShiftApplication getApplication() {
		return bootableApplication;
	}

	@Override
	public void deploy() {
		deployImage();
	}

	@Override
	public void undeploy() {
		OpenShiftUtils.deleteResourcesWithLabel(openShift, APP_LABEL_KEY, bootableApplication.getName());
		// the bootable JAR provisioning process _might_ need to clean some custom build configs, builds,
		// config maps, image streams and build pods which might appear as leftovers in OpenShift::clean()
		// when the build and master namespaces are the same
		openShift.getBuildConfigs()
				.stream()
				.filter(bc -> bc.getMetadata().getName().startsWith(bootableApplication.getName()))
				.forEach(openShift::deleteBuildConfig);
		openShift.getBuilds()
				.stream()
				.filter(b -> b.getMetadata().getName().startsWith(bootableApplication.getName()))
				.forEach(openShift::deleteBuild);
		openShift.getConfigMaps()
				.stream()
				.filter(cf -> cf.getMetadata().getName().startsWith(bootableApplication.getName()))
				.forEach(openShift::deleteConfigMap);
		openShift.getImageStreams()
				.stream()
				.filter(is -> is.getMetadata().getName().startsWith(bootableApplication.getName()))
				.forEach(openShift::deleteImageStream);
		openShift.getPods()
				.stream()
				.filter(pod -> pod.getMetadata().getName().startsWith(bootableApplication.getName()))
				.forEach(openShift::deletePod);
	}

	@Override
	public void scale(int replicas, boolean wait) {
		openShift.scale(bootableApplication.getName(), replicas);
		if (wait) {
			waitForReplicas(replicas);
		}
	}

	public void waitForReplicas(int replicas) {
		OpenShiftWaiters.get(openShift, ffCheck).areExactlyNPodsReady(replicas, bootableApplication.getName())
				.level(Level.DEBUG)
				.waitFor();
		WaitersUtil.serviceEndpointsAreReady(openShift, getApplication().getName(), replicas, 8080)
				.level(Level.DEBUG)
				.waitFor();
		if (replicas > 0) {
			WaitersUtil.routeIsUp(getUrl(bootableApplication.getName(), false))
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	/**
	 * Holds the resolved build information.
	 */
	private static class BuildResult {
		String containerImageName;
		String containerImageNamespace;
	}

	private BuildResult resolveBuild() {
		BuildInput buildInput = bootableApplication.getBuildInput();
		Objects.requireNonNull(buildInput);

		BuildResult result = new BuildResult();

		if (BinarySource.class.isAssignableFrom(buildInput.getClass())) {
			BinarySource binarySource = (BinarySource) buildInput;
			log.debug("Create application from artifact (path: {}).", binarySource.getArchive().toString());
			List<EnvVar> environmentVariables = new ArrayList<>(bootableApplication.getEnvVars());
			File archiveFile = binarySource.getArchive().toFile();
			BinaryBuild bootableJarBuild;
			if (archiveFile.isDirectory()) {
				bootableJarBuild = new BinarySourceBuild(
						IntersmashConfig.bootableJarImageURL(),
						binarySource.getArchive(),
						environmentVariables.stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue)),
						bootableApplication.getName());
			} else if (archiveFile.isFile()) {
				bootableJarBuild = new BinaryBuildFromFile(
						IntersmashConfig.bootableJarImageURL(),
						binarySource.getArchive(),
						environmentVariables.stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue)),
						bootableApplication.getName());
			} else {
				throw new RuntimeException(
						String.format("'%s' archive path must be either a directory or a file", archiveFile.getAbsolutePath()));
			}

			ManagedBuildReference reference = BuildManagers.get().deploy(bootableJarBuild);
			BuildManagers.get().hasBuildCompleted(bootableJarBuild).waitFor();

			result.containerImageName = reference.getStreamName();
			result.containerImageNamespace = reference.getNamespace();
		} else {
			throw new RuntimeException("Application artifact path or git reference has to be specified");
		}

		return result;
	}

	private void deployImage() {
		ffCheck = FailFastUtils.getFailFastCheck(ZonedDateTime.now(),
				bootableApplication.getName());

		BuildResult buildResult = resolveBuild();
		String name = bootableApplication.getName();
		Map<String, String> labels = new HashMap<>();
		labels.put(APP_LABEL_KEY, name);
		labels.put("name", name);

		// Collect all resources
		List<HasMetadata> resources = new ArrayList<>();

		// ---- Build the Container ----
		List<EnvVar> envVars = bootableApplication.getEnvVars().stream()
				.map(ev -> new EnvVar(ev.getName(), ev.getValue(), null))
				.collect(Collectors.toCollection(ArrayList::new));

		// Volumes and volume mounts
		List<io.fabric8.kubernetes.api.model.Volume> volumes = new ArrayList<>();
		List<io.fabric8.kubernetes.api.model.VolumeMount> volumeMounts = new ArrayList<>();

		// Secret volumes
		for (Secret secret : bootableApplication.getSecrets()) {
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

		ContainerBuilder containerBuilder = new ContainerBuilder()
				.withName(name)
				.withImage(buildResult.containerImageName)
				.withImagePullPolicy("Always")
				.withPorts(new ContainerPortBuilder().withContainerPort(8080).build())
				.withEnv(envVars)
				.withVolumeMounts(volumeMounts);

		// Let subclasses configure probes
		configureAppBuilder(containerBuilder);

		// ---- Build DeploymentConfig ----
		List<io.fabric8.openshift.api.model.DeploymentTriggerPolicy> dcTriggers = new ArrayList<>();
		dcTriggers.add(new DeploymentTriggerPolicyBuilder()
				.withType("ConfigChange")
				.build());

		io.fabric8.kubernetes.api.model.ObjectReferenceBuilder imageRef = new io.fabric8.kubernetes.api.model.ObjectReferenceBuilder()
				.withKind("ImageStreamTag")
				.withName(buildResult.containerImageName + ":latest");
		if (buildResult.containerImageNamespace != null) {
			imageRef.withNamespace(buildResult.containerImageNamespace);
		}
		dcTriggers.add(new DeploymentTriggerPolicyBuilder()
				.withType("ImageChange")
				.withNewImageChangeParams()
				.withAutomatic(true)
				.withContainerNames(name)
				.withFrom(imageRef.build())
				.endImageChangeParams()
				.build());

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

		// ---- Service ----
		resources.add(new ServiceBuilder()
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

		openShift.createResources(resources);
		OpenShiftWaiters.get(openShift, ffCheck).isDcReady(name).level(Level.DEBUG).waitFor();
		// 1 by default
		waitForReplicas(1);
	}

	@Override
	public List<Pod> getPods() {
		return openShift.getPods(getApplication().getName());
	}
}
