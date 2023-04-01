/**
 * Copyright (C) 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.status;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * KeycloakRealmStatus defines the observed state of KeycloakRealm.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakRealmStatus {
	/**
	 * Current phase of the operator.
	 */
	private String phase;

	/**
	 * Human-readable message indicating details about current operator phase or error.
	 */
	private String message;

	/**
	 * True if all resources are in a ready state and all work is done.
	 */
	private boolean ready;

	/**
	 * A map of all the secondary resources types and names created for this CR.
	 * e.g "Deployment": [ "DeploymentName1", "DeploymentName2" ].
	 */
	private Map<String, List<String>> secondaryResources;

	private String loginURL;
}
