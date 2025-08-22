/*
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
package org.jboss.intersmash.provision.openshift;

import java.util.Map;

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.openshift.PostgreSQLImageOpenShiftApplication;

import cz.xtf.builder.builders.ApplicationBuilder;
import cz.xtf.builder.builders.pod.ContainerBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostgreSQLImageOpenShiftProvisioner extends DBImageOpenShiftProvisioner<PostgreSQLImageOpenShiftApplication> {

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
		vars.remove(PostgreSQLImageOpenShiftApplication.POSTGRESQL_USER);
		vars.remove(PostgreSQLImageOpenShiftApplication.POSTGRESQL_PASSWORD);
		vars.remove(PostgreSQLImageOpenShiftApplication.POSTGRESQL_ADMIN_PASSWORD);
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
		// the application secret is used to configure the PostgreSql container env vars, such as POSTGRESQL_USER,
		// POSTGRESQL_PASSWORD, POSTGRESQL_ADMIN_PASSWORD
		appBuilder.deploymentConfig().podTemplate().container().configFromConfigMap(
				getApplication().getApplicationSecretName(),
				(String t) -> t.replace("-", "_").toUpperCase(),
				PostgreSQLImageOpenShiftApplication.POSTGRESQL_USER_KEY,
				PostgreSQLImageOpenShiftApplication.POSTGRESQL_PASSWORD_KEY,
				PostgreSQLImageOpenShiftApplication.POSTGRESQL_ADMIN_PASSWORD_KEY);
	}
}
