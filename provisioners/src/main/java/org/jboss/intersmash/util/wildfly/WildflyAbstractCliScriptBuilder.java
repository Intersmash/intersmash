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
package org.jboss.intersmash.util.wildfly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Class provide supports for building CLI commands that are used to configure WILDFLY on OpenShift via custom scripts and
 * config map or on Baremetal
 */
abstract public class WildflyAbstractCliScriptBuilder {

	private boolean batch;
	private List<String> commands;

	public WildflyAbstractCliScriptBuilder() {
		this.batch = false;
		this.commands = new ArrayList<>();
	}

	public WildflyAbstractCliScriptBuilder batch() {
		batch = true;
		return this;
	}

	public WildflyAbstractCliScriptBuilder addCommand(final String command) {
		Objects.requireNonNull(command);
		commands.add(command);
		return this;
	}

	public WildflyAbstractCliScriptBuilder addCommands(final List<String> commands) {
		Objects.requireNonNull(commands);
		this.commands.addAll(commands);
		return this;
	}

	public WildflyAbstractCliScriptBuilder commands(final List<String> commands) {
		Objects.requireNonNull(commands);
		this.commands = new ArrayList<>(commands);
		return this;
	}

	protected List<String> build(String profile) {
		List<String> commands = new ArrayList<>();
		// TODO: not sure about this
		if (profile != null) {
			commands.add("embed-server --std-out=echo  --server-config=" + profile);
		}
		if (batch)
			commands.add("batch");
		commands.addAll(this.commands);
		if (batch)
			commands.add("run-batch");
		if (profile != null) {
			commands.add("quit");
		}

		return Collections.unmodifiableList(commands);
	}

	abstract public List<String> build();
}
