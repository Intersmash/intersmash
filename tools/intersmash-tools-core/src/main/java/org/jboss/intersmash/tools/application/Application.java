package org.jboss.intersmash.tools.application;

/**
 * This interface is not supposed to be implemented by user Applications. See the "Mapping of implemented provisioners"
 * section of Intersmash README.md file for the up-to-date list of supported end users Applications.
 */
public interface Application {

	/**
	 * Serve as an identifier for the application.
	 *
	 * @return name which serves as an identifier for the application
	 */
	String getName();
}
