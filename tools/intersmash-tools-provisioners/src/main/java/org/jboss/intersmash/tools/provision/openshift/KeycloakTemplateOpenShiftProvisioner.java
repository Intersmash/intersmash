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
import org.jboss.intersmash.tools.application.openshift.KeycloakTemplateOpenShiftApplication;
import org.jboss.intersmash.tools.provision.openshift.template.KeycloakTemplateProvisioner;
import org.jboss.intersmash.tools.provision.openshift.template.OpenShiftTemplate;
import org.jboss.intersmash.tools.provision.openshift.template.OpenShiftTemplateProvisioner;
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
public class KeycloakTemplateOpenShiftProvisioner implements OpenShiftProvisioner<KeycloakTemplateOpenShiftApplication> {

	// TODO: what if only OpenShiftApplication is implemented
	private final KeycloakTemplateOpenShiftApplication keycloakApplication;
	private final OpenShiftTemplate keycloakTemplate;
	private List<ImageStream> deployedImageStreams;
	private Template deployedTemplate;

	public KeycloakTemplateOpenShiftProvisioner(@NonNull KeycloakTemplateOpenShiftApplication application) {
		this.keycloakApplication = application;
		this.keycloakTemplate = application.getTemplate();
	}

	@Override
	public KeycloakTemplateOpenShiftApplication getApplication() {
		return keycloakApplication;
	}

	@Override
	public void deploy() {
		deployTemplate();
		if (keycloakApplication.getRealmConfigurationFilePath() != null) {
			importRealmConfigurationFromFile();
		}
	}

	@Override
	public void undeploy() {
		Map<String, String> labels = new HashMap<>(2);
		labels.put("application", keycloakApplication.getName());
		labels.put(APP_LABEL_KEY, keycloakApplication.getName());
		OpenShiftUtils.deleteResourcesWithLabels(openShift, labels);
		// when using geit repo S2I create soe custom maps and buil pods
		openShift.getConfigMaps()
				.stream()
				.filter(cfMap -> cfMap.getMetadata().getName().startsWith(keycloakApplication.getName()))
				.forEach(openShift::deleteConfigMap);
		openShift.getPods()
				.stream()
				.filter(pod -> pod.getMetadata().getName().startsWith(keycloakApplication.getName()))
				.forEach(openShift::deletePod);
		deployedImageStreams.forEach(openShift::deleteImageStream);
		openShift.deleteTemplate(deployedTemplate);
	}

	private void deployTemplate() {
		FailFastCheck failFastCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				keycloakApplication.getName());
		OpenShiftTemplateProvisioner templateProvisioner = new KeycloakTemplateProvisioner();
		deployedImageStreams = templateProvisioner.deployImageStreams();
		deployedTemplate = templateProvisioner.deployTemplate(keycloakTemplate);

		openShift.processAndDeployTemplate(deployedTemplate.getMetadata().getName(),
				keycloakApplication.getParameters());
		// run post deploy scripts before waiting, there is a plenty of time (app building) for openshift to deal with it
		postDeploy(keycloakApplication);

		// Equivalent of oc get route sso-app -o template --template "{{.spec.host}}"
		Route route = openShift.getRoute(keycloakApplication.getHttpsRouteName());
		if (route == null) {
			throw new RuntimeException(String.format("RH-SSO Template \"%s\" doesn't provide an HTTPS Route!",
					keycloakApplication.getTemplate().getLabel()));
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

	private void postDeploy(KeycloakTemplateOpenShiftApplication application) {
		if (IntersmashConfig.scriptDebug() != null) {
			DeploymentConfig dc = openShift.getDeploymentConfig(application.getName());
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
		Map<String, String> parameters = keycloakApplication.getParameters();
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
		final String url = getUrl("secure-" + keycloakApplication.getName(), true) + "/auth/";

		KeycloakAdminClient keycloakAdminClient = null;
		try {
			keycloakAdminClient = new KeycloakAdminClient(url, ssoRealm, ssoUser, ssoPassword);
			try (InputStream is = Files.newInputStream(keycloakApplication.getRealmConfigurationFilePath())) {
				keycloakAdminClient.importRealmConfiguration(is);
			}
		} catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException | IOException e) {
			throw new RuntimeException("Error while importing realm configuration.", e);
		}
	}

	@Override
	public void scale(int replicas, boolean wait) {
		openShift.scale(keycloakApplication.getName(), replicas);
		if (wait) {
			OpenShiftWaiters.get(openShift, () -> false).areExactlyNPodsReady(replicas, keycloakApplication.getName())
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
