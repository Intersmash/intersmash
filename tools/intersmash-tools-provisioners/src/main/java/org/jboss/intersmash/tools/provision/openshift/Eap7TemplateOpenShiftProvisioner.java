/**
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
package org.jboss.intersmash.tools.provision.openshift;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.Eap7TemplateOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.WildflyOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.template.Eap7Template;
import org.jboss.intersmash.tools.provision.openshift.template.Eap7TemplateProvisioner;
import org.jboss.intersmash.tools.provision.openshift.template.OpenShiftTemplate;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.Template;
import lombok.NonNull;

public class Eap7TemplateOpenShiftProvisioner
		implements OpenShiftProvisioner<Eap7TemplateOpenShiftApplication> {
	private final Eap7TemplateOpenShiftApplication application;
	private final OpenShiftTemplate template;
	private List<ImageStream> deployedImageStreams;
	private Template deployedTemplate;
	private FailFastCheck ffCheck = () -> false;

	public Eap7TemplateOpenShiftProvisioner(@NonNull Eap7TemplateOpenShiftApplication application) {
		this.application = application;
		this.template = application.getTemplate();
	}

	@Override
	public Eap7TemplateOpenShiftApplication getApplication() {
		return application;
	}

	@Override
	public void deploy() {
		deployTemplate();
	}

	@Override
	public void undeploy() {
		Map<String, String> labels = new HashMap<>(2);
		labels.put("application", application.getName());
		labels.put(APP_LABEL_KEY, application.getName());
		OpenShiftUtils.deleteResourcesWithLabels(openShift, labels);
		// when using geit repo S2I create soe custom maps and build pods
		openShift.getConfigMaps()
				.stream()
				.filter(cfMap -> cfMap.getMetadata().getName().startsWith(application.getName()))
				.forEach(openShift::deleteConfigMap);
		openShift.getPods()
				.stream()
				.filter(pod -> pod.getMetadata().getName().startsWith(application.getName()))
				.forEach(openShift::deletePod);
		deployedImageStreams.forEach(openShift::deleteImageStream);
		openShift.deleteTemplate(deployedTemplate);
	}

	@Override
	public void scale(int replicas, boolean wait) {
		openShift.scale(application.getName(), replicas);
		if (wait) {
			waitForReplicas(replicas);
		}
	}

	public void waitForReplicas(int replicas) {
		OpenShiftWaiters.get(openShift, ffCheck).areExactlyNPodsReady(replicas, application.getName()).level(Level.DEBUG)
				.waitFor();
		WaitersUtil.serviceEndpointsAreReady(openShift, getApplication().getName(), replicas, 8080)
				.level(Level.DEBUG)
				.waitFor();
		if (replicas > 0) {
			WaitersUtil.routeIsUp(getUrl(application.getName(), false))
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	private void deployTemplate() {
		// create/update image stream
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				application.getName());
		Eap7TemplateProvisioner templateProvisioner = new Eap7TemplateProvisioner();
		deployedImageStreams = templateProvisioner.deployImageStreams();
		deployedTemplate = templateProvisioner.deployTemplate(template);

		// env vars
		Map<String, String> params = new HashMap<>(application.getParameters());

		// Here we'll set the expected template EAP_IMAGE_URL and EAP_RUNTIME_IMAGE_URL param
		// values explicitly, based on the previously deployed resources, in case those were not provided.
		if (!params.containsKey("EAP_IMAGE_NAME")) {
			// let's build a consistent value for the expected builder image name
			Optional<String> deployedBuilderImageNameSearch = deployedImageStreams.stream()
					.filter(is -> !is.getMetadata().getName().contains("runtime"))
					.findAny()
					.map((i) -> String.format("%s:%s", i.getMetadata().getName(),
							i.getMetadata().getAnnotations().get("version")));
			if (!deployedBuilderImageNameSearch.isPresent()) {
				throw new IllegalStateException(String.format(
						"The expected EAP 7 builder image stream was not found in the %s namespace", openShift.getNamespace()));
			}
			params.put("EAP_IMAGE_NAME", deployedBuilderImageNameSearch.get());
		}
		if (!params.containsKey("EAP_RUNTIME_IMAGE_NAME")) {
			// and for the runtime one as well
			Optional<String> deployedRuntimeImageName = deployedImageStreams.stream()
					.filter(is -> is.getMetadata().getName().contains("runtime"))
					.map((i) -> String.format("%s:%s", i.getMetadata().getName(),
							i.getMetadata().getAnnotations().get("version")))
					.findAny();
			if (!deployedRuntimeImageName.isPresent()) {
				throw new IllegalStateException(String.format(
						"The expected EAP 7 runtime image stream was not found in the %s namespace", openShift.getNamespace()));
			}
			params.put("EAP_RUNTIME_IMAGE_NAME", deployedRuntimeImageName.get());
		}

		// map the application configuration
		if (!params.containsKey("IMAGE_STREAM_NAMESPACE"))
			params.put("IMAGE_STREAM_NAMESPACE", OpenShiftConfig.namespace());
		if (!params.containsKey("APPLICATION_NAME"))
			params.put("APPLICATION_NAME", application.getName());

		if (!params.containsKey("SSO_IMAGE_NAME") && template.equals(Eap7Template.SSO)) {
			params.put("SSO_IMAGE_NAME", IntersmashConfig.rhSsoImageName());
		}

		// setup context dir
		if (!params.containsKey("CONTEXT_DIR")) {
			params.put("CONTEXT_DIR", "");
		}

		openShift.processAndDeployTemplate(deployedTemplate.getMetadata().getName(), params);
		// run post deploy scripts before waiting, there is a plenty of time (app building) for openshift to deal with it
		postDeploy(application);

		OpenShiftWaiters.get(openShift, ffCheck).isDcReady(application.getName()).level(Level.DEBUG).waitFor();
		// TODO - this is still risky as a template might change or get parametrized
		waitForReplicas(1);
	}

	private void postDeploy(WildflyOpenShiftApplication eapApplication) {
		if (IntersmashConfig.scriptDebug() != null || template.equals(Eap7Template.SSO)) {
			DeploymentConfig dc = openShift.getDeploymentConfig(eapApplication.getName());
			if (IntersmashConfig.scriptDebug() != null) {
				dc.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv()
						.add(new EnvVarBuilder().withName(SCRIPT_DEBUG).withValue(IntersmashConfig.scriptDebug()).build());
			}
			openShift.deploymentConfigs().createOrReplace(dc);
		}

		// setup cliScript;
		if (eapApplication.getCliScript() != null && !eapApplication.getCliScript().isEmpty()) {
			// mount postconfigure CLI commands
			String postconfigure = "#!/usr/bin/env bash\n"
					+ "echo \"Executing postconfigure.sh\"\n"
					+ "$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/extensions/configure.cli\n";
			ConfigMap cfMap = new ConfigMapBuilder().withNewMetadata()
					.withName("jboss-cli")
					.withLabels(Collections.singletonMap(APP_LABEL_KEY, eapApplication.getName()))
					.endMetadata().addToData("postconfigure.sh", postconfigure)
					.addToData("configure.cli", String.join("\n", eapApplication.getCliScript()))
					.build();
			openShift.createConfigMap(cfMap);
			// TODO make it JAVA https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.3/html-single/getting_started_with_jboss_eap_for_openshift_container_platform/index#custom_scripts

			String output = OpenShifts.masterBinary().execute("set", "volume", "dc/" + eapApplication.getName(),
					"--add", "--name=jboss-cli", "-m", "/opt/eap/extensions", "-t", "configmap", "--configmap-name=jboss-cli",
					"--default-mode=0755");
			// output is null in case of failure, see ERROR logs
			if (output == null) {
				throw new RuntimeException(
						"Failed to mount CLI custom script to deployment config. See logs for more details.");
			}
		}
	}

	@Override
	public List<Pod> getPods() {
		return openShift.getPods(getApplication().getName());
	}
}
