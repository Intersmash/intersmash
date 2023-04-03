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
package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.fabric8.kubernetes.api.model.EnvVar;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Experimental section
 * NOTE: This section might change or get removed without any notice. It may also cause the deployment to behave
 * in an unpredictable fashion. Please use with care.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ExperimentalSpec {

	/**
	 *  Arguments to the entrypoint. Translates into Container CMD.
	 */
	private List<String> args;

	/**
	 * Container command. Translates into Container ENTRYPOINT.
	 */
	private List<String> command;

	/**
	 * List of environment variables to set in the container.
	 */
	private List<EnvVar> env;

	/**
	 * Additional volume mounts
	 */
	private VolumesSpec volumes;
}
