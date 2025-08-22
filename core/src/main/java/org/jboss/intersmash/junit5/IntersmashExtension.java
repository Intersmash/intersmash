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
package org.jboss.intersmash.junit5;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.jboss.intersmash.IntersmashConfig;
import org.jboss.intersmash.annotations.Intersmash;
import org.jboss.intersmash.annotations.Service;
import org.jboss.intersmash.annotations.ServiceProvisioner;
import org.jboss.intersmash.annotations.ServiceUrl;
import org.jboss.intersmash.application.Application;
import org.jboss.intersmash.k8s.KubernetesConfig;
import org.jboss.intersmash.k8s.client.Kuberneteses;
import org.jboss.intersmash.provision.Provisioner;
import org.jboss.intersmash.provision.ProvisionerManager;
import org.jboss.intersmash.provision.olm.OperatorGroup;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShifts;
import lombok.extern.slf4j.Slf4j;

/**
 * Intersmash JUnit extension that handles the process of provisioning a cross-product scenario before starting the
 * test execution.
 */
@Slf4j
public class IntersmashExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor {

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		Optional<Throwable> tt = Optional.empty();
		try {
			log.debug("beforeAll");
			// let's store the Intersmash definition in the extension context store
			Intersmash intersmash = IntersmashExtensionHelper.getIntersmash(extensionContext);
			if (intersmash == null) {
				log.warn("No @Intersmah defined");
				return;
			}
			final Boolean intersmashTargetingOperator = IntersmashExtensionHelper
					.isIntersmashTargetingOperator(extensionContext);
			final Boolean intersmashTargetingKubernetes = IntersmashExtensionHelper
					.isIntersmashTargetingKubernetes(extensionContext);
			final Boolean intersmashTargetingOpenShift = IntersmashExtensionHelper
					.isIntersmashTargetingOpenShift(extensionContext);

			// Cleanup - BTW we don't want to touch anything if the deployment phase is skipped
			if (!IntersmashConfig.skipDeploy()) {
				if (intersmashTargetingOperator) {
					operatorCleanup(intersmashTargetingKubernetes,
							intersmashTargetingOpenShift && !IntersmashConfig.isOcp3x(OpenShifts.admin()));
					deployOperatorGroup(extensionContext);
				}
				if (intersmashTargetingOpenShift) {
					OpenShifts.master().clean().waitFor();
				}
				if (intersmashTargetingKubernetes) {
					Kuberneteses.master().clean().waitFor();
				}
			}

			// deploy
			Service[] services = intersmash.value();
			log.debug("# of services: {}", services.length);
			for (Service service : services) {
				final Application application = getApplicationFromService(service);
				final String name = application.getClass().getName();
				log.info("Caching provisioner for {}", name);
				// store provisioners right now, those might be needed in each phase independently
				Provisioner provisioner = ProvisionerManager.getProvisioner(application);
				// keep the provisioner in the JUpiter Extension Store
				IntersmashExtensionHelper.getProvisioners(extensionContext).put(application.getClass().getName(), provisioner);
				if (!IntersmashConfig.skipDeploy()) {
					deployApplication(
							IntersmashExtensionHelper.getProvisioners(extensionContext).get(application.getClass().getName()));
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
			for (Provisioner provisioner : IntersmashExtensionHelper.getProvisioners(extensionContext).values()) {
				undeployApplication(provisioner);
			}
			// operator group is not bound to a specific product
			// no Operator support on OCP3 clusters, OLM doesn't run there
			if (IntersmashExtensionHelper.isIntersmashTargetingOperator(extensionContext)) {
				operatorCleanup(IntersmashExtensionHelper.isIntersmashTargetingKubernetes(extensionContext),
						IntersmashExtensionHelper.isIntersmashTargetingOpenShift(extensionContext)
								&& !IntersmashConfig.isOcp3x(OpenShifts.admin()));
			}
			// let's cleanup once we're done
			safetyCleanup(extensionContext);
		}
	}

	private static void safetyCleanup(ExtensionContext extensionContext) {
		log.info("Cleaning up the remaining resources on the cluster.");
		if (IntersmashExtensionHelper.isIntersmashTargetingOpenShift(extensionContext)) {
			OpenShifts.master().clean().waitFor();
		}
		if (IntersmashExtensionHelper.isIntersmashTargetingKubernetes(extensionContext)) {
			Kuberneteses.master().clean().waitFor();
		}
	}

	@Override
	public void postProcessTestInstance(Object o, ExtensionContext extensionContext) throws Exception {
		injectServiceUrl(o, extensionContext);
		injectServiceProvisioner(o, extensionContext);
	}

	private void injectServiceUrl(Object o, ExtensionContext extensionContext) throws Exception {
		log.debug("injectServiceUrl");
		List<Field> annotatedFields = AnnotationSupport.findAnnotatedFields(o.getClass(), ServiceUrl.class);
		for (Field field : annotatedFields) {
			ServiceUrl serviceUrl = field.getAnnotation(ServiceUrl.class);
			Provisioner provisioner = IntersmashExtensionHelper.getProvisioners(extensionContext)
					.get(serviceUrl.value().getName());
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
			Provisioner provisioner = IntersmashExtensionHelper.getProvisioners(extensionContext)
					.get(serviceProvisioner.value().getName());
			if (Provisioner.class.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				field.set(o, provisioner);
			} else {
				throw new RuntimeException(
						"Cannot inject service provisioner into field of type: " + field.getType().getSimpleName());
			}
		}
	}

	private static void deployOperatorGroup(ExtensionContext extensionContext) throws IOException {
		if (IntersmashExtensionHelper.isIntersmashTargetingKubernetes(extensionContext)) {
			log.debug("Deploy operatorgroup [{}] to enable operators subscription into tested namespace",
					new OperatorGroup(KubernetesConfig.namespace()).getMetadata().getName());
			OpenShifts.adminBinary().execute("apply", "-f",
					new OperatorGroup(KubernetesConfig.namespace()).save().getAbsolutePath());
		}
		if (IntersmashExtensionHelper.isIntersmashTargetingOpenShift(extensionContext)
				&& !IntersmashConfig.isOcp3x(OpenShifts.admin())) {
			log.debug("Deploy operatorgroup [{}] to enable operators subscription into tested namespace",
					new OperatorGroup(OpenShiftConfig.namespace()).getMetadata().getName());
			OpenShifts.adminBinary().execute("apply", "-f",
					new OperatorGroup(OpenShiftConfig.namespace()).save().getAbsolutePath());
		}
	}

	/**
	 * Clean all OLM related objects.
	 * <p>
	 */
	public static void operatorCleanup(final boolean cleanupKubernetes, final boolean cleanupOpenShift) {
		if (cleanupKubernetes) {
			Kuberneteses.adminBinary().execute("delete", "subscription", "--all");
			Kuberneteses.adminBinary().execute("delete", "csvs", "--all");
			Kuberneteses.adminBinary().execute("delete", "operatorgroup", "--all");
		}
		if (cleanupOpenShift) {
			OpenShifts.adminBinary().execute("delete", "subscription", "--all");
			OpenShifts.adminBinary().execute("delete", "csvs", "--all");
			OpenShifts.adminBinary().execute("delete", "operatorgroup", "--all");
		}
	}
}
