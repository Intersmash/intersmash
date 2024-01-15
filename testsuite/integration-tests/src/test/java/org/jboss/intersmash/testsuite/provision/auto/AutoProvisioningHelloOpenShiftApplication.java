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
package org.jboss.intersmash.testsuite.provision.auto;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.jboss.intersmash.tools.application.auto.AutoProvisioningExecutionException;
import org.jboss.intersmash.tools.application.openshift.AutoProvisioningOpenShiftApplication;
import org.jboss.intersmash.tools.application.openshift.OpenShiftApplication;

import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.openshift.api.model.Route;
import lombok.extern.slf4j.Slf4j;

/**
 * This {@link OpenShiftApplication} concrete subclass is providing the methods implementation needed by the
 * declarative provisioning process.
 *
 * The goal is to test the automatic (or declarative or application dictated) provisioning process by deploying the
 * <i>hello-openshift</i> application (inspired by
 * https://github.com/openshift/origin/tree/master/examples/hello-openshift) through a deployment resource.
 */
@Slf4j
public class AutoProvisioningHelloOpenShiftApplication implements AutoProvisioningOpenShiftApplication {

	private static final String HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION = "target/test-classes/org/jboss/intersmash/tools/provision/openshift/auto/hello-openshift-deployment.yaml";
	private static final String HELLO_OPENSHIFT_SERVICE_DEFINITION = "target/test-classes/org/jboss/intersmash/tools/provision/openshift/auto/hello-openshift-service.yaml";
	private static final String HELLO_OPENSHIFT_ROUTE_DEFINITION = "target/test-classes/org/jboss/intersmash/tools/provision/openshift/auto/hello-openshift-route.yaml";
	private static final String HELLO_OPENSHIFT_APP_NAME = "hello-openshift";
	private static final String HELLO_OPENSHIFT_APP_REPLICAS_PLACEHOLDER = "autoprovisioning-hello-openshift-app-replicas-changeme";

	@Override
	public void preDeploy() {
		// on preDeploy, we've got nothing to do... (yet, maybe we could create the secret for securing the route)
		/// TODO
	}

	@Override
	public void deploy() throws AutoProvisioningExecutionException {
		final int replicas = 1;
		// set initial replicas
		try {
			adjustConfiguration(HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION,
					String.format("replicas: %s", HELLO_OPENSHIFT_APP_REPLICAS_PLACEHOLDER),
					String.format("replicas: %d", replicas));
		} catch (IOException e) {
			throw new AutoProvisioningExecutionException(e);
		}
		try {
			// on @Deploy, we'll provision the app pod as per the JSON definition in HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION
			OpenShifts.masterBinary().execute(
					"apply", "-f", HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION);
		} finally {
			try {
				adjustConfiguration(HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION,
						String.format("replicas: %d", replicas),
						String.format("replicas: %s", HELLO_OPENSHIFT_APP_REPLICAS_PLACEHOLDER));
			} catch (IOException e) {
				throw new AutoProvisioningExecutionException(e);
			}
		}
		// let's wait for the exact number of pods
		OpenShiftWaiters.get(OpenShifts.master(), () -> false).areExactlyNPodsReady(
				1, "app", getName()).waitFor();
		// then create a service for the hello-openshift app
		OpenShifts.masterBinary().execute(
				"apply", "-f", HELLO_OPENSHIFT_SERVICE_DEFINITION);
		// and let's expose the service through a route so that it is available externally
		OpenShifts.masterBinary().execute(
				"apply", "-f", HELLO_OPENSHIFT_ROUTE_DEFINITION);
	}

	@Override
	public void scale(int replicas, boolean wait) throws AutoProvisioningExecutionException {
		try {
			adjustConfiguration(HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION,
					String.format("replicas: %s", HELLO_OPENSHIFT_APP_REPLICAS_PLACEHOLDER),
					String.format("replicas: %d", replicas));
		} catch (IOException e) {
			e.printStackTrace();
			throw new AutoProvisioningExecutionException(e);
		}
		try {
			// on scale we'll update the Pod resource definition in order to set the number of replicas
			OpenShifts.masterBinary().execute(
					"apply", "-f", HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION);
		} finally {
			try {
				adjustConfiguration(HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION,
						String.format("replicas: %d", replicas),
						String.format("replicas: %s", HELLO_OPENSHIFT_APP_REPLICAS_PLACEHOLDER));
			} catch (IOException e) {
				throw new AutoProvisioningExecutionException(e);
			}
		}
		// on request, we can wait for the scaling operation to finish
		if (wait) {
			//	let's wait for the exact number of pods
			OpenShiftWaiters.get(OpenShifts.master(), () -> false).areExactlyNPodsReady(
					replicas, "app", getName()).waitFor();
		}
	}

	@Override
	public void undeploy() throws AutoProvisioningExecutionException {
		// on undeploy we're removing the application external route...
		OpenShifts.masterBinary().execute(
				"delete", "-f", HELLO_OPENSHIFT_ROUTE_DEFINITION);
		// ... its service
		OpenShifts.masterBinary().execute(
				"delete", "-f", HELLO_OPENSHIFT_SERVICE_DEFINITION);
		// ... then gracefully stop (scale down to 0)
		scale(0, true);
		// ... and finally remove the deployment
		OpenShifts.masterBinary().execute(
				"delete", "-f", HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION);
	}

	@Override
	public void postUndeploy() {
		// on postUndeploy we've got nothing to do but we could remove any secret created by preDeploy
		/// TODO
	}

	@Override
	public URL getURL() throws AutoProvisioningExecutionException {
		Route route = OpenShifts.master().getRoute(getName() + "-route");
		if (Objects.nonNull(route)) {
			try {
				return new URL("http://" + route.getSpec().getHost());
			} catch (MalformedURLException e) {
				throw new AutoProvisioningExecutionException(e);
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return HELLO_OPENSHIFT_APP_NAME;
	}

	private static void adjustConfiguration(String filePath, String oldString, String newString) throws IOException {
		Path configurationPath = Paths.get(filePath);

		String yamlContent = new String(Files.readAllBytes(configurationPath), StandardCharsets.UTF_8);
		yamlContent = yamlContent.replace(oldString, newString);
		Files.write(configurationPath, yamlContent.getBytes(StandardCharsets.UTF_8));
	}
}
