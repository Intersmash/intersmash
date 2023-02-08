package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.spec;

public final class AdminAuthBuilder {
	private String secretName;
	private String username;
	private String password;

	/**
	 * Set the name of the secret containing both admin username and password
	 *
	 * @param secretName Name of the secret containing both admin username and password
	 * @return this
	 */
	public AdminAuthBuilder secretName(String secretName) {
		this.secretName = secretName;
		return this;
	}

	/**
	 * Set the secret and key containing the admin username for authentication.
	 *
	 * @param username Secret and key containing the admin username for authentication
	 * @return this
	 */
	public AdminAuthBuilder username(String username) {
		this.username = username;
		return this;
	}

	/**
	 * Set the secret and key containing the admin password for authentication.
	 *
	 * @param password Secret and key containing the admin password for authentication
	 * @return this
	 */
	public AdminAuthBuilder password(String password) {
		this.password = password;
		return this;
	}

	public AdminAuth build() {
		AdminAuth adminAuth = new AdminAuth();
		adminAuth.setSecretName(secretName);
		adminAuth.setUsername(username);
		adminAuth.setPassword(password);
		return adminAuth;
	}
}
