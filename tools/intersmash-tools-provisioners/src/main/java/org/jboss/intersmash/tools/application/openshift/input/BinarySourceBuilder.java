package org.jboss.intersmash.tools.application.openshift.input;

import java.nio.file.Path;

import org.jboss.shrinkwrap.api.spec.WebArchive;

public interface BinarySourceBuilder {

	BinarySourceBuilder archive(Path archive);

	BinarySourceBuilder archive(WebArchive archive);

	BuildInput build();

}
