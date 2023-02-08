package org.jboss.intersmash.tools.provision.openshift;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.MysqlImageOpenShiftApplication;

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
