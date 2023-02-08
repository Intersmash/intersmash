# Intersmash Operator based provisioning implementation How-to

This guide shows how to update Intersmash provisioners with a support for a new operator.
Intersmash operator based provisioning was initially implemented by coding all the required components manually.

After the initial implementation was done, the Hyperfoil operator based provisioner has been implemented following
a different approach, i.e. by generating the required Java Operator SDK through the Fabric8 Kubernetes Client Java 
Generator, from the Hyperfoil CRDs. In this very case the JOSDK was generated manually, by using a CLI.

Finally, the ActiveMQ operato based provisioner JOSDK was generated using the Fabric8 Kubernetes Client Java
Generator Maven plugin from the ActiveMQ CRDs, and the generated sources are not under version control.

This guide will demonstrate the initial approach and will use the Keycloak operator support addition works as an 
example of a manually implemented operator based provisioner.
In the future the existing operator based provisioning should be all aligned to use the generated JOSDK approach.

## Manual implementation

**checklist:**
 - [ ] Prepare resources
 - [ ] Create Provisioner & Application stubs
 - [ ] Create model
 - [ ] Create builders for spec
 - [ ] Create clients
 - [ ] Create model tests (verify client methods)
 - [ ] Implement methods from OpenShiftProvisioner interface
 - [ ] Register a new provisioner

### Prepare resources
* get operator sources - see `Repository` section in Web console OperatorHub operator entry
* get documentation (could contain some leads and feed for javadoc)
* get operator ID from operator catalogue - see `oc get packagemanifest -n openshift-marketplace`
* get provided Custom Resource Definitions (CRDs) (printed as a part of `OperatorSubscriptionTestCase`)

Put all of this collected info into the Intersmash issue which is tracking the operator addition works.

#### Example Outcome
* sources: https://github.com/keycloak/keycloak-operator/tree/master/pkg/apis/keycloak/v1alpha1
* CRDs: https://github.com/keycloak/keycloak-operator/tree/master/deploy/crds
* examples: https://github.com/keycloak/keycloak-operator/tree/master/deploy/examples
* documentation: https://access.redhat.com/documentation/en-us/red_hat_single_sign-on/7.4/html/server_installation_and_configuration_guide/operator
* operator marketplace id = `keycloak-operator`
* provided CRs: []

NOTE: make sure to look into a correct branch for product operator sources (e.g. `master` branch with project operator sources and CRDs could be a year away from the product one)! Either ask devs or compare the CRD brought by operator installation with ones in the repository.

```
$ oc get crd | grep keycloak
keycloakbackups.keycloak.org                                2020-12-07T11:27:42Z
keycloakclients.keycloak.org                                2020-12-07T11:27:41Z
keycloakrealms.keycloak.org                                 2020-12-07T11:27:41Z
keycloaks.keycloak.org                                      2020-12-07T11:27:42Z
keycloakusers.keycloak.org                                  2020-12-07T11:27:42Z
```

These match the https://github.com/keycloak/keycloak-operator/tree/8ee8a64c66e8bad847ec3190f23a0fcd334a870b/deploy/crds at the moment, sync the model classes and builders with these. Be aware, that OCP will not print any warning in case an unknown element is used within the CRD specs.

### Create Application & Provisioner
* create org.jboss.intersmash.tools.application.openshift.${PRODUCT}OperatorApplication extends OperatorApplication
* create org.jboss.intersmash.tools.provision.openshift.${PRODUCT}OperatorProvisioner extends OperatorProvisioner<${PRODUCT}OperatorApplication>
	* get operator ID from openshift-marketplace (oc get packagemanifest -n openshift-marketplace) and create a constructor matching the one from abstract OperatorProvisioner class
    * use `throw new UnsupportedOperationException("TODO");` to implement the mandatory methods inherited from Provisioner interfaces for now
* add a new test to OperatorSubscriptionTestCase    
* Note: create a middle package between ${PRODUCT}.MIDDLE.{$OPERATOR_ID} packages as some products could have more than one operator (not just Custom resources owned by a single operator, e.g. ActiveMQ, Kafka)

