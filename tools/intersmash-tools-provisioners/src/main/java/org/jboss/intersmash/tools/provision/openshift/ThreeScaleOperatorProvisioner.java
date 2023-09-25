package org.jboss.intersmash.tools.provision.openshift;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.assertj.core.util.Lists;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.ThreeScaleOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.operator.OperatorProvisioner;
import org.slf4j.event.Level;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.client.OpenShiftClient;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.threescale.apps.v1alpha1.APIManager;
import net.threescale.apps.v1alpha1.APIManagerSpec;
import net.threescale.apps.v1alpha1.apimanagerspec.Apicast;
import net.threescale.apps.v1alpha1.apimanagerspec.Zync;
import net.threescale.apps.v1alpha1.apimanagerspec.apicast.ProductionSpec;
import net.threescale.apps.v1alpha1.apimanagerspec.apicast.StagingSpec;
import net.threescale.apps.v1alpha1.apimanagerspec.backend.CronSpec;
import net.threescale.apps.v1alpha1.apimanagerspec.backend.ListenerSpec;
import net.threescale.apps.v1alpha1.apimanagerspec.backend.WorkerSpec;
import net.threescale.apps.v1alpha1.apimanagerspec.system.AppSpec;
import net.threescale.apps.v1alpha1.apimanagerspec.system.SidekiqSpec;
import net.threescale.apps.v1alpha1.apimanagerspec.zync.QueSpec;
import net.threescale.capabilities.v1beta1.Application;
import net.threescale.capabilities.v1beta1.Backend;
import net.threescale.capabilities.v1beta1.DeveloperAccount;
import net.threescale.capabilities.v1beta1.DeveloperUser;
import net.threescale.capabilities.v1beta1.Product;
import net.threescale.capabilities.v1beta1.ProxyConfigPromote;

@Slf4j
public class ThreeScaleOperatorProvisioner extends OperatorProvisioner<ThreeScaleOperatorApplication> {

	private static final String OPERATOR_ID = IntersmashConfig.threeScaleOperatorPackageManifest();
	private static final String STATEFUL_SET_NAME = "3scale";
	private final OpenShiftBinary adminBinary;

	public ThreeScaleOperatorProvisioner(@NonNull ThreeScaleOperatorApplication threeScaleOperatorApplication) {
		super(threeScaleOperatorApplication, OPERATOR_ID);
		this.adminBinary = OpenShifts.adminBinary();
		this.ffCheck = FailFastUtils.getFailFastCheck(EventHelper.timeOfLastEventBMOrTestNamespaceOrEpoch(),
				getApplication().getName());
	}

	public static String getOperatorId() {
		return OPERATOR_ID;
	}

	@Override
	protected String getOperatorCatalogSource() {
		return IntersmashConfig.threeScaleOperatorCatalogSource();
	}

	@Override
	protected String getOperatorIndexImage() {
		return IntersmashConfig.threeScaleOperatorIndexImage();
	}

	@Override
	protected String getOperatorChannel() {
		return IntersmashConfig.threeScaleOperatorChannel();
	}

	@Override
	public void subscribe() {
		subscribe(
				INSTALLPLAN_APPROVAL_AUTOMATIC,
				Map.of(
						"THREESCALE_DEBUG", "1"));
	}

