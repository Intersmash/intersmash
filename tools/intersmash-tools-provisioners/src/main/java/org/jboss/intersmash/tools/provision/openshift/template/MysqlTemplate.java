package org.jboss.intersmash.tools.provision.openshift.template;

/**
 * OpenShift templates for Mysql.
 *
 * These are build in {@code openshift} namespace by default.
 */
public enum MysqlTemplate implements OpenShiftTemplate {
	MYSQL_EPHEMERAL("mysql-ephemeral"),
	MYSQL_PERSISTENT("mysql-persistent");

	private String name;

	MysqlTemplate(String name) {
		this.name = name;
	}

	@Override
	public String getLabel() {
		return name;
	}
}
