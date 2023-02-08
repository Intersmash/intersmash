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
