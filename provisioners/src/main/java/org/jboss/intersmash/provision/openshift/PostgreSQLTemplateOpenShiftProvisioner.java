/*
 * Copyright (C) 2025 Red Hat, Inc.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.application.openshift.PostgreSQLTemplateOpenShiftApplication;
import org.jboss.intersmash.provision.openshift.template.OpenShiftTemplate;
import org.slf4j.event.Level;

import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.failfast.FailFastCheck;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.Template;
import lombok.extern.slf4j.Slf4j;

/**
 * Provisions PostgreSql on OpenShift via PostgreSql template available on OpenShift and based on a
 * {@link PostgreSQLTemplateOpenShiftApplication} concrete implementation.
 *
 */
@Slf4j
public class PostgreSQLTemplateOpenShiftProvisioner implements OpenShiftProvisioner<PostgreSQLTemplateOpenShiftApplication> {
	private FailFastCheck ffCheck = () -> false;
	private final PostgreSQLTemplateOpenShiftApplication postgreSQLApplication;
	private final OpenShiftTemplate postgreSQLTemplate;
	private KubernetesList kubernetesList;

	public PostgreSQLTemplateOpenShiftProvisioner(PostgreSQLTemplateOpenShiftApplication postgreSQLApplication) {
		this.postgreSQLApplication = postgreSQLApplication;
		this.postgreSQLTemplate = postgreSQLApplication.getTemplate();
	}

	@Override
	public PostgreSQLTemplateOpenShiftApplication getApplication() {
		return postgreSQLApplication;
	}

	@Override
	public void deploy() {
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				postgreSQLApplication.getName());

		Map<String, String> parameters = new HashMap<>(postgreSQLApplication.getParameters());
		// DATABASE_SERVICE_NAME parameter has to be aligned with the getName() in case that default is not used
		if (!postgreSQLApplication.getName().equals("postgresql") || parameters.containsKey("DATABASE_SERVICE_NAME")) {
			String dbServiceName = parameters.getOrDefault("DATABASE_SERVICE_NAME", "");
			if (!dbServiceName.equals(postgreSQLApplication.getName())) {
				log.warn("DATABASE_SERVICE_NAME has to be aligned with the application name, setting it according to name: {}.",
						postgreSQLApplication.getName());
				parameters.put("DATABASE_SERVICE_NAME", postgreSQLApplication.getName());
			}
		}
		// Get the template from the openshift namespace, recreate it into test namespace and deploy
		Template template = OpenShifts.master("openshift")
				.getTemplate(postgreSQLApplication.getTemplate().getLabel());
		template.getMetadata().setNamespace(openShift.getNamespace());
		template.getMetadata().setResourceVersion(null);
		openShift.createTemplate(template);
		kubernetesList = openShift.processAndDeployTemplate(template.getMetadata().getName(), parameters);
		OpenShiftWaiters.get(openShift, ffCheck).isDcReady(postgreSQLApplication.getName()).waitFor();
	}

	@Override
	public void undeploy() {
		openShift.deleteResources(kubernetesList);
		openShift.deleteTemplate(postgreSQLApplication.getTemplate().getLabel());
	}

	@Override
	public List<Pod> getPods() {
		return openShift.getPods(getApplication().getName());
	}

	@Override
	public void scale(int replicas, boolean wait) {
		openShift.scale(postgreSQLApplication.getName(), replicas);
		if (wait) {
			OpenShiftWaiters.get(openShift, ffCheck).areExactlyNPodsReady(replicas, "name", postgreSQLTemplate.getLabel())
					.level(Level.DEBUG).waitFor();
		}
	}

	@Override
	public String getUrl(String routeName, boolean secure) {
		throw new UnsupportedOperationException("Route is not created for DB applications.");
	}
}
