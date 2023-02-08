package org.jboss.intersmash.tools.application.openshift;

import io.hyperfoil.v1alpha2.Hyperfoil;

public interface HyperfoilOperatorApplication extends OperatorApplication {
	Hyperfoil getHyperfoil();
}
