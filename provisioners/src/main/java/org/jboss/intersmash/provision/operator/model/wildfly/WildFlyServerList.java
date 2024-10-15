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
package org.jboss.intersmash.provision.operator.model.wildfly;

import java.util.ArrayList;
import java.util.List;

import org.wildfly.v1alpha1.WildFlyServer;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.fabric8.kubernetes.api.model.ListMeta;
import io.fabric8.kubernetes.client.CustomResourceList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <a href="https://github.com/wildfly/wildfly-operator/blob/master/doc/apis.adoc#wildflyserverlist">WildFlyServerList</a>
 * defines a list of WildFly deployments.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class WildFlyServerList extends CustomResourceList<WildFlyServer> {

	/**
	 * Standard list’s metadata.
	 */
	@JsonProperty("metadata")
	private ListMeta metadata;

	/**
	 * List of {@link WildFlyServer}
	 */
	@JsonProperty("items")
	private List<WildFlyServer> items = new ArrayList<>();
}
