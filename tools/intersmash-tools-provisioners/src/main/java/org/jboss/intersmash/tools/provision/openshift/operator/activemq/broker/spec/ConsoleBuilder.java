package org.jboss.intersmash.tools.provision.openshift.operator.activemq.broker.spec;

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
