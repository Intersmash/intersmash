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

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.intersmash.model.helm.charts.values.eap8.HelmEap8Release;
import org.jboss.intersmash.model.helm.charts.values.eap81.HelmEap81Release;
import org.jboss.intersmash.model.helm.charts.values.eap82.HelmEap82Release;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Build;
import org.jboss.intersmash.model.helm.charts.values.wildfly.HelmWildflyRelease;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Ingress;
import org.jboss.intersmash.model.helm.charts.values.wildfly.Tls;
import org.jboss.intersmash.model.helm.charts.values.xp5.HelmXp5Release;
import org.jboss.intersmash.model.helm.charts.values.xp6.HelmXp6Release;
import org.jboss.intersmash.provision.helm.Image;
import org.jboss.intersmash.provision.helm.wildfly.eap8.Eap8HelmChartReleaseAdapter;
import org.jboss.intersmash.provision.helm.wildfly.eap81.Eap81HelmChartReleaseAdapter;
import org.jboss.intersmash.provision.helm.wildfly.eap82.Eap82HelmChartReleaseAdapter;
import org.jboss.intersmash.provision.helm.wildfly.xp5.EapXp5HelmChartReleaseAdapter;
import org.jboss.intersmash.provision.helm.wildfly.xp6.EapXp6HelmChartReleaseAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
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

		eap8HelmChartRelease
				.withSourceRepositoryUrl("url")
				.withSourceRepositoryRef("ref")
				.withS2iFeaturePacks("fp1,fp2")
				.withS2iGalleonLayers("gl1,gl2")
				.withS2iChannel("ch1")
				.withContextDir("context-dir")
				.withBuildEnabled(Boolean.TRUE)
				.withJdkBuilderImage(new WildflyHelmChartRelease.JdkImage("jdk17-B",
						WildflyHelmChartRelease.JdkImage.Version.JDK_17))
				.withJdkRuntimeImage(new WildflyHelmChartRelease.JdkImage("jdk17-R",
						WildflyHelmChartRelease.JdkImage.Version.JDK_17))
				.withDeployEnabled(Boolean.FALSE)
				.withReplicas(42)
				.withRouteHost("route-host")
				.withVolume(new VolumeBuilder()
						.withName("v1")
						.withSecret(new SecretVolumeSourceBuilder()
								.withSecretName("s1")
								.build())
						.build())
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
		Assertions.assertEquals("jdk17-B", eap8HelmChartRelease.getJdkBuilderImage().getImage());
		Assertions.assertEquals("jdk17-R", eap8HelmChartRelease.getJdkRuntimeImage().getImage());
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
	 * Verify that the EAP 8.1 adapter for the EAP 8.1 values file POJO is properly storing and exposing the adaptee values
	 */
	@Test
	public void verifyEap81DynamicallyFilledAdapterTest() {
		// arrange
		HelmEap81Release adaptee = new HelmEap81Release();
		Eap81HelmChartReleaseAdapter concreteAdapter = new Eap81HelmChartReleaseAdapter(adaptee);
		Eap81HelmChartReleaseAdapter eap81HelmChartRelease = concreteAdapter;

		eap81HelmChartRelease
				.withSourceRepositoryUrl("url")
				.withSourceRepositoryRef("ref")
				.withS2iFeaturePacks("fp1,fp2")
				.withS2iGalleonLayers("gl1,gl2")
				.withS2iChannel("ch1")
				.withContextDir("context-dir")
				.withBuildEnabled(Boolean.TRUE)
				.withJdkBuilderImage(new WildflyHelmChartRelease.JdkImage("jdk17-B",
						WildflyHelmChartRelease.JdkImage.Version.JDK_17))
				.withJdkRuntimeImage(new WildflyHelmChartRelease.JdkImage("jdk17-R",
						WildflyHelmChartRelease.JdkImage.Version.JDK_17))
				.withDeployEnabled(Boolean.FALSE)
				.withReplicas(42)
				.withRouteHost("route-host")
				.withVolume(new VolumeBuilder()
						.withName("v1")
						.withSecret(new SecretVolumeSourceBuilder()
								.withSecretName("s1")
								.build())
						.build())
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
		final HelmEap81Release actualAdaptee = concreteAdapter.getAdaptee();

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
		Assertions.assertEquals("url", eap81HelmChartRelease.getSourceRepositoryUrl());
		Assertions.assertEquals("ref", eap81HelmChartRelease.getSourceRepositoryRef());
		Assertions.assertEquals("context-dir", eap81HelmChartRelease.getContextDir());
		Assertions.assertEquals("fp1,fp2",
				eap81HelmChartRelease.getS2iFeaturePacks().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals("gl1,gl2",
				eap81HelmChartRelease.getS2iGalleonLayers().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals("ch1", eap81HelmChartRelease.getS2iChannels().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals(Boolean.TRUE, eap81HelmChartRelease.isBuildEnabled());
		Assertions.assertEquals("jdk17-B", eap81HelmChartRelease.getJdkBuilderImage().getImage());
		Assertions.assertEquals("jdk17-R", eap81HelmChartRelease.getJdkRuntimeImage().getImage());
		Assertions.assertEquals(Boolean.FALSE, eap81HelmChartRelease.isDeployEnabled());
		Assertions.assertEquals(42, eap81HelmChartRelease.getReplicas());
		Assertions.assertEquals("route-host", eap81HelmChartRelease.getRouteHost());
		Assertions.assertEquals(1, eap81HelmChartRelease.getVolumes().size());
		Assertions.assertEquals(1, eap81HelmChartRelease.getVolumeMounts().size());
		Assertions.assertEquals(Boolean.FALSE, eap81HelmChartRelease.isRouteTLSEnabled());
		Assertions.assertEquals(Boolean.FALSE, eap81HelmChartRelease.isTlsEnabled());
		Assertions.assertEquals(1, eap81HelmChartRelease.getBuildEnvironmentVariables().size());
		Assertions.assertEquals(1, eap81HelmChartRelease.getDeploymentEnvironmentVariables().size());
		Assertions.assertTrue(List.class.isAssignableFrom(actualAdaptee.getBuild().getImages().getClass()));
		Assertions.assertEquals(2, eap81HelmChartRelease.getInjectedImages().size());
	}

	/**
	 * Verify that the EAP 8.2 adapter for the EAP 8.2 values file POJO is properly storing and exposing the adaptee values
	 */
	@Test
	public void verifyEap82DynamicallyFilledAdapterTest() {
		// arrange
		HelmEap82Release adaptee = new HelmEap82Release();
		Eap82HelmChartReleaseAdapter concreteAdapter = new Eap82HelmChartReleaseAdapter(adaptee);
		Eap82HelmChartReleaseAdapter eap82HelmChartRelease = concreteAdapter;

		eap82HelmChartRelease
				.withSourceRepositoryUrl("url")
				.withSourceRepositoryRef("ref")
				.withS2iFeaturePacks("fp1,fp2")
				.withS2iGalleonLayers("gl1,gl2")
				.withS2iChannel("ch1")
				.withContextDir("context-dir")
				.withBuildEnabled(Boolean.TRUE)
				.withJdkBuilderImage(new WildflyHelmChartRelease.JdkImage("jdk25-B",
						WildflyHelmChartRelease.JdkImage.Version.JDK_25))
				.withJdkRuntimeImage(new WildflyHelmChartRelease.JdkImage("jdk25-R",
						WildflyHelmChartRelease.JdkImage.Version.JDK_25))
				.withDeployEnabled(Boolean.FALSE)
				.withReplicas(42)
				.withRouteHost("route-host")
				.withVolume(new VolumeBuilder()
						.withName("v1")
						.withSecret(new SecretVolumeSourceBuilder()
								.withSecretName("s1")
								.build())
						.build())
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
		final HelmEap82Release actualAdaptee = concreteAdapter.getAdaptee();

		// assert
		Assertions.assertEquals("url", actualAdaptee.getBuild().getUri());
		Assertions.assertEquals("ref", actualAdaptee.getBuild().getRef());
		Assertions.assertEquals("context-dir", actualAdaptee.getBuild().getContextDir());
		Assertions.assertEquals("fp1,fp2", actualAdaptee.getBuild().getS2i().getFeaturePacks());
		Assertions.assertEquals("gl1,gl2", actualAdaptee.getBuild().getS2i().getGalleonLayers());
		Assertions.assertEquals("ch1", actualAdaptee.getBuild().getS2i().getChannels());
		Assertions.assertEquals(Boolean.TRUE, actualAdaptee.getBuild().getEnabled());
		Assertions.assertEquals("jdk25-B", actualAdaptee.getBuild().getS2i().getJdk25().getBuilderImage());
		Assertions.assertEquals("jdk25-R", actualAdaptee.getBuild().getS2i().getJdk25().getRuntimeImage());
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
		Assertions.assertEquals("url", eap82HelmChartRelease.getSourceRepositoryUrl());
		Assertions.assertEquals("ref", eap82HelmChartRelease.getSourceRepositoryRef());
		Assertions.assertEquals("context-dir", eap82HelmChartRelease.getContextDir());
		Assertions.assertEquals("fp1,fp2",
				eap82HelmChartRelease.getS2iFeaturePacks().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals("gl1,gl2",
				eap82HelmChartRelease.getS2iGalleonLayers().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals("ch1", eap82HelmChartRelease.getS2iChannels().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals(Boolean.TRUE, eap82HelmChartRelease.isBuildEnabled());
		Assertions.assertEquals("jdk25-B", eap82HelmChartRelease.getJdkBuilderImage().getImage());
		Assertions.assertEquals("jdk25-R", eap82HelmChartRelease.getJdkRuntimeImage().getImage());
		Assertions.assertEquals(Boolean.FALSE, eap82HelmChartRelease.isDeployEnabled());
		Assertions.assertEquals(42, eap82HelmChartRelease.getReplicas());
		Assertions.assertEquals("route-host", eap82HelmChartRelease.getRouteHost());
		Assertions.assertEquals(1, eap82HelmChartRelease.getVolumes().size());
		Assertions.assertEquals(1, eap82HelmChartRelease.getVolumeMounts().size());
		Assertions.assertEquals(Boolean.FALSE, eap82HelmChartRelease.isRouteTLSEnabled());
		Assertions.assertEquals(Boolean.FALSE, eap82HelmChartRelease.isTlsEnabled());
		Assertions.assertEquals(1, eap82HelmChartRelease.getBuildEnvironmentVariables().size());
		Assertions.assertEquals(1, eap82HelmChartRelease.getDeploymentEnvironmentVariables().size());
		Assertions.assertTrue(List.class.isAssignableFrom(actualAdaptee.getBuild().getImages().getClass()));
		Assertions.assertEquals(2, eap82HelmChartRelease.getInjectedImages().size());
	}

	/**
	 * Verify that the WildFly adapter for the WildFly values file POJO is properly storing and exposing the adaptee values
	 */
	@Test
	public void verifyWildFlyDynamicallyFilledAdapterTest() {
		// arrange
		HelmWildflyRelease adaptee = new HelmWildflyRelease();
		WildFlyHelmChartReleaseAdapter concreteAdapter = new WildFlyHelmChartReleaseAdapter(adaptee);
		WildFlyHelmChartReleaseAdapter wildflyHelmChartRelease = concreteAdapter;

		Tls ingressTls = new Tls().withSecret("ingress-tls-secret");
		Ingress ingress = new Ingress()
				.withEnabled(true)
				.withClassName("IngressClassName")
				.withHost("ingress-host")
				.withPath("/ingress-path")
				.withPathType(Ingress.PathType.PREFIX)
				.withTls(ingressTls);
		wildflyHelmChartRelease
				.withSourceRepositoryUrl("url")
				.withSourceRepositoryRef("ref")
				.withS2iFeaturePacks("fp1,fp2")
				.withS2iGalleonLayers("gl1,gl2")
				.withS2iChannel("ch1")
				.withContextDir("context-dir")
				.withBuildEnabled(Boolean.TRUE)
				.withBuildMode(WildflyHelmChartRelease.BuildMode.BOOTABLE_JAR)
				.withJdkBuilderImage(new WildflyHelmChartRelease.JdkImage("jdk17-B",
						WildflyHelmChartRelease.JdkImage.Version.JDK_17))
				.withJdkRuntimeImage(new WildflyHelmChartRelease.JdkImage("jdk17-R",
						WildflyHelmChartRelease.JdkImage.Version.JDK_17))
				.withBootableJarBuilderImage("BJ-I")
				.withDeployEnabled(Boolean.FALSE)
				.withReplicas(42)
				.withRouteHost("route-host")
				.withIngress(ingress)
				.withVolume(new VolumeBuilder()
						.withName("v1")
						.withSecret(new SecretVolumeSourceBuilder()
								.withSecretName("s1")
								.build())
						.build())
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
		Assertions.assertNotNull(actualAdaptee.getDeploy().getIngress());
		Assertions.assertEquals(Boolean.TRUE, actualAdaptee.getDeploy().getIngress().getEnabled());
		Assertions.assertEquals("IngressClassName", actualAdaptee.getDeploy().getIngress().getClassName());
		Assertions.assertEquals("ingress-host", actualAdaptee.getDeploy().getIngress().getHost());
		Assertions.assertEquals("/ingress-path", actualAdaptee.getDeploy().getIngress().getPath());
		Assertions.assertEquals(Ingress.PathType.PREFIX, actualAdaptee.getDeploy().getIngress().getPathType());
		Assertions.assertNotNull(actualAdaptee.getDeploy().getIngress().getTls());
		Assertions.assertEquals("ingress-tls-secret", actualAdaptee.getDeploy().getIngress().getTls().getSecret());
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
		Assertions.assertEquals("url", wildflyHelmChartRelease.getSourceRepositoryUrl());
		Assertions.assertEquals("ref", wildflyHelmChartRelease.getSourceRepositoryRef());
		Assertions.assertEquals("context-dir", wildflyHelmChartRelease.getContextDir());
		Assertions.assertEquals("fp1,fp2",
				wildflyHelmChartRelease.getS2iFeaturePacks().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals("gl1,gl2",
				wildflyHelmChartRelease.getS2iGalleonLayers().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals(Boolean.TRUE, wildflyHelmChartRelease.isBuildEnabled());
		Assertions.assertEquals(WildflyHelmChartRelease.BuildMode.BOOTABLE_JAR, wildflyHelmChartRelease.getBuildMode());
		Assertions.assertEquals("jdk17-B", wildflyHelmChartRelease.getJdkBuilderImage().getImage());
		Assertions.assertEquals("jdk17-R", wildflyHelmChartRelease.getJdkRuntimeImage().getImage());
		Assertions.assertEquals("BJ-I", wildflyHelmChartRelease.getBootableJarBuilderImage());
		Assertions.assertEquals(Boolean.FALSE, wildflyHelmChartRelease.isDeployEnabled());
		Assertions.assertEquals(42, wildflyHelmChartRelease.getReplicas());
		Assertions.assertEquals("route-host", wildflyHelmChartRelease.getRouteHost());
		Assertions.assertNotNull(wildflyHelmChartRelease.getIngress());
		Assertions.assertEquals(Boolean.TRUE, wildflyHelmChartRelease.getIngress().getEnabled());
		Assertions.assertEquals("IngressClassName", wildflyHelmChartRelease.getIngress().getClassName());
		Assertions.assertEquals("ingress-host", wildflyHelmChartRelease.getIngress().getHost());
		Assertions.assertEquals("/ingress-path", wildflyHelmChartRelease.getIngress().getPath());
		Assertions.assertEquals(Ingress.PathType.PREFIX, wildflyHelmChartRelease.getIngress().getPathType());
		Assertions.assertNotNull(wildflyHelmChartRelease.getIngress().getTls());
		Assertions.assertEquals("ingress-tls-secret", wildflyHelmChartRelease.getIngress().getTls().getSecret());
		Assertions.assertEquals(1, wildflyHelmChartRelease.getVolumes().size());
		Assertions.assertEquals(1, wildflyHelmChartRelease.getVolumeMounts().size());
		Assertions.assertEquals(Boolean.FALSE, wildflyHelmChartRelease.isRouteTLSEnabled());
		Assertions.assertEquals(Boolean.FALSE, wildflyHelmChartRelease.isTlsEnabled());
		Assertions.assertEquals(1, wildflyHelmChartRelease.getBuildEnvironmentVariables().size());
		Assertions.assertEquals(1, wildflyHelmChartRelease.getDeploymentEnvironmentVariables().size());
		Assertions.assertEquals(2, wildflyHelmChartRelease.getInjectedImages().size());
	}

	/**
	 * Verify that the EAP XP 5 adapter for the EAP XP 5 values file POJO is properly storing and exposing the adaptee values
	 */
	@Test
	public void verifyEapXp5DynamicallyFilledAdapterTest() {
		// arrange
		HelmXp5Release adaptee = new HelmXp5Release();
		EapXp5HelmChartReleaseAdapter concreteAdapter = new EapXp5HelmChartReleaseAdapter(adaptee);
		EapXp5HelmChartReleaseAdapter eapXp5HelmChartRelease = concreteAdapter;

		eapXp5HelmChartRelease
				.withSourceRepositoryUrl("url")
				.withSourceRepositoryRef("ref")
				.withS2iFeaturePacks("fp1,fp2")
				.withS2iGalleonLayers("gl1,gl2")
				.withS2iChannel("ch1")
				.withContextDir("context-dir")
				.withBuildEnabled(Boolean.TRUE)
				.withBuildMode(WildflyHelmChartRelease.BuildMode.BOOTABLE_JAR)
				.withJdkBuilderImage(new WildflyHelmChartRelease.JdkImage("jdk17-B",
						WildflyHelmChartRelease.JdkImage.Version.JDK_17))
				.withJdkRuntimeImage(new WildflyHelmChartRelease.JdkImage("jdk17-R",
						WildflyHelmChartRelease.JdkImage.Version.JDK_17))
				.withDeployEnabled(Boolean.FALSE)
				.withReplicas(42)
				.withRouteHost("route-host")
				.withVolume(new VolumeBuilder()
						.withName("v1")
						.withSecret(new SecretVolumeSourceBuilder()
								.withSecretName("s1")
								.build())
						.build())
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
		Assertions.assertEquals("jdk17-B", eapXp5HelmChartRelease.getJdkBuilderImage().getImage());
		Assertions.assertEquals("jdk17-R", eapXp5HelmChartRelease.getJdkRuntimeImage().getImage());
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
	 * Verify that the EAP XP 5 adapter properly sets even the S2I build mode value
	 */
	@Test
	public void verifyEapXp5DynamicallyFilledAdapterS2iBuildModeTest() {
		// arrange
		HelmXp5Release adaptee = new HelmXp5Release();
		EapXp5HelmChartReleaseAdapter concreteAdapter = new EapXp5HelmChartReleaseAdapter(adaptee);

		concreteAdapter.withBuildMode(WildflyHelmChartRelease.BuildMode.S2I);

		final HelmXp5Release actualAdaptee = concreteAdapter.getAdaptee();

		Assertions.assertEquals(org.jboss.intersmash.model.helm.charts.values.xp5.Build.Mode.S_2_I,
				actualAdaptee.getBuild().getMode());
		Assertions.assertEquals(WildflyHelmChartRelease.BuildMode.S2I, concreteAdapter.getBuildMode());
	}

	/**
	 * Verify that the EAP XP 6 adapter for the EAP XP 6 values file POJO is properly storing and exposing the adaptee values
	 */
	@Test
	public void verifyEapXp6DynamicallyFilledAdapterTest() {
		// arrange
		HelmXp6Release adaptee = new HelmXp6Release();
		EapXp6HelmChartReleaseAdapter concreteAdapter = new EapXp6HelmChartReleaseAdapter(adaptee);
		EapXp6HelmChartReleaseAdapter eapXp6HelmChartRelease = concreteAdapter;

		eapXp6HelmChartRelease
				.withSourceRepositoryUrl("url")
				.withSourceRepositoryRef("ref")
				.withS2iFeaturePacks("fp1,fp2")
				.withS2iGalleonLayers("gl1,gl2")
				.withS2iChannel("ch1")
				.withContextDir("context-dir")
				.withBuildEnabled(Boolean.TRUE)
				.withBuildMode(WildflyHelmChartRelease.BuildMode.BOOTABLE_JAR)
				.withBuildMode(WildflyHelmChartRelease.BuildMode.BOOTABLE_JAR)
				.withJdkBuilderImage(new WildflyHelmChartRelease.JdkImage("jdk17-B",
						WildflyHelmChartRelease.JdkImage.Version.JDK_17))
				.withJdkRuntimeImage(new WildflyHelmChartRelease.JdkImage("jdk17-R",
						WildflyHelmChartRelease.JdkImage.Version.JDK_17))
				.withDeployEnabled(Boolean.FALSE)
				.withReplicas(42)
				.withRouteHost("route-host")
				.withVolume(new VolumeBuilder()
						.withName("v1")
						.withSecret(new SecretVolumeSourceBuilder()
								.withSecretName("s1")
								.build())
						.build())
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
		final HelmXp6Release actualAdaptee = concreteAdapter.getAdaptee();

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
		Assertions.assertEquals(org.jboss.intersmash.model.helm.charts.values.xp6.Build.Mode.BOOTABLE_JAR,
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
		Assertions.assertEquals("url", eapXp6HelmChartRelease.getSourceRepositoryUrl());
		Assertions.assertEquals("ref", eapXp6HelmChartRelease.getSourceRepositoryRef());
		Assertions.assertEquals("context-dir", eapXp6HelmChartRelease.getContextDir());
		Assertions.assertEquals("fp1,fp2",
				eapXp6HelmChartRelease.getS2iFeaturePacks().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals("gl1,gl2",
				eapXp6HelmChartRelease.getS2iGalleonLayers().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals("ch1", eapXp6HelmChartRelease.getS2iChannels().stream().collect(Collectors.joining(",")));
		Assertions.assertEquals(Boolean.TRUE, eapXp6HelmChartRelease.isBuildEnabled());
		Assertions.assertEquals(WildflyHelmChartRelease.BuildMode.BOOTABLE_JAR, eapXp6HelmChartRelease.getBuildMode());
		Assertions.assertEquals("jdk17-B", eapXp6HelmChartRelease.getJdkBuilderImage().getImage());
		Assertions.assertEquals("jdk17-R", eapXp6HelmChartRelease.getJdkRuntimeImage().getImage());
		Assertions.assertEquals(Boolean.FALSE, eapXp6HelmChartRelease.isDeployEnabled());
		Assertions.assertEquals(42, eapXp6HelmChartRelease.getReplicas());
		Assertions.assertEquals("route-host", eapXp6HelmChartRelease.getRouteHost());
		Assertions.assertEquals(1, eapXp6HelmChartRelease.getVolumes().size());
		Assertions.assertEquals(1, eapXp6HelmChartRelease.getVolumeMounts().size());
		Assertions.assertEquals(Boolean.FALSE, eapXp6HelmChartRelease.isRouteTLSEnabled());
		Assertions.assertEquals(Boolean.FALSE, eapXp6HelmChartRelease.isTlsEnabled());
		Assertions.assertEquals(1, eapXp6HelmChartRelease.getBuildEnvironmentVariables().size());
		Assertions.assertEquals(1, eapXp6HelmChartRelease.getDeploymentEnvironmentVariables().size());
		Assertions.assertTrue(List.class.isAssignableFrom(actualAdaptee.getBuild().getImages().getClass()));
		Assertions.assertEquals(2, eapXp6HelmChartRelease.getInjectedImages().size());
	}

	/**
	 * Verify that the EAP XP 6 adapter properly sets even the S2I build mode value
	 */
	@Test
	public void verifyEapXp6DynamicallyFilledAdapterS2iBuildModeTest() {
		// arrange
		HelmXp6Release adaptee = new HelmXp6Release();
		EapXp6HelmChartReleaseAdapter concreteAdapter = new EapXp6HelmChartReleaseAdapter(adaptee);

		concreteAdapter
				.withBuildMode(WildflyHelmChartRelease.BuildMode.S2I);

		final HelmXp6Release actualAdaptee = concreteAdapter.getAdaptee();

		Assertions.assertEquals(org.jboss.intersmash.model.helm.charts.values.xp6.Build.Mode.S_2_I,
				actualAdaptee.getBuild().getMode());
		Assertions.assertEquals(WildflyHelmChartRelease.BuildMode.S2I, concreteAdapter.getBuildMode());
	}

}
