/**
 * Copyright (C) 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