#### Example Outcome
* `openshift.application.org.jboss.intersmash.tools.KeycloakOperatorApplication`
* `openshift.provision.org.jboss.intersmash.tools.KeycloakOperatorProvisioner`
    * operator id = `keycloak-operator`
    * run OperatorSubscriptionTestCase#keycloakOperatorProvisionerSmokeTest to get more details on operator (e.g. list of owned CRDs)
```
[2020-12-01 08:09:13,717] DEBUG- Operator ID: keycloak-operator
[2020-12-01 08:09:13,717] DEBUG- Operator Channel: alpha
[2020-12-01 08:09:13,717] DEBUG- Operator CSV: keycloak-operator.7.4.3
[2020-12-01 08:09:13,718] DEBUG- Provided APIs: [keycloaks.keycloak.org, keycloakrealms.keycloak.org, keycloakbackups.keycloak.org, keycloakclients.keycloak.org, keycloakusers.keycloak.org]
```

### Create model
We need to create a model for every CR `Kind` (see CRD files) provided by operator.

E.g. for https://github.com/keycloak/keycloak-operator/blob/master/deploy/crds/keycloak.org_keycloakusers_crd.yaml create 
* class KeycloakUser extends CustomResource
* create `spec` and `status` packages
* follow the sources https://github.com/keycloak/keycloak-operator/blob/master/pkg/apis/keycloak/v1alpha1/keycloakuser_types.go to implement the rest of the model
    * make use of @Lombok in model classes
    * although it sucks greatly, please add javadoc to fields - copy from sources/crd/docs
* add getter for every custom resource into ${PRODUCT}OperatorApplication - use List<{$CR_KIND}> where multiple custom resource instances are allowed     

#### Example Outcome
org.jboss.intersmash.tools.provision.openshift.operator.keycloak:
* backup - `KeycloakBackup` with model classes
* client - `KeycloakClient` with model classes
* keycloak - `Keycloak` with model classes
* realm - `KeycloakRealm` with model classes
* user - `KeycloakUser` with model classes

```java
public interface KeycloakOperatorApplication extends OperatorApplication {

	Keycloak getKeycloak();

	List<KeycloakBackup> getKeycloakBackups();
	
	List<KeycloakClient> getKeycloakClients();
	
	List<KeycloakRealm> getKeycloakRealms();
	
	List<KeycloakUser> getKeycloakUsers();
}
```

#### Note
###### A way generate model automatically
There could be a way to avoid doing this manually, either to use Swagger to generate model from CRDs, or some other tools to generate it from Go classes. Back in a day, we tried to use Swagger, but didn't get much out of it. This task requires further exploration (DoL could be a good fit for this). However, I'm not sure, whether we will be able to generate 100% of the code (e.g. javadoc).

### Create clients
https://github.com/fabric8io/kubernetes-client/blob/master/kubernetes-examples/src/main/java/io/fabric8/kubernetes/examples/CRDExample.java shows a way how we work with custom resources. Create a ${CR_KIND}List (see the `listKind` field in CRD) and ${CR_KIND}Doneable support classes for all provided CRs.
* class ${CR_KIND}List extends CustomResourceList<{$CR}>
* class ${CR_KIND}Doneable extends CustomResourceDoneable<${CR_KIND}>

Update the ${PRODUCT}OperatorProvisioner. Create CR na name constant and NonNamespaceOperation for every custom resource provided by an operator (look into existing operator Provisioners for more details). Create method to initialize the client and to obtain a reference to custom resource instance running on OPC (this one will have to be parametrize in case there could be more than one resource of same kind managed by a single operator - e.g. ActiveMQ operator can have multiple addresses).   

