package org.jboss.intersmash.tools.application.openshift.input;

import java.nio.file.Path;

/**
 * {@link BuildInput} represented by path to archive to be deployed
 */
public interface BinarySource extends BuildInput {

	Path getArchive();

}
