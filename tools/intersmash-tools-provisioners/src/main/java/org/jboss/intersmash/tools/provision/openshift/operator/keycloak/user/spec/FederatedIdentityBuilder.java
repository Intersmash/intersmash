package org.jboss.intersmash.tools.provision.openshift.operator.keycloak.user.spec;

public final class FederatedIdentityBuilder {
	private String identityProvider;
	private String userId;
	private String userName;

	/**
	 * Set the Identity Provider.
	 *
	 * @param identityProvider the Identity Provider that should be used
	 * @return this
	 */
	public FederatedIdentityBuilder identityProvider(String identityProvider) {
		this.identityProvider = identityProvider;
		return this;
	}

	/**
	 * Set the Federated Identity User ID.
	 *
	 * @param userId Federated Identity User ID
	 * @return this
	 */
	public FederatedIdentityBuilder userId(String userId) {
		this.userId = userId;
		return this;
	}

	/**
	 * Federated Identity User Name.
	 *
	 * @param userName Federated Identity User name
	 * @return this
	 */
	public FederatedIdentityBuilder userName(String userName) {
		this.userName = userName;
		return this;
	}

	public FederatedIdentity build() {
		FederatedIdentity federatedIdentity = new FederatedIdentity();
		federatedIdentity.setIdentityProvider(identityProvider);
		federatedIdentity.setUserId(userId);
		federatedIdentity.setUserName(userName);
		return federatedIdentity;
	}
}