#### Example Output
KeycloakOperatorProvisioner Keycloak client methods example. The methods and resources names are aligned with CR name and kind.
```java
    // ...
	private static final String KEYCLOAK_RESOURCE = "keycloaks.keycloak.org";
	private static NonNamespaceOperation<Keycloak, KeycloakList, KeycloakDoneable, Resource<Keycloak, KeycloakDoneable>> KEYCLOAKS_CLIENT;
	private static final String KEYCLOAK_USER_RESOURCE = "keycloakusers.keycloak.org";
	private static NonNamespaceOperation<KeycloakUser, KeycloakUserList, KeycloakUserDoneable, Resource<KeycloakUser, KeycloakUserDoneable>> KEYCLOAK_USERS_CLIENT;

	// keycloaks.keycloak.org

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_RESOURCE} custom resource
	 */
	NonNamespaceOperation<Keycloak, KeycloakList, KeycloakDoneable, Resource<Keycloak, KeycloakDoneable>> keycloaksClient() {
		if (KEYCLOAKS_CLIENT == null) {
			CustomResourceDefinition customResourceDefinition = OpenShifts.admin().customResourceDefinitions()
					.withName(KEYCLOAK_RESOURCE).get();
			if (!getCustomResourceDefinitions().contains(KEYCLOAK_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_RESOURCE, OPERATOR_ID));
			}

			MixedOperation<Keycloak, KeycloakList, KeycloakDoneable, Resource<Keycloak, KeycloakDoneable>> keycloaksClient = OpenShifts
					.master()
					.customResources(customResourceDefinition, Keycloak.class, KeycloakList.class, KeycloakDoneable.class);
			KEYCLOAKS_CLIENT = keycloaksClient.inNamespace(OpenShiftConfig.namespace());
		}
		return KEYCLOAKS_CLIENT;
	}

	/**
	 * Get a reference to keycloak object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 */
	public Resource<Keycloak, KeycloakDoneable> keycloak() {
		return keycloaksClient().withName(getApplication().getKeycloak().getMetadata().getName());
	}

	// keycloakusers.keycloak.org

	/**
	 * Get a client capable of working with {@link #KEYCLOAK_USER_RESOURCE} custom resource.
	 *
	 * @return client for operations with {@link #KEYCLOAK_USER_RESOURCE} custom resource
	 */
	NonNamespaceOperation<KeycloakUser, KeycloakUserList, KeycloakUserDoneable, Resource<KeycloakUser, KeycloakUserDoneable>> keycloakUsersClient() {
		if (KEYCLOAK_USERS_CLIENT == null) {
			CustomResourceDefinition customResourceDefinition = OpenShifts.admin().customResourceDefinitions()
					.withName(KEYCLOAK_USER_RESOURCE).get();
			if (!getCustomResourceDefinitions().contains(KEYCLOAK_USER_RESOURCE)) {
				throw new RuntimeException(String.format("[%s] custom resource is not provided by [%s] operator.",
						KEYCLOAK_USER_RESOURCE, OPERATOR_ID));
			}

			MixedOperation<KeycloakUser, KeycloakUserList, KeycloakUserDoneable, Resource<KeycloakUser, KeycloakUserDoneable>> keycloakUsersClient = OpenShifts
					.master()
					.customResources(customResourceDefinition, KeycloakUser.class, KeycloakUserList.class,
							KeycloakUserDoneable.class);
			KEYCLOAK_USERS_CLIENT = keycloakUsersClient.inNamespace(OpenShiftConfig.namespace());
		}
		return KEYCLOAK_USERS_CLIENT;
	}

	/**
	 * Get a reference to keycloakuser object. Use get() to get the actual object, or null in case it does not
	 * exist on tested cluster.
	 * 
	 * @param name name of the keycloakuser custom resource
	 */
	public Resource<KeycloakUser, KeycloakUserDoneable> keycloakUser(String name) {
		return keycloakUsersClient().withName(name);
	}

	/**
	 * Get all keycloakusers maintained by the current operator instance.
	 *
	 * Be aware that this method return just a references to the addresses, they might not actually exist on the cluster.
	 * Use get() to get the actual object, or null in case it does not exist on tested cluster.
	 */
	public List<Resource<KeycloakUser, KeycloakUserDoneable>> keycloakUsers() {
		KeycloakOperatorApplication keycloakOperatorApplication = getApplication();
		return keycloakOperatorApplication.getKeycloakUsers().stream()
				.map(keycloakUser -> keycloakUser.getMetadata().getName())
				.map(this::keycloakUser)
				.collect(Collectors.toList());
	}
```

