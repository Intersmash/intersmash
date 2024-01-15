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
package org.jboss.intersmash.provision.openshift.operator.activemq.broker.spec;

import io.amq.broker.v1beta1.activemqartemisspec.Connectors;

public class ConnectorBuilder {
	private Integer port;
	private Boolean verifyHost;
	private Boolean wantClientAuth;
	private Boolean expose;
	private String enabledCipherSuites;
	private String host;
	private Boolean needClientAuth;
	private String name;
	private Boolean sslEnabled;
	private String sniHost;
	private String enabledProtocols;
	private String type;
	private String sslSecret;
	private String sslProvider;

	/**
	 * Initialize the {@link ConnectorBuilder} with given resource name.
	 *
	 * @param name Resource object name
	 */
	public ConnectorBuilder(String name) {
		this.name = name;
	}

	/**
	 * Set the port number.
	 *
	 * @param port The desired port number
	 * @return this
	 */
	public ConnectorBuilder port(Integer port) {
		this.port = port;
		return this;
	}

	/**
	 * Whether the CN of the connecting client's SSL certificate will be compared to its hostname to verify they match.
	 * This is useful only for 2-way SSL.
	 *
	 * @param verifyHost Whether to verify the remote host
	 * @return this
	 */
	public ConnectorBuilder verifyHost(Boolean verifyHost) {
		this.verifyHost = verifyHost;
		return this;
	}

	/**
	 * Tells a client connecting to this acceptor that 2-way SSL is requested but not required. Overridden by needClientAuth.
	 *
	 * @param wantClientAuth Whether authentication from client is required too
	 * @return this
	 */
	public ConnectorBuilder wantClientAuth(Boolean wantClientAuth) {
		this.wantClientAuth = wantClientAuth;
		return this;
	}

	/**
	 * Whether or not to expose this acceptor.
	 *
	 * @param expose Whether the connector is exposed
	 * @return this
	 */
	public ConnectorBuilder expose(Boolean expose) {
		this.expose = expose;
		return this;
	}

	/**
	 * Comma separated list of cipher suites used for SSL communication.
	 *
	 * @param enabledCipherSuites Comma separated list of cipher suites that should be enabled
	 * @return this
	 */
	public ConnectorBuilder enabledCipherSuites(String enabledCipherSuites) {
		this.enabledCipherSuites = enabledCipherSuites;
		return this;
	}

	/**
	 * Hostname or IP to connect to.
	 *
	 * @param host The name of the hot to connect
	 * @return this
	 */
	public ConnectorBuilder host(String host) {
		this.host = host;
		return this;
	}

	/**
	 * Tells a client connecting to this acceptor that 2-way SSL is required.
	 * This property takes precedence over wantClientAuth.
	 *
	 * @param needClientAuth Whether authentication from client is required
	 * @return this
	 */
	public ConnectorBuilder needClientAuth(Boolean needClientAuth) {
		this.needClientAuth = needClientAuth;
		return this;
	}

	/**
	 * Whether or not to enable SSL on this port.
	 *
	 * @param sslEnabled Whether SSL is enabled on the connector port
	 * @return this
	 */
	public ConnectorBuilder sslEnabled(Boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
		return this;
	}

	/**
	 * A regular expression used to match the server_name extension on incoming SSL connections. If the name doesn't
	 * match then the connection to the acceptor will be rejected.
	 *
	 * @param sniHost The desired SNI host
	 * @return this
	 */
	public ConnectorBuilder sniHost(String sniHost) {
		this.sniHost = sniHost;
		return this;
	}

	/**
	 * Comma separated list of protocols used for SSL communication.
	 *
	 * @param enabledProtocols Comma separated list of protocols that should be used for SSL communication
	 * @return this
	 */
	public ConnectorBuilder enabledProtocols(String enabledProtocols) {
		this.enabledProtocols = enabledProtocols;
		return this;
	}

	/**
	 * The type, either tcp or vm.
	 *
	 * @param type Type of desired connector
	 * @return this
	 */
	public ConnectorBuilder type(String type) {
		this.type = type;
		return this;
	}

	/**
	 * Name of the secret to use for ssl information.
	 *
	 * @param sslSecret Name of desired secret for SSL
	 * @return this
	 */
	public ConnectorBuilder sslSecret(String sslSecret) {
		this.sslSecret = sslSecret;
		return this;
	}

	/**
	 * Used to change the SSL Provider between JDK and OPENSSL. The default is JDK.
	 *
	 * @param sslProvider Name of the desired provider
	 * @return this
	 */
	public ConnectorBuilder sslProvider(String sslProvider) {
		this.sslProvider = sslProvider;
		return this;
	}

	public Connectors build() {
		Connectors connector = new Connectors();
		connector.setPort(port);
		connector.setVerifyHost(verifyHost);
		connector.setWantClientAuth(wantClientAuth);
		connector.setExpose(expose);
		connector.setEnabledCipherSuites(enabledCipherSuites);
		connector.setHost(host);
		connector.setNeedClientAuth(needClientAuth);
		connector.setName(name);
		connector.setSslEnabled(sslEnabled);
		connector.setSniHost(sniHost);
		connector.setEnabledProtocols(enabledProtocols);
		connector.setType(type);
		connector.setSslSecret(sslSecret);
		connector.setSslProvider(sslProvider);
		return connector;
	}
}
