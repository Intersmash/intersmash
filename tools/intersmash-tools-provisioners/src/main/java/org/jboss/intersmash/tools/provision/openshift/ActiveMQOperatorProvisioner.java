package org.jboss.intersmash.tools.provision.openshift;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.ActiveMQOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.operator.OperatorProvisioner;
import org.jboss.intersmash.tools.provision.openshift.operator.activemq.address.ActiveMQArtemisAddressList;
import org.jboss.intersmash.tools.provision.openshift.operator.activemq.broker.ActiveMQArtemisList;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShiftWaiters;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import io.amq.broker.v1beta1.ActiveMQArtemis;
import io.amq.broker.v1beta1.ActiveMQArtemisAddress;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import lombok.NonNull;

/**
 * ActiveMQ Operator based provisioner
 */
public class ActiveMQOperatorProvisioner extends OperatorProvisioner<ActiveMQOperatorApplication> {
	private final static String ACTIVE_MQ_ARTEMIS_RESOURCE = "activemqartemises.broker.amq.io";
	private static NonNamespaceOperation<ActiveMQArtemis, ActiveMQArtemisList, Resource<ActiveMQArtemis>> ACTIVE_MQ_ARTEMISES_CLIENT;

	private final static String ACTIVE_MQ_ARTEMIS_ADDRESS_RESOURCE = "activemqartemisaddresses.broker.amq.io";
	private static NonNamespaceOperation<ActiveMQArtemisAddress, ActiveMQArtemisAddressList, Resource<ActiveMQArtemisAddress>> ACTIVE_MQ_ARTEMIS_ADDRESSES_CLIENT;

	//	private final static String ACTIVE_MQ_ARTEMIS_SCALEDOWN_RESOURCE = "activemqartemisscaledowns.broker.amq.io"; // TODO add on demand

	private static final String OPERATOR_ID = "amq-broker-rhel8";

	public ActiveMQOperatorProvisioner(@NonNull ActiveMQOperatorApplication activeMqOperatorApplication) {
		super(activeMqOperatorApplication, OPERATOR_ID);
	}

	public static String getOperatorId() {
		return OPERATOR_ID;
	}

	/**
	 * Get a client capable of working with {@link #ACTIVE_MQ_ARTEMIS_ADDRESS_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #ACTIVE_MQ_ARTEMIS_ADDRESS_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<ActiveMQArtemisAddress, ActiveMQArtemisAddressList, Resource<ActiveMQArtemisAddress>> activeMQArtemisAddressesClient() {
		if (ACTIVE_MQ_ARTEMIS_ADDRESSES_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(ACTIVE_MQ_ARTEMIS_ADDRESS_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(ACTIVE_MQ_ARTEMIS_ADDRESS_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						ACTIVE_MQ_ARTEMIS_ADDRESS_RESOURCE, OPERATOR_ID));
			}

			MixedOperation<ActiveMQArtemisAddress, ActiveMQArtemisAddressList, Resource<ActiveMQArtemisAddress>> addressesClient = OpenShifts
					.master().customResources(crdc, ActiveMQArtemisAddress.class, ActiveMQArtemisAddressList.class);
			ACTIVE_MQ_ARTEMIS_ADDRESSES_CLIENT = addressesClient.inNamespace(OpenShiftConfig.namespace());
		}
		return ACTIVE_MQ_ARTEMIS_ADDRESSES_CLIENT;
	}

	/**
	 * Get a reference to activeMQArtemisAddress object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 *
	 * @param name name of the activeMQArtemisAddress custom resource
	 * @return A concrete {@link Resource} instance representing the {@link ActiveMQArtemisAddress} resource definition
	 */
	public Resource<ActiveMQArtemisAddress> activeMQArtemisAddress(String name) {
		return activeMQArtemisAddressesClient().withName(name);
	}

	/**
	 * Get all activeMQArtemisAddresses maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 * @return A list of {@link Resource} instances representing the {@link ActiveMQArtemisAddress} resource definitions
	 */
	public List<Resource<ActiveMQArtemisAddress>> activeMQArtemisAddresses() {
		ActiveMQOperatorApplication activeMqOperatorApplication = getApplication();
		return activeMqOperatorApplication.getActiveMQArtemisAddresses().stream()
				.map(activeMQArtemisAddress -> activeMQArtemisAddress.getMetadata().getName())
				.map(this::activeMQArtemisAddress)
				.collect(Collectors.toList());
	}

