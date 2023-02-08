package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

/**
 * Specify PodDisruptionBudget configuration.
 */
public final class PodDisruptionBudgetConfigBuilder {
	private boolean enabled;

	/**
	 * If set to true, the operator will create a PodDistruptionBudget for the Keycloak deployment and set its
	 * `maxUnavailable` value to 1.
	 *
	 * @param enabled Whether the operator should create a PodDistruptionBudget for the Keycloak deployment
	 * @return this
	 */
	public PodDisruptionBudgetConfigBuilder enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public PodDisruptionBudgetConfig build() {
		PodDisruptionBudgetConfig podDisruptionBudgetConfig = new PodDisruptionBudgetConfig();
		podDisruptionBudgetConfig.setEnabled(enabled);
		return podDisruptionBudgetConfig;
	}
}
