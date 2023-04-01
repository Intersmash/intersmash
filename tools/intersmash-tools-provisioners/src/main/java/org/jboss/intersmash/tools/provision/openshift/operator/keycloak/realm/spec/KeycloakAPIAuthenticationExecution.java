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
package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.realm.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KeycloakAPIAuthenticationExecution {

	/**
	 * Authenticator.
	 */
	private String authenticator;

	/**
	 * Authenticator Config.
	 */
	private String authenticatorConfig;

	/**
	 * Authenticator flow.
	 */
	private String authenticatorFlow;

	/**
	 * Flow Alias.
	 */
	private String flowAlias;

	/**
	 * Priority.
	 */
	private int priority;

	/**
	 * Requirement [REQUIRED, OPTIONAL, ALTERNATIVE, DISABLED].
	 */
	private String requirement;

	/**
	 * User setup allowed.
	 */
	private boolean userSetupAllowed;
}
