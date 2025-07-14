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
import org.slf4j.event.Level;

import cz.xtf.builder.builders.ApplicationBuilder;
import cz.xtf.builder.builders.PVCBuilder;
import cz.xtf.builder.builders.pod.PersistentVolumeClaim;
import cz.xtf.builder.builders.pod.VolumeMount;
import cz.xtf.builder.builders.route.TransportProtocol;
import cz.xtf.core.bm.BinaryBuildFromFile;
import cz.xtf.core.bm.BuildManagers;
import cz.xtf.core.bm.ManagedBuildReference;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
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

	private ApplicationBuilder getAppBuilder() {
		BuildInput buildInput = application.getBuildInput();
		Objects.requireNonNull(buildInput);

		if (BinarySource.class.isAssignableFrom(buildInput.getClass())) {
			BinarySource binarySource = (BinarySource) buildInput;
			log.debug("Create application builder from artifact (path: {}).", binarySource.getArchive().toString());

			List<EnvVar> environmentVariables = new ArrayList<>(application.getEnvVars());
			// *** WAR, JAR, EAR ***
			// Binary build that expect a file on path that shall be uploaded as {@code ROOT.(suffix)}. {@code suffix} is war, jar,...
			// It is extracted from filename.
			// builder.withNewStrategy().withType("Source").withNewSourceStrategy().withEnv(env).withForcePull(true).withNewFrom()
			//                .withKind("DockerImage").withName(builderImage).endFrom().endSourceStrategy().endStrategy();
			BinaryBuildFromFile build = new BinaryBuildFromFile(
					IntersmashConfig.eap7ImageURL(),
					binarySource.getArchive(),
					environmentVariables.stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue)),
					application.getName() + "-" + IntersmashConfig.eap7ProductCode());
			ManagedBuildReference reference = BuildManagers.get().deploy(build);
			BuildManagers.get().hasBuildCompleted(build).level(Level.DEBUG).waitFor();

			return ApplicationBuilder.fromManagedBuild(application.getName(), reference,
					Collections.singletonMap(APP_LABEL_KEY, application.getName()));
		} else if (GitSource.class.isAssignableFrom(buildInput.getClass())) {
			GitSource gitSource = (GitSource) buildInput;
			log.debug("Create application builder from git reference (repo: {}, ref: {}).",
					gitSource.getUri(), gitSource.getRef());
			ApplicationBuilder appBuilder = ApplicationBuilder.fromS2IBuild(application.getName(),
					IntersmashConfig.eap7ImageURL(),
					gitSource.getUri(),
					Collections.singletonMap(APP_LABEL_KEY, application.getName()));

			appBuilder.buildConfig().onConfigurationChange().gitRef(gitSource.getRef());
			if (!Strings.isNullOrEmpty(gitSource.getContextDir()))
				appBuilder.buildConfig().onConfigurationChange().gitContextDir(gitSource.getContextDir());

			application.getEnvVars().stream()
					.forEach(entry -> appBuilder.buildConfig().sti().addEnvVariable(entry.getName(), entry.getValue()));
			return appBuilder;
		} else {
			throw new RuntimeException("Application artifact path or git reference has to be specified");
		}
	}

	private void deployImage() {
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				application.getName());
		ApplicationBuilder appBuilder = getAppBuilder();
		appBuilder.service()
				.port("8080-tcp", 8080, 8080, TransportProtocol.TCP);

		final String serverHomeDirectory = "eap";
		appBuilder.deploymentConfig().podTemplate().container()
				.addLivenessProbe()
				.setInitialDelay(60)
				.createExecProbe("/bin/bash", "-c", String.format("/opt/%s/bin/livenessProbe.sh", serverHomeDirectory));

		appBuilder.deploymentConfig().podTemplate().container()
				.addReadinessProbe()
				.createExecProbe("/bin/bash", "-c", String.format("/opt/%s/bin/readinessProbe.sh", serverHomeDirectory));

		// setup the ping service for clustering using DNS_PING
		if (application.getPingServiceName() != null) {
			String pingServiceName = application.getPingServiceName();
			int pingServicePort = 8888;
			appBuilder.service(application.getPingServiceName())
					.addAnnotation("service.alpha.kubernetes.io/tolerate-unready-endpoints", "true")
					.headless()
					.port("ping", pingServicePort, pingServicePort, TransportProtocol.TCP);
			Map<String, String> pingServiceEnv = new HashMap<>();
			pingServiceEnv.put("JGROUPS_PING_PROTOCOL", "dns.DNS_PING");
			pingServiceEnv.put("OPENSHIFT_DNS_PING_SERVICE_NAME", pingServiceName);
			pingServiceEnv.put("OPENSHIFT_DNS_PING_SERVICE_PORT", String.valueOf(pingServicePort));
			appBuilder.deploymentConfig().podTemplate().container().envVars(Collections.unmodifiableMap(pingServiceEnv));
		}

		// mount postconfigure CLI commands
		if (!application.getCliScript().isEmpty()) {
			String postconfigure = "#!/usr/bin/env bash\n"
					+ "echo \"Executing postconfigure.sh\"\n"
					+ "$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/extensions/configure.cli\n";
			appBuilder.configMap("jboss-cli")
					.configEntry("postconfigure.sh", postconfigure)
					.configEntry("configure.cli", String.join("\n", application.getCliScript()));

			appBuilder.deploymentConfig()
					.podTemplate()
					.addConfigMapVolume("jboss-cli", "jboss-cli", "0755")
					.container()
					.addVolumeMount("jboss-cli", String.format("/opt/%s/extensions", serverHomeDirectory), false);
		}

		// mount secrets to /etc/secrets
		for (Secret secret : application.getSecrets()) {
			appBuilder.deploymentConfig().podTemplate()
					.addSecretVolume(secret.getMetadata().getName(), secret.getMetadata().getName())
					.container()
					.addVolumeMount(secret.getMetadata().getName(), "/etc/secrets", false);
		}

		appBuilder.route().targetPort(8080);

		// env vars
		appBuilder.deploymentConfig().podTemplate().container()
				.envVars(application.getEnvVars().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue)));

		// enable script debugging
		if (application.getEnvVars().stream().noneMatch((envVar -> envVar.getName().equals("SCRIPT_DEBUG")))) {
			if (IntersmashConfig.scriptDebug() != null)
				addEnvVariable(appBuilder, SCRIPT_DEBUG, IntersmashConfig.scriptDebug(), true,
						!BinarySource.class.isAssignableFrom(application.getBuildInput().getClass()));
		}

		// mount persistent volumes into pod
		if (!application.getPersistentVolumeClaimMounts().isEmpty()) {
			application.getPersistentVolumeClaimMounts().entrySet().stream()
					.forEach(entry -> {
						PersistentVolumeClaim pvc = entry.getKey();
						Set<VolumeMount> vms = entry.getValue();
						appBuilder.deploymentConfig().podTemplate().addPersistenVolumeClaim(pvc.getName(),
								pvc.getClaimName());
						vms.forEach(vm -> appBuilder.deploymentConfig().podTemplate().container().addVolumeMount(pvc.getName(),
								vm.getMountPath(), vm.isReadOnly(), vm.getSubPath()));
						openShift.createPersistentVolumeClaim(
								new PVCBuilder(pvc.getClaimName()).accessRWX().storageSize("100Mi").build());
					});
		}

		appBuilder.buildApplication(openShift).deploy();
		OpenShiftWaiters.get(openShift, ffCheck).isDcReady(application.getName()).level(Level.DEBUG).waitFor();
		// 1 by default
		waitForReplicas(1);
	}

	private void addEnvVariable(ApplicationBuilder appBuilder, final String key, final String value) {
		addEnvVariable(appBuilder, key, value, true, true);
	}

	private void addEnvVariable(ApplicationBuilder appBuilder, final String key, final String value, final boolean addToDC,
			final boolean addToBuild) {
		if (addToDC) {
			appBuilder.deploymentConfig().podTemplate().container().envVar(key, value);
		}
		if (addToBuild) {
			appBuilder.buildConfig().sti().addEnvVariable(key, value);
		}
	}

	@Override
	public List<Pod> getPods() {
		return openShift.getPods(getApplication().getName());
	}
}
