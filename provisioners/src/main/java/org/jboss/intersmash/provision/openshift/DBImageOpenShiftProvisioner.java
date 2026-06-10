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

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.intersmash.application.openshift.DBImageOpenShiftApplication;
import org.jboss.intersmash.tools.client.OpenShiftWaiters;
import org.jboss.intersmash.tools.waiting.failfast.FailFastCheck;
import org.slf4j.event.Level;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.DeploymentTriggerPolicyBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DBImageOpenShiftProvisioner<T extends DBImageOpenShiftApplication> implements OpenShiftProvisioner<T> {

	protected final T dbApplication;
	private FailFastCheck ffCheck = () -> false;

	public DBImageOpenShiftProvisioner(T dbApplication) {
		this.dbApplication = dbApplication;
	}

	@Override
	public T getApplication() {
		return dbApplication;
	}

	@Override
	public void scale(int replicas, boolean wait) {
		openShift.scale(dbApplication.getName(), replicas);
		if (wait) {
			OpenShiftWaiters.get(openShift, ffCheck).areExactlyNPodsReady(replicas, "name", dbApplication.getName())
					.level(Level.DEBUG).waitFor();
		}
	}

	public Map<String, String> getImageVariables() {
		final Map<String, String> vars = new HashMap<>();
		vars.put(getSymbolicName() + "_USER", dbApplication.getUser());
		vars.put(getSymbolicName() + "_USERNAME", dbApplication.getUser());
		vars.put(getSymbolicName() + "_PASSWORD", dbApplication.getPassword());
		vars.put(getSymbolicName() + "_DATABASE", dbApplication.getDbName());
		return vars;
	}

	/**
	 * Hook for subclasses to add additional env vars to the container (e.g., env vars from ConfigMaps).
	 * Called during deploy() after the container has been configured with image variables and port.
	 *
	 * @param envVars the mutable list of env vars that will be set on the container
	 */
	public void customizeContainerEnvVars(List<EnvVar> envVars) {
	}

	public abstract String getSymbolicName();

	@Override
	public void deploy() {
		ffCheck = FailFastUtils.getFailFastCheck(ZonedDateTime.now(),
				dbApplication.getName());

		String name = dbApplication.getName();
		Map<String, String> labels = new HashMap<>();
		labels.put(APP_LABEL_KEY, name);
		labels.put("name", name);

		// Build env vars
		List<EnvVar> envVars = getImageVariables().entrySet().stream()
				.map(e -> new EnvVar(e.getKey(), e.getValue(), null))
				.collect(Collectors.toCollection(ArrayList::new));

		// Let subclasses add additional env vars (e.g., configmap-referenced env vars)
		customizeContainerEnvVars(envVars);

		// Build the container
		ContainerBuilder containerBuilder = new ContainerBuilder()
				.withName(name)
				.withImage(getImage())
				.withImagePullPolicy("Always")
				.withEnv(envVars)
				.withPorts(new ContainerPortBuilder().withContainerPort(getPort()).build());

		// Let subclasses configure probes etc.
		configureContainer(containerBuilder);

		// Build volumes and volume mounts for PVCs
		List<io.fabric8.kubernetes.api.model.Volume> volumes = new ArrayList<>();
		if (dbApplication.getPersistentVolumeClaims() != null && !dbApplication.getPersistentVolumeClaims().isEmpty()) {
			Volume pvc = dbApplication.getPersistentVolumeClaims().get(0);
			String volumeName = pvc.getName();
			String claimName = pvc.getPersistentVolumeClaim().getClaimName();

			volumes.add(new io.fabric8.kubernetes.api.model.VolumeBuilder()
					.withName(volumeName)
					.withNewPersistentVolumeClaim(claimName, false)
					.build());

			containerBuilder.addToVolumeMounts(new VolumeMountBuilder()
					.withName(volumeName)
					.withMountPath(getMountpath())
					.withReadOnly(false)
					.build());

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
		}

		// Build DeploymentConfig
		DeploymentConfig dc = new DeploymentConfigBuilder()
				.withNewMetadata()
				.withName(name)
				.withLabels(labels)
				.endMetadata()
				.withNewSpec()
				.withReplicas(1)
				.withSelector(Collections.singletonMap("name", name))
				.addToTriggers(new DeploymentTriggerPolicyBuilder()
						.withType("ConfigChange")
						.build())
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

		// Build Service
		Map<String, String> serviceLabels = new HashMap<>(labels);
		Map<String, String> serviceSelector = new HashMap<>();
		serviceSelector.put("deploymentconfig", name);
		serviceSelector.put("name", name);

		io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder()
				.withNewMetadata()
				.withName(getServiceName())
				.withLabels(serviceLabels)
				.endMetadata()
				.withNewSpec()
				.withSelector(serviceSelector)
				.withPorts(new ServicePortBuilder()
						.withPort(getPort())
						.withNewTargetPort(getPort())
						.withProtocol("TCP")
						.build())
				.withSessionAffinity("None")
				.endSpec()
				.build();

		List<HasMetadata> resources = new ArrayList<>();
		resources.add(dc);
		resources.add(service);

		openShift.createResources(resources);

		OpenShiftWaiters.get(openShift, ffCheck).isDcReady(name).waitFor();
	}

	public abstract String getImage();

	public abstract int getPort();

	public abstract String getMountpath();

	/**
	 * Hook for subclasses to configure the container (e.g., add probes).
	 *
	 * @param containerBuilder the Fabric8 ContainerBuilder to configure
	 */
	protected void configureContainer(ContainerBuilder containerBuilder) {

	}

	@Override
	public List<Pod> getPods() {
		return openShift.getPods(getApplication().getName());
	}

	@Override
	public void undeploy() {
		OpenShiftUtils.deleteResourcesWithLabels(openShift, Collections.singletonMap(APP_LABEL_KEY, dbApplication.getName()));
	}

	@Override
	public URL getURL() {
		throw new UnsupportedOperationException("Route is not created for DB applications.");
	}

	@Override
	public String getUrl(String routeName, boolean secure) {
		throw new UnsupportedOperationException("Route is not created for DB applications.");
	}

	/**
	 * Returns the service name used to expose the database functionality inside OpenShift.
	 * @return service name to access the database
	 */
	public String getServiceName() {
		return dbApplication.getName() + "-service";
	}

}
