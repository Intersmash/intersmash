package org.jboss.intersmash.testsuite.k8s;

import org.jboss.intersmash.tools.k8s.junit5.KubernetesNamespaceCreator;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KubernetesNamespaceCreator.class)
public interface NamespaceCreationCapable {
}
