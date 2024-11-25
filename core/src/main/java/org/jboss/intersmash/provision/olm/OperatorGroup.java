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
package org.jboss.intersmash.provision.olm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

/**
 * An OperatorGroup is an OLM resource that provides multi-tenant configuration to OLM-installed Operators.
 * An OperatorGroup selects target namespaces in which to generate required RBAC access for its member Operators.
 * <p>
 * https://docs.openshift.com/container-platform/4.4/operators/understanding_olm/olm-understanding-operatorgroups.html#olm-operatorgroups-about_olm-understanding-operatorgroups
 */
@Group("operators.coreos.com")
@Version("v1")
public class OperatorGroup extends CustomResource implements SerializationCapableResource<OperatorGroup> {

	private Map<String, List<String>> spec = new HashMap<>();

	public OperatorGroup() {
		this.setKind("OperatorGroup");
		this.setApiVersion("operators.coreos.com/v1"); // todo we could get this one from target cluster as well
	}

	public OperatorGroup(String namespace) {
		this();
		this.getMetadata().setName(namespace + "-operators");
		this.getMetadata().setNamespace(namespace);
		List<String> targetNamespaces = new ArrayList<>();
		targetNamespaces.add(namespace);
		this.getSpec().put("targetNamespaces", targetNamespaces);
	}

	public Map<String, List<String>> getSpec() {
		return spec;
	}

	public void setSpec(Map<String, List<String>> spec) {
		this.spec = spec;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OperatorGroup))
			return false;
		OperatorGroup that = (OperatorGroup) o;
		return spec.equals(that.spec) &&
				this.getMetadata().getName().equals(that.getMetadata().getName()) &&
				this.getMetadata().getNamespace().equals(that.getMetadata().getNamespace());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getKind(), getApiVersion(), getMetadata(), spec);
	}

	@Override
	public String toString() {
		return super.toString() + " OperatorGroup{" +
				"spec=" + spec +
				'}';
	}

	@Override
	public OperatorGroup load(OperatorGroup loaded) {
		this.setMetadata(loaded.getMetadata());
		this.setSpec(loaded.getSpec());
		return this;
	}
}
