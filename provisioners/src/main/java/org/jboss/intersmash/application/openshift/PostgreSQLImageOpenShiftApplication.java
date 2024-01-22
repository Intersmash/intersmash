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
package org.jboss.intersmash.application.openshift;

import java.util.Collections;
import java.util.List;

import org.jboss.intersmash.provision.openshift.PostgreSQLImageOpenShiftProvisioner;

import cz.xtf.builder.builders.SecretBuilder;
import cz.xtf.builder.builders.secret.SecretType;
import io.fabric8.kubernetes.api.model.Secret;

/**
 * End user Application interface which presents PostgreSQL image application on OpenShift Container Platform.
 *
 * PostgreSQL application that is supposed to run on OpenShift needs to implement this interface.
 * Usage:
 * <pre>
 *     &#064;Intersmash(
 *     		&#064;Service(PgSQLApp.class)
 *     })
 * </pre>
 *
 * The application will be deployed by:
 * <ul>
 *     <li>{@link PostgreSQLImageOpenShiftProvisioner}</li>
 * </ul>
 */
public interface PostgreSQLImageOpenShiftApplication extends DBImageOpenShiftApplication, HasSecrets {

	String POSTGRESQL_USER = "POSTGRESQL_USER";
	String POSTGRESQL_PASSWORD = "POSTGRESQL_PASSWORD";
	String POSTGRESQL_ADMIN_PASSWORD = "POSTGRESQL_ADMIN_PASSWORD";

	String POSTGRESQL_USER_KEY = POSTGRESQL_USER.replace("_", "-").toLowerCase();
	String POSTGRESQL_PASSWORD_KEY = POSTGRESQL_PASSWORD.replace("_", "-").toLowerCase();
	String POSTGRESQL_ADMIN_PASSWORD_KEY = POSTGRESQL_ADMIN_PASSWORD.replace("_", "-").toLowerCase();

	/**
	 * @return name of the secret containing username and password for the database
	 */
	default String getApplicationSecretName() {
		return getName() + "-credentials";
	}

	default String getName() {
		return "postgresql";
	}

	default String getAdminPassword() {
		return "admin123";
	}

	@Override
	default List<Secret> getSecrets() {
		return Collections.singletonList(new SecretBuilder(getApplicationSecretName())
				.setType(SecretType.OPAQUE).addData(POSTGRESQL_USER_KEY, getUser().getBytes())
				.addData(POSTGRESQL_PASSWORD_KEY, getPassword().getBytes())
				.addData(POSTGRESQL_ADMIN_PASSWORD_KEY,
						getAdminPassword().getBytes())
				.build());
	}
}
