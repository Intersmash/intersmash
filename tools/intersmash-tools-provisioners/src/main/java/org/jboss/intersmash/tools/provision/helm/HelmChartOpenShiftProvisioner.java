package org.jboss.intersmash.tools.provision.helm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.intersmash.tools.application.openshift.helm.HelmChartOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.helm.HelmChartRelease;
import org.jboss.intersmash.tools.application.openshift.helm.SerializableHelmChartRelease;
import org.jboss.intersmash.tools.provision.openshift.OpenShiftProvisioner;
import org.jboss.intersmash.tools.util.git.GitProject;
import org.jboss.intersmash.tools.util.git.GitUtil;
import org.slf4j.event.Level;

import cz.xtf.core.helm.HelmBinary;
import cz.xtf.core.helm.HelmClients;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.NonNull;

/**
 * An OpenShift provisioner that can provision applications through Helm Charts.
 *
 * The {@link HelmChartOpenShiftApplication} type which this generic provisioner is constrained on exposes the release
 * data through {@link HelmChartOpenShiftApplication#getRelease()}, which can be accessed during the provisioning
 * lifecycle, e.g.: when checking expected number of replicas or when scaling the replicas up or down.
 * The concrete {@link HelmChartRelease} instance obtained by
 * {@link HelmChartOpenShiftApplication#getRelease()} also implements
 * {@link SerializableHelmChartRelease} and thus provides
 * the ability to serialize the release data to a Helm Charts values file, which will be passed as an argument to
 * the `helm install/upgrade` commands.
 *
 * Concrete implementations must provide the path for the Chart to be deployed and can extend the behavior.
 */
