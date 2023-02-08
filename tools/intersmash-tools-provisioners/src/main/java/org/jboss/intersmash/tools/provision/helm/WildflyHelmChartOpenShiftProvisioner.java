package org.jboss.intersmash.tools.provision.helm;

import org.jboss.intersmash.tools.application.openshift.helm.WildflyHelmChartOpenShiftApplication;
import org.jboss.intersmash.tools.provision.openshift.WaitersUtil;
import org.jboss.intersmash.tools.util.openshift.WildflyOpenShiftUtils;
import org.slf4j.event.Level;

import cz.xtf.core.image.Image;
import cz.xtf.core.openshift.OpenShifts;
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
				Image.from(this.getApplication().getRuntimeImage()).getImageStream(computeRuntimeImageStreamName(),
						RUNTIME_IMAGE_STREAM_TAG));
		OpenShifts.master().deleteImageStream(
				Image.from(this.getApplication().getBuilderImage()).getImageStream(computeBuilderImageStreamName(),
						BUILDER_IMAGE_STREAM_TAG));
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
