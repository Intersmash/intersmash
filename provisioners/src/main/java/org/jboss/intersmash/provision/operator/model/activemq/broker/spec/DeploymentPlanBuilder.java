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
package org.jboss.intersmash.provision.operator.model.activemq.broker.spec;

import org.jboss.intersmash.IntersmashConfig;

import io.amq.broker.v1beta1.activemqartemisspec.DeploymentPlan;

public class DeploymentPlanBuilder {
	private String image = IntersmashConfig.activeMQImageUrl();
	private String initImage = IntersmashConfig.activeMQInitImageUrl();
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
	 * The init image used for the broker deployment.
	 *
	 * @param initImage The desired init Image to be used for deployment
	 * @return this
	 */
	public DeploymentPlanBuilder initImage(String initImage) {
		this.initImage = initImage;
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
		deploymentPlan.setInitImage(initImage);
		deploymentPlan.setJournalType(journalType);
		deploymentPlan.setMessageMigration(messageMigration);
		deploymentPlan.setPersistenceEnabled(persistenceEnabled);
		deploymentPlan.setRequireLogin(requireLogin);
		deploymentPlan.setSize(size);
		return deploymentPlan;
	}
}