public abstract class HelmChartOpenShiftProvisioner<A extends HelmChartOpenShiftApplication>
		implements OpenShiftProvisioner<A> {
	protected static final HelmBinary helmBinary = HelmClients.adminBinary();
	protected final A application;
	protected FailFastCheck ffCheck = () -> false;

	private final static Map<String, Map<String, Path>> HELM_CHARTS = new HashMap<>();

	public HelmChartOpenShiftProvisioner(@NonNull A application) {
		this.application = application;
	}

	@Override
	public A getApplication() {
		return application;
	}

	@Override
	public void deploy() {
		// validate
		if (this.getApplication().getRelease() == null) {
			throw new IllegalStateException(
					String.format(
							"No release information has been provided, (%s) cannot be provisioned",
							this.getApplication().getName()));
		}
		final Path helmChartsPath = this.getHelmCharts().get(this.getApplication().getHelmChartsRepositoryName());
		if (helmChartsPath == null) {
			throw new IllegalStateException(
					String.format(
							"The path for the selected Helm Charts (%s) was not found, %s will not be provisioned",
							this.getApplication().getHelmChartsRepositoryName(),
							this.getApplication().getName()));
		}
		helmBinary.execute(getHelmChartInstallArguments(this.getApplication(), helmChartsPath));
		if (this.getApplication().getRelease().getReplicas() > 0) {
			waitForReplicas(this.getApplication().getRelease().getReplicas());
		}
	}

	@Override
	public void undeploy() {
		helmBinary.execute(getHelmChartUninstallArguments(this.getApplication().getName()));
		OpenShiftWaiters.get(openShift, ffCheck).areExactlyNPodsReady(0, "app.kubernetes.io/instance", application.getName())
				.level(Level.DEBUG)
				.waitFor();
	}

	@Override
	public List<Pod> getPods() {
		return openShift.getLabeledPods("app.kubernetes.io/instance", application.getName());
	}

	@Override
	public void scale(int replicas, boolean wait) {
		this.getApplication().getRelease().setReplicas(replicas);
		final Path helmChartsPath = this.getHelmCharts().get(this.getApplication().getHelmChartsRepositoryName());
		helmBinary.execute(getHelmChartUpgradeArguments(this.getApplication(), helmChartsPath));
		if (wait) {
			waitForReplicas(replicas);
		}
	}

	protected void waitForReplicas(int replicas) {
		OpenShiftWaiters.get(openShift, ffCheck)
				.areExactlyNPodsReady(replicas, "app.kubernetes.io/instance", application.getName()).level(Level.DEBUG)
				.waitFor();
	}

	private static String[] getHelmChartUpgradeArguments(
			final HelmChartOpenShiftApplication application, final Path helmChartPath) {
		List<String> arguments = Stream.of("upgrade", application.getName(), helmChartPath.toAbsolutePath().toString())
				.collect(Collectors.toList());
		arguments.addAll(Arrays.asList(getHelmChartValuesFilesArguments(application)));
		arguments.addAll(Arrays.asList(
				"--kubeconfig", OpenShifts.adminBinary().getOcConfigPath(),
				// since we deploy from cloned charts repository, we need to set the "--dependency-update"
				// flag to fetch any non-local dependencies that chart requires
				// in order to prevent any issues with the helm chart
				"--dependency-update"));
		return arguments.stream().toArray(String[]::new);
	}

	private static String[] getHelmChartInstallArguments(
			final HelmChartOpenShiftApplication application, final Path helmChartPath) {
		List<String> arguments = Stream.of("install", application.getName(), helmChartPath.toAbsolutePath().toString(),
				"--replace").collect(Collectors.toList());
		arguments.addAll(Arrays.asList(getHelmChartValuesFilesArguments(application)));
		arguments.addAll(Arrays.asList(
				"--kubeconfig", OpenShifts.adminBinary().getOcConfigPath(),
				// since we deploy from cloned charts repository, we need to set the "--dependency-update"
				// flag to fetch any non-local dependencies that chart requires
				// in order to prevent any issues with the helm chart
				"--dependency-update"));
		return arguments.stream().toArray(String[]::new);
	}

	private static String[] getHelmChartUninstallArguments(final String releaseName) {
		return Stream.of("uninstall", releaseName, "--kubeconfig", OpenShifts.adminBinary().getOcConfigPath())
				.collect(Collectors.toList()).stream().toArray(String[]::new);
	}

	private static String[] getHelmChartValuesFilesArguments(HelmChartOpenShiftApplication application) {
		// adds a values file for the release data
		List<String> arguments = Stream.of("-f", application.getRelease().toValuesFile().toAbsolutePath().toString())
				.collect(Collectors.toList());
		// adds all the passed value files
		arguments.addAll(
				application.getRelease().getAdditionalValuesFiles().stream()
						.map(file -> Arrays.asList("-f", file.toAbsolutePath().toString()))
						.flatMap(List::stream).collect(Collectors.toList()));
		return arguments.stream().toArray(String[]::new);
	}

	protected Map<String, Path> getHelmCharts() {
		final String cachedChartsKey = forgeHelmChartsKey();
		if (!HELM_CHARTS.containsKey(cachedChartsKey)) {
			String helmChartsRepository = this.getApplication().getHelmChartsRepositoryUrl();
			String helmChartsBranch = this.getApplication().getHelmChartsRepositoryRef();
			GitProject helmChartsProject = GitUtil.cloneRepository(
					GitUtil.REPOSITORIES
							.resolve(String.format("%s-%s", this.getClass().getSimpleName(),
									this.getApplication().getHelmChartsRepositoryName()))
							.resolve("helm-charts"),
					helmChartsRepository,
					helmChartsBranch);
			HELM_CHARTS.put(cachedChartsKey,
					retrieveCharts(helmChartsProject.getPath(), this.getApplication().getHelmChartsRepositoryName()));
		}
		return HELM_CHARTS.get(cachedChartsKey);
	}

	private String forgeHelmChartsKey() {
		return String.format("%s:%s:%s",
				this.getApplication().getHelmChartsRepositoryUrl(),
				this.getApplication().getHelmChartsRepositoryRef(),
				this.getApplication().getHelmChartsRepositoryName());
	}

	/**
	 * Parses Helm Chart repository into a Map&lt;String, Path&gt;
	 * with entries containing chart names and their absolute paths
	 * Example {
	 * 		"eap74" : /tmp/wildfly-charts/charts/eap74,
	 * 		"eap-xp2" : /tmp/wildfly-charts/charts/eap-xp2
	 * 	}
	 * @param projectRoot of the Helm Chart repository
	 * @return Map&lt;String, Path&gt; of Helm Charts and their absolute path
	 */
	private Map<String, Path> retrieveCharts(Path projectRoot, final String helmChartsName) {
		String prefix = helmChartsName;
		try (Stream<Path> paths = Files.walk(projectRoot.resolve("charts"))) {
			return paths
					.filter(path -> path.getFileName().startsWith(prefix))
					.collect(Collectors.toMap(path -> path.getFileName().toString(), path -> path));
		} catch (IOException e) {
			throw new IllegalStateException(
					"An IOException was thrown while retrieving Helm charts, please double check your testing profile Helm properties",
					e);
		}
	}
}
