package org.jboss.intersmash.tools.provision.openshift.operator.wildfly.status;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <a href="https://github.com/wildfly/wildfly-operator/blob/master/doc/apis.adoc#wildflyserverstatus">WildFlyServerStatus</a>
 * is the most recent observed status of the WildFly deployment. Read-only.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class WildFlyServerStatus {

	/**
	 * Hosts that route to the application HTTP service.
	 */
	List<String> hosts = new ArrayList<>();

	/**
	 * Status of the pods.
	 */
	List<PodStatus> pods = new ArrayList<>();

	/**
	 * Number of replicas
	 */
	Integer replicas;

	/**
	 * Number of pods which are under scale down cleaning process
	 */
	Integer scalingdownPods;
}
