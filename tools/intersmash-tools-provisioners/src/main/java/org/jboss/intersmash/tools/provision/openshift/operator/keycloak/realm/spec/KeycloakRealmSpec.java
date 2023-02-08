package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.fabric8.kubernetes.api.model.LabelSelector;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * KeycloakRealmSpec defines the desired state of KeycloakRealm.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakRealmSpec {

	//	/**
	//	 * When set to true, this KeycloakRealm will be marked as unmanaged and not be managed by this operator.
	//	 * It can then be used for targeting purposes.
	//	 */
	//	private boolean unmanaged;

	/**
	 * Selector for looking up Keycloak Custom Resources.
	 */
	private LabelSelector instanceSelector;

	/**
	 * Keycloak Realm REST object.
	 */
	private KeycloakAPIRealm realm;

	/**
	 * A list of overrides to the default Realm behavior.
	 */
	private List<RedirectorIdentityProviderOverride> realmOverrides;
}
