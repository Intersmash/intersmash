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
package org.jboss.intersmash.tools.provision.k8s;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.intersmash.tools.application.k8s.KubernetesApplication;
import org.jboss.intersmash.tools.application.openshift.HasConfigMaps;
import org.jboss.intersmash.tools.application.openshift.HasSecrets;
import org.jboss.intersmash.tools.provision.Provisioner;
import org.jboss.intersmash.tools.provision.openshift.HasPods;
import org.jboss.intersmash.tools.provision.openshift.Scalable;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Provisioner that is supposed to deploy an application on Kubernetes.
 */
public interface KubernetesProvisioner<T extends KubernetesApplication> extends Provisioner<T>, Scalable, HasPods {

	//	TODO - check for aq new class of statics like XTF OpenShifts?
	KubernetesClient kubernetes = newKubernetesClient();

	static KubernetesClient newKubernetesClient() {
		Config config = new ConfigBuilder()
				.build();
		return new DefaultKubernetesClient(config);
	}

	@Override
	default void preDeploy() {
		// create secrets
		if (HasSecrets.class.isAssignableFrom(getApplication().getClass())) {
			((HasSecrets) getApplication()).getSecrets().forEach(s -> kubernetes.secrets().create(s));
		}
		// create configMaps
		if (HasConfigMaps.class.isAssignableFrom(getApplication().getClass())) {
			((HasConfigMaps) getApplication()).getConfigMaps().forEach(c -> kubernetes.configMaps().create(c));
		}
	}

	@Override
	default void postUndeploy() {
		// delete secrets
		if (HasSecrets.class.isAssignableFrom(getApplication().getClass())) {
			((HasSecrets) getApplication()).getSecrets().forEach(s -> kubernetes.secrets().delete(s));
		}
		// delete configMaps
		if (HasConfigMaps.class.isAssignableFrom(getApplication().getClass())) {
			((HasConfigMaps) getApplication()).getConfigMaps().forEach(c -> kubernetes.configMaps().delete(c));
		}
	}

	// TODO - check (use a static class method like XTF OpenShift::generateHostName ?)
	default String getUrl(String routeName, boolean secure) {
		String protocol = secure ? "https" : "http";
		return protocol + "://" + kubernetes.getMasterUrl() + "-" + routeName + "-"
				+ kubernetes.getConfiguration().getNamespace();
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
}
