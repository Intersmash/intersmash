/*
 * Copyright (C) 2025 Red Hat, Inc.
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
package org.jboss.intersmash.tools.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jboss.intersmash.tools.config.WaitingConfig;
import org.jboss.intersmash.tools.waiting.SimpleWaiter;
import org.jboss.intersmash.tools.waiting.Waiter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpsUtils {

	public static int getCode(String url) {
		try {
			HttpURLConnection connection;
			if (url.startsWith("https")) {
				connection = getHttpsConnection(urlFromString(url));
			} else {
				connection = getHttpConnection(urlFromString(url));
			}
			connection.connect();
			int code = connection.getResponseCode();
			connection.disconnect();
			return code;
		} catch (IOException e) {
			throw new HttpsException(e);
		}
	}

	public static Waiter doesUrlReturnOK(String url) {
		return new SimpleWaiter(() -> {
			try {
				return getCode(url) == 200;
			} catch (Exception e) {
				return false;
			}
		}, TimeUnit.MILLISECONDS, WaitingConfig.timeout(), "Waiting for URL to return OK: " + url);
	}

	public static void copyHttpsURLToFile(String source, File destination, int connectionTimeout, int readTimeout)
			throws IOException {
		URLConnection connection = getHttpsConnection(urlFromString(source));
		connection.setConnectTimeout(connectionTimeout);
		connection.setReadTimeout(readTimeout);
		try (InputStream is = connection.getInputStream()) {
			Files.copy(is, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static HttpURLConnection getHttpConnection(URL url) {
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			return connection;
		} catch (IOException e) {
			throw new HttpsException(e);
		}
	}

	private static HttpsURLConnection getHttpsConnection(URL url) {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[] { new TrustAllManager() }, new SecureRandom());

			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setSSLSocketFactory(sslContext.getSocketFactory());
			connection.setHostnameVerifier((s, session) -> true);
			connection.setRequestMethod("GET");

			return connection;
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new HttpsException("TLS setup failed", e);
		} catch (IOException e) {
			throw new HttpsException(e);
		}
	}

	private static URL urlFromString(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new HttpsException("Invalid url: " + url);
		}
	}

	private static class TrustAllManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}
}
