package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan;

import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec.InfinispanSpec;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.status.InfinispanStatus;
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
 * https://github.com/infinispan/infinispan-operator/blob/2.0.x/pkg/apis/infinispan/v1/infinispan_types.go
 * https://github.com/infinispan/infinispan-operator/blob/2.0.x/deploy/crds/infinispan.org_infinispans_crd.yaml
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Group("infinispan.org")
@Version("v1")
public class Infinispan extends CustomResource implements OpenShiftResource<Infinispan> {

	/**
	 * Standard object’s metadata.
	 */
	private ObjectMeta metadata;

	/**
	 * InfinispanSpec defines the desired state of Infinispan.
	 */
	private InfinispanSpec spec;

	/**
	 * InfinispanStatus defines the observed state of Infinispan.
	 */
	private InfinispanStatus status;

	@Override
	public Infinispan load(Infinispan loaded) {
		this.setMetadata(loaded.getMetadata());
		this.setSpec(loaded.getSpec());
		return this;
	}
}
