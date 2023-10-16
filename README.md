# Intersmash - Application Services Interoperability Testing

The goal of the Intersmash project is to support the provisioning and execution of cross-product test scenarios which 
simulate complex use-cases where multiple Runtime products are involved.

---
[//]: # ( https://ecotrust-canada.github.io/markdown-toc/ )
- [Intersmash - Application Services Interoperability Testing](#intersmash---application-services-interoperability-testing)
  * [Platforms](#platforms)
    + [Something more about OpenShift](#something-more-about-openshift)
  * [Architecture](#architecture)
    + [Intersmash and Java](#intersmash-and-java)
    + [Intersmash key concepts and components](#intersmash-key-concepts-and-components)
    + [Intersmash modules](#intersmash-modules)
    + [Main libraries and frameworks used](#main-libraries-and-frameworks-used)
      - [JUnit/Jupiter](#junit-jupiter)
      - [XTF](#xtf)
    + [Provisioning](#provisioning)
      - [OpenShift provisioning](#openshift-provisioning)
        - [Supported product image versions](#supported-product-image-versions)
      - [Operator-based provisioning](#operator-based-provisioning)
      - [Automatic provisioning](#automatic-provisioning)
      - [Helm Charts based provisioning](#helm-charts-based-provisioning)
  * [Sources and Javadoc artifacts](#sources-and-javadoc-artifacts)
  * [Getting started](#getting-started)
    + [Configuration properties](#configuration-properties)
  * [Pod logs Streaming](#pod-logs-streaming)
  * [Intersmash Deployments](#intersmash-deployments)
    + [intersmash-deployments-provider](#intersmash-deployments-provider)
    + [intersmash-deployments-shared](#intersmash-deployments-shared)
      * [wildfly-bootable-jar](#wildfly-bootable-jar)
      * [wildfly-helloworld](#wildfly-helloworld)
  * [Publishing Intersmash Framework artifacts](#publishing-intersmash-framework-artifacts)
  * [Contributing](#contributing)
  * [CI](#ci)
  * [Future plans](#future-plans)
  * [Issue tracking](#issue-tracking)

## Platforms

Although Intersmash is designed to allow executions on different platforms, at the moment, we fully focus on OpenShift
support, support for other platforms (Kubernetes, Bare-metal, kubernetes etc.) could be added later on demand.

### Something more about OpenShift

[OpenShift](https://www.redhat.com/en/technologies/cloud-computing/openshift) is an enterprise Kubernetes platform and
as such, it that enables a cloud-like experience everywhere it's deployed.

Being built on top of Kubernetes itself, OpenShift provides an improved end user experience by enriching the set of
APIs, resources and processes that Kubernetes encapsulates.

For the above-mentioned reasons, OpenShift is widely used by teams around the world and has become more and more popular
recently.
This is actually the main point for Intersmash provisioning implementation being based on such platform, at the moment.

Intersmash interaction with OpenShift is basically found in _provisioners_' implementations, where APIs and resources
are used to deliver clustered services according to use case requirements.
Images and image streams, builds, deployments, operators' subscriptions and management, are the most common elements of
such implementations, and mimic specific product processes which are usually executed by automating `oc`  - i.e. the
OpenShift CLI - commands.

The following one is a list of some `oc` commands that Intersmash abstractions got inspiration from:
* `oc apply`
* `oc new-app`
* `oc get csv`
* `oc get packagemanifests`
* ...

In order to use and contribute to Intersmash, it is useful to understand OpenShift basics and to experiment with
provisioning clustered workflows by using different techniques, as direct image, templates or operators.

## Architecture

This is the `Intersmash Library` project, and it hosts several modules which are meant to provide the testing engine
workflow APIs (`intersmash-tools-core`), tooling implementation (`intersmash-tools-provisioners`), shared deployments
(`intersmash-deployments`), a testsuite (`intersmash-testsuite`) and examples (`intersmash-demos`).

### Intersmash and Java

* Intersmash Framework tooling modules support Java 11 language features, and the compiled code is set to be compliant with 
such version as well.

### Intersmash key concepts and components

Intersmash has been designed to provide components that interact in the context of a testing workflow in order to:

1. provision a products' stack environment, which can be called the _scenario_
2. execute tests in order to validate the behavior of the _scenario_

There are three main components on which such a design relies on:

* _annotations_: to describe the scenario, and the services it's made of directly where it's needed, i.e. on the test 
  class declaration
  
* _applications_: to describe a given service that belongs to a scenario, in terms of configuration and specific 
  properties.  

* _provisioners_: to take care of and control the specific workflow that needs to be executed for the given products
  that a scenario consists of.

When contributing to or using Intersmash, two main roles can be mapped to the potential areas that the above-mentioned 
components target:  

* _developer_ - when operating in this role, a user will potentially contribute to the Intersmash Library project (e.g.
  by impementing a new provisioner, or fixing an existing one) or when creating a new test scenario, or fixing one.  
  
* _tester_ - when operating in this role a user will execute actual tests and review the results, ideally reporting 
* product bugs. 

You'll find more details and examples on how these components can be used and are expected to work, in the following 
sections.
  
### Intersmash modules

Every module should contain a separate set of unit tests to verify the basic provided functionality. 

* **Deployments (deployments)** - sources for shared deployments which can be used by the testsuite or by 
* the examples. 
  Moreover, the `intersmash-deployments-provider` submodule provides support to get path to compiled deployments. Or 
you can get any deployment by GAV.
* **Tools (intersmash-tools)** - set of support tooling for platform configuration and application deployment, it is 
  sometimes referred to as the "tooling layer"
  * **intersmash-tools-core** - core interfaces, annotations and extensions.
  * **intersmash-tools-provisioners** - implementations of interfaces from `core` for selected 
    Red Hat products.
* **Testsuite** - tests that verify the integration with OpenShift, hence a cluster is needed.  

The following is an example of a test:

```java
@Intersmash({
        // 1
        @Service(OpenShiftInfinispanApp.class),
        @Service(OpenShiftWildflyApp.class)
    }
)
public class SampleTest {
    @ServiceUrl(OpenShiftWildflyApp.class) // 2
    private String wildflyRouteUrl;

    @ServiceProvisioner(OpenShiftWildflyApp.class) // 3
    private OpenShiftProvisioner wildflyOpenShiftProvisioner;

    @Test
    public void test() {
        // Do your thing.
    }
}
```
* **1:** The applications/products used in the test are listed as items in `@Intersmash` annotation.
Each of the mentioned classes will provide the platform-specific configuration for the given application/product.
You may deploy application on some server (Wildfly), setup services that don't require a deployment, or just deploy a 
database.
* **2:** To inject information about the application into the test, the tooling layer provides some annotations.
The application class needs to match the one which is configured in the classes in point *#1*.
`@ServiceUrl` provides support to inject into String or URL type field.
Beware, some applications (e.g. DB running on OpenShift) might not provide `@ServiceUrl` as there is no route to them 
  created.
* **3:** Should you need control over platform-specific actions, you can also inject a provisioner.
This will enable you to, for example, scale your deployment up and down.

### Main libraries and frameworks used

#### JUnit/Jupiter

[JUnit 5](https://junit.org/junit5/docs/current/user-guide/#overview-what-is-junit-5) 
is the next generation of JUnit. It provides a new test engine implementation for Java, a programming and extension 
model and a consistent set of annotations and APIs to support the developer experience.

It also provides support for legacy tests which are leveraging previous JUnit versions, as Junit 4.

Intersmash uses JUnit5 to implement its own test execution workflow, specifically by implementing 
[Jupiter test lifecycle callbacks](https://junit.org/junit5/docs/current/user-guide/#extensions-lifecycle-callbacks) 
through a main entry point extension, namely 
[IntersmashExtension](./intersmash-tools/intersmash-tools-core/src/main/java/org/jboss/intersmash/tools/annotations/Intersmash.java), 
which can be used to annotate test classes which should be run by Intersmash.
By hooking to the workflow execution, Intersmash can derive the test scenario from annotated classes and orchestrate the 
provisioning of services through _provisioners_.

Intersmash also uses more components from the JUnit 5 framework, as for instance by implementing 
[ExecutionCondition](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/ExecutionCondition.html) 
in order to [selectively enable tests execution](https://junit.org/junit5/docs/current/user-guide/#writing-tests-conditional-execution).  

#### XTF

[XTF](https://github.com/xtf-cz/xtf) is a framework designed to ease up aspects of testing in the OpenShift environment.

Intersmash leverages the library to simplify operations and communication with an OpenShift cluster.
It was chosen because it is used by several projects and is maintained and supported by the community.

The most popular connection between Intersmash and XTF is the `cz.xtf.core.openshift.OpenShifts` class which is used to
obtain clients with administrative and user permissions and perform operations that consume the OpenShift API, such as 
the CRUD ones, to manipulate image streams, custom resources etc.

Besides, Intersmash relies on many other XTF components, such as `ApplicationBuilder` and the `XTFConfig` API, which 
are used by many provisioner implementations. 

### Provisioning 

#### OpenShift provisioning

Every application defined by `@Service` could be served by a different Provisioner depending on which Application 
interface it implements (see the table of mapping located below). Provisioner implementation provides the requested 
functionality while taking into account the selected platform, product, type of source, etc.

Additional set of Applications supported with relevant Provisioner classes will be implemented on demand once requested.

Example implementation of Application:

```java
public class PostgresqlApplication implements PostgreSQLTemplateOpenShiftApplication {
	static String NAME = "postgresql";

	@Override
	public PostgreSQLTemplate getTemplate() {
		return PostgreSQLTemplate.POSTGRESQL_EPHEMERAL;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
```

Example test scenario which deploys the `PostgresqlApplication` (more applications would be part of `@Intersmash` in real 
integration use case)

```java
@Intersmash(@Service(PostgresqlApplication.class))
@Slf4j
public class PostgresqlProvisionTest {
	@ApplicationController(PostgresqlApplication.class)
	OpenShiftProvisioner provisioner;

	@Test
	public void printPostgresqlPods() {
		provisioner.getPods().forEach(pod -> {
			log.info(pod.getMetadata().getName());
			log.info("READY: " + pod.getStatus().getContainerStatuses().get(0).getReady());
		});
	}
}
```

Mapping of implemented provisioners:

| Product    | Application                            | Provisioner                            | Notes                                                                                                                                                                                                                                                                                                       |
|:-----------|:---------------------------------------|:---------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ActiveMQ   | ActiveMQOperatorApplication*           | ActiveMQOperatorProvisioner            |                                                                                                                                                                                                                                                                                                             |
| Kafka      | KafkaOperatorApplication               | KafkaOperatorProvisioner               |                                                                                                                                                                                                                                                                                                             |
| Wildfly    | WildflyImageOpenShiftApplication       | WildflyImageOpenShiftProvisioner       | Available both for Git sources and binary based s2i v2 build (either a pre-built deployment or a filesystem resource like a Maven project folder)                                                                                                                                                           |
| Wildfly    | WildflyHelmChartOpenShiftApplication   | WildflyHelmChartOpenShiftProvisioner   | The `wildfly-2.3.2` tag of https://github.com/wildfly/wildfly-charts is used and the model is generated based on the https://raw.githubusercontent.com/wildfly/wildfly-charts/main/charts/wildfly/values.schema.json  value schema file                                                                     |                                                                                                                                                   |
| Infinispan | InfinispanOperatorApplication          | InfinispanOperatorProvisioner          |                                                                                                                                                                                                                                                                                                             |
| Keycloak   | KeycloakOperatorApplication            | KeycloakOperatorProvisioner            |                                                                                                                                                                                                                                                                                                             |
| Keycloak   | KeycloakRealmImportOperatorApplication | KeycloakRealmImportOperatorProvisioner | The latest Quarkus based Keycloak Operator, doesn't provide stable CRDs yet (see https://www.keycloak.org/2022/09/operator-crs); this operator offers a temporary solution which supports the `Keycloak` and `KeycloakRealmImport` Custom Resources: these are the only supported CR at the time of writing |

Additional services provisioners:

| Service    | Application                            | Provisioner                            |
|:-----------|:---------------------------------------|:---------------------------------------|
| MySQL      | MysqlImageOpenShiftApplication         | MysqlImageOpenShiftProvisioner         |
| PostgreSQL | PostgreSQLImageOpenShiftApplication    | PostgreSQLImageOpenShiftProvisioner    |
| PostgreSQL | PostgreSQLTemplateOpenShiftApplication | PostgreSQLTemplateOpenShiftProvisioner |

The only thing users have to take care of is to implement the correct `Application` (see the table above) interface and 
pass the class via `@Service` annotation, it's a task for the framework to choose a suitable Provisioner implementation 
(an exception will be thrown in case no suitable Provisioner was yet implemented). 
See [ProvisionerManager](intersmash-tools/intersmash-tools-core/src/main/java/org/jboss/intersmash/tools/provision/ProvisionerManager.java) and [ProvisionerFactory](intersmash-tools/intersmash-tools-core/src/main/java/org/jboss/intersmash/tools/provision/ProvisionerFactory.java) for implementation details regarding the Provisioner selection.

#### Operator-based provisioning

The Intersmash Library provides support for operator-based provisioning, i.e. it automates the behavior needed to deploy 
a given service on cloud environments via APIs that leverage the 
[Operator Framework](https://github.com/operator-framework). 

Intersmash makes this feature available for currently supported products (see the table below), but that can be 
extended easily, since Intersmash _provisioners_ are pluggable components.

| Product                   | Supported Operator version | Channel name | Supported product version | Repository                                                | Notes                                                                                                                                                                              |
|:--------------------------|:---------------------------|:-------------|:--------------------------|:----------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Hyperfoil                 | 0.24.2                     | alpha        | 0.24.2                    | https://github.com/Hyperfoil/hyperfoil-operator           | We force the CRs version for the used Hyperfoil runtime to be 0.24.2, see https://github.com/Hyperfoil/hyperfoil-operator/issues/18                                                |
| Infinispan                | 2.3.4                      | 2.3.x        | 14.0.17.Final             | https://github.com/infinispan/infinispan-operator         | As available on the OpenShift OperatorHub (community-operators)                                                                                                                    |
| Red Hat DataGrid          | 8.4.8                      | 8.4.x        | 8.4.4.GA                  | https://github.com/infinispan/infinispan-operator         | As available on the OpenShift OperatorHub                                                                                                                                          | 
| WildFly                   | 0.5.6                      | alpha        | 29.0.1.Final              | https://github.com/wildfly/wildfly-operator               | As available on https://operatorhub.io/operator/wildfly                                                                                                                            |
| Red Hat JBoss EAP         | 2.4.2                      | stable       | EAP 8.0 Beta, EAP 7.4.z   | https://github.com/wildfly/wildfly-operator               | As available on the OpenShift OperatorHub                                                                                                                                          |
| Kafka provided by Strimzi | 0.37.0-SNAPSHOT            | stable       | 3.5.1                     | https://github.com/strimzi/strimzi-kafka-operator         | A snapshot version of Strimzi API is used in order to be compatible with Fabric8 Kubernetes client artifacts                                                                       |
| Red HatAMQ Streams        | 2.5.0-0                    | stable       | 3.5.0                     | https://github.com/strimzi/strimzi-kafka-operator         | As available on the OpenShift OperatorHub. A snapshot version of Strimzi API is used in order to be compatible with Fabric8 Kubernetes client artifacts                            |
| ActiveMQ Artemis          | 1.0.11                     | upstream     | 1.0.15                    | https://github.com/artemiscloud/activemq-artemis-operator | We are using a custom index image, i.e. quay.io/jbossqe-eap/intersmash-activemq-operator-catalog:v1.0.11, built as described in https://github.com/Intersmash/intersmash/issues/32 |
| Red HatAMQ Broker         | 7.11.2-opr-1               | 7.11.x       | 7.11.z                    | https://github.com/rh-messaging/activemq-artemis-operator | As available on the OpenShift OperatorHub                                                                                                                                          |  
| Keycloak                  | 22.0.4                     | upstream     | 22.0.4                    | https://github.com/keycloak/keycloak/tree/main/operator   | Latest Keycloak, based on Quarkus. Supports a limited number of CR (Keycloak and KeycloakRealmImport): more to come in upcoming versions                                           |

Intersmash operator-based provisioners implement a common contract and high level behavior which is defined by the 
[OperatorProvisioner](./intersmash-tools/intersmash-tools-core/src/main/java/org/jboss/intersmash/tools/provision/openshift/operator/OperatorProvisioner.java)
class.

You can also learn more by going through the Intersmash 
[Operator provisioning How-to](intersmash-tools/intersmash-tools-provisioners/docs/operator-howto.md), 
which describes the steps needed to create a new operator-based provisioner.

#### Automatic provisioning
Automatic (or application dictated) provisioning is a way to delegate the whole provisioning process to the application
descriptor, rather than depending on a provisioner.
Having a concrete instance of an application class that implements `AutoProvisioningOpenShiftApplication` will let 
you add methods that map to the provisioning process workflow, e.g.: `deploy`, `scale`, `undeploy`:

```java
public class AutoProvisioningHelloOpenShiftApplication implements AutoProvisioningOpenShiftApplication {

  private static final String HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION = "target/test-classes/org/jboss/intersmash/tools/provision/openshift/auto/hello-openshift-deployment.yaml";
  private static final String HELLO_OPENSHIFT_SERVICE_DEFINITION = "target/test-classes/org/jboss/intersmash/tools/provision/openshift/auto/hello-openshift-service.yaml";
  private static final String HELLO_OPENSHIFT_ROUTE_DEFINITION = "target/test-classes/org/jboss/intersmash/tools/provision/openshift/auto/hello-openshift-route.yaml";
  private static final String HELLO_OPENSHIFT_APP_NAME = "hello-openshift";
  private static final String HELLO_OPENSHIFT_APP_REPLICAS_PLACEHOLDER = "autoprovisioning-hello-openshift-app-replicas-changeme";

  @Override
  public void preDeploy() {
    // on preDeploy, we've got nothing to do... (yet, maybe we could create the secret for securing the route)
    /// TODO
  }

  @Override
  public void deploy() throws AutoProvisioningException {
    final int replicas = 1;
    // set initial replicas
    try {
      adjustConfiguration(HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION,
              String.format("replicas: %s", HELLO_OPENSHIFT_APP_REPLICAS_PLACEHOLDER),
              String.format("replicas: %d", replicas));
    } catch (IOException e) {
      throw new AutoProvisioningException(e);
    }
    try {
      // on deploy, we'll provision the app pod as per the JSON definition in HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION
      OpenShifts.masterBinary().execute(
              "apply", "-f", HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION);
    } finally {
      try {
        adjustConfiguration(HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION,
                String.format("replicas: %d", replicas),
                String.format("replicas: %s", HELLO_OPENSHIFT_APP_REPLICAS_PLACEHOLDER));
      } catch (IOException e) {
        throw new AutoProvisioningException(e);
      }
    }
    // let's wait for the exact number of pods
    OpenShiftWaiters.get(OpenShifts.master(), () -> false).areExactlyNPodsReady(
            1, "app", getName()).waitFor();
    // then create a service for the hello-openshift app
    OpenShifts.masterBinary().execute(
            "apply", "-f", HELLO_OPENSHIFT_SERVICE_DEFINITION);
    // and let's expose the service through a route so that it is available externally
    OpenShifts.masterBinary().execute(
            "apply", "-f", HELLO_OPENSHIFT_ROUTE_DEFINITION);
  }
  // ...
}
```

These classes can implement their own provisioning process by using the above mentioned annotations in order to 
accomplish a declarative configuration, instead that requiring a specific provisioner that would instead leverage 
a client API.

Application instances implementing the `AutoProvisioningOpenShiftApplication` interface can be used the same ways as the 
other `Application` types, i.e. in a testing scenario defined by a test class and its `@Intersmash` annotations.

For an example of automatic provisioning process, just run the following commands: 
```text
$ mvn clean package -DskipTests
...
$ mvn test -pl intersmash-tools/intersmash-tools-core -Dtest=AutoProvisioningTests
```

#### Helm Charts based provisioning

Helm Charts based provisioning relies on Helm Charts values files, by defining the `HelmChartRelease` interface. 
This means that the selected provisioner expects for the application descriptor to be able of serializing the 
release data onto a file, which eventually is used to install or upgrade a given application service release.

The `HelmChartProvisioner` class holds the behavioral logic described above, while concrete implementations 
provide extension logic to fit various service types' requirements, e.g. `WildflyHelmChartProvisioner` provides 
logic to create Wildfly image streams during the pre-deploy phase, or to handle cleanup during the post-deployment one.

Application descriptors for test scenarios that involve Helm Based provisioning must expose a concrete 
implementation of the `HelmChartRelease` interface, for the provisioner to use it during the application service 
life cycle.

What above is all that is needed - together with the related Intersmash concepts - to implement the components that compose 
an Helm Charts provisioned test scenario, similarly to what happens with other provisioners, see the classes and APIs in
the `org.jboss.intersmash.tools.application.openshift.helm` and `org.jboss.intersmash.tools.provision.helm` packages.

The process of implementing a concrete `HelmChartRelease` that serializes to a valid Helm Charts values file can be 
simplified by leveraging POJOs that can be generated by external tools and wrap them into an adapter that implements 
the `HelmChartRelease` interface. The adapter is responsible for exposing and manipulating the internal release 
POJOs.

This avoids the need for coding a dedicated `HelmChartRelease` implementation and the related serialization logic, but it
also allows for loading data (through deserializaton) easily from static files.
Of course the release data can be manipulated through the generated Java model APIs.

##### Properties
Intersmash Helm Charts based provisioning relies on some configuration properties in order to execute, see the `intersmash.wildfly*.helm.*`
set of properties mentioned in the Configuration properties section.


## Sources and Javadoc artifacts

The Intersmash tooling modules are configured to generate both sources and Javadoc JAR artifacts each time they're 
built.

The `doc` profiles must be activated in order to produce the sources and Javadoc artifacts, e.g.:

```shell
mvn clean install -DskipTests -Pdoc
```

a new directory, namely `apidocs`, will be generated inside the `target` directory of both `intersmash-tools-core` and 
`intersmash-tools-provisioners`, and HTML files containing the Javadoc will be added to it.

Besides, two additional artifacts will be generated alongside with the usual ones, i.e. the _sources_ and _Javadoc_ 
artifacts. For instance, for `intersmash-tools-provisioner` we'd have the following artifacts generated:

* intersmash-tools-provisioners-_&lt;version&gt;_.jar - Actual provisioners implementation
* intersmash-tools-provisioners-_&lt;version&gt;_-sources.jar - Unzip to get the sources or use in your IDE
* intersmash-tools-provisioners-_&lt;version&gt;_-javadoc.jar - unzip to get Javadoc


## Getting started
Intersmash is using [XTF](https://github.com/xtf-cz/xtf) to communicate with OpenShift cluster for the OpenShift cloud 
environment.
The [XTFConfig](https://github.com/xtf-cz/xtf/blob/master/core/src/main/java/cz/xtf/core/config/XTFConfig.java) class 
is also used internally, properties are resolved as following: System Properties > Environment Properties > 
test.properties file > global-test.properties, see https://github.com/xtf-cz/xtf#configuration for more details on this.

### Configuration properties
The following properties could be used to configure Intersmash:

| Property                                         | Description                                                                         |
|--------------------------------------------------|-------------------------------------------------------------------------------------|
| intersmash.skip.deploy                           | Skip the deployment phase, tests will be run against a prepared environment *       |
| intersmash.skip.undeploy                         | Do not cleanup environment after test (development use)                             |
| intersmash.deployments.repository.ref            | Manually set git repository branch of intersmash-deployments                        |
| intersmash.deployments.repository.url            | Manually set git repository url of intersmash-deployments                           |
| intersmash.openshift.script.debug                | Add parameter SCRIPT_DEBUG=true to DeploymentConfig/Pod                             |
| intersmash.operators.source                      | Sets CatalogSource that should be used to deploy Operators                          |
| intersmash.wildfly.image                         | Wildfly Builder image URL                                                           |
| intersmash.wildfly.runtime.image                 | Wildfly Runtime image URL                                                           |
| intersmash.wildfly.helm.charts.repo              | Wildfly Helm Charts repository URL                                                  |
| intersmash.wildfly.helm.charts.branch            | Wildfly Helm Charts repository branch                                               |	
| intersmash.wildfly.helm.charts.name              | Wildfly Helm Charts repository namespaces                                           |
| intersmash.wildfly.operators.catalog_source      | Wildfly custom catalog for Operator (must be in namespace openshift-marketplace)    |
| intersmash.wildfly.operators.index_image         | Wildfly custom index image for Operator                                             |
| intersmash.wildfly.operators.package_manifest    | Wildfly custom package manifest for Operator                                        |
| intersmash.wildfly.operators.channel             | Wildfly desired channel for Operator                                                |
| intersmash.infinispan.image                      | Infinispan image URL                                                                |
| intersmash.infinispan.templates                  | URL where Infinispan OpenShift templates resides                                    |
| intersmash.infinispan.operators.catalog_source   | Infinispan custom catalog for Operator (must be in namespace openshift-marketplace) |
| intersmash.infinispan.operators.index_image      | Infinispan custom index image for Operator                                          |
| intersmash.infinispan.operators.package_manifest | Infinispan custom package manifest for Operator                                     |
| intersmash.infinispan.operators.channel          | Infinispan desired channel for Operator                                             |
| intersmash.keycloak.image                        | Keycloak image URL                                                                  |
| intersmash.keycloak.templates                    | URL where Keycloak OpenShift templates resides                                      |
| intersmash.keycloak.operators.catalog_source     | Keycloak custom catalog for Operator (must be in namespace openshift-marketplace)   |
| intersmash.keycloak.operators.index_image        | Keycloak custom index image for Operator                                            |
| intersmash.keycloak.operators.package_manifest   | Keycloak custom package manifest for Operator                                       |
| intersmash.keycloak.operators.channel            | Keycloak desired channel for Operator                                               |
| intersmash.kafka.operators.catalog_source        | Kafka custom catalog for Operator (must be in namespace openshift-marketplace)      |
| intersmash.kafka.operators.index_image           | Kafka custom index image for Operator                                               |
| intersmash.kafka.operators.package_manifest      | Kafka custom package manifest for Operator                                          |
| intersmash.kafka.operators.channel               | Kafka desired channel for Operator                                                  |
| intersmash.activemq.image                        | ActiveMQ Broker image                                                               |
| intersmash.activemq.init.image                   | ActiveMQ Broker init image                                                          |
| intersmash.activemq.operators.catalog_source     | ActiveMQ custom catalog for Operator (must be in namespace openshift-marketplace)   |
| intersmash.activemq.operators.index_image        | ActiveMQ custom index image for Operators                                           |
| intersmash.activemq.operators.package_manifest   | ActiveMQ custom package manifest for Operators                                      |
| intersmash.activemq.operators.channel            | ActiveMQ desired channel for Operator                                               |
| intersmash.hyperfoil.operators.catalog_source    | HyperFoil custom catalog for Operator (must be in namespace openshift-marketplace)  |
| intersmash.hyperfoil.operators.index_image       | HyperFoil custom index image for Operators                                          |
| intersmash.hyperfoil.operators.package_manifest  | HyperFoil custom package manifest for Operators                                     |
| intersmash.hyperfoil.operators.channel           | HyperFoil desired channel for Operator                                              |
| wildfly.feature-pack.location                    | Wildfly feature pack used by shared configurable deployments                        |
| wildfly.ee-feature-pack.location                 | Wildfly EE feature pack used by shared configurable deployments                     |
| wildfly.cloud-feature-pack.location              | Wildfly cloud feature pack used by shared configurable deployments                  |

 [*] - When `intersmash.skip.deploy` is set, please take into account that the prepared environment should be 
 configured accordingly to the `Application` descriptors which are defined by the test class `@Service` annotations.
 E.g.: if a test class defines an `WildflyOperatorApplication` as one of its services, and such application sets the `name`
 to be "wildfly-operator-app", then a Wildfly operator application with such a name should exist in the prepared environment.
 This feature is useful to save debugging time during development, where you can deploy a complex scenario and then 
 enable the property to just execute tests in the following runs.

## Pod Logs Streaming
This feature allows you can stream services output while the test is running; this way you can see immediately what is 
happening.
This is of great help when debugging provisioning, specifically on Cloud environments, which installed would require for 
you to access your Pods. See https://github.com/xtf-cz/xtf#service-logs-streaming-sls

## Intersmash Deployments
The module contains examples of deployments (all related to WildFly at the moment) and demonstrates some features of 
`intersmash-deployments` layer.

### intersmash-deployments-provider
Deployments provider can be used to get a reference for a path to the deployment archive located in local maven repository. 
See e.g. the `wildfly-bootablejar` demo test for more details.
The module also includes some Maven utilities which methods can be used by external deployment providers.

### intersmash-deployments-shared
Shared Intersmash deployments. These are used both by tooling tests (e.g.: the ones in the `intersmash-tools-provisioners` 
module) and example tests, i.e. those living in the `intersmash-demos` module.

#### wildfly-bootable-jar
The example shows how to use the `bootable-jar-baremetal` & `bootable-jar-openshift` Maven control files and related 
profiles to get more variants of a deployment archive from a single deployment project. 
Archive path reference obtained by a deployment provisioner (see `intersmash-demos-deployments-provider`) can be 
uploaded by a binary build mechanism into a tested image to create a new application service.

#### wildfly-helloworld
Inspired by a Wildfly Hello World Quickstart for OpenShift Container.

### openshift-jakartya-sample-standalone
Simple OpenShift Jakarta example application that doesn't inherit Intersmash parent POMs configuration.

## Publishing Intersmash Library artifacts

The Intersmash Library project is configured to publish some artifacts on public Maven repository, so that external 
projects can depend on them, e.g.: the `intersmash-tools-core.jar` artifact which contains the `@Intersmash` annotation 
definition.

Just run:

```shell
mvn clean deploy -DskipTests 
```

## Contributing

See [Intersmash contributing guidelines](CONTRIBUTING.md).

## Future plans

- Verify community bits (images, Helm Charts etc.)
- CI
- Kubernetes support

## Issue tracking

See [Intersmash contributing guidelines](CONTRIBUTING.md).
