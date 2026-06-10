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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.intersmash.tools.config.WaitingConfig;
import org.jboss.intersmash.tools.waiting.SimpleWaiter;
import org.jboss.intersmash.tools.waiting.Waiter;
import org.jboss.intersmash.tools.waiting.failfast.FailFastCheck;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildList;
import io.fabric8.openshift.api.model.DeploymentConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides waiter methods for common OpenShift conditions such as DeploymentConfig readiness,
 * pod counts by label, and build completion.
 *
 * <p>This class replaces XTF's {@code cz.xtf.core.openshift.OpenShiftWaiters} with an Intersmash-native
 * implementation that uses the Intersmash waiting infrastructure ({@link SimpleWaiter}, {@link Waiter}).</p>
 */
@Slf4j
public class OpenShiftWaiters {

	private final OpenShift openShift;
	private final FailFastCheck failFast;

	/**
	 * Creates a new OpenShiftWaiters instance.
	 *
	 * @param openShift the OpenShift client to use for queries
	 * @param failFast the fail-fast check, or {@code null} to disable fail-fast
	 */
	public OpenShiftWaiters(OpenShift openShift, FailFastCheck failFast) {
		this.openShift = openShift;
		this.failFast = failFast != null ? failFast : () -> false;
	}

	/**
	 * Static factory method.
	 *
	 * @param openShift the OpenShift client to use for queries
	 * @param failFast the fail-fast check, or {@code null} to disable fail-fast
	 * @return a new OpenShiftWaiters instance
	 */
	public static OpenShiftWaiters get(OpenShift openShift, FailFastCheck failFast) {
		return new OpenShiftWaiters(openShift, failFast);
	}

	// ========================
	// DeploymentConfig waiters
	// ========================

	/**
	 * Waits for all pods created by the given DeploymentConfig to be ready.
	 * Tolerates any number of container restarts. Uses default timeout.
	 *
	 * @param dcName the name of the DeploymentConfig
	 * @return a Waiter that succeeds when the DeploymentConfig's pods are ready
	 */
	public Waiter isDcReady(String dcName) {
		return isDcReady(dcName, Integer.MAX_VALUE);
	}

	/**
	 * Waits for all pods created by the given DeploymentConfig to be ready.
	 * Fails if any pod has restarted at least {@code restartTolerance} times. Uses default timeout.
	 *
	 * @param dcName the name of the DeploymentConfig
	 * @param restartTolerance maximum tolerated restart count before failing
	 * @return a Waiter that succeeds when the DeploymentConfig's pods are ready
	 */
	public Waiter isDcReady(String dcName, int restartTolerance) {
		return isDeploymentReady(dcName, () -> openShift.getPods(dcName), restartTolerance)
				.reason("Waiting till all pods created by " + dcName + " deployment config are ready");
	}

	/**
	 * Waits for all pods of a specific version of the given DeploymentConfig to be ready.
	 * Tolerates any number of container restarts. Uses default timeout.
	 *
	 * @param dcName the name of the DeploymentConfig
	 * @param version the deployment version
	 * @return a Waiter that succeeds when the versioned deployment's pods are ready
	 */
	public Waiter isDeploymentReady(String dcName, int version) {
		return isDeploymentReady(dcName, version, Integer.MAX_VALUE);
	}

	/**
	 * Waits for all pods of a specific version of the given DeploymentConfig to be ready.
	 * Fails if any pod has restarted at least {@code restartTolerance} times. Uses default timeout.
	 *
	 * @param dcName the name of the DeploymentConfig
	 * @param version the deployment version
	 * @param restartTolerance maximum tolerated restart count before failing
	 * @return a Waiter that succeeds when the versioned deployment's pods are ready
	 */
	public Waiter isDeploymentReady(String dcName, int version, int restartTolerance) {
		return isDeploymentReady(dcName, () -> openShift.getLabeledPods("deployment", dcName + "-" + version),
				restartTolerance)
				.reason("Waiting till all pods created by " + version + ". of " + dcName
						+ " deployment config are ready");
	}

