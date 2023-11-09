package org.jboss.intersmash.tools.provision.openshift;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.RhSsoTemplateOpenShiftApplication;
import org.jboss.intersmash.tools.provision.openshift.template.OpenShiftTemplate;
import org.jboss.intersmash.tools.provision.openshift.template.OpenShiftTemplateProvisioner;
import org.jboss.intersmash.tools.provision.openshift.template.RhSsoTemplateProvisioner;
import org.jboss.intersmash.tools.util.keycloak.KeycloakAdminClient;
import org.slf4j.event.Level;

import cz.xtf.client.Http;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.Template;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RhSsoTemplateOpenShiftProvisioner implements OpenShiftProvisioner<RhSsoTemplateOpenShiftApplication> {

	// TODO: what if only OpenShiftApplication is implemented
	private final RhSsoTemplateOpenShiftApplication rhSsoApplication;
	private final OpenShiftTemplate rhSsoTemplate;
	private List<ImageStream> deployedImageStreams;
	private Template deployedTemplate;

	public RhSsoTemplateOpenShiftProvisioner(@NonNull RhSsoTemplateOpenShiftApplication rhSsoApplication) {
		this.rhSsoApplication = rhSsoApplication;
		this.rhSsoTemplate = rhSsoApplication.getTemplate();
	}

	@Override
	public RhSsoTemplateOpenShiftApplication getApplication() {
		return rhSsoApplication;
	}

	@Override
	public void deploy() {
		deployTemplate();
		if (rhSsoApplication.getRealmConfigurationFilePath() != null) {
			importRealmConfigurationFromFile();
		}
	}

	@Override
	public void undeploy() {
		Map<String, String> labels = new HashMap<>(2);
		labels.put("application", rhSsoApplication.getName());
		labels.put(APP_LABEL_KEY, rhSsoApplication.getName());
		OpenShiftUtils.deleteResourcesWithLabels(openShift, labels);
		// when using geit repo S2I create soe custom maps and buil pods
		openShift.getConfigMaps()
				.stream()
				.filter(cfMap -> cfMap.getMetadata().getName().startsWith(rhSsoApplication.getName()))
				.forEach(openShift::deleteConfigMap);
		openShift.getPods()
				.stream()
				.filter(pod -> pod.getMetadata().getName().startsWith(rhSsoApplication.getName()))
				.forEach(openShift::deletePod);
		deployedImageStreams.forEach(openShift::deleteImageStream);
		openShift.deleteTemplate(deployedTemplate);
	}

	private void deployTemplate() {
		FailFastCheck failFastCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				rhSsoApplication.getName());
		OpenShiftTemplateProvisioner templateProvisioner = new RhSsoTemplateProvisioner();
		deployedImageStreams = templateProvisioner.deployImageStreams();
		deployedTemplate = templateProvisioner.deployTemplate(rhSsoTemplate);

		openShift.processAndDeployTemplate(deployedTemplate.getMetadata().getName(),
				rhSsoApplication.getParameters());
		// run post deploy scripts before waiting, there is a plenty of time (app building) for openshift to deal with it
		postDeploy(rhSsoApplication);

		// Equivalent of oc get route sso-app -o template --template "{{.spec.host}}"
		Route route = openShift.getRoute(rhSsoApplication.getHttpsRouteName());
		if (route == null) {
			throw new RuntimeException(String.format("RH-SSO Template \"%s\" doesn't provide an HTTPS Route!",
					rhSsoApplication.getTemplate().getLabel()));
		}
		String url = "https://" + route.getSpec().getHost() + "/auth/";
		try {
			Http.get(url)
					.trustAll()
					.waiters()
					.ok()
					.failFast(failFastCheck)
					.level(Level.DEBUG)
					.waitFor();
		} catch (MalformedURLException e) {
			throw new RuntimeException(String.format("RH-SSO secure host name url \"%s\" is malformed", url), e);
		}
	}

	private void postDeploy(RhSsoTemplateOpenShiftApplication rhSsoApplication) {
		if (IntersmashConfig.scriptDebug() != null) {
			DeploymentConfig dc = openShift.getDeploymentConfig(rhSsoApplication.getName());
			dc.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv()
					.add(new EnvVarBuilder().withName(SCRIPT_DEBUG).withValue(IntersmashConfig.scriptDebug()).build());
			openShift.deploymentConfigs().createOrReplace(dc);
		}
	}

	private void importRealmConfigurationFromFile() {
		log.debug("Realm configuration is specified, importing it.");
		final String ssoRealmParameter = "SSO_REALM";
		final String ssoServiceUsernameParameter = "SSO_SERVICE_USERNAME";
		final String ssoServicePasswordParameter = "SSO_SERVICE_PASSWORD";

		// validate all required parameters are set
		Map<String, String> parameters = rhSsoApplication.getParameters();
		List.of(ssoRealmParameter, ssoServiceUsernameParameter, ssoServicePasswordParameter)
				.forEach(expected -> {
					if (parameters.get(expected) == null || parameters.get(expected).isBlank()) {
						throw new IllegalStateException(
								"Realm configuration is specified but required template parameter" + expected
										+ " isn't.");
					}
				});

		final String ssoRealm = parameters.get(ssoRealmParameter);
		final String ssoUser = parameters.get(ssoServiceUsernameParameter);
		final String ssoPassword = parameters.get(ssoServicePasswordParameter);
		final String url = getUrl("secure-" + rhSsoApplication.getName(), true) + "/auth/";

		KeycloakAdminClient rhSsoAdminClient = null;
		try {
			rhSsoAdminClient = new KeycloakAdminClient(url, ssoRealm, ssoUser, ssoPassword);
			try (InputStream is = Files.newInputStream(rhSsoApplication.getRealmConfigurationFilePath())) {
				rhSsoAdminClient.importRealmConfiguration(is);
			}
		} catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException | IOException e) {
			throw new RuntimeException("Error while importing realm configuration.", e);
		}
	}

	@Override
	public void scale(int replicas, boolean wait) {
		openShift.scale(rhSsoApplication.getName(), replicas);
		if (wait) {
			OpenShiftWaiters.get(openShift, () -> false).areExactlyNPodsReady(replicas, rhSsoApplication.getName())
					.level(Level.DEBUG).waitFor();
		}
	}

	@Override
	public List<Pod> getPods() {
		return openShift.getPods(getApplication().getName());
	}

	@Override
	public String getUrl(String routeName, boolean secure) {
		String protocol = secure ? "https" : "http";
		return protocol + "://" + openShift.generateHostname(routeName);
	}
}
