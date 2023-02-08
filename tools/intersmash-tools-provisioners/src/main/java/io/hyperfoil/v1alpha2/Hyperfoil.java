package io.hyperfoil.v1alpha2;

@io.fabric8.kubernetes.model.annotation.Version(value = "v1alpha2", storage = true, served = true)
@io.fabric8.kubernetes.model.annotation.Group("hyperfoil.io")
public class Hyperfoil extends io.fabric8.kubernetes.client.CustomResource<HyperfoilSpec, HyperfoilStatus>
		implements io.fabric8.kubernetes.api.model.Namespaced {
}
