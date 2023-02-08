package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.keycloak.spec;

import java.util.ArrayList;
import java.util.List;

public final class VolumesSpecBuilder {
	private List<VolumeSpec> items;
	private int defaultMode;

	public VolumesSpecBuilder items(List<VolumeSpec> items) {
		this.items = items;
		return this;
	}

	public VolumesSpecBuilder items(VolumeSpec item) {
		if (items == null) {
			items = new ArrayList<>();
		}
		items.add(item);
		return this;
	}

	public VolumesSpecBuilder defaultMode(int defaultMode) {
		this.defaultMode = defaultMode;
		return this;
	}

	public VolumesSpec build() {
		VolumesSpec volumesSpec = new VolumesSpec();
		volumesSpec.setItems(items);
		volumesSpec.setDefaultMode(defaultMode);
		return volumesSpec;
	}
}
