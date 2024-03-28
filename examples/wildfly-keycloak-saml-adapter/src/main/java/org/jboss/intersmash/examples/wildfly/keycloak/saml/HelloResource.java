package org.jboss.intersmash.examples.wildfly.keycloak.saml;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
@ApplicationScoped
public class HelloResource {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String greet() {
		return "Hello world!";
	}
}