### Create builders for spec
We need two kinds of builders for spec model classes. The one for the ${CR_KIND}Spec class, which will act as an entry point for users to build the ${CR_KIND} objects, and the rest for the remaining model classes in `spec` package. Do not create a builder classes manually, make use of some plugin (e.g. https://plugins.jetbrains.com/plugin/6585-builder-generator). Although it sucks even more greatly here, please add javadoc to builder methods (copy from fields) - copy from sources/crd/docs.

###### Builder best practices
* create enums where possible
```java
	private String strategy;    	
    
    /**
     * Specify migration strategy.
     */
    public MigrateConfigBuilder strategy(MigrationStrategy strategy) {
    	if (strategy != MigrationStrategy.NO_STRATEGY) {
    		this.strategy = strategy.getValue();
    	}
    	return this;
    }

    public enum MigrationStrategy {
		NO_STRATEGY(""),
		STRATEGY_RECREATE("recreate"),
		STRATEGY_ROLLING("rolling");

		private String value;

		MigrationStrategy(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
```

* create methods also for a single addition of collection type resources
```java
   	private List<String> args;	

    /**
	 *  Arguments to the entrypoint. Translates into Container CMD.
	 */
	public ExperimentalSpecBuilder args(List<String> args) {
		this.args = args;
		return this;
	}

	/**
	 *  Add argument to the entrypoint. Translates into Container CMD.
	 */
	public ExperimentalSpecBuilder args(String arg) {
		if (args == null) {
			args = new ArrayList<>();
		}
		args.add(arg);
		return this;
	}
```

#### Builder for ${CR_KIND}Spec
Create a builder for ${CR_KIND}SpecBuilder to the same package as ${CR_KIND}.
* class name: omit the `Spec` keyword - name it `${CR_KIND}Builder`
* method prefix: do not use any method prefix
* destination package: same as the ${CR_KIND} class (not `spec` package)
* add `private String name;` and `private Map<String, String> labels;` fields for resource metadata
* remove the `private` constructor
    * create constructors for `(String name)` & `(String name, Map<String, String> labels)` parameters
* remove static initializer
 * update the `build()` method
    * return `${CR_KIND}` instead of ${CR_KIND}Spec
    * init ${CR_KIND} metadata
    * set `name` field to metadata
    * set `labels` field to metadata
    * set ${CR_KIND}Spec as ${CR_KIND} spec field

#### Example Output
${CR_KIND}SpecBuilder example
```java
public final class KeycloakUserBuilder {
	private String name;
	private Map<String, String> labels;
	private LabelSelector realmSelector;
	private KeycloakAPIUser user;

	/**
	 * Initialize the {@link KeycloakUserBuilder} with given resource name.
	 *
	 * @param name resource object name
	 */
	public KeycloakUserBuilder(String name) {
		this.name = name;
	}

	/**
	 * Initialize the {@link KeycloakUserBuilder} with given resource name and labels.
	 *
	 * @param name resource object name
	 * @param labels key/value pairs that are attached to objects
	 */
	public KeycloakUserBuilder(String name, Map<String, String> labels) {
		this.name = name;
		this.labels = labels;
	}

	/**
	 * Selector for looking up KeycloakRealm Custom Resources.
	 */
	public KeycloakUserBuilder realmSelector(LabelSelector realmSelector) {
		this.realmSelector = realmSelector;
		return this;
	}

	/**
	 * Keycloak User REST object.
	 */
	public KeycloakUserBuilder user(KeycloakAPIUser user) {
		this.user = user;
		return this;
	}

	public KeycloakUser build() {
		KeycloakUser keycloakUser = new KeycloakUser();
		keycloakUser.setMetadata(new ObjectMeta());
		keycloakUser.getMetadata().setName(name);
		keycloakUser.getMetadata().setLabels(labels);

		KeycloakUserSpec keycloakUserSpec = new KeycloakUserSpec();
		keycloakUserSpec.setRealmSelector(realmSelector);
		keycloakUserSpec.setUser(user);
		keycloakUser.setSpec(keycloakUserSpec);
		return keycloakUser;
	}
}
```

#### The rest of the model
* method prefix: do not use any method prefix, keep defaults for class name and destination package (`spec`)
* remove the `private` constructor (keep the default from Object)
* remove static initializer

Feel free to update builders with enums if required for some fields. 

#### Example Output
${NON_CR_KIND}SpecBuilder example
```java
public final class ConfigMapVolumeSpecBuilder {
	private String name;
	private String mountPath;
	private List<KeyToPath> items;

	/**
	 * ConfigMap name.
	 */
	public ConfigMapVolumeSpecBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * An absolute path where to mount it.
	 */
	public ConfigMapVolumeSpecBuilder mountPath(String mountPath) {
		this.mountPath = mountPath;
		return this;
	}

	/**
	 * ConfigMap mount details.
	 */
	public ConfigMapVolumeSpecBuilder items(List<KeyToPath> items) {
		this.items = items;
		return this;
	}

	/**
	 * ConfigMap mount details.
	 */
	public ConfigMapVolumeSpecBuilder items(KeyToPath item) {
		if (items == null) {
			items = new ArrayList<>();
		}
		items.add(item);
		return this;
	}

	public ConfigMapVolumeSpec build() {
		ConfigMapVolumeSpec configMapVolumeSpec = new ConfigMapVolumeSpec();
		configMapVolumeSpec.setName(name);
		configMapVolumeSpec.setMountPath(mountPath);
		configMapVolumeSpec.setItems(items);
		return configMapVolumeSpec;
	}
}
```

### Create model tests (verify client methods)
Create a new ${PRODUCT}OperatorProvisionerTest to verify the model and client methods. The goal of these tests is to verify that we're able to work with custom resources provided by operator, not to verify the actual operator functionality.

Use the product documentation and operator examples (could be found in `deploy/examples` in some operators source repositories) for CRD snippets.

Make sure to update the `OperatorCondition#operatorTests` with a new operator test class, so the test will not run on OCP3 clusters (no OLM support).

#### Example Outcome
As said before, the goal of these tests is to verify the basic functionality of client methods, not to verify the actual CRD setup works as expected (that should be verified by product QE tests)
```java
	/**
	 * https://github.com/keycloak/keycloak-operator/blob/master/deploy/examples/user/basic_user.yaml
	 */
	@Test
	public void basicUser() {
		name = "example-realm-user";
		KeycloakUser keycloakUser = new KeycloakUserBuilder(name, matchLabels)
				.user(new KeycloakAPIUserBuilder()
						.username("realm_user")
						.firstName("John")
						.lastName("Doe")
						.email("user@example.com")
						.enabled(true)
						.emailVerified(false)
						.build())
				.realmSelector(new LabelSelectorBuilder().withMatchLabels(matchLabels).build())
				.build();

		verifyUser(keycloakUser);
	}


	private void verifyUser(KeycloakUser keycloakUser) {
		// create and verify that object exists
		keycloakOperatorProvisioner.keycloakUsersClient().createOrReplace(keycloakUser);
		new SimpleWaiter(() -> keycloakOperatorProvisioner.keycloakUsersClient().list().getItems().size() == 1).waitFor();
		Assertions.assertEquals(keycloakUser.getSpec(), keycloakOperatorProvisioner.keycloakUser(name).get().getSpec());

		// delete and verify that object was removed
		keycloakOperatorProvisioner.keycloakUsersClient().withName(name).withPropagationPolicy(DeletionPropagation.FOREGROUND)
				.delete();
		new SimpleWaiter(() -> keycloakOperatorProvisioner.keycloakUsersClient().list().getItems().size() == 0).waitFor();
	}
```

### Implement methods from OpenShiftProvisioner interface
Implement the missing methods from `OpenShiftProvisioner` interface (e.g. deploy, undeploy, scale, etc.). The method's implementation will vary based on the nature of the operator and custom resources the operator is providing. See the existing operator implementations for some inspiration.

In case that operator provides some general route to the service it provides, override the `getURL()` to return URL to such a route.

Throw `UnsupportedOperationException` for cases where implementation is not possible (e.g. `scale()` the provisioner without pods).

### Register a new provisioner
Create a new `class ${PRODUCT}OperatorProvisionerFactory implements ProvisionerFactory<${PRODUCT}OperatorProvisioner>` for the provisioner which would give Intersmash information about what kind of application is our new provisioner able to serve. Register a new provisioner factory for SPI in `provision.org.jboss.intersmash.tools.ProvisionerFactory` file located within `META-INF/services` directory of `intersmash-tools-provisioners` module, so it can be collected by a `ProvisionerManager` on next run.
As a part of service factory registration, update also the `ProvisionerManagerTestCase` with a new @Test method.

See the existing ProvisionerFactory implementations for more details.

Once the provisioner is registered, please add a new entry to `Mapping of implemented provisioners` section of Intersmash README file.

With a new provisioner ready to serve, add a new provisioner demonstration into `intersmash-demos-tests` module.