	@Override
	public void deploy() {
		subscribe();

		// oc get routes/console -n openshift-console -o template --template '{{.spec.host}}'
		// e.g. console-openshift-console.apps.mnovak-bnqp.eapqe.psi.redhat.com
		String openshiftConsoleHost = adminBinary.execute(
				"get",
				"routes/console",
				"-n",
				"openshift-console",
				"-o",
				"template",
				"--template", "{{.spec.host}}");
		// we want 3scale to handle the current namespace
		String wildcardDomain = openshiftConsoleHost.replaceFirst("^[^.]+", OpenShiftConfig.namespace());

		// APIManager
		APIManagerSpec apiManagerSpec = new APIManagerSpec();
		apiManagerSpec.setWildcardDomain(wildcardDomain);

		net.threescale.apps.v1alpha1.apimanagerspec.System system = new net.threescale.apps.v1alpha1.apimanagerspec.System();
		AppSpec appSpec = new AppSpec();
		appSpec.setReplicas(1L);
		system.setAppSpec(appSpec);
		SidekiqSpec sidekiqSpec = new SidekiqSpec();
		sidekiqSpec.setReplicas(1L);
		system.setSidekiqSpec(sidekiqSpec);
		apiManagerSpec.setSystem(system);

		Zync zync = new Zync();
		net.threescale.apps.v1alpha1.apimanagerspec.zync.AppSpec appSpecZ = new net.threescale.apps.v1alpha1.apimanagerspec.zync.AppSpec();
		appSpecZ.setReplicas(1L);
		zync.setAppSpec(appSpecZ);
		QueSpec queSpec = new QueSpec();
		queSpec.setReplicas(1L);
		zync.setQueSpec(queSpec);
		apiManagerSpec.setZync(zync);

		net.threescale.apps.v1alpha1.apimanagerspec.Backend backend = new net.threescale.apps.v1alpha1.apimanagerspec.Backend();
		CronSpec cronSpec = new CronSpec();
		cronSpec.setReplicas(1L);
		backend.setCronSpec(cronSpec);
		ListenerSpec listenerSpec = new ListenerSpec();
		listenerSpec.setReplicas(1L);
		backend.setListenerSpec(listenerSpec);
		WorkerSpec workerSpec = new WorkerSpec();
		workerSpec.setReplicas(1L);
		backend.setWorkerSpec(workerSpec);
		apiManagerSpec.setBackend(backend);

		Apicast apicast = new Apicast();
		ProductionSpec productionSpec = new ProductionSpec();
		productionSpec.setReplicas(1L);
		apicast.setProductionSpec(productionSpec);
		StagingSpec stagingSpec = new StagingSpec();
		stagingSpec.setReplicas(1L);
		apicast.setStagingSpec(stagingSpec);
		apiManagerSpec.setApicast(apicast);

		APIManager apiManager = new APIManager();
		ObjectMeta metadata = new ObjectMeta();
		metadata.setName(getApplication().getName());
		apiManager.setMetadata(metadata);
		apiManager.setSpec(apiManagerSpec);

		apiManagerClient().resource(apiManager).create();
	}

	@Override
	public void undeploy() {
		apiManagerClient().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		developerAccountClient().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		developerUserClient().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		backendClient().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		productClient().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		applicationClient().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		proxyConfigPromoteClient().withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
	}

	@Override
	public List<Pod> getPods() {
		StatefulSet statefulSet = OpenShiftProvisioner.openShift.getStatefulSet(STATEFUL_SET_NAME);
		return Objects.nonNull(statefulSet)
				? OpenShiftProvisioner.openShift.getLabeledPods("controller-revision-hash",
						statefulSet.getStatus().getUpdateRevision())
				: Lists.emptyList();
	}

	@Override
	public String getUrl(String routeName, boolean secure) {
		return super.getUrl(routeName, secure);
	}

	@Override
	public void scale(int replicas, boolean wait) {
		log.debug("scale");
	}

	@SneakyThrows
	@Override
	protected void waitForOperatorPod() {

		// Get all deployments defined in the Operator's ClusterServiceVersion
		// e.g. {"threescale-operator-controller-manager-v2":"1"}
		String deploymentsJsonStr = adminBinary.execute(
				"get",
				"csvs",
				getCurrentCSV(),
				"-o",
				"template",
				"--template",
				"{{range .spec.install.spec.deployments}}{{$comma := \"\"}}{{\"{\"}}\"{{.name}}\":\"{{.spec.replicas}}\"{{$comma = \",\"}}{{\"}\"}}{{end}}");
		Map<String, String> deployments = new ObjectMapper().readValue(
				deploymentsJsonStr,
				HashMap.class);

		for (Map.Entry<String, String> deployment : deployments.entrySet()) {
			String name = deployment.getKey();
			long replicas = Long.parseLong(deployment.getValue());

			// Get all the labels for the current deployment
			// e.g. {"app":"3scale-api-management","control-plane":"controller-manager"}
			String labelsJsonStr = adminBinary.execute(
					"get",
					"csvs",
					getCurrentCSV(),
					"-o",
					"template",
					"--template",
					String.format(
							"{{range .spec.install.spec.deployments}}{{if eq .name \"%s\"}}{{$comma := \"\"}}{{\"{\"}}{{range $key, $value := .spec.selector.matchLabels}}{{$comma}}\"{{$key}}\":\"{{$value}}\"{{$comma = \",\"}}{{end}}{{\"}\"}}{{end}}{{end}}",
							name));
			Map<String, String> labels = new ObjectMapper().readValue(
					labelsJsonStr,
					HashMap.class);

			// wait for the number of replicas defined in the current deployment to be Ready
			new SimpleWaiter(() -> {
				List<Pod> pods = openShift.getLabeledPods(labels);
				long podsActuallyReady = pods.stream().filter(
						pod -> pod.getStatus().getConditions().stream().anyMatch(
								podCondition -> podCondition.getStatus().equalsIgnoreCase("True")
										&& podCondition.getType().equalsIgnoreCase("Ready")))
						.count();
				return podsActuallyReady == replicas;
			}).level(Level.DEBUG).waitFor();
		}
	}

