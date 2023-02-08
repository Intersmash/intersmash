package org.jboss.intersmash.tools.provision.openshift.operator.activemq.broker.spec;

import org.jboss.intersmash.tools.IntersmashConfig;

import io.amq.broker.v1beta1.activemqartemisspec.DeploymentPlan;

public class DeploymentPlanBuilder {
	private String image = IntersmashConfig.activeMQImageUrl();
	private String journalType;
	private Boolean messageMigration;
	private Boolean persistenceEnabled;
	private Boolean requireLogin;
	private Integer size = 2;

	/**
	 * The image used for the broker deployment.
	 *
	 * @param image The desired image to be used for deployment
	 * @return this
	 */
	public DeploymentPlanBuilder image(String image) {
		this.image = image;
		return this;
	}

	/**
	 * If aio use ASYNCIO, if nio use NIO for journal IO.
	 *
	 * @param journalType The desired journal type
	 * @return this
	 */
	public DeploymentPlanBuilder journalType(String journalType) {
		this.journalType = journalType;
		return this;
	}

	/**
	 * If true migrate messages on scaledown.
	 *
	 * @param messageMigration Whether to migrate existing messages, e.g. during scaledown
	 * @return this
	 */
	public DeploymentPlanBuilder messageMigration(Boolean messageMigration) {
		this.messageMigration = messageMigration;
		return this;
	}

	/**
	 * If true use persistent volume via persistent volume claim for journal storage.
	 *
	 * @param persistenceEnabled Whether the journal should be persisted
	 * @return this
	 */
	public DeploymentPlanBuilder persistenceEnabled(Boolean persistenceEnabled) {
		this.persistenceEnabled = persistenceEnabled;
		return this;
	}

	/**
	 * If true require user password login credentials for broker protocol ports.
	 *
	 * @param requireLogin Whether credentials for broker protocol port should be required
	 * @return this
	 */
	public DeploymentPlanBuilder requireLogin(Boolean requireLogin) {
		this.requireLogin = requireLogin;
		return this;
	}

	/**
	 * The number of broker pods to deploy.
	 *
	 * @param size The desired number of broker pods to be deployed
	 * @return this
	 */
	public DeploymentPlanBuilder size(Integer size) {
		this.size = size;
		return this;
	}

	public DeploymentPlan build() {
		DeploymentPlan deploymentPlan = new DeploymentPlan();
		deploymentPlan.setImage(image);
		deploymentPlan.setJournalType(journalType);
		deploymentPlan.setMessageMigration(messageMigration);
		deploymentPlan.setPersistenceEnabled(persistenceEnabled);
		deploymentPlan.setRequireLogin(requireLogin);
		deploymentPlan.setSize(size);
		return deploymentPlan;
	}
}
