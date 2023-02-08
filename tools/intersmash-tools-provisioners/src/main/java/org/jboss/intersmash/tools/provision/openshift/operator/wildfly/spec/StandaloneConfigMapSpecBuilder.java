package org.jboss.intersmash.tools.provision.openshift.operator.wildfly.spec;

public final class StandaloneConfigMapSpecBuilder {
	private String name;
	private String key;

	/**
	 * Initialize the {@link StandaloneConfigMapSpecBuilder} with given resource name.
	 *
	 * @param name of the {@link io.fabric8.kubernetes.api.model.ConfigMap} containing the standalone configuration XML file.
	 */
	public StandaloneConfigMapSpecBuilder(String name) {
		this.name = name;
	}

	/**
	 * Key of the {@link io.fabric8.kubernetes.api.model.ConfigMap} whose value is the
	 * standalone configuration XML file. If omitted, the spec will look for the standalone.xml key.
	 *
	 * @param key Desired key of for the {@link io.fabric8.kubernetes.api.model.ConfigMap} whose value is the
	 * 	 standalone configuration XML file.
	 * @return this
	 */
	public StandaloneConfigMapSpecBuilder key(String key) {
		this.key = key;
		return this;
	}

	public StandaloneConfigMapSpec build() {
		StandaloneConfigMapSpec standaloneConfigMapSpec = new StandaloneConfigMapSpec();
		standaloneConfigMapSpec.setName(name);
		standaloneConfigMapSpec.setKey(key);
		return standaloneConfigMapSpec;
	}
}
