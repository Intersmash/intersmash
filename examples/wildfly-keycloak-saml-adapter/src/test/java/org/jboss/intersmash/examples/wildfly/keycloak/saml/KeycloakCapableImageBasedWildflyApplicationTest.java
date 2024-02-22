package org.jboss.intersmash.examples.wildfly.keycloak.saml;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.http.HttpStatus;
import org.jboss.intersmash.annotations.Intersmash;
import org.jboss.intersmash.annotations.Service;
import org.jboss.intersmash.annotations.ServiceUrl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Intersmash(@Service(KeycloakCapableImageBasedWildflyApplication.class))
public class KeycloakCapableImageBasedWildflyApplicationTest {

	@ServiceUrl(KeycloakCapableImageBasedWildflyApplication.class)
	private String wildflyApplicationUrl;

	@Test
	public void testGreeting() throws URISyntaxException, IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI(wildflyApplicationUrl + "/hello"))
				.GET()
				.build();

		HttpResponse<String> response = HttpClient
				.newBuilder()
				.build()
				.send(request, HttpResponse.BodyHandlers.ofString());

		Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode());
		Assertions.assertEquals("Hello world!", response.body());
	}

	@Test
	public void testSamlSubsystemIsDefined() throws URISyntaxException, IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI(wildflyApplicationUrl + "/config/read"))
				.GET()
				.build();

		HttpResponse<String> response = HttpClient
				.newBuilder()
				.build()
				.send(request, HttpResponse.BodyHandlers.ofString());

		Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode());
		Assertions.assertTrue(response.body().contains("<extension module=\"org.keycloak.keycloak-saml-adapter-subsystem\"/>"));
	}
}
