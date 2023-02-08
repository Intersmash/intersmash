package org.jboss.intersmash.tools.application.openshift.input;

public interface GitSourceBuilder {

	GitSourceBuilder uri(String uri);

	GitSourceBuilder ref(String ref);

	GitSourceBuilder contextDir(String contextDir);

	BuildInput build();

}
