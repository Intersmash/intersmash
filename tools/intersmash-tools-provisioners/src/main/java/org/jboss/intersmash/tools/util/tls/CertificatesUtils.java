package org.jboss.intersmash.tools.util.tls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
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
		public Secret tlsSecret;
	}

	/**
	 * Generates a key and self-signed certificate for that key; it also generates a truststore containing the certificate;
	 * @return
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
			Secret tlsSecret = OpenShifts.master().getSecret(tlsSecretName);
			Assertions.assertNotNull(tlsSecret);
			certificateAndKey.tlsSecret = tlsSecret;
			return certificateAndKey;
		}

		// create key + self-signed certificate: they are typically used by the server exposing the endpoints over TLS
		processCall(caDir, "openssl", "req", "-subj", "/CN=" + hostname + "/OU=TF/O=XTF/L=Milan/C=IT",
				"-newkey", "rsa:2048", "-nodes", "-keyout", key, "-x509", "-days", "365", "-out", certificate);

		// add self-signed certificate to keystore: it's typically used by the clients contacting the endpoints over TLS
		processCall(caDir, "keytool", "-import", "-noprompt", "-alias", hostname, "-keystore",
				truststore, "-file", certificate, "-storetype", "JKS", "-storepass", truststorePassword);

		// create secret
		try {
			Secret tlsSecret = createTlsSecret(tlsSecretName, certificateAndKey.key, certificateAndKey.certificate);
			Assertions.assertNotNull(tlsSecret);
			certificateAndKey.tlsSecret = tlsSecret;
		} catch (IOException e) {
			throw new RuntimeException("Failed to create secret " + tlsSecretName, e);
		}

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
			throw new IllegalStateException("Failed executing " + String.join(" ", args));
		}

		if (result != 0) {
			throw new IllegalStateException("Failed executing " + String.join(" ", args));
		}
	}

	public static Secret createTlsSecret(String secretName, Path key, Path certificate) throws IOException {
		Map<String, String> data = new HashMap<>();
		String keyDerData = Files.readString(key);
		String crtDerData = Files.readString(certificate);
		data.put("tls.key", Base64.getEncoder().encodeToString(keyDerData.getBytes()));
		data.put("tls.crt", Base64.getEncoder().encodeToString(crtDerData.getBytes()));
		final Secret secret = new SecretBuilder()
				.withNewMetadata().withName(secretName).endMetadata()
				.withType("kubernetes.io/tls")
				.withImmutable(false)
				.addToData(data)
				.build();
		return OpenShifts.master().secrets().inNamespace(OpenShiftConfig.namespace()).createOrReplace(secret);
	}
}