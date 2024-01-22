/**
 * Copyright (C) 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyperfoil.v1alpha2;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "agentDeployTimeout", "auth", "image", "log", "persistentVolumeClaim",
		"postHooks", "preHooks", "route", "secretEnvVars", "triggerUrl", "version" })
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class HyperfoilSpec implements io.fabric8.kubernetes.api.model.KubernetesResource {

	/**
	 * Deploy timeout for agents, in milliseconds.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("agentDeployTimeout")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Deploy timeout for agents, in milliseconds.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private Long agentDeployTimeout;

	public Long getAgentDeployTimeout() {
		return agentDeployTimeout;
	}

	public void setAgentDeployTimeout(Long agentDeployTimeout) {
		this.agentDeployTimeout = agentDeployTimeout;
	}

	/**
	 * Authentication/authorization settings.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("auth")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Authentication/authorization settings.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private io.hyperfoil.v1alpha2.hyperfoilspec.Auth auth;

	public io.hyperfoil.v1alpha2.hyperfoilspec.Auth getAuth() {
		return auth;
	}

	public void setAuth(io.hyperfoil.v1alpha2.hyperfoilspec.Auth auth) {
		this.auth = auth;
	}

	/**
	 * Controller image. If 'version' is defined, too, the tag is replaced (or appended). Defaults to 'quay.io/hyperfoil/hyperfoil'
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("image")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Controller image. If 'version' is defined, too, the tag is replaced (or appended). Defaults to 'quay.io/hyperfoil/hyperfoil'")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String image;

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * Name of the config map and optionally its entry (separated by '/': e.g myconfigmap/log4j2-superverbose.xml) storing Log4j2 configuration file. By default the Controller uses its embedded configuration.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("log")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Name of the config map and optionally its entry (separated by '/': e.g myconfigmap/log4j2-superverbose.xml) storing Log4j2 configuration file. By default the Controller uses its embedded configuration.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String log;

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	/**
	 * Name of the PVC hyperfoil should mount for its workdir.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("persistentVolumeClaim")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Name of the PVC hyperfoil should mount for its workdir.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String persistentVolumeClaim;

	public String getPersistentVolumeClaim() {
		return persistentVolumeClaim;
	}

	public void setPersistentVolumeClaim(String persistentVolumeClaim) {
		this.persistentVolumeClaim = persistentVolumeClaim;
	}

	/**
	 * Names of config maps and optionally keys (separated by '/') holding hooks that run after the run finishes.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("postHooks")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Names of config maps and optionally keys (separated by '/') holding hooks that run after the run finishes.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<String> postHooks;

	public java.util.List<String> getPostHooks() {
		return postHooks;
	}

	public void setPostHooks(java.util.List<String> postHooks) {
		this.postHooks = postHooks;
	}

	/**
	 * Names of config maps and optionally keys (separated by '/') holding hooks that run before the run starts.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("preHooks")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Names of config maps and optionally keys (separated by '/') holding hooks that run before the run starts.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<String> preHooks;

	public java.util.List<String> getPreHooks() {
		return preHooks;
	}

	public void setPreHooks(java.util.List<String> preHooks) {
		this.preHooks = preHooks;
	}

	/**
	 * Specification of the exposed route.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("route")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Specification of the exposed route.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private io.hyperfoil.v1alpha2.hyperfoilspec.Route route;

	public io.hyperfoil.v1alpha2.hyperfoilspec.Route getRoute() {
		return route;
	}

	public void setRoute(io.hyperfoil.v1alpha2.hyperfoilspec.Route route) {
		this.route = route;
	}

	/**
	 * List of secrets in this namespace; each entry from those secrets will be mapped as environment variable, using the key as variable name.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("secretEnvVars")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("List of secrets in this namespace; each entry from those secrets will be mapped as environment variable, using the key as variable name.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private java.util.List<String> secretEnvVars;

	public java.util.List<String> getSecretEnvVars() {
		return secretEnvVars;
	}

	public void setSecretEnvVars(java.util.List<String> secretEnvVars) {
		this.secretEnvVars = secretEnvVars;
	}

	@com.fasterxml.jackson.annotation.JsonProperty("triggerUrl")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("If this is set the controller does not start benchmark run right away after hitting /benchmark/my-benchmark/start ; instead it responds with status 301 and header Location set to concatenation of this string and 'BENCHMARK=my-benchmark&RUN_ID=xxxx'. CLI interprets that response as a request to hit CI instance on this URL, assuming that CI will trigger a new job that will eventually call /benchmark/my-benchmark/start?runId=xxxx with header 'x-trigger-job'. This is useful if the the CI has to synchronize Hyperfoil to other benchmarks that don't use this controller instance.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String triggerUrl;

	public String getTriggerUrl() {
		return triggerUrl;
	}

	public void setTriggerUrl(String triggerUrl) {
		this.triggerUrl = triggerUrl;
	}

	/**
	 * Tag for controller image. Defaults to version matching the operator version.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("version")
	@com.fasterxml.jackson.annotation.JsonPropertyDescription("Tag for controller image. Defaults to version matching the operator version.")
	@com.fasterxml.jackson.annotation.JsonSetter(nulls = com.fasterxml.jackson.annotation.Nulls.SKIP)
	private String version;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
