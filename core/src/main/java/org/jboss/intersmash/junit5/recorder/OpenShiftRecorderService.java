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
package org.jboss.intersmash.junit5.recorder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.intersmash.tools.client.OpenShift;
import org.jboss.intersmash.tools.client.OpenShifts;
import org.jboss.intersmash.tools.config.IntersmashProperties;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.Route;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenShiftRecorderService {

	private static final String FILTER_INITIALIZATION_DONE = "FILTERS_INITIALIZATION_DONE";
	private static final String POD_FILTER = "POD_FILTER";
	private static final String DC_FILTER = "DC_FILTER";
	private static final String BUILD_FILTER = "BUILD_FILTER";
	private static final String BC_FILTER = "BC_FILTER";
	private static final String IS_FILTER = "IS_FILTER";
	private static final String SS_FILTER = "SS_FILTER";
	private static final String ROUTE_FILTER = "ROUTE_FILTER";
	private static final String CONFIGMAP_FILTER = "CONFIGMAP_FILTER";
	private static final String SERVICE_FILTER = "SERVICE_FILTER";
	private static final String EVENT_FILTER = "EVENT_FILTER";

	private int uniqueExecutionIdentifier;

	public void initFilters(ExtensionContext context) {
		ExtensionContext.Store classStore = getClassStore(context);
		OpenShift master = OpenShifts.master();

		initClassFilter(context, POD_FILTER, master, Pod.class);
		initClassFilter(context, DC_FILTER, master, DeploymentConfig.class);
		initClassFilter(context, BUILD_FILTER, master, Build.class);
		initClassFilter(context, BC_FILTER, master, BuildConfig.class);
		initClassFilter(context, IS_FILTER, master, ImageStream.class);
		initClassFilter(context, SS_FILTER, master, StatefulSet.class);
		initClassFilter(context, ROUTE_FILTER, master, Route.class);
		initClassFilter(context, CONFIGMAP_FILTER, master, ConfigMap.class);
		initClassFilter(context, SERVICE_FILTER, master, Service.class);
		classStore.put(EVENT_FILTER,
				new EventsFilterBuilder().setExcludedUntil(ResourcesTimestampHelper.timeOfLastEvent(master)));

		setFiltersInitializationStatus(context, false);
	}

	public void updateFilters(ExtensionContext context) {
		OpenShift master = OpenShifts.master();
		if (!isFilterInitializationComplete(context)) {
			updateClassFilter(context, POD_FILTER, master, Pod.class);
			updateClassFilter(context, DC_FILTER, master, DeploymentConfig.class);
			updateClassFilter(context, BUILD_FILTER, master, Build.class);
			updateClassFilter(context, BC_FILTER, master, BuildConfig.class);
			updateClassFilter(context, IS_FILTER, master, ImageStream.class);
			updateClassFilter(context, SS_FILTER, master, StatefulSet.class);
			updateClassFilter(context, ROUTE_FILTER, master, Route.class);
			updateClassFilter(context, CONFIGMAP_FILTER, master, ConfigMap.class);
			updateClassFilter(context, SERVICE_FILTER, master, Service.class);
			updateClassFilter(context, EVENT_FILTER, master, Event.class);

			setFiltersInitializationStatus(context, true);
		}

		initMethodFilter(context, POD_FILTER, master, Pod.class);
		initMethodFilter(context, DC_FILTER, master, DeploymentConfig.class);
		initMethodFilter(context, BUILD_FILTER, master, Build.class);
		initMethodFilter(context, BC_FILTER, master, BuildConfig.class);
		initMethodFilter(context, IS_FILTER, master, ImageStream.class);
		initMethodFilter(context, SS_FILTER, master, StatefulSet.class);
		initMethodFilter(context, ROUTE_FILTER, master, Route.class);
		initMethodFilter(context, CONFIGMAP_FILTER, master, ConfigMap.class);
		initMethodFilter(context, SERVICE_FILTER, master, Service.class);
		initMethodFilter(context, EVENT_FILTER, master, Event.class);
	}

	public void recordState(ExtensionContext context) throws IOException {
		uniqueExecutionIdentifier = ThreadLocalRandom.current().nextInt(Short.MAX_VALUE);

		savePods(context, getFilter(context, POD_FILTER));
		saveDCs(context, getFilter(context, DC_FILTER));
		saveBuilds(context, getFilter(context, BUILD_FILTER));
		saveBCs(context, getFilter(context, BC_FILTER));
		saveISs(context, getFilter(context, IS_FILTER));
		saveStatefulSets(context, getFilter(context, SS_FILTER));
		saveRoutes(context, getFilter(context, ROUTE_FILTER));
		saveConfigMaps(context, getFilter(context, CONFIGMAP_FILTER));
		saveServices(context, getFilter(context, SERVICE_FILTER));
		saveSecrets(context);
		savePodLogs(context, getFilter(context, POD_FILTER));
		saveBuildLogs(context, getFilter(context, BUILD_FILTER));
		saveEvents(context, getFilter(context, EVENT_FILTER));
	}

	private boolean isFilterInitializationComplete(ExtensionContext context) {
		ExtensionContext.Store classStore = getClassStore(context);
		return classStore.get(FILTER_INITIALIZATION_DONE, AtomicBoolean.class).get();
	}

	private void setFiltersInitializationStatus(ExtensionContext context, boolean done) {
		ExtensionContext.Store classStore = getClassStore(context);
		classStore.put(FILTER_INITIALIZATION_DONE, new AtomicBoolean(done));
	}

	private void initClassFilter(ExtensionContext context, String key, OpenShift openShift,
			Class<? extends HasMetadata> resourceClass) {
		ExtensionContext.Store classStore = getClassStore(context);
		classStore.put(key, new ResourcesFilterBuilder<>()
				.setExcludedUntil(ResourcesTimestampHelper.timeOfLastResourceOf(openShift, resourceClass)));
	}

	private void updateClassFilter(ExtensionContext context, String key, OpenShift openShift,
			Class<? extends HasMetadata> resourceClass) {
		ExtensionContext.Store classStore = getClassStore(context);
		ResourcesFilterBuilder<?> filter = classStore.get(key, ResourcesFilterBuilder.class);
		filter.setIncludedAlwaysWindow(
				filter.getExcludedUntil(),
				ResourcesTimestampHelper.timeOfLastResourceOf(openShift, resourceClass));
		filter.setExcludedUntil(null);
	}

	private void initMethodFilter(ExtensionContext context, String key, OpenShift openShift,
			Class<? extends HasMetadata> resourceClass) {
		ExtensionContext.Store classStore = getClassStore(context);
		ExtensionContext.Store methodStore = getMethodStore(context);
		try {
			methodStore.put(key, classStore.get(key, ResourcesFilterBuilder.class)
					.clone()
					.setExcludedUntil(ResourcesTimestampHelper.timeOfLastResourceOf(openShift, resourceClass)));
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	private ExtensionContext.Store getClassStore(ExtensionContext extensionContext) {
		return extensionContext.getStore(ExtensionContext.Namespace.create(extensionContext.getRequiredTestClass()));
	}

	private ExtensionContext.Store getMethodStore(ExtensionContext extensionContext) {
		return extensionContext
				.getStore(ExtensionContext.Namespace.create(extensionContext.getRequiredTestClass(),
						extensionContext.getTestClass()));
	}

	@SuppressWarnings("unchecked")
	private <E extends HasMetadata> ResourcesFilterBuilder<E> getFilter(ExtensionContext context, String key) {
		OpenShiftRecorder classOpenShiftRecorder = AnnotationSupport
				.findAnnotation(context.getRequiredTestClass(), OpenShiftRecorder.class).orElse(null);
		OpenShiftRecorder methodOpenShiftRecorder = AnnotationSupport
				.findAnnotation(context.getElement(), OpenShiftRecorder.class).orElse(null);
		OpenShiftRecorder openShiftRecorder = methodOpenShiftRecorder != null
				? methodOpenShiftRecorder
				: classOpenShiftRecorder;

		String[] resourceNames = openShiftRecorder != null ? openShiftRecorder.resourceNames() : null;

		ResourcesFilterBuilder<E> filter = context.getTestMethod().isPresent()
				? (ResourcesFilterBuilder<E>) getMethodStore(context).get(key, ResourcesFilterBuilder.class)
				: (ResourcesFilterBuilder<E>) getClassStore(context).get(key, ResourcesFilterBuilder.class);

		if (resourceNames != null
				&& !(resourceNames.length == 1 && resourceNames[0].equals(""))) {
			filter.filterByResourceNames();
			filter.setResourceNames(resourceNames);
		} else {
			filter.filterByLastSeenResources();
		}
		return filter;
	}

	private void saveStatefulSets(ExtensionContext context, ResourcesFilterBuilder<StatefulSet> filter)
			throws IOException {
		final Path logPath = Paths.get(attachmentsDir(), dirNameForTest(context), "statefulSets.log");
		try (final ResourcesPrinterHelper<StatefulSet> printer = ResourcesPrinterHelper.forStatefulSet(logPath)) {
			OpenShifts.master().getStatefulSets().stream()
					.filter(filter.build())
					.forEach(printer::row);
		}
	}

	private void saveISs(ExtensionContext context, ResourcesFilterBuilder<ImageStream> filter) throws IOException {
		final Path logPath = Paths.get(attachmentsDir(), dirNameForTest(context),
				"imageStreams-" + OpenShifts.master().getNamespace() + ".log");
		try (final ResourcesPrinterHelper<ImageStream> printer = ResourcesPrinterHelper.forISs(logPath)) {
			OpenShifts.master().getImageStreams().stream()
					.filter(filter.build())
					.forEach(printer::row);
		}
	}

	private void saveBCs(ExtensionContext context, ResourcesFilterBuilder<BuildConfig> filter) throws IOException {
		final Path logPath = Paths.get(attachmentsDir(), dirNameForTest(context),
				"buildConfigs-" + OpenShifts.master().getNamespace() + ".log");
		try (final ResourcesPrinterHelper<BuildConfig> printer = ResourcesPrinterHelper.forBCs(logPath)) {
			OpenShifts.master().getBuildConfigs().stream()
					.filter(filter.build())
					.forEach(printer::row);
		}
	}

	private void saveBuilds(ExtensionContext context, ResourcesFilterBuilder<Build> filter) throws IOException {
		final Path logPath = Paths.get(attachmentsDir(), dirNameForTest(context),
				"builds-" + OpenShifts.master().getNamespace() + ".log");
		try (final ResourcesPrinterHelper<Build> printer = ResourcesPrinterHelper.forBuilds(logPath)) {
			OpenShifts.master().getBuilds().stream()
					.filter(filter.build())
					.forEach(printer::row);
		}
	}

	private void saveSecrets(ExtensionContext context) throws IOException {
		final Path logPath = Paths.get(attachmentsDir(), dirNameForTest(context), "secrets.log");
		try (final ResourcesPrinterHelper<Secret> printer = ResourcesPrinterHelper.forSecrets(logPath)) {
			OpenShifts.master().getSecrets()
					.forEach(printer::row);
		}
	}

	private void saveServices(ExtensionContext context, ResourcesFilterBuilder<Service> filter) throws IOException {
		final Path logPath = Paths.get(attachmentsDir(), dirNameForTest(context), "services.log");
		try (final ResourcesPrinterHelper<Service> printer = ResourcesPrinterHelper.forServices(logPath)) {
			OpenShifts.master().getServices().stream()
					.filter(filter.build())
					.forEach(printer::row);
		}
	}

	private void saveRoutes(ExtensionContext context, ResourcesFilterBuilder<Route> filter) throws IOException {
		final Path logPath = Paths.get(attachmentsDir(), dirNameForTest(context), "routes.log");
		try (final ResourcesPrinterHelper<Route> printer = ResourcesPrinterHelper.forRoutes(logPath)) {
			OpenShifts.master().getRoutes().stream()
					.filter(filter.build())
					.forEach(printer::row);
		}
	}

	private void saveConfigMaps(ExtensionContext context, ResourcesFilterBuilder<ConfigMap> filter) throws IOException {
		final Path logPath = Paths.get(attachmentsDir(), dirNameForTest(context), "configMaps.log");
		try (final ResourcesPrinterHelper<ConfigMap> printer = ResourcesPrinterHelper.forConfigMaps(logPath)) {
			OpenShifts.master().getConfigMaps().stream()
					.filter(filter.build())
					.forEach(printer::row);
		}
	}

	private void savePods(ExtensionContext context, ResourcesFilterBuilder<Pod> filter) throws IOException {
		final Path logPath = Paths.get(attachmentsDir(), dirNameForTest(context),
				"pods-" + OpenShifts.master().getNamespace() + ".log");
		try (final ResourcesPrinterHelper<Pod> printer = ResourcesPrinterHelper.forPods(logPath)) {
			OpenShifts.master().getPods().stream()
					.filter(filter.build())
					.forEach(printer::row);
		}
	}

	private void saveDCs(ExtensionContext context, ResourcesFilterBuilder<DeploymentConfig> filter) throws IOException {
		final Path logPath = Paths.get(attachmentsDir(), dirNameForTest(context), "deploymentConfigs.log");
		try (final ResourcesPrinterHelper<DeploymentConfig> printer = ResourcesPrinterHelper.forDCs(logPath)) {
			OpenShifts.master().getDeploymentConfigs().stream()
					.filter(filter.build())
					.forEach(printer::row);
		}
	}

	private void savePodLogs(ExtensionContext context, ResourcesFilterBuilder<Pod> filter) {
		OpenShift master = OpenShifts.master();
		master.getPods().stream()
				.filter(filter.build())
				.filter(pod -> pod.getStatus().getInitContainerStatuses().stream()
						.noneMatch(cs -> !(cs.getState().getTerminated() != null
								&& "Completed".equalsIgnoreCase(cs.getState().getTerminated().getReason()))))
				.forEach(pod -> {
					try {
						master.storePodLog(pod,
								Paths.get(attachmentsDir(), dirNameForTest(context)),
								pod.getMetadata().getName() + ".log");
					} catch (IOException e) {
						log.warn("Failed to store pod log for {}: {}", pod.getMetadata().getName(), e.getMessage());
					}
				});
	}

	private void saveEvents(ExtensionContext context, ResourcesFilterBuilder<Event> filter) throws IOException {
		final Path logPath = Paths.get(attachmentsDir(), dirNameForTest(context),
				"events-" + OpenShifts.master().getNamespace() + ".log");
		try (final ResourcesPrinterHelper<Event> printer = ResourcesPrinterHelper.forEvents(logPath)) {
			OpenShifts.master().getEvents().stream()
					.filter(filter.build())
					.forEach(printer::row);
		}
	}

	private void saveBuildLogs(ExtensionContext context, ResourcesFilterBuilder<Build> filter) {
		OpenShift master = OpenShifts.master();
		master.getBuilds().stream()
				.filter(filter.build())
				.forEach(build -> {
					try {
						master.storeBuildLog(build,
								Paths.get(attachmentsDir(), dirNameForTest(context)),
								build.getMetadata().getName() + ".log");
					} catch (IOException e) {
						log.warn("Failed to store build log for {}: {}", build.getMetadata().getName(), e.getMessage());
					}
				});
	}

	private String attachmentsDir() {
		String dir = IntersmashProperties.get("intersmash.record.dir");
		if (dir == null) {
			dir = IntersmashProperties.get("xtf.record.dir");
		}
		return dir != null ? dir : System.getProperty("user.dir");
	}

	private String dirNameForTest(ExtensionContext context) {
		if (context.getTestMethod().isPresent()) {
			return context.getTestClass().get().getName() + "." + context.getTestMethod().get().getName()
					+ context.getDisplayName();
		} else {
			return context.getTestClass().get().getName() + "-" + uniqueExecutionIdentifier;
		}
	}
}
