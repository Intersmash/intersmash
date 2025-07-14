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
package org.jboss.intersmash.util;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import cz.xtf.core.config.OpenShiftConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * CL based implementation of a class for generating keystores signed by one common generated Certification Authority
 */
@Slf4j
public class CommandLineBasedKeystoreGenerator {
	public static final String KEYSTORE_FILE_EXTENSION = ".keystore";
	private static Path TMP_DIRECTORY = Paths.get("tmp").toAbsolutePath().resolve("keystores");
	@Getter
	private static Path caDir;
	@Getter
	private static Path truststore;
	@Getter
	private static final String password = "password";
	private static final String INTERSMASH_CA = "Intersmash.ca";
	private static final String TRUSTSTORE_FILE_NAME = "truststore";
	public static final String CA_CERTIFICATE_PEM_FILE_NAME = "ca-certificate.pem";

	/**
	 * Statically set everything up in order to have private key, certificate and truststore ready
	 */
	static {
		try {
			TMP_DIRECTORY.toFile().mkdirs();
			caDir = Files.createTempDirectory(TMP_DIRECTORY, "ca");
			// Generate key and cert
			processCall(caDir, "openssl",
					"req",
					"-new",
					"-newkey",
					"rsa:4096",
					"-x509",
					"-keyout",
					"ca-key.pem",
					"-out",
					CA_CERTIFICATE_PEM_FILE_NAME,
					"-days",
					"365",
					"-passout",
					"pass:" + password,
					"-subj",
					"/C=CZ/ST=CZ/L=Brno/O=QE/CN=" + INTERSMASH_CA);
			// Generate truststore that includes the generated CA cert
			processCall(caDir, "keytool",
					"-import",
					"-noprompt",
					"-keystore",
					TRUSTSTORE_FILE_NAME,
					"-file",
					CA_CERTIFICATE_PEM_FILE_NAME,
					"-alias",
					INTERSMASH_CA,
					"-storepass",
					password);
			// Download and import the openshift server certs into the generated truststore
			processCall(caDir, "/bin/sh",
					"-c",
					"echo \"Q\" | openssl s_client -connect " + getOpenShiftHostAndPort()
							+ " -showcerts 2>/dev/null > serversOpenSslResponse");
			processCall(caDir, "/bin/sh",
					"-c",
					"csplit -f serverCert -s serversOpenSslResponse '/^-----BEGIN CERTIFICATE-----$/' '{*}'");
			processCall(caDir, "/bin/sh",
					"-c",
					"find . -type f -not -name \"serverCert00\" -name \"serverCert[0-9][0-9]\" -exec openssl x509 -in {} -out {}.pem \\;");
			processCall(caDir, "/bin/sh",
					"-c",
					"find . -type f -name \"serverCert[0-9][0-9].pem\" -exec keytool -import -noprompt -keystore "
							+ TRUSTSTORE_FILE_NAME + " -file {} -alias {} -storepass " + password + " \\;");
			truststore = caDir.resolve(TRUSTSTORE_FILE_NAME);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to initialize Intersmash CA", e);
		}
	}

	public static Path generateKeystore(String hostname) {
		return generateKeystore(hostname, null, hostname, false);
	}

	public static Path generateKeystore(String hostname, String keyAlias) {
		return generateKeystore(hostname, null, keyAlias, false);
	}

	public static Path generateKeystore(String hostname, String keyAlias, boolean deleteCaFromKeyStore) {
		return generateKeystore(hostname, null, keyAlias, deleteCaFromKeyStore);
	}

	public static Path generateKeystore(String hostname, String[] alternativeHostnames) {
		return generateKeystore(hostname, alternativeHostnames, hostname, false);
	}

