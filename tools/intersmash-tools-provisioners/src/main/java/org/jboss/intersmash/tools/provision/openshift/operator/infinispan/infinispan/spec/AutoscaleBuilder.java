package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

public final class AutoscaleBuilder {
	private int maxReplicas;
	private int minReplicas;
	private int maxMemUsagePercent;
	private int minMemUsagePercent;

	/**
	 * Defines the maximum number of nodes for the cluster.
	 *
	 * @param maxReplicas Maximum number of nodes for the cluster.
	 * @return this
	 */
	public AutoscaleBuilder maxReplicas(int maxReplicas) {
		this.maxReplicas = maxReplicas;
		return this;
	}

	/**
	 * Defines the minimum number of nodes for the cluster
	 *
	 * @param minReplicas Minimum number of nodes for the cluster.
	 * @return this
	 */
	public AutoscaleBuilder minReplicas(int minReplicas) {
		this.minReplicas = minReplicas;
		return this;
	}

	/**
	 * Configures the maximum threshold, as a percentage, for memory usage on each node
	 *
	 * @param maxMemUsagePercent Maximum threshold, as a percentage, for memory usage on each node
	 * @return this
	 */
	public AutoscaleBuilder maxMemUsagePercent(int maxMemUsagePercent) {
		this.maxMemUsagePercent = maxMemUsagePercent;
		return this;
	}

	/**
	 * Configures the minimum threshold, as a percentage, for memory usage across the cluster
	 *
	 * @param minMemUsagePercent Minimum threshold, as a percentage, for memory usage on each node
	 * @return this
	 */
	public AutoscaleBuilder minMemUsagePercent(int minMemUsagePercent) {
		this.minMemUsagePercent = minMemUsagePercent;
		return this;
	}

	public Autoscale build() {
		Autoscale autoscale = new Autoscale();
		autoscale.setMaxReplicas(maxReplicas);
		autoscale.setMinReplicas(minReplicas);
		autoscale.setMaxMemUsagePercent(maxMemUsagePercent);
		autoscale.setMinMemUsagePercent(minMemUsagePercent);
		return autoscale;
	}
}
