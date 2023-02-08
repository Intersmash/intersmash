package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm;

import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec.KeycloakRealmSpec;
import org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.status.KeycloakRealmStatus;
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
 * https://github.com/keycloak/keycloak-operator/blob/master/pkg/apis/keycloak/v1alpha1/keycloakrealm_types.go
 * https://github.com/keycloak/keycloak-operator/blob/master/deploy/crds/keycloak.org_keycloakrealms_crd.yaml
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Group("keycloak.org")
@Version("v1alpha1")
public class KeycloakRealm extends CustomResource implements OpenShiftResource<KeycloakRealm> {
	/**
	 * Standard object’s metadata.
	 */
	private ObjectMeta metadata;

	/**
	 * KeycloakRealmSpec defines the desired state of KeycloakRealm.
	 */
	private KeycloakRealmSpec spec;

	/**
	 * KeycloakRealmStatus defines the observed state of KeycloakRealm.
	 */
	private KeycloakRealmStatus status;

	@Override
	public KeycloakRealm load(KeycloakRealm loaded) {
		this.setMetadata(loaded.getMetadata());
		this.setSpec(loaded.getSpec());
		return this;
	}
}
