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
package org.jboss.intersmash.tools.provision.openshift.operator.wildfly.status;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <a href="https://github.com/wildfly/wildfly-operator/blob/master/doc/apis.adoc#wildflyserverstatus">WildFlyServerStatus</a>
 * is the most recent observed status of the WildFly deployment. Read-only.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class WildFlyServerStatus {

	/**
	 * Hosts that route to the application HTTP service.
	 */
	List<String> hosts = new ArrayList<>();

	/**
	 * Status of the pods.
	 */
	List<PodStatus> pods = new ArrayList<>();

	/**
	 * Number of replicas
	 */
	Integer replicas;

	/**
	 * Number of pods which are under scale down cleaning process
	 */
	Integer scalingdownPods;
}
