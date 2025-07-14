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

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.application.openshift.MysqlImageOpenShiftApplication;

import cz.xtf.builder.builders.pod.ContainerBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlImageOpenShiftProvisioner extends DBImageOpenShiftProvisioner<MysqlImageOpenShiftApplication> {

	public MysqlImageOpenShiftProvisioner(MysqlImageOpenShiftApplication mysqlApplication) {
		super(mysqlApplication);
	}

	@Override
	public String getSymbolicName() {
		return "MYSQL";
	}

	@Override
	public String getImage() {
		return IntersmashConfig.getMysqlImage();
	}

	@Override
	public int getPort() {
		return 3306;
	}

	@Override
	public String getMountpath() {
		return "/var/lib/mysql/data";
	}

	@Override
	protected void configureContainer(ContainerBuilder containerBuilder) {
		containerBuilder.addLivenessProbe().setInitialDelay(30).createTcpProbe("3306");
		containerBuilder.addReadinessProbe().setInitialDelaySeconds(5).createExecProbe("/bin/sh", "-i", "-c",
				"MYSQL_PWD=\"$MYSQL_PASSWORD\" mysql -h 127.0.0.1 -u $MYSQL_USER -D $MYSQL_DATABASE -e 'SELECT 1'");
	}
}
