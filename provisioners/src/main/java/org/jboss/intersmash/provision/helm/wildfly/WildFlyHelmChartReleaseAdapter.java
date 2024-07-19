package org.jboss.intersmash.provision.helm.wildfly;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.intersmash.application.openshift.helm.HelmChartRelease;
import org.jboss.intersmash.model.helm.charts.values.wildfly.BootableJar;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Build;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Deploy;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Env;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Env__1;
import org.jboss.intersmash.model.helm.charts.values.wildfly.HelmWildflyRelease;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Route;
import org.jboss.intersmash.model.helm.charts.values.wildfly.S2i;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Tls;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Tls__1;
import org.jboss.intersmash.provision.helm.HelmChartReleaseAdapter;
import org.jboss.intersmash.provision.helm.Image;

import com.google.common.base.Strings;

import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * An adapter that implements a valid WildFly {@link HelmChartRelease} by exposing an internal instance of
 * {@link HelmWildflyRelease} to store a WildFly Helm Charts release data and to represent a values file which can be serialized
 * as an output for {@link HelmChartRelease#toValuesFile()}
 *
 * This adapter is compliant with the contract which is required by the
 * {@link org.jboss.intersmash.provision.helm.HelmChartOpenShiftProvisioner} logic, i.e. to
 * implement {@link HelmChartRelease}, and allows for us to leverage a generated
 * {@link WildFlyHelmChartReleaseAdapter#adaptee}, i.e. in terms of UX, provide native release YAML definitions.
 *
 * Of course the generated APIs can also be used programmatically.
 */
@Slf4j
public class WildFlyHelmChartReleaseAdapter extends HelmChartReleaseAdapter<HelmWildflyRelease>
		implements HelmChartRelease, WildflyHelmChartRelease {

	public WildFlyHelmChartReleaseAdapter(@NonNull HelmWildflyRelease release) {
		super(release, new ArrayList<>());
		initDefaultReplicas(release);
	}

	public WildFlyHelmChartReleaseAdapter(@NonNull HelmWildflyRelease release, List<Path> additionalValuesFiles) {
		super(release, additionalValuesFiles);
		initDefaultReplicas(release);
	}

	@Override
	public Map<String, String> getDeploymentEnvironmentVariables() {
		return adaptee.getDeploy() == null ? Map.of()
				: adaptee.getDeploy().getEnv() == null || adaptee.getDeploy().getEnv().isEmpty() ? Map.of()
						: adaptee.getDeploy().getEnv().stream()
								.collect(Collectors.toMap(e -> e.getName(), e -> e.getValue()));
	}

	@Override
	public void setDeploymentEnvironmentVariables(Map<String, String> deploymentEnvironmentVariables) {
		if (adaptee.getDeploy() == null) {
			adaptee.setDeploy(new Deploy());
		}
		if ((adaptee.getDeploy().getEnv() == null) || adaptee.getDeploy().getEnv().isEmpty()) {
			adaptee.getDeploy().setEnv(new ArrayList<>());
		} else {
			adaptee.getDeploy().getEnv().clear();
		}
		// let's check just here, so there's a way to clear, i.e. by passing an empty or null list
		if ((deploymentEnvironmentVariables != null) && !deploymentEnvironmentVariables.isEmpty()) {
			deploymentEnvironmentVariables.entrySet().stream()
					.map(e -> adaptee.getDeploy().getEnv().add(new Env__1().withName(e.getKey()).withValue(e.getValue())));
		}
	}

	public WildflyHelmChartRelease withDeploymentEnvironmentVariables(
			Map<String, String> deploymentEnvironmentVariables) {
		this.setDeploymentEnvironmentVariables(deploymentEnvironmentVariables);
		return this;
	}

	@Override
	public WildflyHelmChartRelease withDeploymentEnvironmentVariable(String key, String value) {
		if (adaptee.getDeploy() == null) {
			adaptee.setDeploy(new Deploy());
		}
		if ((adaptee.getDeploy().getEnv() == null) || adaptee.getDeploy().getEnv().isEmpty()) {
			adaptee.getDeploy().setEnv(new ArrayList<>());
		}
		adaptee.getDeploy().getEnv().add(new Env__1().withName(key).withValue(value));
		return this;
	}

	@Override
	public Map<String, String> getBuildEnvironmentVariables() {
		return adaptee.getBuild() == null ? Map.of()
				: adaptee.getBuild().getEnv() == null || adaptee.getBuild().getEnv().isEmpty() ? Map.of()
						: adaptee.getBuild().getEnv().stream()
								.collect(Collectors.toMap(e -> e.getName(), e -> e.getValue()));
	}

	@Override
	public void setBuildEnvironmentVariables(Map<String, String> buildEnvironmentVariables) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		if ((adaptee.getBuild().getEnv() == null) || adaptee.getBuild().getEnv().isEmpty()) {
			adaptee.getBuild().setEnv(new ArrayList<>());
		} else {
			adaptee.getBuild().getEnv().clear();
		}
		// let's check just here, so there's a way to clear, i.e. by passing an empty or null list
		if ((buildEnvironmentVariables != null) && !buildEnvironmentVariables.isEmpty()) {
			buildEnvironmentVariables.entrySet().stream()
					.map(e -> adaptee.getBuild().getEnv().add(new Env().withName(e.getKey()).withValue(e.getValue())));
		}
	}

	public WildflyHelmChartRelease withBuildEnvironmentVariables(Map<String, String> buildEnvironmentVariables) {
		this.setBuildEnvironmentVariables(buildEnvironmentVariables);
		return this;
	}

	@Override
	public WildflyHelmChartRelease withBuildEnvironmentVariable(String key, String value) {

		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		if ((adaptee.getBuild().getEnv() == null) || adaptee.getBuild().getEnv().isEmpty()) {
			adaptee.getBuild().setEnv(new ArrayList<>());
		}
		adaptee.getBuild().getEnv().add(new Env().withName(key).withValue(value));
		return this;
	}

	@Override
	public List<Image> getInjectedImages() {
		return adaptee.getBuild() == null ? List.of()
				: adaptee.getBuild().getImages() == null ? List.of()
						: (List<Image>) adaptee.getBuild().getImages();
	}

	@Override
	public void setInjectedImages(List<Image> injectedImages) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		if ((adaptee.getBuild().getImages() == null)) {
			adaptee.getBuild().setImages(new ArrayList<>());
		} else {
			((List) adaptee.getBuild().getImages()).clear();
		}
		// let's check just here, so there's a way to clear, i.e. by passing an empty or null list
		if ((injectedImages != null) && !injectedImages.isEmpty()) {
			((List) adaptee.getBuild().getImages()).addAll(injectedImages);
		}
	}

	@Override
	public WildflyHelmChartRelease withInjectedImages(List<Image> injectedImages) {
		this.setInjectedImages(injectedImages);
		return this;
	}

	@Override
	public WildflyHelmChartRelease withInjectedImage(Image injectedImage) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		if (adaptee.getBuild().getImages() == null) {
			adaptee.getBuild().setImages(new ArrayList<>());
		}
		((List) adaptee.getBuild().getImages()).add(injectedImage);
		return this;
	}

	@Override
	public List<Volume> getVolumes() {
		return adaptee.getDeploy() == null ? List.of()
				: adaptee.getDeploy().getVolumes() == null ? List.of()
						: adaptee.getDeploy().getVolumes().stream()
								.map(v -> (Volume) v)
								.collect(Collectors.toList());
	}

	@Override
	public void setVolumes(List<Volume> volumes) {
		if (adaptee.getDeploy() == null) {
			adaptee.setDeploy(new Deploy());
		}
		if ((adaptee.getDeploy().getVolumes() == null) || adaptee.getDeploy().getVolumes().isEmpty()) {
			adaptee.getDeploy().setVolumes(new ArrayList<>());
		} else {
			adaptee.getDeploy().getVolumes().clear();
		}
		// let's check just here, so there's a way to clear, i.e. by passing an empty or null list
		if ((volumes != null) && !volumes.isEmpty()) {
			volumes.stream()
					.map(v -> adaptee.getDeploy().getVolumes().add(v))
					.collect(Collectors.toList());
		}
	}

	public WildflyHelmChartRelease withVolumes(List<Volume> volumes) {
		this.setVolumes(volumes);
		return this;
	}

	@Override
	public WildflyHelmChartRelease withVolume(Volume volume) {
		if (adaptee.getDeploy() == null) {
			adaptee.setDeploy(new Deploy());
		}
		if ((adaptee.getDeploy().getVolumes() == null) || adaptee.getDeploy().getVolumes().isEmpty()) {
			adaptee.getDeploy().setVolumes(new ArrayList<>());
		}
		adaptee.getDeploy().getVolumes().add(volume);
		return this;
	}

	@Override
	public List<VolumeMount> getVolumeMounts() {
		return adaptee.getDeploy() == null ? List.of()
				: adaptee.getDeploy().getVolumeMounts() == null ? List.of()
						: adaptee.getDeploy().getVolumeMounts().stream()
								.map(v -> new VolumeMount(
										v.getMountPath(),
										v.getMountPropagation(),
										v.getName(),
										v.getReadOnly(),
										v.getSubPath(), null))
								.collect(Collectors.toList());
	}

	@Override
	public void setVolumeMounts(List<VolumeMount> volumeMounts) {
		if (adaptee.getDeploy() == null) {
			adaptee.setDeploy(new Deploy());
		}
		if ((adaptee.getDeploy().getVolumeMounts() == null) || adaptee.getDeploy().getVolumeMounts().isEmpty()) {
			adaptee.getDeploy().setVolumeMounts(new ArrayList<>());
		} else {
			adaptee.getDeploy().getVolumeMounts().clear();
		}
		// let's check just here, so there's a way to clear, i.e. by passing an empty or null list
		if ((volumeMounts != null) && !volumeMounts.isEmpty()) {
			volumeMounts.stream()
					.map(v -> adaptee.getDeploy().getVolumeMounts().add(
							new org.jboss.intersmash.model.helm.charts.values.wildfly.VolumeMount()
									.withName(v.getName())
									.withMountPath(v.getMountPath())
									.withMountPropagation(v.getMountPropagation())
									.withReadOnly(v.getReadOnly())
									.withSubPath(v.getSubPath())))
					.collect(Collectors.toList());
		}
	}

	public WildflyHelmChartRelease withVolumeMounts(List<VolumeMount> volumeMounts) {
		this.setVolumeMounts(volumeMounts);
		return this;
	}

	@Override
	public WildflyHelmChartRelease withVolumeMount(VolumeMount volumeMount) {
		if (adaptee.getDeploy() == null) {
			adaptee.setDeploy(new Deploy());
		}
		if ((adaptee.getDeploy().getVolumeMounts() == null) || adaptee.getDeploy().getVolumeMounts().isEmpty()) {
			adaptee.getDeploy().setVolumeMounts(new ArrayList<>());
		}
		adaptee.getDeploy().getVolumeMounts().add(
				new org.jboss.intersmash.model.helm.charts.values.wildfly.VolumeMount()
						.withName(volumeMount.getName())
						.withMountPath(volumeMount.getMountPath())
						.withMountPropagation(volumeMount.getMountPropagation())
						.withReadOnly(volumeMount.getReadOnly())
						.withSubPath(volumeMount.getSubPath()));
		return this;
	}

	@Override
	public String getRouteHost() {
		return adaptee.getDeploy() == null ? ""
				: adaptee.getDeploy().getRoute() == null ? "" : adaptee.getDeploy().getRoute().getHost();
	}

	@Override
	public void setRouteHost(String routeHost) {
		if (adaptee.getDeploy() == null) {
			adaptee.setDeploy(new Deploy());
		}
		if (adaptee.getDeploy().getRoute() == null) {
			adaptee.getDeploy().setRoute(new Route());
		}
		adaptee.getDeploy().getRoute().setHost(routeHost);
	}

	public WildflyHelmChartRelease withRouteHost(String routeHost) {
		this.setRouteHost(routeHost);
		return this;
	}

	@Override
	public boolean isRouteTLSEnabled() {
		return adaptee.getDeploy() == null ? Boolean.FALSE
				: adaptee.getDeploy().getRoute() == null ? Boolean.FALSE
						: adaptee.getDeploy().getRoute().getTls() == null ? Boolean.FALSE
								: adaptee.getDeploy().getRoute().getTls().getEnabled();
	}

	@Override
	public void setRouteTLSEnabled(boolean routeTLSEnabled) {
		if (adaptee.getDeploy() == null) {
			adaptee.setDeploy(new Deploy());
		}
		if (adaptee.getDeploy().getRoute() == null) {
			adaptee.getDeploy().setRoute(new Route());
		}
		if (adaptee.getDeploy().getRoute().getTls() == null) {
			adaptee.getDeploy().getRoute().setTls(new Tls());
		}
		adaptee.getDeploy().getRoute().getTls().setEnabled(routeTLSEnabled);
	}

	@Override
	public WildflyHelmChartRelease withRouteTLSEnabled(boolean routeTLSEnabled) {
		this.setRouteTLSEnabled(routeTLSEnabled);
		return this;
	}

	@Override
	public boolean isTlsEnabled() {
		return adaptee.getDeploy() == null ? Boolean.FALSE
				: adaptee.getDeploy().getRoute() == null ? Boolean.FALSE
						: adaptee.getDeploy().getTls() == null ? Boolean.FALSE : adaptee.getDeploy().getTls().getEnabled();
	}

	@Override
	public void setTlsEnabled(boolean tlsEnabled) {
		if (adaptee.getDeploy() == null) {
			adaptee.setDeploy(new Deploy());
		}
		if (adaptee.getDeploy().getTls() == null) {
			adaptee.getDeploy().setTls(new Tls__1());
		}
		adaptee.getDeploy().getTls().setEnabled(tlsEnabled);
	}

	@Override
	public WildflyHelmChartRelease withTlsEnabled(boolean tlsEnabled) {
		this.setTlsEnabled(tlsEnabled);
		return this;
	}

	@Override
	public LinkedHashSet<String> getS2iFeaturePacks() {
		LinkedHashSet result = new LinkedHashSet();
		if (adaptee.getBuild() != null && adaptee.getBuild().getS2i() != null
				&& !Strings.isNullOrEmpty(adaptee.getBuild().getS2i().getFeaturePacks())) {
			Arrays.stream(adaptee.getBuild().getS2i().getFeaturePacks().split(","))
					.forEach(fp -> result.add(fp));
		}
		return result;
	}

	@Override
	public void setS2iFeaturePacks(LinkedHashSet<String> s2iFeaturePacks) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		if (adaptee.getBuild().getS2i() == null) {
			adaptee.getBuild().setS2i(new S2i());
		}
		// let's check just here, so there's a way to clear, i.e. by passing an empty or null list
		if ((s2iFeaturePacks != null) && !s2iFeaturePacks.isEmpty()) {
			adaptee.getBuild().getS2i().setFeaturePacks(s2iFeaturePacks.stream().collect(Collectors.joining(",")));
		}
	}

	@Override
	public WildflyHelmChartRelease withS2iFeaturePacks(LinkedHashSet<String> s2iFeaturePacks) {
		this.setS2iFeaturePacks(s2iFeaturePacks);
		return this;
	}

	@Override
	public WildflyHelmChartRelease withS2iFeaturePacks(String s2iFeaturePacks) {
		LinkedHashSet s2iFeaturePacksSet = new LinkedHashSet<>(
				Arrays.stream(s2iFeaturePacks.split(",")).collect(Collectors.toList()));
		this.setS2iFeaturePacks(s2iFeaturePacksSet);
		return this;
	}

	@Override
	public WildflyHelmChartRelease withS2iFeaturePack(String s2iFeaturePack) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		if (adaptee.getBuild().getS2i() == null) {
			adaptee.getBuild().setS2i(new S2i());
		}
		final List<String> featurePacks = new ArrayList<>();
		if (!Strings.isNullOrEmpty(adaptee.getBuild().getS2i().getFeaturePacks())) {
			featurePacks.addAll(Arrays.stream(adaptee.getBuild().getS2i().getFeaturePacks().split(","))
					.collect(Collectors.toList()));
		}
		featurePacks.add(s2iFeaturePack);
		adaptee.getBuild().getS2i().setFeaturePacks(featurePacks.stream().collect(Collectors.joining(",")));
		return this;
	}

	@Override
	public LinkedHashSet<String> getS2iGalleonLayers() {
		LinkedHashSet result = new LinkedHashSet();
		if (adaptee.getBuild() != null && adaptee.getBuild().getS2i() != null
				&& !Strings.isNullOrEmpty(adaptee.getBuild().getS2i().getGalleonLayers())) {
			Arrays.stream(adaptee.getBuild().getS2i().getGalleonLayers().split(","))
					.forEach(fp -> result.add(fp));
		}
		return result;
	}

	@Override
	public void setS2iGalleonLayers(LinkedHashSet<String> s2iGalleonLayers) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		if (adaptee.getBuild().getS2i() == null) {
			adaptee.getBuild().setS2i(new S2i());
		}
		// let's check just here, so there's a way to clear, i.e. by passing an empty or null list
		if ((s2iGalleonLayers != null) && !s2iGalleonLayers.isEmpty()) {
			adaptee.getBuild().getS2i().setGalleonLayers(s2iGalleonLayers.stream().collect(Collectors.joining(",")));
		}
	}

	@Override
	public WildflyHelmChartRelease withS2iGalleonLayers(LinkedHashSet<String> s2iGalleonLayers) {
		this.setS2iGalleonLayers(s2iGalleonLayers);
		return this;
	}

	@Override
	public WildflyHelmChartRelease withS2iGalleonLayers(String s2iGalleonLayers) {
		LinkedHashSet s2iGalleonLayersSet = new LinkedHashSet<>(
				Arrays.stream(s2iGalleonLayers.split(",")).collect(Collectors.toList()));
		this.setS2iGalleonLayers(s2iGalleonLayersSet);
		return this;
	}

	@Override
	public WildflyHelmChartRelease withS2iGalleonLayer(String s2iGalleonLayer) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		if (adaptee.getBuild().getS2i() == null) {
			adaptee.getBuild().setS2i(new S2i());
		}
		final List<String> galleonLayers = new ArrayList<>();
		if (!Strings.isNullOrEmpty(adaptee.getBuild().getS2i().getGalleonLayers())) {
			galleonLayers.addAll(Arrays.stream(adaptee.getBuild().getS2i().getGalleonLayers().split(","))
					.collect(Collectors.toList()));
		}
		galleonLayers.add(s2iGalleonLayer);
		adaptee.getBuild().getS2i().setGalleonLayers(galleonLayers.stream().collect(Collectors.joining(",")));
		return this;
	}

	@Override
	public LinkedHashSet<String> getS2iChannels() {
		// no channels definition for WIldFly Helm Charts
		return new LinkedHashSet<>();
	}

	@Override
	public void setS2iChannels(LinkedHashSet<String> s2iChannels) {
		log.warn(
				"Setting s2i build channels on a WildFly release has no effect since WildFly Helm Charts don't support channels");
	}

	@Override
	public WildflyHelmChartRelease withS2iChannels(LinkedHashSet<String> s2iChannels) {
		this.setS2iChannels(s2iChannels);
		return this;
	}

	@Override
	public WildflyHelmChartRelease withS2iChannel(String s2iChannel) {
		log.warn(
				"Setting s2i build channels on a WildFly release has no effect since WildFly Helm Charts don't support channels");
		return this;
	}

	@Override
	public BuildMode getBuildMode() {
		if (adaptee.getBuild() == null || adaptee.getBuild().getMode() == null) {
			return null;
		}
		switch (adaptee.getBuild().getMode()) {
			case S_2_I:
				return BuildMode.S2I;
			case BOOTABLE_JAR:
				return BuildMode.BOOTABLE_JAR;
			default:
				return null;
		}
	}

	@Override
	public void setBuildMode(BuildMode buildMode) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		switch (buildMode) {
			case S2I:
				adaptee.getBuild().setMode(Build.Mode.S_2_I);
				break;
			case BOOTABLE_JAR:
				adaptee.getBuild().setMode(Build.Mode.BOOTABLE_JAR);
				break;
			default:
				log.warn("Unrecognized build mode option, not doing anything.");
		}
	}

	@Override
	public WildflyHelmChartRelease withBuildMode(BuildMode buildMode) {
		this.setBuildMode(buildMode);
		return this;
	}

	@Override
	public String getBootableJarBuilderImage() {
		return adaptee.getBuild() == null ? ""
				: adaptee.getBuild().getBootableJar() == null ? "" : adaptee.getBuild().getBootableJar().getBuilderImage();
	}

	@Override
	public void setBootableJarBuilderImage(String bootableJarBuilderImage) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		if (adaptee.getBuild().getBootableJar() == null) {
			adaptee.getBuild().setBootableJar(new BootableJar());
		}
		adaptee.getBuild().getBootableJar().setBuilderImage(bootableJarBuilderImage);
	}

	@Override
	public WildflyHelmChartRelease withBootableJarBuilderImage(String bootableJarBuilderImage) {
		this.setBootableJarBuilderImage(bootableJarBuilderImage);
		return this;
	}

	@Override
	public String getSourceRepositoryUrl() {
		return adaptee.getBuild() == null ? "" : adaptee.getBuild().getUri();
	}

	@Override
	public void setSourceRepositoryUrl(String sourceRepositoryUrl) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		adaptee.getBuild().setUri(sourceRepositoryUrl);
	}

	@Override
	public WildflyHelmChartRelease withSourceRepositoryUrl(String sourceRepositoryUrl) {
		this.setSourceRepositoryUrl(sourceRepositoryUrl);
		return this;
	}

	@Override
	public String getSourceRepositoryRef() {
		return adaptee.getBuild() == null ? "" : adaptee.getBuild().getRef();
	}

	@Override
	public void setSourceRepositoryRef(String sourceRepositoryRef) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		adaptee.getBuild().setRef(sourceRepositoryRef);
	}

	@Override
	public WildflyHelmChartRelease withSourceRepositoryRef(String sourceRepositoryRef) {
		this.setSourceRepositoryRef(sourceRepositoryRef);
		return this;
	}

	@Override
	public String getContextDir() {
		return adaptee.getBuild() == null ? "" : adaptee.getBuild().getContextDir();
	}

	@Override
	public void setContextDir(String contextDir) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		adaptee.getBuild().setContextDir(contextDir);
	}

	@Override
	public WildflyHelmChartRelease withContextDir(String contextDir) {
		this.setContextDir(contextDir);
		return this;
	}

	@Override
	public Integer getReplicas() {
		return adaptee.getDeploy() == null ? 0 : adaptee.getDeploy().getReplicas();
	}

	@Override
	public void setReplicas(Integer replicas) {
		if (adaptee.getDeploy() == null) {
			adaptee.setDeploy(new Deploy());
		}
		adaptee.getDeploy().setReplicas(replicas);
	}

	@Override
	public WildflyHelmChartRelease withReplicas(Integer replicas) {
		this.setReplicas(replicas);
		return this;
	}

	@Override
	public boolean isBuildEnabled() {
		return adaptee.getBuild() == null ? Boolean.FALSE : adaptee.getBuild().getEnabled();
	}

	@Override
	public void setBuildEnabled(boolean buildEnabled) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		adaptee.getBuild().setEnabled(buildEnabled);
	}

	@Override
	public WildflyHelmChartRelease withBuildEnabled(boolean buildEnabled) {
		this.setBuildEnabled(buildEnabled);
		return this;
	}

	@Override
	public boolean isDeployEnabled() {
		return adaptee.getDeploy() == null ? Boolean.FALSE : adaptee.getDeploy().getEnabled();
	}

	@Override
	public void setDeployEnabled(boolean deployEnabled) {
		if (adaptee.getDeploy() == null) {
			adaptee.setDeploy(new Deploy());
		}
		adaptee.getDeploy().setEnabled(deployEnabled);
	}

	@Override
	public WildflyHelmChartRelease withDeployEnabled(boolean deployEnabled) {
		this.setDeployEnabled(deployEnabled);
		return this;
	}

	@Override
	public String getJdk17BuilderImage() {
		// WilFly Helm Charts do not differentiate between JDKs
		return adaptee.getBuild() == null ? "" : adaptee.getBuild().getS2i().getBuilderImage();
	}

	@Override
	public void setJdk17BuilderImage(String jdk17BuilderImage) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		if (adaptee.getBuild().getS2i() == null) {
			adaptee.getBuild().setS2i(new S2i());
		}
		// WilFly Helm Charts do not differentiate between JDKs
		adaptee.getBuild().getS2i().setBuilderImage(jdk17BuilderImage);
	}

	@Override
	public WildflyHelmChartRelease withJdk17BuilderImage(String jdk17BuilderImage) {
		this.setJdk17BuilderImage(jdk17BuilderImage);
		return this;
	}

	@Override
	public String getJdk17RuntimeImage() {
		// WilFly Helm Charts do not differentiate between JDKs
		return adaptee.getBuild() == null ? "" : adaptee.getBuild().getS2i().getRuntimeImage();
	}

	@Override
	public void setJdk17RuntimeImage(String jdk17RuntimeImage) {
		if (adaptee.getBuild() == null) {
			adaptee.setBuild(new Build());
		}
		if (adaptee.getBuild().getS2i() == null) {
			adaptee.getBuild().setS2i(new S2i());
		}
		// WilFly Helm Charts do not differentiate between JDKs
		adaptee.getBuild().getS2i().setRuntimeImage(jdk17RuntimeImage);
	}

	@Override
	public WildflyHelmChartRelease withJdk17RuntimeImage(String jdk17RuntimeImage) {
		this.setJdk17RuntimeImage(jdk17RuntimeImage);
		return this;
	}

	private void initDefaultReplicas(HelmWildflyRelease release) {
		if (release.getDeploy() == null) {
			release = release.withDeploy(new Deploy());
		}
		if (release.getDeploy().getReplicas() == null) {
			release.getDeploy().setReplicas(1);
		}
	}
}
