package org.jboss.intersmash.tools.util.tls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.fabric8.kubernetes.api.model.Secret;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CertificatesUtils {

	@Getter
	private static Path caDir;

	static {
		try {
			caDir = Files.createTempDirectory(CertificatesUtils.class.getSimpleName().toLowerCase());
		} catch (IOException e) {
			throw new RuntimeException("Failed to create temp directory!");
		}
	}

	public static class CertificateAndKey {
		public Path certificate;
		public Path key;
		public Path truststore;
		public String truststorePassword;
		public String truststoreAlias;
		public boolean existing = false;
	}

	/**
	 * Generates a key and self-signed certificate for that key; it also generates a truststore containing the certificate;
	 * @param hostname: to be used as Common Name (CN) for the certificate
	 * @param tlsSecretName: name of the secret to be created in OpenShift containing key and certificate
	 * @return wrapper object {@link CertificateAndKey} containing details about the newly created key, certificate and secret
	 */
	public static CertificateAndKey generateSelfSignedCertificateAndKey(String hostname, String tlsSecretName) {
		CertificateAndKey certificateAndKey = new CertificateAndKey();

		String certificate = hostname + "-certificate.pem";
		String key = hostname + "-key.pem";
		String truststoreFormat = "jks";
		String truststorePassword = "1234PIPPOBAUDO";
		String truststore = hostname + "-truststore." + truststoreFormat;

		certificateAndKey.key = Paths.get(caDir.toFile().getAbsolutePath(), key);
		certificateAndKey.certificate = Paths.get(caDir.toFile().getAbsolutePath(), certificate);
		certificateAndKey.truststore = Paths.get(caDir.toFile().getAbsolutePath(), truststore);
		certificateAndKey.truststoreAlias = hostname;
		certificateAndKey.truststorePassword = truststorePassword;

		if (caDir.resolve(certificate).toFile().exists() &&
				caDir.resolve(key).toFile().exists() &&
				caDir.resolve(truststore).toFile().exists()) {
			certificateAndKey.existing = true;
			return certificateAndKey;
		}

		// create key + self-signed certificate: they are typically used by the server exposing the endpoints over TLS
		processCall(caDir, "openssl", "req", "-subj", "/CN=" + hostname + "/OU=TF/O=XTF/L=Milan/C=IT",
				"-newkey", "rsa:2048", "-nodes", "-keyout", key, "-x509", "-days", "365", "-out", certificate);

		// add self-signed certificate to keystore: it's typically used by the clients contacting the endpoints over TLS
		processCall(caDir, "keytool", "-import", "-noprompt", "-alias", hostname, "-keystore",
				truststore, "-file", certificate, "-storetype", "JKS", "-storepass", truststorePassword);

		return certificateAndKey;
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
			throw new RuntimeException("Failed executing " + String.join(" ", args));
		}

		if (result != 0) {
			throw new RuntimeException("Failed executing " + String.join(" ", args));
		}
	}
}