	// https://github.com/fabric8io/kubernetes-client/blob/main/doc/CHEATSHEET.md#resource-typed-api
	// https://github.com/fabric8io/kubernetes-client/blob/main/doc/CHEATSHEET.md#resource-typeless-api
	// https://github.com/fabric8io/kubernetes-client/discussions/4393
	private NonNamespaceOperation<APIManager, KubernetesResourceList<APIManager>, Resource<APIManager>> apiManagerClient() {
		OpenShift openShift = OpenShifts.master();
		OpenShiftClient client = openShift.getClient();
		MixedOperation<APIManager, KubernetesResourceList<APIManager>, Resource<APIManager>> crdClient = client
				.resources(APIManager.class);
		return crdClient.inNamespace(OpenShiftConfig.namespace());
	}

	private NonNamespaceOperation<DeveloperAccount, KubernetesResourceList<DeveloperAccount>, Resource<DeveloperAccount>> developerAccountClient() {
		OpenShift openShift = OpenShifts.master();
		OpenShiftClient client = openShift.getClient();
		MixedOperation<DeveloperAccount, KubernetesResourceList<DeveloperAccount>, Resource<DeveloperAccount>> crdClient = client
				.resources(DeveloperAccount.class);
		return crdClient.inNamespace(OpenShiftConfig.namespace());
	}

	private NonNamespaceOperation<DeveloperUser, KubernetesResourceList<DeveloperUser>, Resource<DeveloperUser>> developerUserClient() {
		OpenShift openShift = OpenShifts.master();
		OpenShiftClient client = openShift.getClient();
		MixedOperation<DeveloperUser, KubernetesResourceList<DeveloperUser>, Resource<DeveloperUser>> crdClient = client
				.resources(DeveloperUser.class);
		return crdClient.inNamespace(OpenShiftConfig.namespace());
	}

	private NonNamespaceOperation<Backend, KubernetesResourceList<Backend>, Resource<Backend>> backendClient() {
		OpenShift openShift = OpenShifts.master();
		OpenShiftClient client = openShift.getClient();
		MixedOperation<Backend, KubernetesResourceList<Backend>, Resource<Backend>> crdClient = client
				.resources(Backend.class);
		return crdClient.inNamespace(OpenShiftConfig.namespace());
	}

	private NonNamespaceOperation<Product, KubernetesResourceList<Product>, Resource<Product>> productClient() {
		OpenShift openShift = OpenShifts.master();
		OpenShiftClient client = openShift.getClient();
		MixedOperation<Product, KubernetesResourceList<Product>, Resource<Product>> crdClient = client
				.resources(Product.class);
		return crdClient.inNamespace(OpenShiftConfig.namespace());
	}

	private NonNamespaceOperation<Application, KubernetesResourceList<Application>, Resource<Application>> applicationClient() {
		OpenShift openShift = OpenShifts.master();
		OpenShiftClient client = openShift.getClient();
		MixedOperation<Application, KubernetesResourceList<Application>, Resource<Application>> crdClient = client
				.resources(Application.class);
		return crdClient.inNamespace(OpenShiftConfig.namespace());
	}

	private NonNamespaceOperation<ProxyConfigPromote, KubernetesResourceList<ProxyConfigPromote>, Resource<ProxyConfigPromote>> proxyConfigPromoteClient() {
		OpenShift openShift = OpenShifts.master();
		OpenShiftClient client = openShift.getClient();
		MixedOperation<ProxyConfigPromote, KubernetesResourceList<ProxyConfigPromote>, Resource<ProxyConfigPromote>> crdClient = client
				.resources(ProxyConfigPromote.class);
		return crdClient.inNamespace(OpenShiftConfig.namespace());
	}
}
