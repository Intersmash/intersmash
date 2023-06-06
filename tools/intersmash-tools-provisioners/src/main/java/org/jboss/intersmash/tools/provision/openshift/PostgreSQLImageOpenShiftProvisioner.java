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
package org.jboss.intersmash.tools.provision.openshift;

import java.util.Map;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.PostgreSQLImageOpenShiftApplication;

import cz.xtf.builder.builders.ApplicationBuilder;
import cz.xtf.builder.builders.pod.ContainerBuilder;
import cz.xtf.builder.builders.secret.SecretType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostgreSQLImageOpenShiftProvisioner extends DBImageOpenShiftProvisioner<PostgreSQLImageOpenShiftApplication> {

	public static final String POSTGRESQL_USER = "POSTGRESQL_USER";
	public static final String POSTGRESQL_PASSWORD = "POSTGRESQL_PASSWORD";
	public static final String POSTGRESQL_ADMIN_PASSWORD = "POSTGRESQL_ADMIN_PASSWORD";

	public static final String POSTGRESQL_USER_KEY = POSTGRESQL_USER.replace("_", "-").toLowerCase();
	public static final String POSTGRESQL_PASSWORD_KEY = POSTGRESQL_PASSWORD.replace("_", "-").toLowerCase();
	public static final String POSTGRESQL_ADMIN_PASSWORD_KEY = POSTGRESQL_ADMIN_PASSWORD.replace("_", "-").toLowerCase();

	public PostgreSQLImageOpenShiftProvisioner(PostgreSQLImageOpenShiftApplication pgSQLApplication) {
		super(pgSQLApplication);
	}

	@Override
	public String getSymbolicName() {
		return "POSTGRESQL";
	}

	@Override
	public String getImage() {
		return IntersmashConfig.getPostgreSQLImage();
	}

	@Override
	public int getPort() {
		return 5432;
	}

	@Override
	public String getMountpath() {
		return "/var/lib/pgsql/data";
	}

	@Override
	protected void configureContainer(ContainerBuilder containerBuilder) {
		containerBuilder.addLivenessProbe().setInitialDelay(300).createTcpProbe("5432");
		containerBuilder.addReadinessProbe().setInitialDelaySeconds(5).createExecProbe("/bin/sh", "-i", "-c",
				"psql -h 127.0.0.1 -U $POSTGRESQL_USER -q -d $POSTGRESQL_DATABASE -c 'SELECT 1'");
	}

	@Override
	public Map<String, String> getImageVariables() {
		Map<String, String> vars = super.getImageVariables();
		vars.remove(POSTGRESQL_USER);
		vars.remove(POSTGRESQL_PASSWORD);
		vars.remove(POSTGRESQL_ADMIN_PASSWORD);
		vars.remove("POSTGRESQL_USERNAME");
		vars.put("POSTGRESQL_MAX_CONNECTIONS", "100");
		vars.put("POSTGRESQL_SHARED_BUFFERS", "16MB");
		vars.put("POSTGRESQL_MAX_PREPARED_TRANSACTIONS", "90");
		vars.put("POSTGRESQL_DATABASE", dbApplication.getDbName());
		// Temporary workaround for https://github.com/sclorg/postgresql-container/issues/297
		// Increase the "set_passwords.sh" timeout from the default 60s to 300s to give the
		// PostgreSQL server chance properly to start under high OCP cluster load
		vars.put("PGCTLTIMEOUT", "300");
		return vars;
	}

	@Override
	public void customizeApplication(ApplicationBuilder appBuilder) {
		// the secret is supposed to be used by applications connecting to the database
		appBuilder.secret(getSecretName())
				.setType(SecretType.OPAQUE)
				.addData(POSTGRESQL_USER_KEY, dbApplication.getUser().getBytes())
				.addData(POSTGRESQL_PASSWORD_KEY, dbApplication.getPassword().getBytes())
				.addData(POSTGRESQL_ADMIN_PASSWORD_KEY,
						dbApplication.getAdminPassword().getBytes());
		// the secret is also used to configure POSTGRESQL_USER, POSTGRESQL_PASSWORD, POSTGRESQL_ADMIN_PASSWORD
		appBuilder.deploymentConfig().podTemplate().container().configFromConfigMap(
				getSecretName(),
				(String t) -> t.replace("-", "_").toUpperCase(),
				POSTGRESQL_USER_KEY,
				POSTGRESQL_PASSWORD_KEY,
				POSTGRESQL_ADMIN_PASSWORD_KEY);
	}
}
