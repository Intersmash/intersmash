package org.jboss.intersmash.tools.application.openshift;

import java.util.Collections;
import java.util.List;

import net.threescale.apps.v1alpha1.APIManager;
import net.threescale.capabilities.v1beta1.Application;
import net.threescale.capabilities.v1beta1.Backend;
import net.threescale.capabilities.v1beta1.DeveloperAccount;
import net.threescale.capabilities.v1beta1.DeveloperUser;
import net.threescale.capabilities.v1beta1.Product;
import net.threescale.capabilities.v1beta1.ProxyConfigPromote;

public interface ThreeScaleOperatorApplication extends OperatorApplication {

	APIManager getAPIManager();

	default List<DeveloperAccount> getDeveloperAccounts() {
		return Collections.emptyList();
	}

	default List<DeveloperUser> getDeveloperUsers() {
		return Collections.emptyList();
	}

	default List<Backend> getBackends() {
		return Collections.emptyList();
	}

	default List<Product> getProducts() {
		return Collections.emptyList();
	}

	default List<Application> getApplications() {
		return Collections.emptyList();
	}

	default List<ProxyConfigPromote> getProxyConfigPromotes() {
		return Collections.emptyList();
	}
}
