package io.hyperfoil.v1alpha2.hyperfoilspec;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "secret" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class Auth implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * Optional; Name of secret used for basic authentication. Must contain key 'password'.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("secret")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Optional; Name of secret used for basic authentication. Must contain key 'password'.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String secret;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
}
