package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

public final class InfinispanAuthInfoBuilder {
	private String type;

	public InfinispanAuthInfoBuilder type(String type) {
		this.type = type;
		return this;
	}

	public InfinispanAuthInfo build() {
		InfinispanAuthInfo infinispanAuthInfo = new InfinispanAuthInfo();
		infinispanAuthInfo.setType(type);
		return infinispanAuthInfo;
	}
}
