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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jboss.intersmash.application.Application;
import org.jboss.intersmash.application.k8s.HasConfigMaps;
import org.jboss.intersmash.application.k8s.HasPods;
import org.jboss.intersmash.application.k8s.HasSecrets;
import org.jboss.intersmash.provision.Provisioner;
import org.jboss.intersmash.provision.k8s.Scalable;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.NamespacedKubernetesClientAdapter;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;

/**
 * Provisioner that is supposed to deploy an application on OpenShift.
 */
public interface OpenShiftProvisioner<T extends Application> extends Provisioner<T>, Scalable, HasPods {
	String SCRIPT_DEBUG = "SCRIPT_DEBUG";
	String APP_LABEL_KEY = "intersmash.app";

	OpenShift openShift = OpenShifts.master();

	default NamespacedKubernetesClientAdapter<NamespacedOpenShiftClient> client() {
		return openShift;
	}

	default String execute(String... args) {
		return OpenShifts.adminBinary().execute(args);
	}

	@Override
	default void preDeploy() {
		// create secrets
		if (HasSecrets.class.isAssignableFrom(getApplication().getClass())) {
			((HasSecrets) getApplication()).getSecrets().forEach(openShift::createSecret);
		}
		// create configMaps
		if (HasConfigMaps.class.isAssignableFrom(getApplication().getClass())) {
			((HasConfigMaps) getApplication()).getConfigMaps().forEach(openShift::createConfigMap);
		}
	}

	@Override
	default void postUndeploy() {
		// delete secrets
		if (HasSecrets.class.isAssignableFrom(getApplication().getClass())) {
			((HasSecrets) getApplication()).getSecrets().forEach(openShift::deleteSecret);
		}
		// delete configMaps
		if (HasConfigMaps.class.isAssignableFrom(getApplication().getClass())) {
			((HasConfigMaps) getApplication()).getConfigMaps().forEach(openShift::deleteConfigMap);
		}
	}

	default String getUrl(String routeName, boolean secure) {
		String protocol = secure ? "https" : "http";
		return protocol + "://" + openShift.generateHostname(routeName);
	}

	@Override
	default URL getURL() {
		try {
			return new URL(getUrl(getApplication().getName(), false));
		} catch (MalformedURLException ex) {
			throw new RuntimeException(
					String.format("Failed to get an URL for the \"%s\" route", this.getClass().getSimpleName()), ex);
		}
	}

	@Override
	default List<Pod> getPods() {
		return OpenShiftProvisioner.openShift.inNamespace(OpenShiftConfig.namespace()).pods().list().getItems();
	}

	default NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> customResourceDefinitionsClient() {
		return OpenShifts.admin().apiextensions().v1().customResourceDefinitions();
	}
}
