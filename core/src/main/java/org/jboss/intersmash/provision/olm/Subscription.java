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
package org.jboss.intersmash.provision.olm;

import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.util.Strings;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.SubscriptionBuilder;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.SubscriptionFluent;

/**
 * The Subscription configures when and how to update a ClusterService, binds a ClusterService to a channel in a
 * CatalogSource and configures the update strategy for a ClusterService (automatic, manual approval, etc).
 *
 * <p>This class is a wrapper for {@link io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription} which
 * adds some capabilities</p>
 * <p>
 * https://medium.com/@luis.ariz/operator-lifecycle-manager-review-f0885f9f3f1f
 * </p>
 */
public class Subscription extends io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription
		implements SerializationCapableResource<Subscription> {

	public Subscription() {
		super();
	}

	private SubscriptionFluent<SubscriptionBuilder>.SpecNested<SubscriptionBuilder> getConfiguredSubscriptionBuilder(
			final String sourceNamespace, final String targetNamespace, final String source, final String name,
			final String channel, final String installPlanApproval) {
		return new SubscriptionBuilder()
				.withNewMetadata()
				.withName(name)
				.withNamespace(targetNamespace)
				.endMetadata()
				.withNewSpec()
				.withChannel(channel)
				.withName(name)
				.withSource(source)
				.withSourceNamespace(sourceNamespace)
				.withInstallPlanApproval(Strings.isNullOrEmpty(installPlanApproval) ? "Automatic" : installPlanApproval);
	}

	public Subscription(final String sourceNamespace, final String targetNamespace, final String source,
			final String name, final String channel, final String installPlanApproval,
			final Map<String, String> envVariables) {
		this();
		io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription loaded = getConfiguredSubscriptionBuilder(
				sourceNamespace, targetNamespace, source, name, channel, installPlanApproval)
				.withNewConfig()
				.addAllToEnv(
						envVariables.entrySet().stream()
								.map(entry -> new EnvVar(entry.getKey(), entry.getValue(), null))
								.collect(Collectors.toList()))
				.endConfig()
				.endSpec()
				.build();
		this.setMetadata(loaded.getMetadata());
		this.setSpec(loaded.getSpec());
	}

	public Subscription(final String sourceNamespace, final String targetNamespace, final String source,
			final String name, final String channel, final String installPlanApproval) {
		this();
		io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription loaded = getConfiguredSubscriptionBuilder(
				sourceNamespace, targetNamespace, source, name, channel, installPlanApproval)
				.endSpec()
				.build();
		this.setMetadata(loaded.getMetadata());
		this.setSpec(loaded.getSpec());
	}

	@Override
	public Subscription load(Subscription loaded) {
		this.setMetadata(loaded.getMetadata());
		this.setSpec(loaded.getSpec());
		return this;
	}
}
