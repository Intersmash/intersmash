package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache;

import java.util.Map;

import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.spec.AdminAuth;
import org.jboss.intersmash.tools.provision.openshift.operator.infinispan.cache.spec.CacheSpec;

import io.fabric8.kubernetes.api.model.ObjectMeta;

public final class CacheBuilder {
	private Map<String, String> labels;
	private AdminAuth adminAuth;
	private String name;
	private String clusterName;
	private String template;
	private String templateName;

	/**
	 * Initialize the {@link CacheBuilder} with given resource name.
	 *
	 * @param name resource object name
	 */
	public CacheBuilder(String name) {
		this.name = name;
	}

	/**
	 * Initialize the {@link CacheBuilder} with given resource name and labels.
	 *
	 * @param name resource object name
	 * @param labels key/value pairs that are attached to objects
	 */
	public CacheBuilder(String name, Map<String, String> labels) {
		this.name = name;
		this.labels = labels;
	}

	/**
	 * Set the authentication info
	 *
	 * @param adminAuth Authentication info
	 * @return this
	 */
	public CacheBuilder adminAuth(AdminAuth adminAuth) {
		this.adminAuth = adminAuth;
		return this;
	}

	/**
	 * Set the name of the cluster where to create the cache
	 *
	 * @param clusterName Name of the cluster where to create the cache
	 * @return this
	 */
	public CacheBuilder clusterName(String clusterName) {
		this.clusterName = clusterName;
		return this;
	}

	/**
	 * Set the name of the cache to be created. If empty ObjectMeta.Name will be used
	 *
	 * @param name Name of the cache to be created. If empty ObjectMeta.Name will be used
	 * @return this
	 */
	public CacheBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Set the cache template in XML format
	 *
	 * @param template Cache template in XML format
	 * @return this
	 */
	public CacheBuilder template(String template) {
		this.template = template;
		return this;
	}

	/**
	 * Set the name of the template to be used to create this cache
	 *
	 * @param templateName Name of the template to be used to create this cache
	 * @return this
	 */
	public CacheBuilder templateName(String templateName) {
		this.templateName = templateName;
		return this;
	}

	public Cache build() {
		Cache cache = new Cache();
		cache.setMetadata(new ObjectMeta());
		cache.getMetadata().setName(name);
		cache.getMetadata().setLabels(labels);
		CacheSpec cacheSpec = new CacheSpec();
		cacheSpec.setAdminAuth(adminAuth);
		cacheSpec.setClusterName(clusterName);
		cacheSpec.setName(name);
		cacheSpec.setTemplate(template);
		cacheSpec.setTemplateName(templateName);
		cache.setSpec(cacheSpec);
		return cache;
	}
}
