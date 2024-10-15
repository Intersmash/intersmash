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
package org.jboss.intersmash.provision.operator.model.keycloak.keycloak.spec;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.v1alpha1.keycloakspec.keycloakdeploymentspec.Experimental;
import org.keycloak.v1alpha1.keycloakspec.keycloakdeploymentspec.experimental.Env;
import org.keycloak.v1alpha1.keycloakspec.keycloakdeploymentspec.experimental.Volumes;
import org.keycloak.v1alpha1.keycloakspec.keycloakdeploymentspec.experimental.volumes.Items;

/**
 * Experimental section
 * NOTE: This section might change or get removed without any notice. It may also cause the deployment to behave
 * in an unpredictable fashion. Please use with care.
 */
public final class ExperimentalSpecBuilder {
	private List<String> args;
	private List<String> command;
	private List<Env> env;
	private Volumes volumes;

	/**
	 * Set the arguments to the entrypoint. Translates into Container CMD.
	 *
	 * @param args List of arguments to the entrypoint
	 * @return this
	 */
	public ExperimentalSpecBuilder args(List<String> args) {
		this.args = args;
		return this;
	}

	/**
	 * Add one argument to the entrypoint. Translates into Container CMD.
	 *
	 * @param arg Argument to the entrypoint
	 * @return this
	 */
	public ExperimentalSpecBuilder args(String arg) {
		if (args == null) {
			args = new ArrayList<>();
		}
		args.add(arg);
		return this;
	}

	/**
	 * Set container commands. Translates into Container ENTRYPOINT.
	 *
	 * @param command List of commands to the entrypoint
	 * @return this
	 */
	public ExperimentalSpecBuilder command(List<String> command) {
		this.command = command;
		return this;
	}

	/**
	 * Add one container command. Translates into Container ENTRYPOINT.
	 *
	 * @param command Command to the entrypoint
	 * @return this
	 */
	public ExperimentalSpecBuilder command(String command) {
		if (this.command == null) {
			this.command = new ArrayList<>();
		}
		this.command.add(command);
		return this;
	}

	/**
	 * List of environment variables to set in the container.
	 *
	 * @param env List of environment variables
	 * @return this
	 */
	public ExperimentalSpecBuilder env(List<Env> env) {
		this.env = env;
		return this;
	}

	/**
	 * Add one environment variable to set in the container.
	 *
	 * @param env Environment variable that should be added
	 * @return this
	 */
	public ExperimentalSpecBuilder env(Env env) {
		if (this.env == null) {
			this.env = new ArrayList<>();
		}
		this.env.add(env);
		return this;
	}

	/**
	 * Additional volume mounts.
	 *
	 * @param volumes {@link Items} instance storing volume mount definitions
	 * @return this
	 */
	public ExperimentalSpecBuilder volumes(Volumes volumes) {
		this.volumes = volumes;
		return this;
	}

	public Experimental build() {
		Experimental experimentalSpec = new Experimental();
		experimentalSpec.setArgs(args);
		experimentalSpec.setCommand(command);
		experimentalSpec.setEnv(env);
		experimentalSpec.setVolumes(volumes);
		return experimentalSpec;
	}
}
