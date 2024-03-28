package org.jboss.intersmash.examples.wildfly.keycloak.saml;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Path("/config")
@ApplicationScoped
public class ServerConfigurationResource {
	@GET
	@Path("/read")
	@Produces(MediaType.TEXT_PLAIN)
	public String read() throws IOException {
		String fileName = System.getProperty("jboss.server.config.dir") + "/standalone.xml";
		try (FileInputStream fis = new FileInputStream(fileName)) {
			return new BufferedReader(
					new InputStreamReader(fis, StandardCharsets.UTF_8))
					.lines()
					.collect(Collectors.joining("\n"));

		}
	}
}