	private SimpleWaiter isDeploymentReady(String dcName, java.util.function.Supplier<List<Pod>> podSupplier,
			int restartTolerance) {
		return new SimpleWaiter(() -> {
			DeploymentConfig dc = openShift.getDeploymentConfig(dcName);
			if (dc == null) {
				return false;
			}
			int replicas = dc.getSpec().getReplicas() != null ? dc.getSpec().getReplicas() : 1;
			List<Pod> pods = podSupplier.get();
			long readyCount = pods.stream().filter(this::isPodReady).count();
			return readyCount == replicas;
		}, TimeUnit.MILLISECONDS, WaitingConfig.timeout())
				.failureCondition(() -> hasAnyPodRestartedAtLeastNTimes(podSupplier.get(), restartTolerance))
				.failFast(failFast);
	}

	// ========================
	// Pod readiness waiters
	// ========================

	/**
	 * Waits for exactly N pods in the project to be in Ready condition.
	 * Uses default timeout. Tolerates any container restarts.
	 *
	 * @param n the expected number of ready pods
	 * @return a Waiter that succeeds when exactly N pods are ready
	 */
	public Waiter areExactlyNPodsReady(int n) {
		return areExactlyNPodsReady(n, openShift::getPods)
				.reason("Waiting for exactly " + n + " pods to be ready.");
	}

	/**
	 * Waits for exactly N pods matching the "deploymentconfig" label to be in Ready condition.
	 *
	 * @param n the expected number of ready pods
	 * @param dcName the value for the "deploymentconfig" label
	 * @return a Waiter that succeeds when exactly N matching pods are ready
	 */
	public Waiter areExactlyNPodsReady(int n, String dcName) {
		return areExactlyNPodsReady(n, "deploymentconfig", dcName);
	}

	/**
	 * Waits for exactly N pods matching the given label to be in Ready condition.
	 *
	 * @param n the expected number of ready pods
	 * @param key the label key
	 * @param value the label value
	 * @return a Waiter that succeeds when exactly N matching pods are ready
	 */
	public Waiter areExactlyNPodsReady(int n, String key, String value) {
		return areExactlyNPodsReady(n, () -> openShift.getLabeledPods(key, value))
				.reason("Waiting for exactly " + n + " pods with label " + key + "=" + value + " to be ready.");
	}

	private SimpleWaiter areExactlyNPodsReady(int n, java.util.function.Supplier<List<Pod>> podSupplier) {
		return new SimpleWaiter(() -> {
			List<Pod> pods = podSupplier.get();
			long readyCount = pods.stream().filter(this::isPodReady).count();
			return readyCount == n;
		}, TimeUnit.MILLISECONDS, WaitingConfig.timeout())
				.failFast(failFast);
	}

	// ========================
	// Pod running waiters
	// ========================

	/**
	 * Waits for exactly N pods in the project to be in Running phase.
	 * Uses default timeout. Tolerates any container restarts.
	 *
	 * @param n the expected number of running pods
	 * @return a Waiter that succeeds when exactly N pods are running
	 */
	public Waiter areExactlyNPodsRunning(int n) {
		return areExactlyNPodsRunning(n, openShift::getPods)
				.reason("Waiting for exactly " + n + " pods to be running.");
	}

	/**
	 * Waits for exactly N pods matching the "deploymentconfig" label to be in Running phase.
	 *
	 * @param n the expected number of running pods
	 * @param dcName the value for the "deploymentconfig" label
	 * @return a Waiter that succeeds when exactly N matching pods are running
	 */
	public Waiter areExactlyNPodsRunning(int n, String dcName) {
		return areExactlyNPodsRunning(n, "deploymentconfig", dcName);
	}

	/**
	 * Waits for exactly N pods matching the given label to be in Running phase.
	 *
	 * @param n the expected number of running pods
	 * @param key the label key
	 * @param value the label value
	 * @return a Waiter that succeeds when exactly N matching pods are running
	 */
	public Waiter areExactlyNPodsRunning(int n, String key, String value) {
		return areExactlyNPodsRunning(n, () -> openShift.getLabeledPods(key, value))
				.reason("Waiting for exactly " + n + " pods with label " + key + "=" + value + " to be running.");
	}

	private SimpleWaiter areExactlyNPodsRunning(int n, java.util.function.Supplier<List<Pod>> podSupplier) {
		return new SimpleWaiter(() -> {
			List<Pod> pods = podSupplier.get();
			long runningCount = pods.stream()
					.filter(pod -> pod.getStatus() != null && "Running".equals(pod.getStatus().getPhase()))
					.count();
			return runningCount == n;
		}, TimeUnit.MILLISECONDS, WaitingConfig.timeout())
				.failFast(failFast);
	}

