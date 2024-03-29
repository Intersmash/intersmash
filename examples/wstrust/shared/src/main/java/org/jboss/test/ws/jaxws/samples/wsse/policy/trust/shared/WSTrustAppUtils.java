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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class WSTrustAppUtils {

	public static String getServerHost() {
		final String host = System.getProperty("jboss.bind.address", "localhost");
		return toIPv6URLFormat(host);
	}

	private static String toIPv6URLFormat(final String host) {
		try {
			if (host.startsWith("[") || host.startsWith(":")) {
				if (System.getProperty("java.net.preferIPv4Stack") == null) {
					throw new IllegalStateException(
							"always provide java.net.preferIPv4Stack JVM property when using IPv6 address format");
				}
				if (System.getProperty("java.net.preferIPv6Addresses") == null) {
					throw new IllegalStateException(
							"always provide java.net.preferIPv6Addresses JVM property when using IPv6 address format");
				}
			}
			final boolean isIPv6Address = InetAddress.getByName(host) instanceof Inet6Address;
			final boolean isIPv6Formatted = isIPv6Address && host.startsWith("[");
			return isIPv6Address && !isIPv6Formatted ? "[" + host + "]" : host;
		} catch (final UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
}
