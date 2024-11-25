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
package org.jboss.intersmash.provision.operator.model.activemq.broker.spec;

import io.amq.broker.v1beta1.activemqartemisspec.Console;

public class ConsoleBuilder {
	private Boolean expose;
	private Boolean sslEnabled;
	private String sslSecret;
	private Boolean useClientAuth;

	/**
	 * Whether or not to expose this port.
	 *
	 * @param expose Whether the console is exposed
	 * @return this
	 */
	public ConsoleBuilder expose(Boolean expose) {
		this.expose = expose;
		return this;
	}

	/**
	 * Whether or not to enable SSL on this port.
	 *
	 * @param sslEnabled Whether SSL is enabled on the console port
	 * @return this
	 */
	public ConsoleBuilder sslEnabled(Boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
		return this;
	}

	/**
	 * Name of the secret to use for ssl information.
	 *
	 * @param sslSecret Name of desired secret for SSL
	 * @return this
	 */
	public ConsoleBuilder sslSecret(String sslSecret) {
		this.sslSecret = sslSecret;
		return this;
	}

	/**
	 * Whether the embedded server requires client authentication.
	 *
	 * @param useClientAuth Whether the client authentication is required
	 * @return this
	 */
	public ConsoleBuilder useClientAuth(Boolean useClientAuth) {
		this.useClientAuth = useClientAuth;
		return this;
	}

	public Console build() {
		Console console = new Console();
		console.setExpose(expose);
		console.setSslEnabled(sslEnabled);
		console.setSslSecret(sslSecret);
		console.setUseClientAuth(useClientAuth);
		return console;
	}
}