	// ========================
	// No pods present waiters
	// ========================

	/**
	 * Waits until no pods matching the "deploymentconfig" label are present.
	 *
	 * @param dcName the value for the "deploymentconfig" label
	 * @return a Waiter that succeeds when no matching pods exist
	 */
	public Waiter areNoPodsPresent(String dcName) {
		return areNoPodsPresent("deploymentconfig", dcName);
	}

	/**
	 * Waits until no pods matching the given label are present.
	 *
	 * @param key the label key
	 * @param value the label value
	 * @return a Waiter that succeeds when no matching pods exist
	 */
	public Waiter areNoPodsPresent(String key, String value) {
		return new SimpleWaiter(
				() -> openShift.getLabeledPods(key, value).isEmpty(),
				TimeUnit.MILLISECONDS, WaitingConfig.timeout(),
				"Waiting for no present pods with label " + key + "=" + value + ".")
				.failFast(failFast);
	}

	// ========================
	// Pod restart waiters
	// ========================

	/**
	 * Waits for pods matching the "deploymentconfig" label to have been restarted at least once.
	 *
	 * @param dcName the value for the "deploymentconfig" label
	 * @return a Waiter that succeeds when at least one matching pod has a restart count &gt; 0
	 */
	public Waiter havePodsBeenRestarted(String dcName) {
		return havePodsBeenRestarted("deploymentconfig", dcName);
	}

	/**
	 * Waits for pods matching the given label to have been restarted at least once.
	 *
	 * @param key the label key
	 * @param value the label value
	 * @return a Waiter that succeeds when at least one matching pod has a restart count &gt; 0
	 */
	public Waiter havePodsBeenRestarted(String key, String value) {
		return havePodsBeenRestartedAtLeastNTimes(1, key, value);
	}

	/**
	 * Waits for pods matching the "deploymentconfig" label to have been restarted at least N times.
	 *
	 * @param times the minimum restart count
	 * @param dcName the value for the "deploymentconfig" label
	 * @return a Waiter that succeeds when at least one matching pod has restarted at least N times
	 */
	public Waiter havePodsBeenRestartedAtLeastNTimes(int times, String dcName) {
		return havePodsBeenRestartedAtLeastNTimes(times, "deploymentconfig", dcName);
	}

	/**
	 * Waits for pods matching the given label to have been restarted at least N times.
	 *
	 * @param times the minimum restart count
	 * @param key the label key
	 * @param value the label value
	 * @return a Waiter that succeeds when at least one matching pod has restarted at least N times
	 */
	public Waiter havePodsBeenRestartedAtLeastNTimes(int times, String key, String value) {
		return new SimpleWaiter(
				() -> hasAnyPodRestartedAtLeastNTimes(openShift.getLabeledPods(key, value), times),
				TimeUnit.MILLISECONDS, WaitingConfig.timeout(),
				"Waiting for any pods with label " + key + "=" + value + " having a restart count >= " + times
						+ ".")
				.failFast(failFast);
	}

	// ========================
	// Build waiters
	// ========================

	/**
	 * Waits for the latest build of the given BuildConfig to complete successfully.
	 * Fails immediately if the build enters Failed, Error, or Cancelled phase.
	 *
	 * @param buildConfigName the name of the BuildConfig
	 * @return a Waiter that succeeds when the latest build completes
	 */
	public Waiter hasBuildCompleted(String buildConfigName) {
		return new SimpleWaiter(() -> {
			Build latestBuild = getLatestBuild(buildConfigName);
			if (latestBuild == null || latestBuild.getStatus() == null) {
				return false;
			}
			return "Complete".equals(latestBuild.getStatus().getPhase());
		}, TimeUnit.MILLISECONDS, WaitingConfig.buildTimeout(),
				"Waiting for completion of latest build " + buildConfigName)
				.failureCondition(() -> {
					Build latestBuild = getLatestBuild(buildConfigName);
					if (latestBuild == null || latestBuild.getStatus() == null) {
						return false;
					}
					String phase = latestBuild.getStatus().getPhase();
					return "Failed".equals(phase) || "Error".equals(phase) || "Cancelled".equals(phase);
				})
				.logPoint(Waiter.LogPoint.BOTH)
				.failFast(failFast)
				.interval(5_000);
	}

