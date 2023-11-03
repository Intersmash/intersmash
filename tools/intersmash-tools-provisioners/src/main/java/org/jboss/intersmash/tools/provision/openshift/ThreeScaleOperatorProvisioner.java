package org.jboss.intersmash.tools.provision.openshift;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.assertj.core.util.Lists;
import org.jboss.intersmash.tools.IntersmashConfig;
import org.jboss.intersmash.tools.application.openshift.ThreeScaleOperatorApplication;
import org.jboss.intersmash.tools.provision.openshift.operator.OperatorProvisioner;
import org.slf4j.event.Level;

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.event.helpers.EventHelper;
import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.StrategyDeploymentSpec;
import io.fabric8.openshift.client.OpenShiftClient;
import io.sundr.utils.Maps;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.threescale.apps.v1alpha1.APIManager;
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

	private final Map<String, String> LABELS = Maps.create(
			"intersmash-provisioner-id", OPERATOR_ID,
			"intersmash-application-name", getApplication().getName());

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

	public String getWildcardDomain() {
		// oc get routes/console -n openshift-console -o template --template '{{.spec.host}}'
		// e.g. console-openshift-console.apps.mnovak-bnqp.eapqe.psi.redhat.com
		/*String openshiftConsoleHost = adminBinary.execute(
				"get",
				"routes/console",
				"-n",
				"openshift-console",
				"-o",
				"template",
				"--template", "{{.spec.host}}");*/
		//OpenShifts.admin().
		/*String openshiftConsoleHost = OpenShiftProvisioner.openShift
				.getClient()
				.routes()
				.inNamespace("openshift-console")
				.withName("console")
				.get()
				.getSpec()
				.getHost();
		return openshiftConsoleHost.replaceFirst("^[^.]+", OpenShiftConfig.namespace());*/
		// we want 3scale to handle the current namespace
		return OpenShifts.master().getOpenshiftUrl().getHost().replaceFirst("^api", OpenShiftConfig.namespace() + ".apps");
	}

	@Override
	public void deploy() {
		subscribe();

		// APIManager
		APIManager apiManager = getApplication().getAPIManager();
		apiManager.getMetadata().getLabels().putAll(LABELS);
		apiManager.getSpec().setWildcardDomain(getWildcardDomain());
		apiManagerClient().resource(apiManager).create();

		getApplication().getDeveloperAccounts().forEach(
				developerAccount -> {
					developerAccount.getMetadata().getLabels().putAll(LABELS);
					developerAccountClient().resource(developerAccount).create();
				});
		getApplication().getDeveloperUsers().forEach(
				developerUser -> {
					developerUser.getMetadata().getLabels().putAll(LABELS);
					developerUserClient().resource(developerUser).create();
				});
		getApplication().getBackends().forEach(
				backend -> {
					backend.getMetadata().getLabels().putAll(LABELS);
					backendClient().resource(backend).create();
				});
		getApplication().getProducts().forEach(
				product -> {
					product.getMetadata().getLabels().putAll(LABELS);
					productClient().resource(product).create();
				});
		getApplication().getApplications().forEach(
				application -> {
					application.getMetadata().getLabels().putAll(LABELS);
					applicationClient().resource(application).create();
				});
		getApplication().getProxyConfigPromotes().forEach(
				proxyConfigPromote -> {
					proxyConfigPromote.getMetadata().getLabels().putAll(LABELS);
					proxyConfigPromoteClient().resource(proxyConfigPromote).create();
				});

		// wait for APIManager to be Ready
		new SimpleWaiter(() -> {
			return apiManagerClient().withLabels(LABELS).list().getItems().stream().anyMatch(
					apiManager1 -> {
						long cnt = apiManager1.getStatus().getConditions().stream().filter(
								condition -> condition.getType().equalsIgnoreCase("Available")
										&& condition.getStatus().equalsIgnoreCase("True"))
								.count();
						return cnt > 0;
					});
		}).level(Level.DEBUG).waitFor();
		//TODO: create DeveloperAccount etc..
	}

	@Override
	public void undeploy() {
		apiManagerClient().withLabels(LABELS).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		developerAccountClient().withLabels(LABELS).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		developerUserClient().withLabels(LABELS).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		backendClient().withLabels(LABELS).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		productClient().withLabels(LABELS).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		applicationClient().withLabels(LABELS).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
		proxyConfigPromoteClient().withLabels(LABELS).withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
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
		/*String deploymentsJsonStr = adminBinary.execute(
				"get",
				"csvs",
				getCurrentCSV(),
				"-o",
				"template",
				"--template",
				"{{range .spec.install.spec.deployments}}{{$comma := \"\"}}{{\"{\"}}\"{{.name}}\":\"{{.spec.replicas}}\"{{$comma = \",\"}}{{\"}\"}}{{end}}");
		Map<String, String> deployments = new ObjectMapper().readValue(
				deploymentsJsonStr,
				HashMap.class);*/
		// TODO: ^^replace with e.g. OpenShiftProvisioner.openShift...
		List<StrategyDeploymentSpec> deploymentSpecList = OpenShiftProvisioner.openShift
				.operatorHub()
				.clusterServiceVersions()
				.inNamespace(OpenShiftConfig.namespace())
				.withName(getCurrentCSV())
				.get()
				.getSpec()
				.getInstall()
				.getSpec()
				.getDeployments();

		//for (Map.Entry<String, String> deployment : deployments.entrySet()) {
		for (StrategyDeploymentSpec deploymentSpec : deploymentSpecList) {
			//String name = deployment.getKey();
			//long replicas = Long.parseLong(deployment.getValue());
			long replicas = deploymentSpec.getSpec().getReplicas();
			Map<String, String> labels = deploymentSpec.getSpec().getSelector().getMatchLabels();

			// Get all the labels for the current deployment
			// e.g. {"app":"3scale-api-management","control-plane":"controller-manager"}
			/*String labelsJsonStr = adminBinary.execute(
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
					HashMap.class);*/
			// TODO: ^^replace with e.g. OpenShiftProvisioner.openShift...

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
