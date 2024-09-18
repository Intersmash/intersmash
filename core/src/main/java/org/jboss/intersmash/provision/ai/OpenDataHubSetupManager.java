package org.jboss.intersmash.provision.ai;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.provision.openshift.operator.resources.OperatorGroup;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.builder.Visitor;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenDataHubSetupManager {

	private static final String GLOBAL_OPERATOR_GROUP_NAMESPACE_DEFAULT = "intersmash-odh-dedicated";
	private static OpenDataHubSetupManager instance;
	private final OpenShift openShift;
	/**
	 * A "meta" provisioner that can deploy the components that are needed by Open Data Hub, like Authorino,
	 * Service Mesh, etc.
	 *
	 * @return The {@link OpenDataHubPrerequisitesProvisioner} instance that will handle the provisioning of
	 * ODH prerequisites, see https://github.com/opendatahub-io/opendatahub-operator#prerequisites
	 */
	private OpenDataHubPrerequisitesProvisioner requiredComponentsProvisioner = null;

	public static OpenDataHubSetupManager getInstance() throws IOException {
		if (instance == null) {
			synchronized (OpenDataHubSetupManager.class) {
				if (instance == null) {
					instance = new OpenDataHubSetupManager();
				}
			}
		}
		return instance;
	}

	private OpenDataHubSetupManager() {
		// If it does not exist, let's create a dedicated namespace/project for Intersmash to manage the ODH setup
		this.openShift = OpenShifts.master(GLOBAL_OPERATOR_GROUP_NAMESPACE_DEFAULT);
		if (openShift.getProject(openShift.getNamespace()) == null) {
			openShift.createProjectRequest();
			openShift.waiters().isProjectReady().waitFor();
			// and label it as managed by Intersmash ODH
			configureNamespaceLabel("intersmash-odh/ready", "false");
			configureNamespaceLabel("intersmash-odh/installing", "false");
		}
		// configure a pull secret, which would be used when pulling from secured registries
		if (OpenShiftConfig.pullSecret() != null) {
			openShift.setupPullSecret(OpenShiftConfig.pullSecret());
		}
		openShift.addRoleToGroup("system:image-puller", "ClusterRole", "system:authenticated");
	}

	public OpenDataHubPrerequisitesProvisioner getRequiredComponentsProvisioner() {
		if (requiredComponentsProvisioner == null) {
			requiredComponentsProvisioner = lookUpProvisioner();
		}
		return requiredComponentsProvisioner;
	}

	public void setup() throws IOException {
		configureNamespaceLabel("intersmash-odh/installing", "true");
		try {
			// Create an OperatorGroup resource that will group the global CSVs needed as Open Data Hub required
			// components, e.g.: Authorino, Service Mesh etc. They will all be in the project created by this singleton
			OpenShifts.adminBinary().execute("apply", "-f",
					OperatorGroup.ALL_NAMESPACES.save().getAbsolutePath());
			// use the pluggable provisioner implementation to deploy ODH prerequisites
			OpenDataHubPrerequisitesProvisioner provisioner = getRequiredComponentsProvisioner();
			log.info("Deploying ODH prerequisites via: {}", provisioner.getClass().getName());
			provisioner.configure();
			provisioner.preDeploy();
			provisioner.deploy();
			// and label as ready
			configureNamespaceLabel("intersmash-odh/ready", "true");
		} catch (Exception e) {
			configureNamespaceLabel("intersmash-odh/ready", "false");
		} finally {
			configureNamespaceLabel("intersmash-odh/installing", "false");
		}
	}

	private void configureNamespaceLabel(final String name, final String value) {
		// Adding a label can be only done via 'namespace'. It cannot be set via 'project' API. Thus we do this
		// separately. Also, to update namespace label, it's necessary to have 'patch resource "namespaces"'
		// permission for current user and updated namespace, e.g. by having 'cluster-admin' role.
		// Otherwise you can see:
		// $ oc label namespace <name> "label1=foo"
		// Error from server (Forbidden): namespaces "<name>" is forbidden: User "<user>" cannot patch resource "namespaces" in API group "" in the namespace "<name>"
		try {
			OpenShifts.admin(openShift.getNamespace()).namespaces().withName(openShift.getNamespace())
					.edit(new Visitor<NamespaceBuilder>() {
						@Override
						public void visit(NamespaceBuilder builder) {
							builder.editMetadata()
									.addToLabels(name, value);
						}
					});
		} catch (KubernetesClientException e) {
			throw new IllegalArgumentException("Couldn't assign required labels the new project '"
					+ openShift.getNamespace() + "'. Possible cause are insufficient permissions.");
		}
	}

	public void tearDown() {
		configureNamespaceLabel("intersmash-odh/installing", "true");
		try {
			// label as no longer ready
			configureNamespaceLabel("intersmash-odh/ready", "false");
			// tear down
			OpenDataHubPrerequisitesProvisioner provisioner = getRequiredComponentsProvisioner();
			log.info("Undeploying ODH prerequisites via: {}", provisioner.getClass().getName());
			provisioner.undeploy();
			provisioner.postUndeploy();
			provisioner.dismiss();
			// remove the global operator group related resources
			OpenShifts.adminBinary().execute("delete", "subscription", "--all");
			OpenShifts.adminBinary().execute("delete", "csvs", "--all");
			OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
		} finally {
			configureNamespaceLabel("intersmash-odh/installing", "false");
		}
	}

	public void subscribe(final String namespace) {
		try {
			// Adding a label can be only done via 'namespace'. It cannot be set via 'project' API. Thus we do this
			// separately. Also, to update namespace label, it's necessary to have 'patch resource "namespaces"'
			// permission for current user and updated namespace, e.g. by having 'cluster-admin' role.
			// Otherwise you can see:
			// $ oc label namespace <name> "label1=foo"
			// Error from server (Forbidden): namespaces "<name>" is forbidden: User "<user>" cannot patch resource "namespaces" in API group "" in the namespace "<name>"
			OpenShifts.admin(openShift.getNamespace()).namespaces().withName(openShift.getNamespace())
					.edit(new Visitor<NamespaceBuilder>() {
						@Override
						public void visit(NamespaceBuilder builder) {
							builder.editMetadata()
									.addToLabels(String.format("intersmash-odh-subscriber/%s", namespace),
											String.valueOf(System.currentTimeMillis()));
						}
					});
		} catch (KubernetesClientException e) {
			throw new IllegalArgumentException("Couldn't add a subscription label the project '"
					+ openShift.getNamespace() + "'. Possible cause are insufficient permissions.");
		}
	}

	public List<String> getSubscribers() {
		return OpenShifts.admin(openShift.getNamespace()).namespaces().withName(openShift.getNamespace()).get().getMetadata()
				.getLabels().entrySet()
				.stream()
				.filter(e -> e.getKey().startsWith("intersmash-odh-subscriber/"))
				.map(e -> e.getKey().substring("intersmash-odh-subscriber/".length())).collect(Collectors.toList());
	}

	public Boolean isReady() {
		return Boolean.valueOf(OpenShifts.admin(openShift.getNamespace()).namespaces().withName(openShift.getNamespace()).get()
				.getMetadata().getLabels().entrySet()
				.stream()
				.filter(e -> "intersmash-odh/ready".equals(e.getKey()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"The \"intersmash-odh/ready\" label was not found, cannot determine whether the ODH Manager namespace is ready"))
				.getValue());
	}

	public Boolean isInstalling() {
		return OpenShifts.admin(openShift.getNamespace()).namespaces().withName(openShift.getNamespace()).get().getMetadata()
				.getLabels().entrySet()
				.stream()
				.anyMatch(e -> "intersmash-odh/installing".equals(e.getKey()) && Boolean.parseBoolean(e.getValue()));
	}

	public void unsubscribe(final String namespace) {
		try {
			// Remove the subscription label
			OpenShifts.admin(openShift.getNamespace()).namespaces().withName(openShift.getNamespace())
					.edit(new Visitor<NamespaceBuilder>() {
						@Override
						public void visit(NamespaceBuilder builder) {
							builder.editMetadata()
									.removeFromLabels(String.format("intersmash-odh-subscriber/%s", namespace));
						}
					});
		} catch (KubernetesClientException e) {
			throw new IllegalArgumentException("Couldn't remove a subscription label from the project '"
					+ openShift.getNamespace() + "'. Possible cause are insufficient permissions.");
		}
	}

	private OpenDataHubPrerequisitesProvisioner lookUpProvisioner() {
		final String openDataHubUnmanagedInstallClass = IntersmashConfig.openDataHubUnmanagedInstallClass();
		AtomicReference<OpenDataHubPrerequisitesProvisioner> lookedUp = new AtomicReference<>();
		ServiceLoader.load(
				OpenDataHubPrerequisitesProvisionerFactory.class)
				.stream()
				.map(ServiceLoader.Provider::get)
				.sorted((p1, p2) -> p2.getOrder().compareTo(p1.getOrder()))
				.forEachOrdered(pf -> {
					OpenDataHubPrerequisitesProvisioner p = pf.getProvisioner(openDataHubUnmanagedInstallClass);
					if (p != null && lookedUp.get() == null) {
						lookedUp.set(p);
					}
				});
		if (lookedUp.get() == null) {
			throw new UnsupportedOperationException(
					"No suitable Provisioner found for unmanaged Open Data Hub prerequisites installation via class: "
							+ openDataHubUnmanagedInstallClass);
		}
		return lookedUp.get();
	}
}
