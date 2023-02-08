package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.api.model.EnvVar;

/**
 * Experimental section
 * NOTE: This section might change or get removed without any notice. It may also cause the deployment to behave
 * in an unpredictable fashion. Please use with care.
 */
public final class ExperimentalSpecBuilder {
	private List<String> args;
	private List<String> command;
	private List<EnvVar> env;
	private VolumesSpec volumes;

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
	public ExperimentalSpecBuilder env(List<EnvVar> env) {
		this.env = env;
		return this;
	}

	/**
	 * Add one environment variable to set in the container.
	 *
	 * @param env Environment variable that should be added
	 * @return this
	 */
	public ExperimentalSpecBuilder env(EnvVar env) {
		if (this.env == null) {
			this.env = new ArrayList<>();
		}
		this.env.add(env);
		return this;
	}

	/**
	 * Additional volume mounts.
	 *
	 * @param volumes {@link VolumesSpec} instance storing volume mount definitions
	 * @return this
	 */
	public ExperimentalSpecBuilder volumes(VolumesSpec volumes) {
		this.volumes = volumes;
		return this;
	}

	public ExperimentalSpec build() {
		ExperimentalSpec experimentalSpec = new ExperimentalSpec();
		experimentalSpec.setArgs(args);
		experimentalSpec.setCommand(command);
		experimentalSpec.setEnv(env);
		experimentalSpec.setVolumes(volumes);
		return experimentalSpec;
	}
}
