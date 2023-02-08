package org.jboss.intersmash.tools.provision.openshift.operator.wildfly.spec;

import org.jboss.intersmash.tools.provision.openshift.operator.wildfly.WildFlyServer;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.fabric8.kubernetes.api.model.EmptyDirVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <a href="https://github.com/wildfly/wildfly-operator/blob/master/doc/apis.adoc#storagespec">StorageSpec</a> defines
 * the configured storage for a {@link WildFlyServer}
 * resource. If neither an emptyDir nor a volumeClaimTemplate is defined, a default
 * {@link EmptyDirVolumeSource} will be used.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class StorageSpec {

	/**
	 * {@link EmptyDirVolumeSource} to be used by the WildFly {@link io.fabric8.kubernetes.api.model.apps.StatefulSet}
	 */
	private EmptyDirVolumeSource emptyDir;

	/**
	 * A {@link PersistentVolumeClaim} spec to be used by the WildFly {@link io.fabric8.kubernetes.api.model.apps.StatefulSet}
	 */
	private PersistentVolumeClaim volumeClaimTemplate;
}
