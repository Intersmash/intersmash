package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache;

import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.spec.CacheSpec;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.status.CacheStatus;
import org.jboss.intersmash.tools.provision.openshift.operator.resources.OpenShiftResource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Cache is the Schema for the caches API
 *
 * https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html/running_data_grid_on_openshift/
 *
 * https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v2alpha1/cache_types.go
 * https://github.com/infinispan/infinispan-operator/blob/2.0.x/deploy/crds/infinispan.org_caches_crd.yaml
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Group("infinispan.org")
@Version("v2alpha1")
public class Cache extends CustomResource implements OpenShiftResource<Cache> {
	/**
	 * Standard object’s metadata.
	 */
	private ObjectMeta metadata;

	/**
	 * CacheSpec defines the desired state of Backup.
	 */
	private CacheSpec spec;

	/**
	 * CacheStatus defines the observed state of Backup.
	 */
	private CacheStatus status;

	@Override
	public Cache load(Cache loaded) {
		this.setMetadata(loaded.getMetadata());
		this.setSpec(loaded.getSpec());
		return this;
	}
}
