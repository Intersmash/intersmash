package org.jboss.intersmash.tools.provision.operator;

import java.util.List;

import org.jboss.intersmash.tools.application.operator.OperatorApplication;
import org.jboss.intersmash.tools.provision.Provisioner;
import org.jboss.intersmash.tools.provision.openshift.HasPods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.operatorhub.lifecyclemanager.v1.PackageManifest;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSource;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSourceList;

public interface OlmOperatorProvisioner<T extends OperatorApplication>
		extends Provisioner<T>, OlmOperatorProvisionerClientBinary, HasPods {

	default PackageManifest getPackageManifest(String operatorName, String operatorNamespace) {
		try {
			return new ObjectMapper()
					.readValue(this.execute("get", "packagemanifest", operatorName, "-n", operatorNamespace, "-o", "json"),
							PackageManifest.class);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Couldn't deserialize package manifest data: " + operatorName, e);
		}
	}

	List<Pod> retrievePods();

	default List<CatalogSource> getCatalogSources(final String catalogSourceNamespace) {
		try {
			return new ObjectMapper().readValue(this.execute("get", "catsrc", "-n", catalogSourceNamespace, "-o", "json"),
					CatalogSourceList.class).getItems();
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Couldn't deserialize catalog source data: " + catalogSourceNamespace, e);
		}
	}

	default CatalogSource getCatalogSource(final String catalogSourceNamespace, final String catalogSourceName) {
		io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSource loaded = getCatalogSources(catalogSourceNamespace)
				.stream()
				.filter(cs -> cs.getMetadata().getName().equalsIgnoreCase(catalogSourceName))
				.findFirst().orElseThrow(
						() -> new IllegalStateException(
								"Unable to retrieve CatalogSource " + catalogSourceName));
		CatalogSource catalogSource = new CatalogSource();
		catalogSource.setMetadata(loaded.getMetadata());
		catalogSource.setSpec(loaded.getSpec());
		return catalogSource;
	}

	String execute(String... args);

	NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, Resource<CustomResourceDefinition>> retrieveCustomResourceDefinitions();

	boolean isSubscribed();

	void subscribe();

	void unsubscribe();
}
