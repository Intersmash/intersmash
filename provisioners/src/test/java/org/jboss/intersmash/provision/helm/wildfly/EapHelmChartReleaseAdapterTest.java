package org.jboss.intersmash.provision.helm.wildfly;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.intersmash.model.helm.charts.values.eap8.HelmEap8Release;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Build;
import org.jboss.intersmash.model.helm.charts.values.wildfly.HelmWildflyRelease;
import org.jboss.intersmash.model.helm.charts.values.xp5.HelmXp5Release;
import org.jboss.intersmash.provision.helm.Image;
import org.jboss.intersmash.provision.helm.wildfly.eap8.Eap8HelmChartReleaseAdapter;
import org.jboss.intersmash.provision.helm.wildfly.xp5.EapXp5HelmChartReleaseAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cz.xtf.builder.builders.pod.SecretVolume;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;

/**
 * Tests that verify that the EAP 8 and WildFly adapters for the related EAP 8 and WildFly values file (abstracted by
 * their equivalent POJO) are working as expected, i.e. values are set properly to the adaptee and return expected
 * values once read.
 */
class EapHelmChartReleaseAdapterTest {

	/**
	 * Verify that the EAP 8 adapter for the EAP 8 values file POJO is properly storing and exposing the adaptee values
	 */
	@Test
	public void verifyEap8DynamicallyFilledAdapterTest() {
		// arrange
		HelmEap8Release adaptee = new HelmEap8Release();
		Eap8HelmChartReleaseAdapter concreteAdapter = new Eap8HelmChartReleaseAdapter(adaptee);
		Eap8HelmChartReleaseAdapter eap8HelmChartRelease = concreteAdapter;

		SecretVolume secretVolume = new SecretVolume("v1", "s1");
		eap8HelmChartRelease
				.withSourceRepositoryUrl("url")
				.withSourceRepositoryRef("ref")
				.withS2iFeaturePacks("fp1,fp2")
				.withS2iGalleonLayers("gl1,gl2")
				.withS2iChannel("ch1")
				.withContextDir("context-dir")
				.withBuildEnabled(Boolean.TRUE)
				.withJdk17BuilderImage("jdk17-B")
				.withJdk17RuntimeImage("jdk17-R")
				.withDeployEnabled(Boolean.FALSE)
				.withReplicas(42)
				.withRouteHost("route-host")
				.withVolume(secretVolume.build())
				.withVolumeMount(new VolumeMountBuilder()
						.withName("vm1")
						.withMountPath("mp1")
						.withReadOnly(Boolean.TRUE).build())
				.withRouteTLSEnabled(Boolean.FALSE)
				.withTlsEnabled(Boolean.FALSE)
				.withBuildEnvironmentVariable("a", "1")
				.withDeploymentEnvironmentVariable("b", "2")
				.withInjectedImage(
						new Image(
								new Image.From("ImageStreamTag", "openshift", "myImage:latest"),
								List.of(new Image.Path("mySourcePath", "myDestPath"))))
				.withInjectedImage(
						new Image(
								new Image.From("ImageStreamTag", "openshift", "anotherImage:latest"),
								List.of(new Image.Path("anotherSourcePath", "anotherDestPath"))));

		// act
		final HelmEap8Release actualAdaptee = concreteAdapter.getAdaptee();

		// assert
		Assertions.assertEquals("url", actualAdaptee.getBuild().getUri());
		Assertions.assertEquals("ref", actualAdaptee.getBuild().getRef());
		Assertions.assertEquals("context-dir", actualAdaptee.getBuild().getContextDir());
		Assertions.assertEquals("fp1,fp2", actualAdaptee.getBuild().getS2i().getFeaturePacks());
		Assertions.assertEquals("gl1,gl2", actualAdaptee.getBuild().getS2i().getGalleonLayers());
		Assertions.assertEquals("ch1", actualAdaptee.getBuild().getS2i().getChannels());
		Assertions.assertEquals(Boolean.TRUE, actualAdaptee.getBuild().getEnabled());
		Assertions.assertEquals("jdk17-B", actualAdaptee.getBuild().getS2i().getJdk17().getBuilderImage());
		Assertions.assertEquals("jdk17-R", actualAdaptee.getBuild().getS2i().getJdk17().getRuntimeImage());
		Assertions.assertEquals(Boolean.FALSE, actualAdaptee.getDeploy().getEnabled());
		Assertions.assertEquals(42, actualAdaptee.getDeploy().getReplicas());
		Assertions.assertEquals("route-host", actualAdaptee.getDeploy().getRoute().getHost());
		Assertions.assertEquals(1, actualAdaptee.getDeploy().getVolumes().size());
		Assertions.assertEquals(1, actualAdaptee.getDeploy().getVolumeMounts().size());
		Assertions.assertEquals(Boolean.FALSE, actualAdaptee.getDeploy().getRoute().getTls().getEnabled());
		Assertions.assertEquals(Boolean.FALSE, actualAdaptee.getDeploy().getTls().getEnabled());
		Assertions.assertEquals(1, actualAdaptee.getBuild().getEnv().size());
		Assertions.assertEquals(1, actualAdaptee.getDeploy().getEnv().size());
		Assertions.assertTrue(List.class.isAssignableFrom(actualAdaptee.getBuild().getImages().getClass()));
		List imagesInstancesList = (List) actualAdaptee.getBuild().getImages();
		Assertions.assertEquals(2, imagesInstancesList.size());
		Assertions.assertTrue(Image.class.isAssignableFrom(imagesInstancesList.get(0).getClass()));
		// round trip check
		Assertions.assertEquals("url", eap8HelmChartRelease.getSourceRepositoryUrl());
		Assertions.assertEquals("ref", eap8HelmChartRelease.getSourceRepositoryRef());
		Assertions.assertEquals("context-dir", eap8HelmChartRelease.getContextDir());
		Assertions.assertEquals("fp1,fp2", eap8HelmChartRelease.getS2iFeaturePacks().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals("gl1,gl2",
				eap8HelmChartRelease.getS2iGalleonLayers().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals("ch1", eap8HelmChartRelease.getS2iChannels().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals(Boolean.TRUE, eap8HelmChartRelease.isBuildEnabled());
		Assertions.assertEquals("jdk17-B", eap8HelmChartRelease.getJdk17BuilderImage());
		Assertions.assertEquals("jdk17-R", eap8HelmChartRelease.getJdk17RuntimeImage());
		Assertions.assertEquals(Boolean.FALSE, eap8HelmChartRelease.isDeployEnabled());
		Assertions.assertEquals(42, eap8HelmChartRelease.getReplicas());
		Assertions.assertEquals("route-host", eap8HelmChartRelease.getRouteHost());
		Assertions.assertEquals(1, eap8HelmChartRelease.getVolumes().size());
		Assertions.assertEquals(1, eap8HelmChartRelease.getVolumeMounts().size());
		Assertions.assertEquals(Boolean.FALSE, eap8HelmChartRelease.isRouteTLSEnabled());
		Assertions.assertEquals(Boolean.FALSE, eap8HelmChartRelease.isTlsEnabled());
		Assertions.assertEquals(1, eap8HelmChartRelease.getBuildEnvironmentVariables().size());
		Assertions.assertEquals(1, eap8HelmChartRelease.getDeploymentEnvironmentVariables().size());
		Assertions.assertTrue(List.class.isAssignableFrom(actualAdaptee.getBuild().getImages().getClass()));
		Assertions.assertEquals(2, eap8HelmChartRelease.getInjectedImages().size());
	}

	/**
	 * Verify that the WildFly adapter for the WildFly values file POJO is properly storing and exposing the adaptee values
	 */
	@Test
	public void verifyWildFlyDynamicallyFilledAdapterTest() {
		// arrange
		HelmWildflyRelease adaptee = new HelmWildflyRelease();
		WildFlyHelmChartReleaseAdapter concreteAdapter = new WildFlyHelmChartReleaseAdapter(adaptee);
		WildflyHelmChartRelease eap8HelmChartRelease = concreteAdapter;

		SecretVolume secretVolume = new SecretVolume("v1", "s1");
		eap8HelmChartRelease
				.withSourceRepositoryUrl("url")
				.withSourceRepositoryRef("ref")
				.withS2iFeaturePacks("fp1,fp2")
				.withS2iGalleonLayers("gl1,gl2")
				.withS2iChannel("ch1")
				.withContextDir("context-dir")
				.withBuildEnabled(Boolean.TRUE)
				.withBuildMode(WildflyHelmChartRelease.BuildMode.BOOTABLE_JAR)
				.withJdk17BuilderImage("jdk17-B")
				.withJdk17RuntimeImage("jdk17-R")
				.withBootableJarBuilderImage("BJ-I")
				.withDeployEnabled(Boolean.FALSE)
				.withReplicas(42)
				.withRouteHost("route-host")
				.withVolume(secretVolume.build())
				.withVolumeMount(new VolumeMountBuilder()
						.withName("vm1")
						.withMountPath("mp1")
						.withReadOnly(Boolean.TRUE).build())
				.withRouteTLSEnabled(Boolean.FALSE)
				.withTlsEnabled(Boolean.FALSE)
				.withBuildEnvironmentVariable("a", "1")
				.withDeploymentEnvironmentVariable("b", "2")
				.withInjectedImage(
						new Image(
								new Image.From("ImageStreamTag", "openshift", "myImage:latest"),
								List.of(new Image.Path("mySourcePath", "myDestPath"))))
				.withInjectedImage(
						new Image(
								new Image.From("ImageStreamTag", "openshift", "anotherImage:latest"),
								List.of(new Image.Path("anotherSourcePath", "anotherDestPath"))));

		// act
		final HelmWildflyRelease actualAdaptee = concreteAdapter.getAdaptee();
		// assert
		Assertions.assertEquals("url", actualAdaptee.getBuild().getUri());
		Assertions.assertEquals("ref", actualAdaptee.getBuild().getRef());
		Assertions.assertEquals("context-dir", actualAdaptee.getBuild().getContextDir());
		Assertions.assertEquals("fp1,fp2", actualAdaptee.getBuild().getS2i().getFeaturePacks());
		Assertions.assertEquals("gl1,gl2", actualAdaptee.getBuild().getS2i().getGalleonLayers());
		Assertions.assertEquals(Boolean.TRUE, actualAdaptee.getBuild().getEnabled());
		Assertions.assertEquals(Build.Mode.BOOTABLE_JAR, actualAdaptee.getBuild().getMode());
		Assertions.assertEquals("jdk17-B", actualAdaptee.getBuild().getS2i().getBuilderImage());
		Assertions.assertEquals("jdk17-R", actualAdaptee.getBuild().getS2i().getRuntimeImage());
		Assertions.assertEquals("BJ-I", actualAdaptee.getBuild().getBootableJar().getBuilderImage());
		Assertions.assertEquals(Boolean.FALSE, actualAdaptee.getDeploy().getEnabled());
		Assertions.assertEquals(42, actualAdaptee.getDeploy().getReplicas());
		Assertions.assertEquals("route-host", actualAdaptee.getDeploy().getRoute().getHost());
		Assertions.assertEquals(1, actualAdaptee.getDeploy().getVolumes().size());
		Assertions.assertEquals(1, actualAdaptee.getDeploy().getVolumeMounts().size());
		Assertions.assertEquals(Boolean.FALSE, actualAdaptee.getDeploy().getRoute().getTls().getEnabled());
		Assertions.assertEquals(Boolean.FALSE, actualAdaptee.getDeploy().getTls().getEnabled());
		Assertions.assertEquals(1, actualAdaptee.getBuild().getEnv().size());
		Assertions.assertEquals(1, actualAdaptee.getDeploy().getEnv().size());
		Assertions.assertTrue(List.class.isAssignableFrom(actualAdaptee.getBuild().getImages().getClass()));
		List imagesInstancesList = (List) actualAdaptee.getBuild().getImages();
		Assertions.assertEquals(2, imagesInstancesList.size());
		Assertions.assertTrue(Image.class.isAssignableFrom(imagesInstancesList.get(0).getClass()));
		// round-trip check
		Assertions.assertEquals("url", eap8HelmChartRelease.getSourceRepositoryUrl());
		Assertions.assertEquals("ref", eap8HelmChartRelease.getSourceRepositoryRef());
		Assertions.assertEquals("context-dir", eap8HelmChartRelease.getContextDir());
		Assertions.assertEquals("fp1,fp2", eap8HelmChartRelease.getS2iFeaturePacks().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals("gl1,gl2",
				eap8HelmChartRelease.getS2iGalleonLayers().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals(Boolean.TRUE, eap8HelmChartRelease.isBuildEnabled());
		Assertions.assertEquals(WildflyHelmChartRelease.BuildMode.BOOTABLE_JAR, eap8HelmChartRelease.getBuildMode());
		Assertions.assertEquals("jdk17-B", eap8HelmChartRelease.getJdk17BuilderImage());
		Assertions.assertEquals("jdk17-R", eap8HelmChartRelease.getJdk17RuntimeImage());
		Assertions.assertEquals("BJ-I", eap8HelmChartRelease.getBootableJarBuilderImage());
		Assertions.assertEquals(Boolean.FALSE, eap8HelmChartRelease.isDeployEnabled());
		Assertions.assertEquals(42, eap8HelmChartRelease.getReplicas());
		Assertions.assertEquals("route-host", eap8HelmChartRelease.getRouteHost());
		Assertions.assertEquals(1, eap8HelmChartRelease.getVolumes().size());
		Assertions.assertEquals(1, eap8HelmChartRelease.getVolumeMounts().size());
		Assertions.assertEquals(Boolean.FALSE, eap8HelmChartRelease.isRouteTLSEnabled());
		Assertions.assertEquals(Boolean.FALSE, eap8HelmChartRelease.isTlsEnabled());
		Assertions.assertEquals(1, eap8HelmChartRelease.getBuildEnvironmentVariables().size());
		Assertions.assertEquals(1, eap8HelmChartRelease.getDeploymentEnvironmentVariables().size());
		Assertions.assertEquals(2, eap8HelmChartRelease.getInjectedImages().size());
	}

	/**
	 * Verify that the EAP XP 5 adapter for the EAP XP 5values file POJO is properly storing and exposing the adaptee values
	 */
	@Test
	public void verifyEapXp5DynamicallyFilledAdapterTest() {
		// arrange
		HelmXp5Release adaptee = new HelmXp5Release();
		EapXp5HelmChartReleaseAdapter concreteAdapter = new EapXp5HelmChartReleaseAdapter(adaptee);
		EapXp5HelmChartReleaseAdapter eapXp5HelmChartRelease = concreteAdapter;

		SecretVolume secretVolume = new SecretVolume("v1", "s1");
		eapXp5HelmChartRelease
				.withSourceRepositoryUrl("url")
				.withSourceRepositoryRef("ref")
				.withS2iFeaturePacks("fp1,fp2")
				.withS2iGalleonLayers("gl1,gl2")
				.withS2iChannel("ch1")
				.withContextDir("context-dir")
				.withBuildEnabled(Boolean.TRUE)
				.withBuildMode(WildflyHelmChartRelease.BuildMode.BOOTABLE_JAR)
				.withJdk17BuilderImage("jdk17-B")
				.withJdk17RuntimeImage("jdk17-R")
				.withDeployEnabled(Boolean.FALSE)
				.withReplicas(42)
				.withRouteHost("route-host")
				.withVolume(secretVolume.build())
				.withVolumeMount(new VolumeMountBuilder()
						.withName("vm1")
						.withMountPath("mp1")
						.withReadOnly(Boolean.TRUE).build())
				.withRouteTLSEnabled(Boolean.FALSE)
				.withTlsEnabled(Boolean.FALSE)
				.withBuildEnvironmentVariable("a", "1")
				.withDeploymentEnvironmentVariable("b", "2")
				.withInjectedImage(
						new Image(
								new Image.From("ImageStreamTag", "openshift", "myImage:latest"),
								List.of(new Image.Path("mySourcePath", "myDestPath"))))
				.withInjectedImage(
						new Image(
								new Image.From("ImageStreamTag", "openshift", "anotherImage:latest"),
								List.of(new Image.Path("anotherSourcePath", "anotherDestPath"))));

		// act
		final HelmXp5Release actualAdaptee = concreteAdapter.getAdaptee();

		// assert
		Assertions.assertEquals("url", actualAdaptee.getBuild().getUri());
		Assertions.assertEquals("ref", actualAdaptee.getBuild().getRef());
		Assertions.assertEquals("context-dir", actualAdaptee.getBuild().getContextDir());
		Assertions.assertEquals("fp1,fp2", actualAdaptee.getBuild().getS2i().getFeaturePacks());
		Assertions.assertEquals("gl1,gl2", actualAdaptee.getBuild().getS2i().getGalleonLayers());
		Assertions.assertEquals("ch1", actualAdaptee.getBuild().getS2i().getChannels());
		Assertions.assertEquals(Boolean.TRUE, actualAdaptee.getBuild().getEnabled());
		Assertions.assertEquals("jdk17-B", actualAdaptee.getBuild().getS2i().getJdk17().getBuilderImage());
		Assertions.assertEquals("jdk17-R", actualAdaptee.getBuild().getS2i().getJdk17().getRuntimeImage());
		Assertions.assertEquals(org.jboss.intersmash.model.helm.charts.values.xp5.Build.Mode.BOOTABLE_JAR,
				actualAdaptee.getBuild().getMode());
		Assertions.assertEquals(Boolean.FALSE, actualAdaptee.getDeploy().getEnabled());
		Assertions.assertEquals(42, actualAdaptee.getDeploy().getReplicas());
		Assertions.assertEquals("route-host", actualAdaptee.getDeploy().getRoute().getHost());
		Assertions.assertEquals(1, actualAdaptee.getDeploy().getVolumes().size());
		Assertions.assertEquals(1, actualAdaptee.getDeploy().getVolumeMounts().size());
		Assertions.assertEquals(Boolean.FALSE, actualAdaptee.getDeploy().getRoute().getTls().getEnabled());
		Assertions.assertEquals(Boolean.FALSE, actualAdaptee.getDeploy().getTls().getEnabled());
		Assertions.assertEquals(1, actualAdaptee.getBuild().getEnv().size());
		Assertions.assertEquals(1, actualAdaptee.getDeploy().getEnv().size());
		Assertions.assertTrue(List.class.isAssignableFrom(actualAdaptee.getBuild().getImages().getClass()));
		List imagesInstancesList = (List) actualAdaptee.getBuild().getImages();
		Assertions.assertEquals(2, imagesInstancesList.size());
		Assertions.assertTrue(Image.class.isAssignableFrom(imagesInstancesList.get(0).getClass()));
		// round trip check
		Assertions.assertEquals("url", eapXp5HelmChartRelease.getSourceRepositoryUrl());
		Assertions.assertEquals("ref", eapXp5HelmChartRelease.getSourceRepositoryRef());
		Assertions.assertEquals("context-dir", eapXp5HelmChartRelease.getContextDir());
		Assertions.assertEquals("fp1,fp2",
				eapXp5HelmChartRelease.getS2iFeaturePacks().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals("gl1,gl2",
				eapXp5HelmChartRelease.getS2iGalleonLayers().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals("ch1", eapXp5HelmChartRelease.getS2iChannels().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals(Boolean.TRUE, eapXp5HelmChartRelease.isBuildEnabled());
		Assertions.assertEquals(WildflyHelmChartRelease.BuildMode.BOOTABLE_JAR, eapXp5HelmChartRelease.getBuildMode());
		Assertions.assertEquals("jdk17-B", eapXp5HelmChartRelease.getJdk17BuilderImage());
		Assertions.assertEquals("jdk17-R", eapXp5HelmChartRelease.getJdk17RuntimeImage());
		Assertions.assertEquals(Boolean.FALSE, eapXp5HelmChartRelease.isDeployEnabled());
		Assertions.assertEquals(42, eapXp5HelmChartRelease.getReplicas());
		Assertions.assertEquals("route-host", eapXp5HelmChartRelease.getRouteHost());
		Assertions.assertEquals(1, eapXp5HelmChartRelease.getVolumes().size());
		Assertions.assertEquals(1, eapXp5HelmChartRelease.getVolumeMounts().size());
		Assertions.assertEquals(Boolean.FALSE, eapXp5HelmChartRelease.isRouteTLSEnabled());
		Assertions.assertEquals(Boolean.FALSE, eapXp5HelmChartRelease.isTlsEnabled());
		Assertions.assertEquals(1, eapXp5HelmChartRelease.getBuildEnvironmentVariables().size());
		Assertions.assertEquals(1, eapXp5HelmChartRelease.getDeploymentEnvironmentVariables().size());
		Assertions.assertTrue(List.class.isAssignableFrom(actualAdaptee.getBuild().getImages().getClass()));
		Assertions.assertEquals(2, eapXp5HelmChartRelease.getInjectedImages().size());
	}

	/**
	 * Verify that the EAP XP 5 adapter properly sets even the S2I build mdoe value
	 */
	@Test
	public void verifyEapXp5DynamicallyFilledAdapterS2iBuildModeTest() {
		// arrange
		HelmXp5Release adaptee = new HelmXp5Release();
		EapXp5HelmChartReleaseAdapter concreteAdapter = new EapXp5HelmChartReleaseAdapter(adaptee);

		concreteAdapter
				.withBuildMode(WildflyHelmChartRelease.BuildMode.S2I);

		final HelmXp5Release actualAdaptee = concreteAdapter.getAdaptee();

		Assertions.assertEquals(org.jboss.intersmash.model.helm.charts.values.xp5.Build.Mode.S_2_I,
				actualAdaptee.getBuild().getMode());
		Assertions.assertEquals(WildflyHelmChartRelease.BuildMode.S2I, concreteAdapter.getBuildMode());
	}

}
