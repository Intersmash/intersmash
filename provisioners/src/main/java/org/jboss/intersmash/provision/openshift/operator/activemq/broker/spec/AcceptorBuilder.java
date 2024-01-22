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

import io.amq.broker.v1beta1.activemqartemisspec.Acceptors;

public class AcceptorBuilder {
	private Integer port;
	private Boolean verifyHost;
	private Boolean wantClientAuth;
	private Boolean expose;
	private String enabledCipherSuites;
	private Boolean needClientAuth;
	private String multicastPrefix;
	private String name;
	private Long connectionsAllowed;
	private Boolean sslEnabled;
	private String sniHost;
	private String enabledProtocols;
	private String protocols;
	private String sslSecret;
	private String sslProvider;
	private String anycastPrefix;

	/**
	 * Initialize the {@link AcceptorBuilder} with given resource name.
	 *
	 * @param name Resource object name
	 */
	public AcceptorBuilder(String name) {
		this.name = name;
	}

	/**
	 * Port number.
	 * @param port Desired port number
	 * @return this
	 */
	public AcceptorBuilder port(Integer port) {
		this.port = port;
		return this;
	}

	/**
	 * The CN of the connecting client's SSL certificate will be compared to its hostname to verify they match.
	 * This is useful only for 2-way SSL.
	 * @param verifyHost Whether host verification will be performed
	 * @return this
	 */
	public AcceptorBuilder verifyHost(Boolean verifyHost) {
		this.verifyHost = verifyHost;
		return this;
	}

	/**
	 * Tells a client connecting to this acceptor that 2-way SSL is requested but not required. Overridden by needClientAuth.
	 * @param wantClientAuth Whether client is requested to authenticate
	 * @return this
	 */
	public AcceptorBuilder wantClientAuth(Boolean wantClientAuth) {
		this.wantClientAuth = wantClientAuth;
		return this;
	}

	/**
	 * Whether or not to expose this acceptor.
	 * @param expose Whether or not to expose this acceptor
	 * @return this
	 */
	public AcceptorBuilder expose(Boolean expose) {
		this.expose = expose;
		return this;
	}

	/**
	 * Comma separated list of cipher suites used for SSL communication.
	 * @param enabledCipherSuites A comma separated list of cipher suites to be used
	 * @return this
	 */
	public AcceptorBuilder enabledCipherSuites(String enabledCipherSuites) {
		this.enabledCipherSuites = enabledCipherSuites;
		return this;
	}

	/**
	 * Tells a client connecting to this acceptor that 2-way SSL is required.
	 * This property takes precedence over {@link #wantClientAuth}.
	 * @param needClientAuth Whether client is required to authenticate
	 * @return this
	 */
	public AcceptorBuilder needClientAuth(Boolean needClientAuth) {
		this.needClientAuth = needClientAuth;
		return this;
	}

	/**
	 * To indicate which kind of routing type to use..
	 * @param multicastPrefix Prefix for the multicast routing type
	 * @return this
	 */
	public AcceptorBuilder multicastPrefix(String multicastPrefix) {
		this.multicastPrefix = multicastPrefix;
		return this;
	}

	/**
	 * Limits the number of connections which the acceptor will allow. When this limit is reached a DEBUG level
	 * message is issued to the log, and the connection is refused.
	 * @param connectionsAllowed Maximum number of allowed connections
	 * @return this
	 */
	public AcceptorBuilder connectionsAllowed(Long connectionsAllowed) {
		this.connectionsAllowed = connectionsAllowed;
		return this;
	}

	/**
	 * Whether or not to enable SSL on this port.
	 * @param sslEnabled Whether SSL is enabled
	 * @return this
	 */
	public AcceptorBuilder sslEnabled(Boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
		return this;
	}

	/**
	 * A regular expression used to match the server_name extension on incoming SSL connections. If the name doesn't
	 * match then the connection to the acceptor will be rejected.
	 * @param sniHost Regex that should match a SNI host name
	 * @return this
	 */
	public AcceptorBuilder sniHost(String sniHost) {
		this.sniHost = sniHost;
		return this;
	}

	/**
	 * Comma separated list of protocols used for SSL communication.
	 * @param enabledProtocols Comma separated list of SSL protocols to be used
	 * @return this
	 */
	public AcceptorBuilder enabledProtocols(String enabledProtocols) {
		this.enabledProtocols = enabledProtocols;
		return this;
	}

	/**
	 * The protocols to enable for this acceptor.
	 * @param protocols Protocols to be used
	 * @return this
	 */
	public AcceptorBuilder protocols(String protocols) {
		this.protocols = protocols;
		return this;
	}

	/**
	 * Name of the secret to use for ssl information.
	 * @param sslSecret Name of the desired secret that should be used for SSL
	 * @return this
	 */
	public AcceptorBuilder sslSecret(String sslSecret) {
		this.sslSecret = sslSecret;
		return this;
	}

	/**
	 * Used to change the SSL Provider between JDK and OPENSSL. The default is JDK.
	 * @param sslProvider Name of the desired provider for SSL
	 * @return this
	 */
	public AcceptorBuilder sslProvider(String sslProvider) {
		this.sslProvider = sslProvider;
		return this;
	}

	/**
	 * To indicate which kind of routing type to use.
	 * @param anycastPrefix Prefix for the desired routing type
	 * @return this
	 */
	public AcceptorBuilder anycastPrefix(String anycastPrefix) {
		this.anycastPrefix = anycastPrefix;
		return this;
	}

	public Acceptors build() {
		Acceptors acceptor = new Acceptors();
		acceptor.setPort(port);
		acceptor.setVerifyHost(verifyHost);
		acceptor.setWantClientAuth(wantClientAuth);
		acceptor.setExpose(expose);
		acceptor.setEnabledCipherSuites(enabledCipherSuites);
		acceptor.setNeedClientAuth(needClientAuth);
		acceptor.setMulticastPrefix(multicastPrefix);
		acceptor.setName(name);
		acceptor.setConnectionsAllowed(connectionsAllowed);
		acceptor.setSslEnabled(sslEnabled);
		acceptor.setSniHost(sniHost);
		acceptor.setEnabledProtocols(enabledProtocols);
		acceptor.setProtocols(protocols);
		acceptor.setSslSecret(sslSecret);
		acceptor.setSslProvider(sslProvider);
		acceptor.setAnycastPrefix(anycastPrefix);
		return acceptor;
	}
}