	/**
	 * Waits for the given build to complete successfully.
	 * Fails immediately if the build enters Failed, Error, or Cancelled phase.
	 *
	 * @param build the Build to wait upon
	 * @return a Waiter that succeeds when the build completes
	 */
	public Waiter hasBuildCompleted(Build build) {
		String buildName = build.getMetadata().getName();
		return new SimpleWaiter(() -> {
			Build b = openShift.getBuild(buildName);
			return b != null && b.getStatus() != null && "Complete".equals(b.getStatus().getPhase());
		}, TimeUnit.MILLISECONDS, WaitingConfig.buildTimeout(),
				"Waiting for completion of build " + buildName)
				.failureCondition(() -> {
					Build b = openShift.getBuild(buildName);
					if (b == null || b.getStatus() == null) {
						return false;
					}
					String phase = b.getStatus().getPhase();
					return "Failed".equals(phase) || "Error".equals(phase) || "Cancelled".equals(phase);
				})
				.logPoint(Waiter.LogPoint.BOTH)
				.failFast(failFast)
				.interval(5_000);
	}

	/**
	 * Waits for a build to exist for the given BuildConfig (i.e. the latest build is present).
	 *
	 * @param buildConfigName the name of the BuildConfig
	 * @return a Waiter that succeeds when at least one build exists for the BuildConfig
	 */
	public Waiter isLatestBuildPresent(String buildConfigName) {
		return new SimpleWaiter(
				() -> getLatestBuild(buildConfigName) != null,
				TimeUnit.MILLISECONDS, WaitingConfig.timeout(),
				"Waiting for presence of latest build of buildconfig " + buildConfigName)
				.logPoint(Waiter.LogPoint.BOTH)
				.failFast(failFast)
				.interval(5_000);
	}

	// ========================
	// Project waiters
	// ========================

	/**
	 * Waits for a project to be ready (i.e. it exists and can be accessed).
	 * Uses a 20-second timeout.
	 *
	 * @return a Waiter that succeeds when the project exists
	 */
	public Waiter isProjectReady() {
		return new SimpleWaiter(() -> {
			try {
				return openShift.getProject(openShift.getNamespace()) != null;
			} catch (Exception e) {
				return false;
			}
		}, TimeUnit.SECONDS, 20,
				"Waiting for the project to be created.")
				.failFast(failFast);
	}

	/**
	 * Waits for the project (namespace) to be clean, i.e. all user-created resources are deleted.
	 *
	 * @return a Waiter that succeeds when the namespace has no removable resources
	 */
	public Waiter isProjectClean() {
		return openShift.clean();
	}

	// ========================
	// Internal helpers
	// ========================

	private boolean isPodReady(Pod pod) {
		if (pod.getStatus() == null || pod.getStatus().getConditions() == null) {
			return false;
		}
		return pod.getStatus().getConditions().stream()
				.anyMatch(condition -> "Ready".equals(condition.getType())
						&& "True".equals(condition.getStatus()));
	}

	private boolean hasAnyPodRestartedAtLeastNTimes(List<Pod> pods, int times) {
		return pods.stream().anyMatch(pod -> pod.getStatus() != null
				&& pod.getStatus().getContainerStatuses() != null
				&& pod.getStatus().getContainerStatuses().stream()
						.anyMatch(cs -> cs.getRestartCount() != null && cs.getRestartCount() >= times));
	}

	private Build getLatestBuild(String buildConfigName) {
		BuildList buildList = openShift.getClient().builds()
				.withLabel("buildconfig", buildConfigName)
				.list();
		if (buildList == null || buildList.getItems() == null || buildList.getItems().isEmpty()) {
			return null;
		}
		return buildList.getItems().stream()
				.filter(b -> b.getMetadata() != null && b.getMetadata().getAnnotations() != null)
				.max((b1, b2) -> {
					String num1 = b1.getMetadata().getAnnotations()
							.getOrDefault("openshift.io/build.number", "0");
					String num2 = b2.getMetadata().getAnnotations()
							.getOrDefault("openshift.io/build.number", "0");
					try {
						return Integer.compare(Integer.parseInt(num1), Integer.parseInt(num2));
					} catch (NumberFormatException e) {
						return num1.compareTo(num2);
					}
				})
				.orElse(null);
	}
}
