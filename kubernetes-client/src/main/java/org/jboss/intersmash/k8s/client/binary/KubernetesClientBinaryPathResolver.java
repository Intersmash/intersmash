package org.jboss.intersmash.k8s.client.binary;

/**
 * Defines a contract for resolving the path of a Kubernetes client binary, which concrete implementations will
 * base upon different criteria.
 */
public interface KubernetesClientBinaryPathResolver {
	String BINARY_NAME = "kubectl";
	String LOCAL_BINARY_CLIENT_TMP_DIR = "tmp/kubectl";

	/**
	 * Resolves Kubernetes client binary path
	 *
	 * @return Kubernetes client binary path
	 */
	String resolve();
}
