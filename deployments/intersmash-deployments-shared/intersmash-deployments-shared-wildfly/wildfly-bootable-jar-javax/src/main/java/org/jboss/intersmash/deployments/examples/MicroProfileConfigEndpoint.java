package org.jboss.intersmash.deployments.examples;

import jakarta.inject.Inject;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/")
public class MicroProfileConfigEndpoint {

	@Inject
	@ConfigProperty(name = "demo-cli-property", defaultValue = "Default value")
	String property;

	@GET
	@Produces("text/plain")
	public Response doGet() {
		return Response.ok("demo-cli-property = " + property).build();
	}

	@ApplicationPath("/")
	public static class RestApplication extends Application {
	}
}
