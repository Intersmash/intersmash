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
package org.jboss.intersmash.tools.junit5;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.annotations.Intersmash;
import org.jboss.intersmash.tools.annotations.Service;
import org.jboss.intersmash.tools.annotations.ServiceProvisioner;
import org.jboss.intersmash.tools.annotations.ServiceUrl;
import org.jboss.intersmash.tools.application.Application;
import org.jboss.intersmash.tools.application.openshift.OpenShiftApplication;
import org.jboss.intersmash.tools.provision.Provisioner;
import org.jboss.intersmash.tools.provision.ProvisionerManager;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.OperatorGroup;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

import cz.xtf.core.openshift.OpenShifts;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntersmashExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor {

	private static final Namespace NAMESPACE = Namespace.create("org", "jboss", "intersmash", "IntersmashExtension");
	private static final String INTERSMASH_SERVICES = "INTERSMASH_SERVICES";

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		Optional<Throwable> tt = Optional.empty();
		try {
			log.debug("beforeAll");
			//			openShiftRecorderService().initFilters(extensionContext);
			Intersmash[] intersmashes = extensionContext.getRequiredTestClass().getAnnotationsByType(Intersmash.class);
			Intersmash intersmash;
			if (intersmashes.length > 0) {
				intersmash = intersmashes[0];
			} else {
				return;
			}

			// we don't want to touch anything if the deployment phase is skipped
			if (!IntersmashConfig.skipDeploy()) {
				if (Arrays.stream(intersmash.value())
						.anyMatch(app -> OpenShiftApplication.class.isAssignableFrom(app.value()))) {
					if (!IntersmashConfig.isOcp3x(OpenShifts.admin())) {
						operatorCleanup();
						log.debug("Deploy operatorgroup [{}] to enable operators subscription into tested namespace",
								OperatorGroup.SINGLE_NAMESPACE.getMetadata().getName());
						OpenShifts.adminBinary().execute("apply", "-f",
								OperatorGroup.SINGLE_NAMESPACE.save().getAbsolutePath());
					}
					OpenShifts.master().clean().waitFor();
				}
			}

			Service[] services = intersmash.value();
			log.debug("# of services: {}", services.length);
			for (Service service : services) {
				Application application = getApplicationFromService(service);
				String name = application.getClass().getName();
				log.info("Caching provisioner for {}", name);
				// store provisioners right now, those might be needed in each phase independently
				Provisioner provisioner = ProvisionerManager.getProvisioner(application);
				// keep the provisioner in the JUpiter Extension Store
				getProvisioners(extensionContext).put(application.getClass().getName(), provisioner);
				if (!IntersmashConfig.skipDeploy()) {
					deployApplication(provisioner);
				}
			}
		} catch (Throwable t) {
			tt = Optional.of(t);
		} finally {
			if (tt.isPresent())
				throw new Exception("Error before test execution!", tt.get());
		}
	}

	private Application getApplicationFromService(Service service) {
		try {
			return service.value().getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException ite) {
			if (ite.getCause().getClass().equals(TestAbortedException.class)) {
				throw new TestAbortedException("Service deployment was aborted (e.g. due to failed assumptions).",
						ite.getCause());
			} else if (ite.getCause().getClass().equals(AssertionFailedError.class)) {
				throw new AssertionFailedError("Service deployment was aborted due to violated assertions.", ite.getCause());
			} else {
				throw new RuntimeException(ite);
			}
		}
	}

	private void deployApplication(Provisioner provisioner) {
		log.info("Deploying ", provisioner.getApplication().getClass().getName());
		provisioner.configure();
		provisioner.preDeploy();
		provisioner.deploy();
	}

	private void undeployApplication(Provisioner provisioner) {
		log.info("Undeploying ", provisioner.getApplication().getClass().getName());
		provisioner.undeploy();
		provisioner.postUndeploy();
		provisioner.dismiss();
	}

	public void afterAll(ExtensionContext extensionContext) {
		log.debug("afterAll");
		// skip undeploy?
		if (IntersmashConfig.skipUndeploy()) {
			log.info("Skipping the after test cleanup operations.");
		} else {
			for (Provisioner provisioner : getProvisioners(extensionContext).values()) {
				undeployApplication(provisioner);
			}
			// operator group is not bound to a specific product
			// no Operator support on OCP3 clusters, OLM doesn't run there
			if (!IntersmashConfig.isOcp3x(OpenShifts.admin())) {
				operatorCleanup();
			}
			// let's cleanup once we're done
			safetyCleanup();
		}
	}

	private static void safetyCleanup() {
		log.info("Cleaning up the remaining resources on the cluster.");
		OpenShifts.master().clean().waitFor();
	}

	/**
	 * Clean all OLM related objects.
	 * <p>
	 */
	public static void operatorCleanup() {
		OpenShifts.adminBinary().execute("delete", "subscription", "--all");
		OpenShifts.adminBinary().execute("delete", "csvs", "--all");
		OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
	}

	@Override
	public void postProcessTestInstance(Object o, ExtensionContext extensionContext) throws Exception {
		injectServiceUrl(o, extensionContext);
		injectServiceProvisioner(o, extensionContext);
	}

	private Map<String, Provisioner> getProvisioners(ExtensionContext extensionContext) {
		Store store = extensionContext.getStore(NAMESPACE);
		Map<String, Provisioner> provisioners = (Map<String, Provisioner>) store.get(INTERSMASH_SERVICES);
		if (provisioners != null) {
			return provisioners;
		} else {
			store.put(INTERSMASH_SERVICES, new HashMap<String, Provisioner>());
			return (Map<String, Provisioner>) store.get(INTERSMASH_SERVICES);
		}
	}

	private void injectServiceUrl(Object o, ExtensionContext extensionContext) throws Exception {
		log.debug("injectServiceUrl");
		List<Field> annotatedFields = AnnotationSupport.findAnnotatedFields(o.getClass(), ServiceUrl.class);
		for (Field field : annotatedFields) {
			ServiceUrl serviceUrl = field.getAnnotation(ServiceUrl.class);
			Provisioner provisioner = getProvisioners(extensionContext).get(serviceUrl.value().getName());
			URL url = provisioner.getURL();
			if (String.class.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				field.set(o, url.toExternalForm());
			} else if (URL.class.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				field.set(o, url);
			} else {
				throw new RuntimeException(
						"Cannot inject service URL into field of type: " + field.getType().getSimpleName());
			}
		}
	}

	private void injectServiceProvisioner(Object o, ExtensionContext extensionContext) throws IllegalAccessException {
		log.debug("injectServiceProvisioner");
		List<Field> annotatedFields = AnnotationSupport.findAnnotatedFields(o.getClass(), ServiceProvisioner.class);
		for (Field field : annotatedFields) {
			ServiceProvisioner serviceProvisioner = field.getAnnotation(ServiceProvisioner.class);
			Provisioner provisioner = getProvisioners(extensionContext).get(serviceProvisioner.value().getName());
			if (Provisioner.class.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				field.set(o, provisioner);
			} else {
				throw new RuntimeException(
						"Cannot inject service provisioner into field of type: " + field.getType().getSimpleName());
			}
		}
	}
}
