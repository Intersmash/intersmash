package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

public final class ExposeSpecBuilder {
	private String type;
	private int nodePort;
	private String host;

	/**
	 * Type specifies different exposition methods for data grid
	 *
	 * @param type The type of different exposition methods for data grid
	 * @return this
	 */
	public ExposeSpecBuilder type(ExposeType type) {
		if (type != ExposeType.None) {
			this.type = type.getValue();
		}
		return this;
	}

	/**
	 * Set the port where Data Grid is exposed
	 *
	 * @param nodePort The port where Data Grid is exposed
	 * @return this
	 */
	public ExposeSpecBuilder nodePort(int nodePort) {
		this.nodePort = nodePort;
		return this;
	}

	/**
	 * Set the host where Data Grid is exposed
	 *
	 * @param host The host where Data Grid is exposed
	 * @return this
	 */
	public ExposeSpecBuilder host(String host) {
		this.host = host;
		return this;
	}

	public ExposeSpec build() {
		ExposeSpec exposeSpec = new ExposeSpec();
		exposeSpec.setType(type);
		exposeSpec.setNodePort(nodePort);
		exposeSpec.setHost(host);
		return exposeSpec;
	}

	/**
	 * Describe different exposition methods for Infinispan
	 */
	public enum ExposeType {
		None(""),
		/**
		 * ExposeTypeNodePort means a service will be exposed on one port of
		 * every node, in addition to 'ClusterIP' type.
		 */
		NodePort("NodePort"),
		/**
		 * ExposeTypeLoadBalancer means a service will be exposed via an
		 * external load balancer (if the cloud provider supports it), in addition
		 * to 'NodePort' type.
		 */
		LoadBalancer("LoadBalancer"),
		/**
		 * ExposeTypeRoute means the service will be exposed via
		 * `Route` on Openshift or via `Ingress` on Kubernetes
		 */
		Route("Route");

		private String value;

		ExposeType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