	public static Path generateKeystore(String hostname, String[] alternativeHostnames, String keyAlias,
			boolean deleteCaFromKeyStore) {
		final String keystore = hostname + KEYSTORE_FILE_EXTENSION;
		if (caDir.resolve(keystore).toFile().exists()) {
			return caDir.resolve(keystore);
		}
		processCall(caDir, "keytool",
				"-genkeypair",
				"-keyalg",
				"RSA",
				"-noprompt",
				"-alias",
				keyAlias,
				"-dname",
				"CN=" + hostname + ", OU=TF, O=XTF, L=Brno, S=CZ, C=CZ",
				"-keystore",
				keystore,
				"-storepass",
				password,
				"-keypass",
				password,
				"-deststoretype",
				"pkcs12");
		processCall(caDir, "keytool",
				"-keystore",
				keystore,
				"-certreq",
				"-alias",
				keyAlias,
				"--keyalg",
				"rsa",
				"-file",
				hostname + ".csr",
				"-storepass",
				password);
		// When provided, injects Subject Alternative Names (SAN) within the certificate
		if (alternativeHostnames != null && alternativeHostnames.length > 0) {
			try {
				Writer writer = new FileWriter(caDir.resolve(keyAlias + ".extensions").toFile());
				writer.write("[ req_ext ]\n");
				writer.write("subjectAltName = @alt_names\n");
				writer.write("\n");
				writer.write("[ alt_names ]\n");

				writer.write("DNS.1 = " + hostname + "\n");
				for (int i = 0; i < alternativeHostnames.length; ++i) {
					writer.write("DNS." + (i + 2) + " = " + alternativeHostnames[i] + "\n");
				}
				writer.flush();
				writer.close();
				processCall(caDir, "openssl",
						"x509",
						"-req",
						"-CA",
						CA_CERTIFICATE_PEM_FILE_NAME,
						"-CAkey",
						"ca-key.pem",
						"-in",
						hostname + ".csr",
						"-out", hostname + ".cer",
						"-days", "365",
						"-CAcreateserial",
						"-passin",
						"pass:" + password,
						"-extfile",
						keyAlias + ".extensions",
						"-extensions",
						"req_ext");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			processCall(caDir, "openssl",
					"x509",
					"-req",
					"-CA",
					CA_CERTIFICATE_PEM_FILE_NAME,
					"-CAkey",
					"ca-key.pem",
					"-in",
					hostname + ".csr",
					"-out",
					hostname + ".cer",
					"-days",
					"365",
					"-CAcreateserial",
					"-passin",
					"pass:" + password);
		}
		processCall(caDir, "keytool",
				"-import",
				"-noprompt",
				"-keystore",
				keystore,
				"-file",
				CA_CERTIFICATE_PEM_FILE_NAME,
				"-alias",
				INTERSMASH_CA,
				"-storepass",
				password);
		processCall(caDir, "keytool",
				"-import",
				"-keystore",
				keystore,
				"-file",
				hostname + ".cer",
				"-alias",
				keyAlias,
				"-storepass", password);
		if (deleteCaFromKeyStore) {
			processCall(caDir, "keytool",
					"-delete",
					"-noprompt",
					"-alias", INTERSMASH_CA,
					"-keystore",
					keystore,
					"-storepass",
					password);
		}
		return caDir.resolve(keystore);
	}

	public static GeneratedPaths generateCerts(String hostname) {
		return generateCerts(hostname, null);
	}

	public static GeneratedPaths generateCerts(String hostname, String[] alternativeHostnames) {
		final String keystore = hostname + KEYSTORE_FILE_EXTENSION;
		generateKeystore(hostname, alternativeHostnames);
		// export cert as CN.keystore.pem
		processCall(caDir, "keytool",
				"-exportcert",
				"-rfc",
				"-keystore",
				keystore,
				"-alias",
				hostname,
				"-storepass",
				password,
				"-file",
				keystore + ".pem");
		// convert to CN.keystore.keywithattrs.pem
		processCall(caDir, "openssl",
				"pkcs12",
				"-in",
				keystore,
				"-nodes",
				"-nocerts",
				"-out",
				keystore + ".keywithattrs.pem",
				"-passin",
				"pass:" + password);
		// Remove the Bag attributes to CN.keystore.key.pem
		processCall(caDir, "openssl",
				"rsa",
				"-in",
				keystore + ".keywithattrs.pem",
				"-out",
				keystore + ".key.pem");
		return new GeneratedPaths(
				caDir.resolve(CA_CERTIFICATE_PEM_FILE_NAME),
				caDir.resolve(TRUSTSTORE_FILE_NAME),
				caDir.resolve(keystore),
				caDir.resolve(keystore + ".key.pem"),
				caDir.resolve(keystore + ".pem"));
	}

	private static String getOpenShiftHostAndPort() {
		String server = OpenShiftConfig.url().replaceFirst("https://", "");
		server = server.endsWith("/") ? server.substring(0, server.length() - 1) : server;
		server = server.contains(":") ? server : server + ":443";
		return server;
	}

	private static void processCall(Path cwd, String... args) {
		ProcessBuilder pb = new ProcessBuilder(args);

		pb.directory(cwd.toFile());
		pb.redirectErrorStream(true);

		int result = -1;
		Process process = null;
		try {
			process = pb.start();
			result = process.waitFor();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				while (reader.ready()) {
					if (result == 0) {
						log.debug(reader.readLine());
					} else {
						log.error(reader.readLine());
					}
				}
			}
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException("Failed executing " + String.join(" ", args));
		}
		if (result != 0) {
			throw new IllegalStateException("Failed executing " + String.join(" ", args));
		}
	}

	/**
	 * POJO to hold the path of the generated certs and stores
	 */
	public static class GeneratedPaths {
		public Path caPem;
		public Path truststore;
		public Path keystore;
		public Path keyPem;
		public Path certPem;

		public GeneratedPaths(final Path caPem, final Path truststore, final Path keystore, final Path keyPem,
				final Path certPem) {
			this.caPem = caPem;
			this.truststore = truststore;
			this.keystore = keystore;
			this.keyPem = keyPem;
			this.certPem = certPem;
		}
	}
}
