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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.application.openshift.DBImageOpenShiftApplication;
import org.slf4j.event.Level;

import cz.xtf.builder.builders.ApplicationBuilder;
import cz.xtf.builder.builders.DeploymentConfigBuilder;
import cz.xtf.builder.builders.PVCBuilder;
import cz.xtf.builder.builders.pod.ContainerBuilder;
import cz.xtf.builder.builders.pod.PersistentVolumeClaim;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.Pod;
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

	public void customizeApplication(ApplicationBuilder appBuilder) {
	}

	public abstract String getSymbolicName();

	@Override
	public void deploy() {
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				dbApplication.getName());
		ApplicationBuilder appBuilder = ApplicationBuilder.fromImage(dbApplication.getName(), getImage(),
				Collections.singletonMap(APP_LABEL_KEY, dbApplication.getName()));

		final DeploymentConfigBuilder builder = appBuilder.deploymentConfig(dbApplication.getName());
		builder.podTemplate().container().envVars(getImageVariables()).port(getPort());

		configureContainer(builder.podTemplate().container());

		// create persistent volume claims on the cluster before deploying
		if (dbApplication.getPersistentVolumeClaims() != null && !dbApplication.getPersistentVolumeClaims().isEmpty()) {
			PersistentVolumeClaim pvc = dbApplication.getPersistentVolumeClaims().get(0);
			openShift.createPersistentVolumeClaim(
					new PVCBuilder(pvc.getClaimName()).accessRWX().storageSize("100Mi").build());
		}

		appBuilder.service(getServiceName()).port(getPort())
				.addContainerSelector("deploymentconfig", dbApplication.getName())
				.addContainerSelector("name", dbApplication.getName());

		customizeApplication(appBuilder);

		appBuilder.buildApplication(openShift).deploy();

		// mount persistent volumes into pod using Fabric8 API directly
		// (bypasses XTF's Volume builder which has a compatibility issue with Fabric8 7.x)
		if (dbApplication.getPersistentVolumeClaims() != null && !dbApplication.getPersistentVolumeClaims().isEmpty()) {
			PersistentVolumeClaim pvc = dbApplication.getPersistentVolumeClaims().get(0);
			io.fabric8.openshift.api.model.DeploymentConfig dc = openShift.deploymentConfigs()
					.withName(dbApplication.getName()).get();
			io.fabric8.kubernetes.api.model.PodSpec podSpec = dc.getSpec().getTemplate().getSpec();

			java.util.List<io.fabric8.kubernetes.api.model.Volume> volumes = podSpec.getVolumes();
			if (volumes == null) {
				volumes = new java.util.ArrayList<>();
				podSpec.setVolumes(volumes);
			}
			volumes.add(new io.fabric8.kubernetes.api.model.VolumeBuilder()
					.withName(pvc.getName())
					.withNewPersistentVolumeClaim(pvc.getClaimName(), false)
					.build());

			io.fabric8.kubernetes.api.model.Container container = podSpec.getContainers().get(0);
			java.util.List<io.fabric8.kubernetes.api.model.VolumeMount> mounts = container.getVolumeMounts();
			if (mounts == null) {
				mounts = new java.util.ArrayList<>();
				container.setVolumeMounts(mounts);
			}
			mounts.add(new io.fabric8.kubernetes.api.model.VolumeMountBuilder()
					.withName(pvc.getName())
					.withMountPath(getMountpath())
					.withReadOnly(false)
					.build());

			openShift.deploymentConfigs().withName(dbApplication.getName()).replace(dc);
		}

		OpenShiftWaiters.get(openShift, ffCheck).isDcReady(appBuilder.getName()).waitFor();
	}

	public abstract String getImage();

	public abstract int getPort();

	public abstract String getMountpath();

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
	 * Returns the service name used to expose the database functionality inside OpenShift;
	 * When using {@link ApplicationBuilder} to build the application (which is always the case here), then the service name defaults to the application nane
	 * @return service name to access the database
	 */
	public String getServiceName() {
		return dbApplication.getName() + "-service";
	}

}
