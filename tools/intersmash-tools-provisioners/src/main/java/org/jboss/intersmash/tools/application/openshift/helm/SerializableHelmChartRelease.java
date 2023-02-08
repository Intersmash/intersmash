package org.jboss.intersmash.tools.application.openshift.helm;

import java.nio.file.Path;

/**
 * Defines the contract for implementors that represent a Helm Chart release values data structure which can be
 * serialized to a file.
 */
public interface SerializableHelmChartRelease {
	/**
	 * Implementors must generate the file by serializing the data at each call and reflect the information held by
	 * the concrete instance
	 * @return Instance of {@link Path} that represents the generated file location
	 */
	Path toValuesFile();
}
