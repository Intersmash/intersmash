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
package org.jboss.intersmash.provision.helm.wildfly;

import java.util.HashMap;
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
public interface WildflyHelmChartRelease<R extends WildflyHelmChartRelease> extends HelmChartRelease {

	Map<String, String> getDeploymentEnvironmentVariables();

	void setDeploymentEnvironmentVariables(Map<String, String> deploymentEnvironmentVariables);

	R withDeploymentEnvironmentVariables(Map<String, String> deploymentEnvironmentVariables);

	R withDeploymentEnvironmentVariable(String key, String value);

	Map<String, String> getBuildEnvironmentVariables();

	void setBuildEnvironmentVariables(Map<String, String> buildEnvironmentVariables);

	R withBuildEnvironmentVariables(Map<String, String> buildEnvironmentVariables);

	R withBuildEnvironmentVariable(String key, String value);

	List<Image> getInjectedImages();

	void setInjectedImages(List<Image> injectedImages);

	R withInjectedImages(List<Image> injectedImages);

	R withInjectedImage(Image injectedImage);

	List<Volume> getVolumes();

	void setVolumes(List<Volume> volumes);

	R withVolumes(List<Volume> volumes);

	R withVolume(Volume volume);

	List<VolumeMount> getVolumeMounts();

	void setVolumeMounts(List<VolumeMount> volumeMounts);

	R withVolumeMounts(List<VolumeMount> volumeMounts);

	R withVolumeMount(VolumeMount volumeMount);

	String getRouteHost();

	void setRouteHost(String routeHost);

	R withRouteHost(String routeHost);

	boolean isRouteTLSEnabled();

	void setRouteTLSEnabled(boolean routeTLSEnabled);

	R withRouteTLSEnabled(boolean routeTLSEnabled);

	boolean isTlsEnabled();

	void setTlsEnabled(boolean tlsEnabled);

	R withTlsEnabled(boolean tlsEnabled);

	LinkedHashSet<String> getS2iFeaturePacks();

	void setS2iFeaturePacks(LinkedHashSet<String> s2iFeaturePacks);

	R withS2iFeaturePacks(LinkedHashSet<String> s2iFeaturePacks);

	R withS2iFeaturePacks(String s2iFeaturePacks);

	R withS2iFeaturePack(String s2iFeaturePack);

	LinkedHashSet<String> getS2iGalleonLayers();

	void setS2iGalleonLayers(LinkedHashSet<String> s2iGalleonLayers);

	R withS2iGalleonLayers(LinkedHashSet<String> s2iGalleonLayers);

	R withS2iGalleonLayers(String s2iGalleonLayers);

	R withS2iGalleonLayer(String s2iGalleonLayer);

	LinkedHashSet<String> getS2iChannels();

	void setS2iChannels(LinkedHashSet<String> s2iFeaturePacks);

	R withS2iChannels(LinkedHashSet<String> s2iChannels);

	R withS2iChannel(String s2iChannel);

	BuildMode getBuildMode();

	void setBuildMode(BuildMode buildMode);

	R withBuildMode(BuildMode buildMode);

	String getBootableJarBuilderImage();

	void setBootableJarBuilderImage(String bootableJarBuilderImage);

	R withBootableJarBuilderImage(String bootableJarBuilderImage);

	String getSourceRepositoryUrl();

	void setSourceRepositoryUrl(String sourceRepositoryUrl);

	R withSourceRepositoryUrl(String sourceRepositoryUrl);

	String getSourceRepositoryRef();

	void setSourceRepositoryRef(String sourceRepositoryRef);

	R withSourceRepositoryRef(String sourceRepositoryRef);

	String getContextDir();

	void setContextDir(String contextDir);

	R withContextDir(String contextDir);

	Integer getReplicas();

	void setReplicas(Integer replicas);

	R withReplicas(Integer replicas);

	boolean isBuildEnabled();

	void setBuildEnabled(boolean buildEnabled);

	R withBuildEnabled(boolean buildEnabled);

	boolean isDeployEnabled();

	void setDeployEnabled(boolean deployEnabled);

	R withDeployEnabled(boolean deployEnabled);

	@Deprecated(forRemoval = true, since = "0.0.5")
	String getJdk17BuilderImage();

	@Deprecated(forRemoval = true, since = "0.0.5")
	void setJdk17BuilderImage(String jdk17BuilderImage);

	@Deprecated(forRemoval = true, since = "0.0.5")
	R withJdk17BuilderImage(String jdk17BuilderImage);

	@Deprecated(forRemoval = true, since = "0.0.5")
	String getJdk17RuntimeImage();

	@Deprecated(forRemoval = true, since = "0.0.5")
	void setJdk17RuntimeImage(String jdk17RuntimeImage);

	@Deprecated(forRemoval = true, since = "0.0.5")
	R withJdk17RuntimeImage(String jdk17RuntimeImage);

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

	JdkImage getJdkBuilderImage();

	void setJdkBuilderImage(JdkImage jdkBuilderImage);

	R withJdkBuilderImage(JdkImage jdkBuilderImage);

	JdkImage getJdkRuntimeImage();

	void setJdkRuntimeImage(JdkImage jdkRuntimeImage);

	R withJdkRuntimeImage(JdkImage jdkRuntimeImage);

	class JdkImage {
		String image;
		Version version;

		public String getImage() {
			return image;
		}

		public void setImage(String image) {
			this.image = image;
		}

		public Version getVersion() {
			return version;
		}

		public void setVersion(Version version) {
			this.version = version;
		}

		public JdkImage(String image, Version version) {
			this.image = image;
			this.version = version;
		}

		public enum Version {
			JDK_17("17"),
			JDK_21("21"),
			JDK_25("25");

			private final String value;

			private final static Map<String, JdkImage.Version> CONSTANTS = new HashMap<>();

			static {
				for (JdkImage.Version c : values()) {
					CONSTANTS.put(c.value, c);
				}
			}

			Version(String value) {
				this.value = value;
			}

			String getValue() {
				return value;
			}

			public static JdkImage.Version fromValue(String value) {
				JdkImage.Version constant = CONSTANTS.get(value);
				if (constant == null) {
					throw new IllegalArgumentException(value);
				} else {
					return constant;
				}
			}
		}
	}
}
