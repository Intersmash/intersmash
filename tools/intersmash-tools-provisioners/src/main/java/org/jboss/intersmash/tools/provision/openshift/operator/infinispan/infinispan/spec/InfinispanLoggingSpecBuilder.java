package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

import java.util.Map;

public final class InfinispanLoggingSpecBuilder {
	private Map<String, String> categories;

	/**
	 * Names logging categories and levels
	 *
	 * @param categories Map storing logging categories and levels
	 * @return this
	 */
	public InfinispanLoggingSpecBuilder categories(Map<String, String> categories) {
		this.categories = categories;
		return this;
	}

	public InfinispanLoggingSpec build() {
		InfinispanLoggingSpec infinispanLoggingSpec = new InfinispanLoggingSpec();
		infinispanLoggingSpec.setCategories(categories);
		return infinispanLoggingSpec;
	}
}
