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
package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

/**
 * Specify Migration configuration.
 */
public final class MigrateConfigBuilder {
	private String strategy;
	private BackupConfig backups;

	/**
	 * Specify migration strategy.
	 *
	 * @param strategy The migration strategy that should  be used
	 * @return this
	 */
	public MigrateConfigBuilder strategy(MigrationStrategy strategy) {
		if (strategy != MigrationStrategy.NO_STRATEGY) {
			this.strategy = strategy.getValue();
		}
		return this;
	}

	/**
	 * Set it to config backup policy for migration.
	 *
	 * @param backups The backup configuration that should be used
	 * @return this
	 */
	public MigrateConfigBuilder backups(BackupConfig backups) {
		this.backups = backups;
		return this;
	}

	public MigrateConfig build() {
		MigrateConfig migrateConfig = new MigrateConfig();
		migrateConfig.setStrategy(strategy);
		migrateConfig.setBackups(backups);
		return migrateConfig;
	}

	public enum MigrationStrategy {
		NO_STRATEGY(""),
		STRATEGY_RECREATE("recreate"),
		STRATEGY_ROLLING("rolling");

		private String value;

		MigrationStrategy(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
