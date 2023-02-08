package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Specify Migration configuration.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class MigrateConfig {
	/**
	 * Specify migration strategy.
	 */
	private String strategy;

	/**
	 * Set it to config backup policy for migration.
	 */
	private BackupConfig backups;
}
