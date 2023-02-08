package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

/**
 * Specify Migration configuration.
 */
public final class BackupConfigBuilder {
	private boolean enabled;

	/**
	 * If set to true, the operator will do database backup before doing migration.
	 *
	 * @param enabled Whether the operator will do database backup before doing migration
	 * @return this
	 */
	public BackupConfigBuilder enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public BackupConfig build() {
		BackupConfig backupConfig = new BackupConfig();
		backupConfig.setEnabled(enabled);
		return backupConfig;
	}
}
