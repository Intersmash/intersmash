package org.jboss.intersmash.tools.provision.openshift.operator.resources;

import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.util.Strings;

import cz.xtf.core.config.OpenShiftConfig;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.SubscriptionBuilder;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.SubscriptionFluent;

/**
 * <p>
 * The Subscription configures when and how to update a ClusterService, binds a ClusterService to a channel in a
 * CatalogSource and configures the update strategy for a ClusterService (automatic, manual approval, etc).
 * </p>
 * <p>This class is a wrapper for {@link io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription} which
 * adds some capabilities</p>
 * <p>
 * https://medium.com/@luis.ariz/operator-lifecycle-manager-review-f0885f9f3f1f
 * </p>
 */
public class Subscription extends io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription
		implements OpenShiftResource<Subscription> {

	public Subscription() {
		super();
	}

	private SubscriptionFluent.SpecNested<SubscriptionBuilder> getConfiguredSubscriptionBuilder(String sourceNamespace,
			String source, String name, String channel,
			String installPlanApproval) {
		return new SubscriptionBuilder()
				.withNewMetadata()
				.withName(name)
				.withNamespace(OpenShiftConfig.namespace())
				.endMetadata()
				.withNewSpec()
				.withChannel(channel)
				.withName(name)
				.withSource(source)
				.withSourceNamespace(sourceNamespace)
				.withInstallPlanApproval(Strings.isNullOrEmpty(installPlanApproval) ? "Automatic" : installPlanApproval);
	}

	public Subscription(String sourceNamespace, String source, String name, String channel, String installPlanApproval,
			Map<String, String> envVariables) {
		this();
		io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription loaded = getConfiguredSubscriptionBuilder(
				sourceNamespace, source, name, channel, installPlanApproval)
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

	public Subscription(String sourceNamespace, String source, String name, String channel, String installPlanApproval) {
		this();
		io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription loaded = getConfiguredSubscriptionBuilder(
				sourceNamespace, source, name, channel, installPlanApproval)
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
