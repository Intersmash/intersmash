package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak;

import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec.KeycloakSpec;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.status.KeycloakStatus;
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
 * https://github.com/keycloak/keycloak-operator/blob/master/pkg/apis/keycloak/v1alpha1/keycloak_types.go
 * https://github.com/keycloak/keycloak-operator/blob/master/deploy/crds/keycloak.org_keycloaks_crd.yaml
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Group("keycloak.org")
@Version("v1alpha1")
public class Keycloak extends CustomResource implements OpenShiftResource<Keycloak> {

	/**
	 * Standard object’s metadata.
	 */
	private ObjectMeta metadata;

	/**
	 * KeycloakSpec defines the desired state of Keycloak.
	 */
	private KeycloakSpec spec;

	/**
	 * KeycloakStatus defines the observed state of Keycloak.
	 */
	private KeycloakStatus status;

	@Override
	public Keycloak load(Keycloak loaded) {
		this.setMetadata(loaded.getMetadata());
		this.setSpec(loaded.getSpec());
		return this;
	}
}
