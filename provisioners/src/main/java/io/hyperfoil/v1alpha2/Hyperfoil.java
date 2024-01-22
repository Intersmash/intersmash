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
package io.hyperfoil.v1alpha2;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1alpha2", storage = true, served = true)
@io.fabric8.kubernetes.model.annotation.Group("hyperfoil.io")
public class Hyperfoil extends io.fabric8.kubernetes.client.CustomResource<HyperfoilSpec, HyperfoilStatus>
		implements io.fabric8.kubernetes.api.model.Namespaced {
}
