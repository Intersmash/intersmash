package org.jboss.intersmash.tools.provision.openshift.operator.activemq.broker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.tools.provision.openshift.operator.activemq.broker.spec.DeploymentPlanBuilder;

import io.amq.broker.v1beta1.ActiveMQArtemis;
import io.amq.broker.v1beta1.ActiveMQArtemisSpec;
import io.amq.broker.v1beta1.activemqartemisspec.Acceptors;
import io.amq.broker.v1beta1.activemqartemisspec.Connectors;
import io.amq.broker.v1beta1.activemqartemisspec.Console;
import io.amq.broker.v1beta1.activemqartemisspec.DeploymentPlan;
import io.amq.broker.v1beta1.activemqartemisspec.Upgrades;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class ActiveMQArtemisBuilder {

	private String name;
	private Map<String, String> labels;
	private List<Acceptors> acceptors;
	private String adminPassword;
	private String adminUser;
	private List<Connectors> connectors;
	private Console console;
	// init default DeploymentPlan (image and size fields) for ActiveMQArtemis resource
	private DeploymentPlan deploymentPlan = new DeploymentPlanBuilder().build();
	private Upgrades upgrades;
	private String version;

	/**
	 * Initialize the {@link ActiveMQArtemisBuilder} with given resource name.
	 *
	 * @param name resource object name
	 */
	public ActiveMQArtemisBuilder(String name) {
		this.name = name;
	}

	/**
	 * Initialize the {@link ActiveMQArtemisBuilder} with given resource name and labels.
	 *
	 * @param name resource object name
	 * @param labels key/value pairs that are attached to objects
	 */
	public ActiveMQArtemisBuilder(String name, Map<String, String> labels) {
		this.name = name;
		this.labels = labels;
	}

	/**
	 * Configuration of all acceptors.
	 * @param acceptors List of {@link Acceptors} instances to define the desired configuration for all acceptors
	 * @return this
	 */
	public ActiveMQArtemisBuilder acceptors(List<Acceptors> acceptors) {
		this.acceptors = acceptors;
		return this;
	}

	/**
	 * Configuration of an acceptor.
	 * @param acceptor {@link Acceptors} instance to define the desired acceptor configuration
	 * @return this
	 */
	public ActiveMQArtemisBuilder acceptors(Acceptors acceptor) {
		if (acceptors == null) {
			acceptors = new ArrayList<>();
		}
		acceptors.add(acceptor);
		return this;
	}

	/**
	 * Password for standard broker user. It is required for connecting to the broker. If left empty, it will be generated.
	 * @return this
	 * @param adminPassword A desired password for the broker standard user
	 */
	public ActiveMQArtemisBuilder adminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
		return this;
	}

	/**
	 * User name for standard broker user. It is required for connecting to the broker. If left empty, it will be generated.
	 * @param adminUser A desired username for the broker standard user
	 * @return this
	 */
	public ActiveMQArtemisBuilder adminUser(String adminUser) {
		this.adminUser = adminUser;
		return this;
	}

	/**
	 * Configuration of all connectors.
	 * @param connectors List of {@link Connectors} instances to define the desired configuration for all connectors
	 * @return this
	 */
	public ActiveMQArtemisBuilder connectors(List<Connectors> connectors) {
		this.connectors = connectors;
		return this;
	}

	/**
	 * Configuration of a connector.
	 * @param connector {@link Connectors} instance to define the desired connector configuration
	 * @return this
	 */
	public ActiveMQArtemisBuilder connectors(Connectors connector) {
		if (connectors == null) {
			connectors = new ArrayList<>();
		}
		connectors.add(connector);
		return this;
	}

	/**
	 * Configuration for the embedded web console.
	 * @param console {@link Console} instance to define the desired console configuration
	 * @return this
	 */
	public ActiveMQArtemisBuilder console(Console console) {
		this.console = console;
		return this;
	}

	/**
	 * Broker deployment configuration.
	 * @param deploymentPlan {@link DeploymentPlan} instance to define the desired deployment configuration
	 * @return this
	 */
	public ActiveMQArtemisBuilder deploymentPlan(DeploymentPlan deploymentPlan) {
		this.deploymentPlan = deploymentPlan;
		return this;
	}

	/**
	 * Specify the level of upgrade that should be allowed when an older product version is detected.
	 * @param upgrades {@link Upgrades} instance to set the level of upgrade
	 * @return this
	 */
	public ActiveMQArtemisBuilder upgrades(Upgrades upgrades) {
		this.upgrades = upgrades;
		return this;
	}

	/**
	 * The version of the application deployment.
	 * @param version Value for the desired application deployment version
	 * @return this
	 */
	public ActiveMQArtemisBuilder version(String version) {
		this.version = version;
		return this;
	}

	public ActiveMQArtemis build() {
		ActiveMQArtemis activeMQArtemis = new ActiveMQArtemis();
		activeMQArtemis.setMetadata(new ObjectMeta());
		activeMQArtemis.getMetadata().setName(name);
		activeMQArtemis.getMetadata().setLabels(labels);

		ActiveMQArtemisSpec activeMQArtemisSpec = new ActiveMQArtemisSpec();
		activeMQArtemisSpec.setAcceptors(acceptors);
		activeMQArtemisSpec.setAdminPassword(adminPassword);
		activeMQArtemisSpec.setAdminUser(adminUser);
		activeMQArtemisSpec.setConnectors(connectors);
		activeMQArtemisSpec.setConsole(console);
		activeMQArtemisSpec.setDeploymentPlan(deploymentPlan);
		activeMQArtemisSpec.setUpgrades(upgrades);
		activeMQArtemisSpec.setVersion(version);
		activeMQArtemis.setSpec(activeMQArtemisSpec);

		return activeMQArtemis;
	}
}