	/**
	 * Get a client capable of working with {@link #ACTIVE_MQ_ARTEMIS_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #ACTIVE_MQ_ARTEMIS_RESOURCE} custom resource
	 */
	public NonNamespaceOperation<ActiveMQArtemis, ActiveMQArtemisList, Resource<ActiveMQArtemis>> activeMQArtemisesClient() {
		if (ACTIVE_MQ_ARTEMISES_CLIENT == null) {
			CustomResourceDefinition crd = OpenShifts.admin().apiextensions().v1().customResourceDefinitions()
					.withName(ACTIVE_MQ_ARTEMIS_RESOURCE).get();
			CustomResourceDefinitionContext crdc = CustomResourceDefinitionContext.fromCrd(crd);
			if (!getCustomResourceDefinitions().contains(ACTIVE_MQ_ARTEMIS_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						ACTIVE_MQ_ARTEMIS_RESOURCE, OPERATOR_ID));
			}

			MixedOperation<ActiveMQArtemis, ActiveMQArtemisList, Resource<ActiveMQArtemis>> amqClient = OpenShifts
					.master().customResources(crdc, ActiveMQArtemis.class, ActiveMQArtemisList.class);
			ACTIVE_MQ_ARTEMISES_CLIENT = amqClient.inNamespace(OpenShiftConfig.namespace());
		}
		return ACTIVE_MQ_ARTEMISES_CLIENT;
	}

	/**
	 * Get a reference to activeMQArtemis object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * @return A concrete {@link Resource} instance representing the {@link ActiveMQArtemis} resource definition
	 */
	public Resource<ActiveMQArtemis> activeMQArtemis() {
		return activeMQArtemisesClient().withName(getApplication().getActiveMQArtemis().getMetadata().getName());
	}

	@Override
	public void deploy() {
		ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				getApplication().getName());
		subscribe();

		int replicas = getApplication().getActiveMQArtemis().getSpec().getDeploymentPlan().getSize();
		// deploy broker
		activeMQArtemisesClient().createOrReplace(getApplication().getActiveMQArtemis());
		// deploy addresses
		getApplication().getActiveMQArtemisAddresses().stream()
				.forEach(activeMQArtemisAddress -> activeMQArtemisAddressesClient().createOrReplace(activeMQArtemisAddress));
		// wait for all resources to be ready
		activeMQArtemisAddresses()
				.forEach(address -> new SimpleWaiter(() -> address.get() != null).level(Level.DEBUG).waitFor());
		new SimpleWaiter(() -> activeMQArtemis().get() != null)
				.failFast(ffCheck)
				.level(Level.DEBUG)
				.waitFor();
		OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck).areExactlyNPodsReady(replicas,
				activeMQArtemis().get().getKind(), getApplication().getActiveMQArtemis().getMetadata().getName())
				.level(Level.DEBUG)
				.waitFor();
		/*
		 * The following wait condition has been suppressed temporarily since:
		 * 1. doesn't allow for the ActiveMQ CR `.podStatus` to be
		 * checked
		 * 2. the condition above waits already for the expected (i.e. representing ActiveMQArtemis CRs) number of CR
		 * pods to be ready
		 */
		//		new SimpleWaiter(() -> activeMQArtemis().get().getStatus().getPodStatus().getReady().size() == replicas)
		//				.failFast(ffCheck)
		//				.level(Level.DEBUG)
		//				.waitFor();
	}

	@Override
	public void undeploy() {
		// delete the resources
		activeMQArtemisAddresses().forEach(address -> address.withPropagationPolicy(DeletionPropagation.FOREGROUND).delete());

		// scaling down to 0 (graceful shutdown) is required since ActiveMQ Operator 7.10
		this.scale(0, Boolean.TRUE);
		activeMQArtemis().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();

		// wait
		new SimpleWaiter(() -> activeMQArtemisesClient().list().getItems().size() == 0).failFast(ffCheck).level(Level.DEBUG)
				.waitFor();
		activeMQArtemisAddresses().forEach(
				address -> new SimpleWaiter(() -> address.get() == null).level(Level.DEBUG).failFast(ffCheck).waitFor());

		unsubscribe();
	}

	@Override
	public void scale(int replicas, boolean wait) {
		ActiveMQArtemis tmpBroker = activeMQArtemis().get();
		tmpBroker.getSpec().getDeploymentPlan().setSize(replicas);
		activeMQArtemis().replace(tmpBroker);
		if (wait) {
			OpenShiftWaiters.get(OpenShiftProvisioner.openShift, ffCheck)
					.areExactlyNPodsReady(replicas, tmpBroker.getKind(), tmpBroker.getMetadata().getName())
					.level(Level.DEBUG)
					.waitFor();
			/*
			 * The following wait condition has been suppressed temporarily since:
			 * 1. doesn't allow for the AMQ Broker CR `.podStatus` to be checked
			 * 2. the condition above waits already for the expected (i.e. representing ActiveMQArtemis CRs) number of CR
			 * pods to be ready
			 */
			//			new SimpleWaiter(() -> activeMQArtemis().get().getStatus().getPodStatus().getReady().size() == replicas)
			//					.failFast(ffCheck)
			//					.level(Level.DEBUG)
			//					.waitFor();
		}
	}

	/**
	 * Get the provisioned application service related Pods
	 * <p>
	 * Currently blocked by the fact that Pod Status pod names do not reflect the reality
	 * <p>
	 * Once these issues are resolved, we can use the ready pod names returned by
	 * {@code ActiveMQArtemisStatus.getPodStatus()} to create the List with pods maintained by the provisioner.
	 *
	 * @return A list of related {@link Pod} instances
	 */
	@Override
	public List<Pod> getPods() {
		throw new UnsupportedOperationException("To be implemented!");
	}

	@Override
	protected String getOperatorCatalogSource() {
		return IntersmashConfig.activeMQOperatorCatalogSource();
	}

	@Override
	protected String getOperatorIndexImage() {
		return IntersmashConfig.activeMQOperatorIndexImage();
	}
}
