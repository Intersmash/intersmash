package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

public final class InfinispanSiteLocationSpecBuilder {
	private String name;
	private String url;
	private String secretName;

	/**
	 * Set the location name
	 *
	 * @param name Location name
	 * @return this
	 */
	public InfinispanSiteLocationSpecBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Set the location URL
	 *
	 * @param url Location URL
	 * @return this
	 */
	public InfinispanSiteLocationSpecBuilder url(String url) {
		this.url = url;
		return this;
	}

	/**
	 * Names the secret used to encrypt communication with the location
	 *
	 * @param secretName Name of the secret used to encrypt communication with the location
	 * @return this
	 */
	public InfinispanSiteLocationSpecBuilder secretName(String secretName) {
		this.secretName = secretName;
		return this;
	}

	public InfinispanSiteLocationSpec build() {
		InfinispanSiteLocationSpec infinispanSiteLocationSpec = new InfinispanSiteLocationSpec();
		infinispanSiteLocationSpec.setName(name);
		infinispanSiteLocationSpec.setUrl(url);
		infinispanSiteLocationSpec.setSecretName(secretName);
		return infinispanSiteLocationSpec;
	}
}
