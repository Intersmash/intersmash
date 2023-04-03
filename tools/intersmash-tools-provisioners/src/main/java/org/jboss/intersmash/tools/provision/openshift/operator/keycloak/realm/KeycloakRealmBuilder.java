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
package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec.KeycloakAPIRealm;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec.KeycloakRealmSpec;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec.RedirectorIdentityProviderOverride;

import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class KeycloakRealmBuilder {
	private String name;
	private Map<String, String> labels;
	//	private boolean unmanaged;
	private LabelSelector instanceSelector;
	private KeycloakAPIRealm realm;
	private List<RedirectorIdentityProviderOverride> realmOverrides;

	/**
	 * Initialize the {@link KeycloakRealmBuilder} with given resource name.
	 *
	 * @param name resource object name
	 */
	public KeycloakRealmBuilder(String name) {
		this.name = name;
	}

	/**
	 * Initialize the {@link KeycloakRealmBuilder} with given resource name and labels.
	 *
	 * @param name resource object name
	 * @param labels key/value pairs that are attached to objects
	 */
	public KeycloakRealmBuilder(String name, Map<String, String> labels) {
		this.name = name;
		this.labels = labels;
	}

	//	/**
	//	 * When set to true, this KeycloakRealm will be marked as unmanaged and not be managed by this operator.
	//	 * It can then be used for targeting purposes.
	//	 */
	//	public KeycloakRealmBuilder unmanaged(boolean unmanaged) {
	//		this.unmanaged = unmanaged;
	//		return this;
	//	}

	/**
	 * Set a selector for looking up Keycloak Custom Resources.
	 *
	 * @param instanceSelector {@link LabelSelector} instance that should be used for looking up Keycloak Custom Resources.
	 * @return this
	 */
	public KeycloakRealmBuilder instanceSelector(LabelSelector instanceSelector) {
		this.instanceSelector = instanceSelector;
		return this;
	}

	/**
	 * Keycloak Realm REST object.
	 *
	 * @param realm {@link KeycloakAPIRealm} instance that should be used as the realm REST client
	 * @return this
	 */
	public KeycloakRealmBuilder realm(KeycloakAPIRealm realm) {
		this.realm = realm;
		return this;
	}

	/**
	 * A list of overrides to the default Realm behavior.
	 *
	 * @param realmOverrides A list of {@link RedirectorIdentityProviderOverride} instances that should be used to
	 *                       override the default realm behavior
	 * @return this
	 */
	public KeycloakRealmBuilder realmOverrides(List<RedirectorIdentityProviderOverride> realmOverrides) {
		this.realmOverrides = realmOverrides;
		return this;
	}

	/**
	 * Add an override to the default Realm behavior.
	 *
	 * @param realmOverride A {@link RedirectorIdentityProviderOverride} instance that should be added to
	 *                       override the default realm behavior
	 * @return this
	 */
	public KeycloakRealmBuilder realmOverrides(RedirectorIdentityProviderOverride realmOverride) {
		if (realmOverrides == null) {
			realmOverrides = new ArrayList<>();
		}
		realmOverrides.add(realmOverride);
		return this;
	}

	public KeycloakRealm build() {
		KeycloakRealm keycloakRealm = new KeycloakRealm();
		keycloakRealm.setMetadata(new ObjectMeta());
		keycloakRealm.getMetadata().setName(name);
		keycloakRealm.getMetadata().setLabels(labels);

		KeycloakRealmSpec keycloakRealmSpec = new KeycloakRealmSpec();
		//		keycloakRealmSpec.setUnmanaged(unmanaged);
		keycloakRealmSpec.setInstanceSelector(instanceSelector);
		keycloakRealmSpec.setRealm(realm);
		keycloakRealmSpec.setRealmOverrides(realmOverrides);
		keycloakRealm.setSpec(keycloakRealmSpec);
		return keycloakRealm;
	}
}
