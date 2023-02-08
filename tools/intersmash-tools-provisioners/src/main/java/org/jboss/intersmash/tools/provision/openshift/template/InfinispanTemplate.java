package org.jboss.intersmash.tools.provision.openshift.template;

/**
 * OpenShift templates for Infinispan.
 * <p>
 */
public enum InfinispanTemplate implements OpenShiftTemplate {
	BASIC("basic"),
	HTTPS("https"),
	MYSQL_PERSISTENT("mysql-persistent"),
	MYSQL("mysql"),
	PARTITION("partition"),
	POSTGRESQL_PERSISTENT("postgresql-persistent"),
	POSTGRESQL("postgresql");

	private String name;

	InfinispanTemplate(String name) {
		this.name = name;
	}

	@Override
	public String getLabel() {
		return name;
	}
}
