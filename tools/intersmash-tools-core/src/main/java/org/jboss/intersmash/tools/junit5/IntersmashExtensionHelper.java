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
package org.jboss.intersmash.tools.junit5;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jboss.intersmash.tools.annotations.Intersmash;
import org.jboss.intersmash.tools.application.k8s.KubernetesApplication;
import org.jboss.intersmash.tools.application.openshift.OpenShiftApplication;
import org.jboss.intersmash.tools.application.operator.OperatorApplication;
import org.jboss.intersmash.tools.provision.Provisioner;
import org.junit.jupiter.api.extension.ExtensionContext;

public class IntersmashExtensionHelper {

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create("org", "jboss", "intersmash",
			"IntersmashExtension");
	private static final String INTERSMASH_SERVICES = "INTERSMASH_SERVICES";
	private static final String INTERSMASH = "INTERSMASH";

	public static Map<String, Provisioner> getProvisioners(ExtensionContext extensionContext) {
		ExtensionContext.Store store = extensionContext.getStore(NAMESPACE);
		Map<String, Provisioner> provisioners = (Map<String, Provisioner>) store.get(INTERSMASH_SERVICES);
		if (provisioners != null) {
			return provisioners;
		} else {
			store.put(INTERSMASH_SERVICES, new HashMap<String, Provisioner>());
			return (Map<String, Provisioner>) store.get(INTERSMASH_SERVICES);
		}
	}

	public static Intersmash getIntersmash(ExtensionContext extensionContext) {
		ExtensionContext.Store store = extensionContext.getStore(NAMESPACE);
		Intersmash result = (Intersmash) store.get(INTERSMASH);
		if (result != null) {
			return result;
		} else {
			Intersmash[] intersmashes = extensionContext.getRequiredTestClass().getAnnotationsByType(Intersmash.class);
			if (intersmashes.length > 0) {
				store.put(INTERSMASH, intersmashes[0]);
				return (Intersmash) store.get(INTERSMASH);
			}
			return null;
		}
	}

	public static Boolean isIntersmashTargetingOpenShift(ExtensionContext extensionContext) {
		return Arrays.stream(getIntersmash(extensionContext).value())
				.anyMatch(app -> OpenShiftApplication.class.isAssignableFrom(app.value()));
	}

	public static Boolean isIntersmashTargetingOperator(ExtensionContext extensionContext) {
		return Arrays.stream(getIntersmash(extensionContext).value())
				.anyMatch(app -> OperatorApplication.class.isAssignableFrom(app.value()));
	}

	public static Boolean isIntersmashTargetingKubernetes(ExtensionContext extensionContext) {
		return Arrays.stream(getIntersmash(extensionContext).value())
				.anyMatch(app -> KubernetesApplication.class.isAssignableFrom(app.value()));
	}
}
