package org.jboss.intersmash.tools.provision.openshift.operator.wildfly.status;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <a href="https://github.com/wildfly/wildfly-operator/blob/master/doc/apis.adoc#podstatus">PodStatus</a> is the most
 * recent observed status of a pod running the WildFly application.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PodStatus {

	/**
	 * Name of the Pod.
	 */
	private String name;

	/**
	 * IP address allocated to the pod.
	 */
	private String podIP;

	/**
	 * State of the pod from perspective of scale down process. By default, it’s active which means it serves requests.
	 */
	private String state;
}
