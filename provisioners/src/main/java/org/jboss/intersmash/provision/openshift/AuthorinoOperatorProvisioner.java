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
package org.jboss.intersmash.provision.openshift;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.openshift.AuthorinoOperatorApplication;
import org.jboss.intersmash.provision.openshift.operator.OperatorProvisioner;
import org.jboss.intersmash.provision.openshift.operator.authorino.AuthConfigList;
import org.jboss.intersmash.provision.openshift.operator.authorino.AuthorinoList;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.kuadrant.authorino.operator.v1beta1.Authorino;
import io.kuadrant.authorino.v1beta2.AuthConfig;
import lombok.NonNull;

/**
 * Authorino Operator based provisioner
 *
 * See: <br>
 * - https://github.com/Kuadrant/authorino/blob/main/docs/architecture.md<br>
 * - https://github.com/Kuadrant/authorino-operator
 */
public class AuthorinoOperatorProvisioner extends OperatorProvisioner<AuthorinoOperatorApplication> {
	private final static String AUTHORINO_RESOURCE = "authorinos.operator.authorino.kuadrant.io";
	private static NonNamespaceOperation<Authorino, AuthorinoList, Resource<Authorino>> AUTHORINOS_CLIENT;

	private final static String AUTHCONFIG_RESOURCE = "authconfigs.authorino.kuadrant.io";
	private static NonNamespaceOperation<AuthConfig, AuthConfigList, Resource<AuthConfig>> AUTHCONFIGS_CLIENT;

	private static final String OPERATOR_ID = IntersmashConfig.authorinoOperatorPackageManifest();

	public AuthorinoOperatorProvisioner(@NonNull AuthorinoOperatorApplication application) {
		super(application, OPERATOR_ID);
	}

	public static String getOperatorId() {
		return OPERATOR_ID;
	}

	/**
	 * Get a client capable of working with {@link #AUTHORINO_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #AUTHORINO_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<Authorino, AuthorinoList, Resource<Authorino>> authorinosClient() {
		if (AUTHORINOS_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(AUTHORINO_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(AUTHORINO_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						AUTHORINO_RESOURCE, OPERATOR_ID));
			}
			MixedOperation<Authorino, AuthorinoList, Resource<Authorino>> client = OpenShifts
					.master().newHasMetadataOperation(crdc, Authorino.class, AuthorinoList.class);
			AUTHORINOS_CLIENT = client.inNamespace(OpenShiftConfig.namespace());
		}
		return AUTHORINOS_CLIENT;
	}

	/**
	 * Get a reference to an {@link Authorino} custom resource. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @return A concrete {@link Resource} instance representing the {@link Authorino} resource definition
	 */
	public Resource<Authorino> authorino() {
		return authorinosClient().withName(getApplication().getAuthorino().getMetadata().getName());
	}

	/**
	 * Get a client capable of working with {@link #AUTHCONFIG_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #AUTHCONFIG_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<AuthConfig, AuthConfigList, Resource<AuthConfig>> authConfigsClient() {
		if (AUTHCONFIGS_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(AUTHCONFIG_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(AUTHCONFIG_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						AUTHCONFIG_RESOURCE, OPERATOR_ID));
			}
			MixedOperation<AuthConfig, AuthConfigList, Resource<AuthConfig>> amqClient = OpenShifts
					.master().newHasMetadataOperation(crdc, AuthConfig.class, AuthConfigList.class);
			AUTHCONFIGS_CLIENT = amqClient.inNamespace(OpenShiftConfig.namespace());
		}
		return AUTHCONFIGS_CLIENT;
	}

	/**
	 * Get all {@link AuthConfig} custom resource instances maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the CRs, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link AuthConfig} resource definitions
	 */
	public List<Resource<AuthConfig>> authConfigs() {
		AuthorinoOperatorApplication application = getApplication();
		return application.getAuthConfigs().stream()
				.map(a -> a.getMetadata().getName())
				.map(this::authConfig)
				.collect(Collectors.toList());
	}

