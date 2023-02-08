package org.jboss.intersmash.tools.provision.openshift.operator.activemq.broker.spec;

import io.amq.broker.v1beta1.activemqartemisspec.Upgrades;

public class UpgradesBuilder {
	private Boolean enabled;
	private Boolean minor;

	/**
	 * Set true to enable automatic micro version product upgrades, it is disabled by default.
	 *
	 * @param enabled Whether micro version updates will be performed automatically
	 * @return this
	 */
	public UpgradesBuilder enabled(Boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/**
	 * Set true to enable automatic minor product version upgrades, it is disabled by default.
	 * Requires {@code }spec.upgrades.enabled} to be true.
	 *
	 * @param minor Whether minor version updates will be performed automatically
	 * @return this
	 */
	public UpgradesBuilder minor(Boolean minor) {
		this.minor = minor;
		return this;
	}

	public Upgrades build() {
		Upgrades upgrades = new Upgrades();
		upgrades.setEnabled(enabled);
		upgrades.setMinor(minor);
		return upgrades;
	}
}
