package org.jboss.intersmash.tools.provision.openshift.template;

/**
 * Interface for OpenShift templates.
 *
 * One example of concrete implementation is
 * {@code org.jboss.intersmash.tools.provision.openshift.template.KeycloakTemplate}
 */
public interface OpenShiftTemplate {

	/**
	 * Get a name of the template. Name is stripped of any product related information and target file extension.
	 * <p>
	 * Template name for datagrid73-mysql-persistent.json file would be mysql-persistent.
	 *
	 * @return name of template
	 */
	String getLabel();
}
