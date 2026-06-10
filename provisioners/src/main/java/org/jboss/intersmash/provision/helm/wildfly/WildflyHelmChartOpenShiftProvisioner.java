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
package org.jboss.intersmash.provision.helm.wildfly;

import org.jboss.intersmash.application.openshift.helm.WildflyHelmChartOpenShiftApplication;
import org.jboss.intersmash.provision.helm.HelmChartOpenShiftProvisioner;
import org.jboss.intersmash.provision.openshift.WaitersUtil;
import org.jboss.intersmash.tools.client.OpenShifts;
import org.jboss.intersmash.util.openshift.WildflyOpenShiftUtils;
import org.slf4j.event.Level;

import io.fabric8.openshift.api.model.ImageStreamBuilder;
import io.fabric8.openshift.api.model.TagReferenceBuilder;
import lombok.NonNull;

/**
 * An OpenShift provisioner that can provision WildFly applications through Wildfly Helm Charts.
 */
public class WildflyHelmChartOpenShiftProvisioner extends HelmChartOpenShiftProvisioner<WildflyHelmChartOpenShiftApplication> {

	private static final String RUNTIME_IMAGE_STREAM_TAG = "latest";
	private static final String BUILDER_IMAGE_STREAM_TAG = "latest";

	public WildflyHelmChartOpenShiftProvisioner(@NonNull WildflyHelmChartOpenShiftApplication application) {
		super(application);
	}

	@Override
	public void preDeploy() {
		super.preDeploy();
		WildflyOpenShiftUtils.createImageStream(this.getApplication().getBuilderImage(), computeBuilderImageStreamName(),
				BUILDER_IMAGE_STREAM_TAG);
		WildflyOpenShiftUtils.createImageStream(this.getApplication().getRuntimeImage(), computeRuntimeImageStreamName(),
				RUNTIME_IMAGE_STREAM_TAG);
	}

	private String computeBuilderImageStreamName() {
		return String.format("wildfly-openshift-%s", this.getApplication().getName());
	}

	private String computeRuntimeImageStreamName() {
		return String.format("wildfly-runtime-openshift-%s", this.getApplication().getName());
	}

	@Override
	public void postUndeploy() {
		OpenShifts.master().deleteImageStream(
				new ImageStreamBuilder()
						.withNewMetadata().withName(computeRuntimeImageStreamName())
						.addToAnnotations("openshift.io/image.insecureRepository", "true")
						.endMetadata()
						.withNewSpec()
						.withTags(new TagReferenceBuilder()
								.withName(RUNTIME_IMAGE_STREAM_TAG)
								.withNewImportPolicy().withInsecure(true).endImportPolicy()
								.withNewFrom().withKind("DockerImage")
								.withName(this.getApplication().getRuntimeImage()).endFrom()
								.build())
						.endSpec()
						.build());
		OpenShifts.master().deleteImageStream(
				new ImageStreamBuilder()
						.withNewMetadata().withName(computeBuilderImageStreamName())
						.addToAnnotations("openshift.io/image.insecureRepository", "true")
						.endMetadata()
						.withNewSpec()
						.withTags(new TagReferenceBuilder()
								.withName(BUILDER_IMAGE_STREAM_TAG)
								.withNewImportPolicy().withInsecure(true).endImportPolicy()
								.withNewFrom().withKind("DockerImage")
								.withName(this.getApplication().getBuilderImage()).endFrom()
								.build())
						.endSpec()
						.build());
		super.postUndeploy();
	}

	@Override
	protected void waitForReplicas(int replicas) {
		super.waitForReplicas(replicas);
		WaitersUtil.serviceEndpointsAreReady(openShift, getApplication().getName(), replicas, 8080)
				.level(Level.DEBUG)
				.waitFor();
		if (replicas > 0) {
			WaitersUtil.routeIsUp(getUrl(application.getName(), false))
					.level(Level.DEBUG)
					.waitFor();
		}
	}
}
