package org.jboss.intersmash.tools.provision.openshift.operator.wildfly;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.fabric8.kubernetes.api.model.ListMeta;
import io.fabric8.kubernetes.client.CustomResourceList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <a href="https://github.com/wildfly/wildfly-operator/blob/master/doc/apis.adoc#wildflyserverlist">WildFlyServerList</a>
 * defines a list of WildFly deployments.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class WildFlyServerList extends CustomResourceList<WildFlyServer> {

	/**
	 * Standard list’s metadata.
	 */
	@JsonProperty("metadata")
	private ListMeta metadata;

	/**
	 * List of {@link WildFlyServer}
	 */
	@JsonProperty("items")
	private List<WildFlyServer> items = new ArrayList<>();
}
