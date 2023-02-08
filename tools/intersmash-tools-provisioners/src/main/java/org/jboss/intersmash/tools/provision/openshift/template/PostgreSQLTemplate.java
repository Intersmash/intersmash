package org.jboss.intersmash.tools.provision.openshift.template;

/**
 * OpenShift templates for PostgreSQL.
 *
 * These are build in {@code openshift} namespace by default.
 */
public enum PostgreSQLTemplate implements OpenShiftTemplate {
	POSTGRESQL_EPHEMERAL("postgresql-ephemeral"),
	POSTGRESQL_PERSISTENT("postgresql-persistent");

	private String name;

	PostgreSQLTemplate(String name) {
		this.name = name;
	}

	@Override
	public String getLabel() {
		return name;
	}

}
