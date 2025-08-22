/*
 * Copyright (C) 2025 Red Hat, Inc.
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
package org.jboss.intersmash.k8s.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.intersmash.k8s.KubernetesConfig;
import org.junit.jupiter.engine.descriptor.MethodBasedTestDescriptor;
import org.junit.platform.engine.TestDescriptor;

import cz.xtf.core.context.TestCaseContext;
import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.kubernetes.api.builder.Visitor;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NamespaceManager {
	/**
	 * By default (xtf.openshift.namespace.per.testcase=false) all entries in map point to value returned by
	 *
	 * @see KubernetesConfig#namespace().
	 *  If intersmash.k8s.namespace.per.testcase=true then each entry points to namespace
	 *      assigned to each test case by {@see NamespaceManager#getNamespaceForTestClass}
	 *
	 *      Maps testcase -> namespace
	 */
	private static final Map<String, String> testcaseToNamespaceMap = new HashMap<String, String>();

	/**
	 * @return Map testcase -> namespace
	 */
	private static Map<String, String> getTestCaseToNamespaceMap() {
		return testcaseToNamespaceMap;
	}

	/**
	 * @return return namespace for testcase or null if not present
	 */
	private static String getNamespaceForTestCase(String testcase) {
		return getTestCaseToNamespaceMap().get(testcase);
	}

	/**
	 * Creates namespace with name returned by @see #getNamespace if it does not exist
	 *
	 * @return true if successful, false otherwise
	 */
	public static boolean createIfDoesNotExistsProject() {
		return createIfDoesNotExistsProject(getNamespace());
	}

	/**
	 * Creates namespace if it does not exist
	 *
	 * @return true if newly created, false otherwise (failed or namespace already present)
	 */
	public static boolean createIfDoesNotExistsProject(String namespace) {
		Kubernetes kubernetes = Kuberneteses.master(namespace);

		// in case namespace is terminating (means is being deleted) then wait
		checkAndWaitIfNamespaceIsTerminating(namespace);

		if (kubernetes.namespaces().withName(namespace).get() == null) {
			log.info("Creating namespace: " + kubernetes.getNamespace());

			Namespace projectNamespace = new Namespace();
			ObjectMeta objectMeta = new ObjectMeta();
			objectMeta.setName(KubernetesConfig.namespace());
			projectNamespace.setMetadata(objectMeta);

			kubernetes.namespaces().create(projectNamespace);

			new SimpleWaiter(() -> kubernetes.namespaces().withName(kubernetes.getNamespace()) != null,
					TimeUnit.MINUTES, 2,
					"Waiting for " + namespace + " project deletion").waitFor();

			try {
				// Adding a label can be only done via 'namespace'. It cannot be set via 'project' API. Thus we do this
				// separately. Also, to update namespace label, it's necessary to have 'patch resource "namespaces"'
				// permission for current user and updated namespace, e.g. by having 'cluster-admin' role.
				// Otherwise you can see:
				// $ oc label namespace <name> "label1=foo"
				// Error from server (Forbidden): namespaces "<name>" is forbidden: User "<user>" cannot patch resource "namespaces" in API group "" in the namespace "<name>"
				Kuberneteses.admin(namespace).namespaces().withName(kubernetes.getNamespace())
						.edit(new Visitor<NamespaceBuilder>() {
							@Override
							public void visit(NamespaceBuilder builder) {
								builder.editMetadata()
										.addToLabels(Kubernetes.INTERSMASH_MANAGED_LABEL, "true");
							}
						});
			} catch (KubernetesClientException e) {
				// We weren't able to assign a label to the new project. Let's just print warning since this information
				// is not critical to the tests execution. Possible cause for this are insufficient permissions since
				// some projects using XTF are executed on OCP instances without 'admin' accounts available.
				log.warn("Couldn't assign label '" + Kubernetes.INTERSMASH_MANAGED_LABEL + "' to the new project '"
						+ kubernetes.getNamespace() + "'. Possible cause are insufficient permissions.");
				log.debug(e.getMessage());
			}

			if (KubernetesConfig.pullSecret() != null) {
				kubernetes.setupPullSecret(KubernetesConfig.pullSecret());
			}
			log.info("Created namespace: " + kubernetes.getNamespace());
			return true;
		}
		return false;
	}

	private static void checkAndWaitIfNamespaceIsTerminating(String namespace) {
		Namespace n = Kuberneteses.admin(namespace).namespaces().withName(namespace).get();
		if (n != null && n.getStatus().getPhase().equals("Terminating")) {
			waitForNamespaceToBeDeleted(namespace);
		}
	}

	/**
	 * Deletes namespace as returned by @see #getNamespace
	 *
	 * @param waitForDeletion whether to wait for deletion (timeout 2 min)
	 *
	 * @return true if successful, false otherwise
	 */
	public static boolean deleteProject(boolean waitForDeletion) {
		return deleteProject(getNamespace(), waitForDeletion);
	}

	/**
	 * Deletes namespace
	 *
	 * @param namespace namespace name to delete
	 * @param waitForDeletion whether to wait for deletion (timeout 2 min)
	 *
	 * @return true if successful, false otherwise
	 */
	public static boolean deleteProject(String namespace, boolean waitForDeletion) {
		boolean deleted = false;
		// problem with OpenShift.getProject() is that it might return null even if namespace still exists (is in terminating state)
		// thus use Openshift.namespaces() which do not suffer by this problem
		// openshift.namespaces() requires admin privileges otherwise following KubernetesClientException is thrown:
		// ... User "xpaasqe" cannot get resource "namespaces" in API group "" in the namespace ...
		if (Kuberneteses.admin(namespace).namespaces().withName(namespace).get() != null) {
			Kubernetes kubernetes = Kuberneteses.master(namespace);
			log.info("Start deleting namespace: " + kubernetes.getNamespace() + ", wait for deletion: " + waitForDeletion);
			List<StatusDetails> details = kubernetes.namespaces().withName(namespace).delete();
			deleted = details.stream().allMatch(d -> d.getCauses().isEmpty());
			if (deleted) {
				log.info("Namespace: " + namespace + ", successfully deleted");
			}
			if (!deleted && waitForDeletion) {
				waitForNamespaceToBeDeleted(namespace);
			}
		}
		return deleted;
	}

	/**
	 *
	 * Deletes namespace as returned by @see #getNamespace.
	 * Deletes namespace only if @see {@link KubernetesConfig#useNamespacePerTestCase()} is true.
	 *
	 * @param waitForDeletion wait for deletion of namespace
	 * @return true if successful, false otherwise
	 */
	public static boolean deleteProjectIfUsedNamespacePerTestCase(boolean waitForDeletion) {
		if (KubernetesConfig.useNamespacePerTestCase()) {
			return deleteProject(waitForDeletion);
		}
		return false;
	}

	private static void waitForNamespaceToBeDeleted(String namespace) {
		BooleanSupplier bs = () -> Kuberneteses.admin(namespace).namespaces().withName(namespace).get() == null;
		new SimpleWaiter(bs, TimeUnit.MINUTES, 2, "Waiting for " + namespace + " project deletion")
				.waitFor();
	}

	private static String getNamespaceForTestClass(TestDescriptor testDescriptor) {
		if (KubernetesConfig.useNamespacePerTestCase()) {
			// some test case names can be really long resulting in long namespace names. This can cause issues
			// with routes which have 64 chars limit for prefix of domain name. In case of route like:
			// galleon-provisioning-xml-prio-mnovak-galleonprovisioningxmltest.apps.eapqe-024-dryf.eapqe.psi.redhat.com
			// route prefix is: galleon-provisioning-xml-prio-mnovak-galleonprovisioningxmltest
			// route suffix is: .apps.eapqe-024-dryf.eapqe.psi.redhat.com
			if ((KubernetesConfig.namespace() + "-"
					+ testDescriptor.getParent().get().getDisplayName().toLowerCase())
					.length() > KubernetesConfig.getNamespaceLengthLimitForUniqueNamespacePerTest()) {

				return KubernetesConfig.namespace() + "-"
						+ StringUtils.truncate(DigestUtils.sha256Hex(testDescriptor.getParent().get().getDisplayName()
								.toLowerCase()),
								KubernetesConfig.getNamespaceLengthLimitForUniqueNamespacePerTest()
										- KubernetesConfig.namespace().length());
			} else {
				return KubernetesConfig.namespace() + "-"
						+ testDescriptor.getParent().get().getDisplayName().toLowerCase(); // namespace must not have upper case letters
			}
		} else {
			return KubernetesConfig.namespace();
		}
	}

	/**
	 * Add mapping test case name -> (automatically generated) namespace for given test case if absent
	 *
	 * @param testDescriptor test descriptor
	 */
	public static void addTestCaseToNamespaceEntryIfAbsent(TestDescriptor testDescriptor) {
		getTestCaseToNamespaceMap().putIfAbsent(((MethodBasedTestDescriptor) testDescriptor).getTestClass().getName(),
				getNamespaceForTestClass(testDescriptor));
	}

	/**
	 * @return Returns default namespace as defined in xtf.openshift.namespace property or namespace for currently running test
	 *         case when:
	 *         -Dintersmash.kubernetes.namespace.per.testcase=true.
	 *         In case when current thread does not have associated test case (for example when initializing Kubernetes instance
	 *         in static variable or static block) then {@link java.lang.RuntimeException} exception is thrown.
	 */
	public static String getNamespace() {
		if (KubernetesConfig.useNamespacePerTestCase()) {
			String namespace = NamespaceManager.getNamespaceForTestCase(TestCaseContext.getRunningTestCaseName());
			if (StringUtils.isEmpty(namespace)) {
				throw new RuntimeException(
						"There is no namespace associated with current thread or test case. This can happen in case that OpenShift instance is created in static variable. In this case avoid using static. Or in thread which is not associated with any test case.");
			}
			return namespace;
		} else {
			return KubernetesConfig.namespace();
		}
	}
}
