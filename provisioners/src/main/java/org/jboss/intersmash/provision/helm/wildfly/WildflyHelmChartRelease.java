package org.jboss.intersmash.provision.helm.wildfly;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.application.openshift.helm.HelmChartRelease;
import org.jboss.intersmash.provision.helm.Image;

import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;

/**
 * A contract that allows to create adapters that work seamlessly for both WildFly and EAP 8 Helm Charts releases.
 *
 * Can be used on the test side on order to accomplish dynamic configuration more easily.
 */
public interface WildflyHelmChartRelease extends HelmChartRelease {

	Map<String, String> getDeploymentEnvironmentVariables();

	void setDeploymentEnvironmentVariables(Map<String, String> deploymentEnvironmentVariables);

	WildflyHelmChartRelease withDeploymentEnvironmentVariables(Map<String, String> deploymentEnvironmentVariables);

	WildflyHelmChartRelease withDeploymentEnvironmentVariable(String key, String value);

	Map<String, String> getBuildEnvironmentVariables();

	void setBuildEnvironmentVariables(Map<String, String> buildEnvironmentVariables);

	WildflyHelmChartRelease withBuildEnvironmentVariables(Map<String, String> buildEnvironmentVariables);

	WildflyHelmChartRelease withBuildEnvironmentVariable(String key, String value);

	List<Image> getInjectedImages();

	void setInjectedImages(List<Image> injectedImages);

	WildflyHelmChartRelease withInjectedImages(List<Image> injectedImages);

	WildflyHelmChartRelease withInjectedImage(Image injectedImage);

	List<Volume> getVolumes();

	void setVolumes(List<Volume> volumes);

	WildflyHelmChartRelease withVolumes(List<Volume> volumes);

	WildflyHelmChartRelease withVolume(Volume volume);

	List<VolumeMount> getVolumeMounts();

	void setVolumeMounts(List<VolumeMount> volumeMounts);

	WildflyHelmChartRelease withVolumeMounts(List<VolumeMount> volumeMounts);

	WildflyHelmChartRelease withVolumeMount(VolumeMount volumeMount);

	String getRouteHost();

	void setRouteHost(String routeHost);

	WildflyHelmChartRelease withRouteHost(String routeHost);

	boolean isRouteTLSEnabled();

	void setRouteTLSEnabled(boolean routeTLSEnabled);

	WildflyHelmChartRelease withRouteTLSEnabled(boolean routeTLSEnabled);

	boolean isTlsEnabled();

	void setTlsEnabled(boolean tlsEnabled);

	WildflyHelmChartRelease withTlsEnabled(boolean tlsEnabled);

	LinkedHashSet<String> getS2iFeaturePacks();

	void setS2iFeaturePacks(LinkedHashSet<String> s2iFeaturePacks);

	WildflyHelmChartRelease withS2iFeaturePacks(LinkedHashSet<String> s2iFeaturePacks);

	WildflyHelmChartRelease withS2iFeaturePacks(String s2iFeaturePacks);

	WildflyHelmChartRelease withS2iFeaturePack(String s2iFeaturePack);

	LinkedHashSet<String> getS2iGalleonLayers();

	void setS2iGalleonLayers(LinkedHashSet<String> s2iGalleonLayers);

	WildflyHelmChartRelease withS2iGalleonLayers(LinkedHashSet<String> s2iGalleonLayers);

	WildflyHelmChartRelease withS2iGalleonLayers(String s2iGalleonLayers);

	WildflyHelmChartRelease withS2iGalleonLayer(String s2iGalleonLayer);

	LinkedHashSet<String> getS2iChannels();

	void setS2iChannels(LinkedHashSet<String> s2iFeaturePacks);

	WildflyHelmChartRelease withS2iChannels(LinkedHashSet<String> s2iChannels);

	WildflyHelmChartRelease withS2iChannel(String s2iChannel);

	BuildMode getBuildMode();

	String getBootableJarBuilderImage();

	void setBootableJarBuilderImage(String bootableJarBuilderImage);

	WildflyHelmChartRelease withBootableJarBuilderImage(String bootableJarBuilderImage);

	String getSourceRepositoryUrl();

	void setSourceRepositoryUrl(String sourceRepositoryUrl);

	WildflyHelmChartRelease withSourceRepositoryUrl(String sourceRepositoryUrl);

	String getSourceRepositoryRef();

	void setSourceRepositoryRef(String sourceRepositoryRef);

	WildflyHelmChartRelease withSourceRepositoryRef(String sourceRepositoryRef);

	String getContextDir();

	void setContextDir(String contextDir);

	WildflyHelmChartRelease withContextDir(String contextDir);

	Integer getReplicas();

	void setReplicas(Integer replicas);

	WildflyHelmChartRelease withReplicas(Integer replicas);

	boolean isBuildEnabled();

	void setBuildEnabled(boolean buildEnabled);

	WildflyHelmChartRelease withBuildEnabled(boolean buildEnabled);

	boolean isDeployEnabled();

	void setDeployEnabled(boolean deployEnabled);

	WildflyHelmChartRelease withDeployEnabled(boolean deployEnabled);

	String getJdk17BuilderImage();

	void setJdk17BuilderImage(String jdk17BuilderImage);

	WildflyHelmChartRelease withJdk17BuilderImage(String jdk17BuilderImage);

	String getJdk17RuntimeImage();

	void setJdk17RuntimeImage(String jdk17RuntimeImage);

	WildflyHelmChartRelease withJdk17RuntimeImage(String jdk17RuntimeImage);

	enum BuildMode {

		S2I("s2i"),
		BOOTABLE_JAR("bootable-jar");

		private String value;

		BuildMode(String value) {
			this.value = value;
		}

		String getValue() {
			return value;
		}
	}
}
