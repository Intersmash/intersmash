package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

public final class InfinispanConditionBuilder {
	private String type;
	private String status;
	private String message;

	/**
	 * Set the type of the condition.
	 *
	 * @param type Type of the condition.
	 * @return this
	 */
	public InfinispanConditionBuilder type(ConditionType type) {
		if (type != ConditionType.None) {
			this.type = type.getValue();
		}
		return this;
	}

	/**
	 * Set the status of the condition.
	 *
	 * @param status Status of the condition.
	 * @return this
	 */
	public InfinispanConditionBuilder status(String status) {
		this.status = status;
		return this;
	}

	/**
	 * Set a human-readable message indicating details about last transition.
	 *
	 * @param message Human-readable message indicating details about last transition.
	 * @return this
	 */
	public InfinispanConditionBuilder message(String message) {
		this.message = message;
		return this;
	}

	public InfinispanCondition build() {
		InfinispanCondition infinispanCondition = new InfinispanCondition();
		infinispanCondition.setType(type);
		infinispanCondition.setStatus(status);
		infinispanCondition.setMessage(message);
		return infinispanCondition;
	}

	/**
	 * Describe the type of the condition
	 */
	public enum ConditionType {
		None(""),
		ConditionPrelimChecksPassed("PreliminaryChecksPassed"),
		ConditionGracefulShutdown("GracefulShutdown"),
		ConditionStopping("Stopping"),
		ConditionUpgrade("Upgrade"),
		ConditionWellFormed("WellFormed");

		private String value;

		ConditionType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