	/**
	 * Get a reference to a {@link AuthConfig} custom resource object.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 *
	 * @param name name of the {@link AuthConfig} custom resource
	 * @return A concrete {@link Resource} instance representing the {@link AuthConfig} resource definition
	 */
	public Resource<AuthConfig> authConfig(final String name) {
		return authConfigsClient().withName(name);
	}

	@Override
	public void deploy() {
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				getApplication().getName());
		subscribe();
		final int replicas = getApplication().getAuthorino().getSpec().getReplicas();
		// deploy the authorino CR
		authorinosClient().createOrReplace(getApplication().getAuthorino());
		// deploy AuthConfig CR(s)
		getApplication().getAuthConfigs().stream()
				.forEach(authConfig -> authConfigsClient().createOrReplace(authConfig));
		// wait for authorino instance to be created...
		new SimpleWaiter(() -> authorino().get() != null)
				.failFast(ffCheck)
				.level(Level.DEBUG)
				.waitFor();
		// and then ready
		OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck).areExactlyNPodsReady(replicas,
				authorino().get().getKind(), getApplication().getAuthorino().getMetadata().getName())
				.level(Level.DEBUG)
				.waitFor();
		// wait for AuthConfig CR(s) to be created
		authConfigs()
				.forEach(authConfig -> new SimpleWaiter(() -> authConfig.get() != null).level(Level.DEBUG).waitFor());
	}

	@Override
	public void undeploy() {
		// delete the AuthConfig CR(s)
		authConfigs().forEach(authConfig -> authConfig.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());
		// delete the Authorino CR, first scaling it down to 0 (graceful shutdown)
		this.scale(0, Boolean.TRUE);
		authorino().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		// wait
		new SimpleWaiter(() -> authorinosClient().list().getItems().size() == 0).failFast(ffCheck).level(Level.DEBUG)
				.waitFor();
		authConfigs().forEach(
				authConfig -> new SimpleWaiter(() -> authConfig.get() == null).level(Level.DEBUG).failFast(ffCheck).waitFor());
		// unsubscribe
		unsubscribe();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		Authorino authorino = authorino().get();
		authorino.getSpec().setReplicas(replicas);
		authorino().replace(authorino);
		if (wait) {
			OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
					.areExactlyNPodsReady(replicas, authorino.getKind(), authorino.getMetadata().getName())
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	/**
	 * Get the provisioned application service related Pods
	 * <p>
	 * Currently blocked by the fact that Pod Status pod names do not reflect the reality
	 * <p>
	 *
	 * @return A list of related {@link Pod} instances
	 */
	@Override
	public List<Pod> getPods() {
		return OpenShiftProvisioner.openShift.getLabeledPods("authorino-resource", getApplication().getName());
	}

	@Override
	protected String getOperatorCatalogSource() {
		return IntersmashConfig.authorinoOperatorCatalogSource();
	}

	@Override
	protected String getOperatorIndexImage() {
		return IntersmashConfig.authorinoOperatorIndexImage();
	}

	@Override
	protected String getOperatorChannel() {
		return IntersmashConfig.authorinoOperatorChannel();
	}

	@Override
	protected void waitForOperatorPod() {
		String[] operatorSpecs = getAdminBinary().execute("get", "csvs", getCurrentCSV(), "-o", "template", "--template",
				"{{range .spec.install.spec.deployments}}{{if .spec.replicas }}{{printf \"%d|%s\\n\" .spec.replicas .name}}{{end}}{{end}}")
				.split(System.lineSeparator());
		for (String spec : operatorSpecs) {
			String[] operatorSpec = spec.split("\\|");
			if (operatorSpec.length != 2) {
				throw new RuntimeException("Failed to get operator deployment spec from csvs!");
			}
			new SimpleWaiter(() -> OpenShiftProvisioner.openShift.getPods().stream().filter(
					pod -> (pod.getMetadata()
							.getName()
							.startsWith(operatorSpec[1])
							&& pod.getStatus().getPhase().equalsIgnoreCase("Running")))
					.count() == Integer.valueOf(operatorSpec[0]))
					.failFast(ffCheck)
					.reason("Wait for expected number of replicas to be active.")
					.level(Level.DEBUG)
					.waitFor();
		}
	}
}
