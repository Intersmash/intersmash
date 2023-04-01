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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.BootableJarOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.input.BinarySource;
import org.jboss.intersmash.tools.application.openshift.input.BuildInput;
import org.slf4j.event.Level;

import cz.xtf.builder.builders.ApplicationBuilder;
import cz.xtf.builder.builders.route.TransportProtocol;
import cz.xtf.core.bm.BinaryBuild;
import cz.xtf.core.bm.BinaryBuildFromFile;
import cz.xtf.core.bm.BinarySourceBuild;
import cz.xtf.core.bm.BuildManagers;
import cz.xtf.core.bm.ManagedBuildReference;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
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

	protected void configureAppBuilder(ApplicationBuilder applicationBuilder) {
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

	private ApplicationBuilder getAppBuilder() {
		BuildInput buildInput = bootableApplication.getBuildInput();
		Objects.requireNonNull(buildInput);

		if (BinarySource.class.isAssignableFrom(buildInput.getClass())) {
			BinarySource binarySource = (BinarySource) buildInput;
			log.debug("Create application builder from artifact (path: {}).", binarySource.getArchive().toString());
			List<EnvVar> environmentVariables = new ArrayList<>(bootableApplication.getEnvVars());
			File archiveFile = binarySource.getArchive().toFile();
			BinaryBuild bootableJarBuild;
			if (archiveFile.isDirectory()) {
				/*
					This scenario is probably unusable and should be pruned: bootable Jar workflow doesn't envision a
					builder image to compile the maven project: the project compilation is supposed to happen outside
					openshift; multiple deployments might have role here: TODO: TO BE INVESTIGATED
				 */
				bootableJarBuild = new BinarySourceBuild(
						IntersmashConfig.bootableJarImageURL(),
						binarySource.getArchive(),
						environmentVariables.stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue)),
						bootableApplication.getName());
			} else if (archiveFile.isFile()) {
				/*
				  S2I Binary build which takes as input a bootable Jar;

				  This kind of build corresponds to the following workflow:

						oc new-build --name=wildfly-build-from-bootable-jar \
							--labels=intersmash.app=wildfly-test-app \
							--binary=true \
							--strategy=source \
							--env=ADMIN_USERNAME=admin \
							--env=ADMIN_PASSWORD=pass.1234 \
							--image=registry.redhat.io/ubi8/openjdk-11

						oc start-build wildfly-build-from-bootable-jar \
							--from-file=bootable-openshift.jar \
							--follow

						oc new-app wildfly-build-from-bootable-jar
				 */
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
			return ApplicationBuilder.fromManagedBuild(
					bootableApplication.getName(),
					reference,
					Collections.singletonMap(APP_LABEL_KEY, bootableApplication.getName()));
		} else {
			throw new RuntimeException("Application artifact path or git reference has to be specified");
		}
		//		when s2i is supported
		//		else if (GitSource.class.isAssignableFrom(buildInput.getClass())) {
		//			GitSource gitSource = (GitSource) buildInput;
		//			log.debug("Create application builder from git reference (repo: {}, ref: {}).",
		//					gitSource.getUri(), gitSource.getRef());
		//			ApplicationBuilder appBuilder = ApplicationBuilder.fromS2IBuild(bootableApplication.getName(),
		//					IntersmashConfig.bootableJarImageURL(),
		//					gitSource.getUri(),
		//					Collections.singletonMap(APP_LABEL_KEY, bootableApplication.getName()));
		//
		//			appBuilder.buildConfig().onConfigurationChange().gitRef(gitSource.getRef());
		//
		//			bootableApplication.getEnvironmentVariables().entrySet().stream()
		//					.forEach(entry -> appBuilder.buildConfig().sti().addEnvVariable(entry.getKey(), entry.getValue()));
		//			return appBuilder;
		//		}
		//

	}

	private void deployImage() {
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				bootableApplication.getName());
		ApplicationBuilder appBuilder = getAppBuilder();
		appBuilder.service()
				.port("8080-tcp", 8080, 8080, TransportProtocol.TCP);

		appBuilder.route().targetPort(8080);

		// env vars
		appBuilder.deploymentConfig().podTemplate().container()
				.envVars(
						bootableApplication.getEnvVars().stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue)));

		configureAppBuilder(appBuilder);

		appBuilder.buildApplication(openShift).deploy();
		OpenShiftWaiters.get(openShift, ffCheck).isDcReady(appBuilder.getName()).level(Level.DEBUG).waitFor();
		// 1 by default
		waitForReplicas(1);
	}

	@Override
	public List<Pod> getPods() {
		return openShift.getPods(getApplication().getName());
	}
}
