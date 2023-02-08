package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.spec;

public final class CacheConditionBuilder {
	private String type;
	private String status;
	private String message;

	/**
	 * Set the type of the condition.
	 *
	 * @param type Type of the condition.
	 * @return this
	 */
	public CacheConditionBuilder type(String type) {
		this.type = type;
		return this;
	}

	/**
	 * Set the status of the condition.
	 *
	 * @param status Status of the condition.
	 * @return this
	 */
	public CacheConditionBuilder status(String status) {
		this.status = status;
		return this;
	}

	/**
	 * Set a human-readable message indicating details about last transition.
	 *
	 * @param message Human-readable message indicating details about last transition.
	 * @return this
	 */
	public CacheConditionBuilder message(String message) {
		this.message = message;
		return this;
	}

	public CacheCondition build() {
		CacheCondition cacheCondition = new CacheCondition();
		cacheCondition.setType(type);
		cacheCondition.setStatus(status);
		cacheCondition.setMessage(message);
		return cacheCondition;
	}
}
