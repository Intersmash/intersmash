package org.jboss.intersmash.tools.application.openshift.input;

/**
 * {@link BuildInput} represented by Git URI and reference
 */
public interface GitSource extends BuildInput {

	String getUri();

	String getRef();

	String getContextDir();

}
