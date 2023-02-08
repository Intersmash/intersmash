package org.jboss.intersmash.tools.provision.openshift.operator.infinispan.infinispan.spec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Specify how Data Grid clusters to back up data across sites
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class InfinispanSitesSpec {

	/**
	 * Specify the local site
	 */
	private InfinispanSitesLocalSpec local;

	/**
	 * Specify the backup locations
	 */
	private List<InfinispanSiteLocationSpec> locations;
}
