package io.hyperfoil.v1alpha2;

import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class HyperfoilBuilder {
	private String name;

	//TODO: complete with all hyperfoil ino

	/**
	 * Initialize the {@link HyperfoilBuilder} with given resource name.
	 *
	 * @param name resource object name
	 */
	public HyperfoilBuilder(String name) {
		this.name = name;
	}

	public Hyperfoil build() {
		Hyperfoil hyperfoil = new Hyperfoil();
		hyperfoil.setMetadata(new ObjectMeta());
		hyperfoil.getMetadata().setName(name);

		HyperfoilSpec spec = new HyperfoilSpec();
		hyperfoil.setSpec(spec);

		return hyperfoil;
	}
}
