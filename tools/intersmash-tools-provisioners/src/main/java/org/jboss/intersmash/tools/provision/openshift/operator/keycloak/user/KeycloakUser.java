package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user;

import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.spec.KeycloakUserSpec;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.status.KeycloakUserStatus;
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
 * https://github.com/keycloak/keycloak-operator/blob/master/pkg/apis/keycloak/v1alpha1/keycloakuser_types.go
 * https://github.com/keycloak/keycloak-operator/blob/master/deploy/crds/keycloak.org_keycloakusers_crd.yaml
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Group("keycloak.org")
@Version("v1alpha1")
public class KeycloakUser extends CustomResource implements OpenShiftResource<KeycloakUser> {
	/**
	 * Standard object’s metadata.
	 */
	private ObjectMeta metadata;

	/**
	 * KeycloakUserSpec defines the desired state of KeycloakUser.
	 */
	private KeycloakUserSpec spec;

	/**
	 * KeycloakUserStatus defines the observed state of KeycloakUser.
	 */
	private KeycloakUserStatus status;

	@Override
	public KeycloakUser load(KeycloakUser loaded) {
		this.setMetadata(loaded.getMetadata());
		this.setSpec(loaded.getSpec());
		return this;
	}
}
