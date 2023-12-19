# Intersmash - Run your cloud-native tests with Java!

Welcome to Intersmash, a Java library that makes it easy to provision and execute (complex) test scenarios on 
OpenShift.

If you work as an enterprise application developer, runtime components engineer, quality engineer or in DevOps, 
Intersmash will help you prototype and test complex interoperability scenarios on cloud-native environments 
and platforms, most notably on OpenShift (and on Kubernetes as well, soon :) ).

Through a convenient set of annotations and APIs, Intersmash provides the developer with tools to describe and deploy 
services and to manage their life-cycle orchestration directly from the test class.


## Building blocks

Intersmash has been designed to provide components that interact in the context of a testing workflow in order to:

1. provision a products' stack environment, i.e. the _scenario_
2. execute tests in order to validate the behavior of the _scenario_

There are three main components on which this design is based:

* _annotations_: describes the scenario and the services it's made of.  These statements are declared in the test class.

* _applications_: interface implementations that describe the configuration and specific properties of a given service that belongs to a scenario.

* _provisioners_: takes care of and controls the specific workflow that needs to be executed for the given products that a scenario consists of. See [the Intersmash provisioning documentation](./tools/intersmash-tools-provisioners/README.md) to learn about supported  services provisioners.

## Usage Example

Two intersmash archives provide the three components described above.  The dependencies are added to the project configuration, e.g.:

```xml
<dependencies>
  <dependency>
    <groupId>org.jboss.intersmash</groupId>
    <artifactId>intersmash-tools-core</artifactId>
  </dependency>
  <dependency>
      <groupId>org.jboss.intersmash</groupId>
      <artifactId>intersmash-tools-provisioners</artifactId>
  </dependency>
</dependencies>
```

The following is an example of a test:

```java
@Intersmash({
        // 1
        @Service(PostgresqlApplication.class),
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


* **1:** The applications/products used in the test are listed as `@Service` items within the `@Intersmash` annotation.
  Each of the mentioned classes provides the platform-specific configuration for the given application/product.
  You may deploy the application on some server (Wildfly), setup services that don't require a deployment, or just deploy a
  database.
* **2:** To inject information about the application into the test, the tooling layer provides several annotations.
  The application class must match the class declared in the @Service annotation in point *#1*.
  `@ServiceUrl` provides support to inject into a String or a URL type field, the address of the service.
  Beware, some applications (e.g. DB running on OpenShift) might not provide `@ServiceUrl` as there is no route to them
  created.
* **3:** Should you need control over platform-specific actions, you can also inject a provisioner via the `@ServiceProvisioner` annotation.
  This will enable you to, for example, scale your deployment up and down.

In the most common use cases, you just need to implement the correct `Application`
interface and pass the class via the`@Service` annotation.  It's the task for the framework to choose a suitable
Provisioner implementation (an exception will be thrown in case no suitable Provisioner was yet implemented).

The following snippet represents the `PostgresqlApplication` class that is being used by the previously 
detailed test class:

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


## Building Intersmash

* Simple build, will build the project's components including the `deployments` module.  That module has WildFly deployments that are used by the `testsuite` with default - i.e. _community_ artifacts:
```shell
mvn clean install -DskipTests
```

* By building with the `wildfly-deployments-build.eap` profile, the WildFly deployments will instead be built by using productised bits configuration:
```shell
mvn clean install -DskipTests -Pwildfly-deployments-build.eap
```

## Running the Intersmash test suite

* Run the testsuite against community deliverables, e.g.: Keycloak operator or WildFly images:
```shell
mvn test -pl testsuite/ -Pts.community
```
This is usually executed to test community images, deliverables and - for application servers like WildFly - deployments built from community artifacts.

* Run the testsuite against productised deliverables, e.g.: Red Hat Single Sign On operator or JBoss EAP images:
```shell
mvn test -pl testsuite/ -Pts.prod
```
This is usually executed to test productised images, deliverables and - for application servers like JBoss EAP - deployments built from community artifacts.

## Configuration
* Intersmash is using [XTF](https://github.com/xtf-cz/xtf) to communicate with OpenShift cluster for the OpenShift cloud
  environment.
  The [XTFConfig](https://github.com/xtf-cz/xtf/blob/master/core/src/main/java/cz/xtf/core/config/XTFConfig.java) class
  is also used internally, properties are resolved as following: System Properties > Environment Properties >
  test.properties file > global-test.properties, see https://github.com/xtf-cz/xtf#configuration for more details on this.

* The following properties can be used to configure Intersmash build and test execution, see the [CI checks e2e tests 
script](.ci/openshift-ci/build-root/e2e-test-prod.sh) as an example:

| Property                                         | Description                                                                                              |
|--------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| intersmash.skip.deploy                           | Skip the deployment phase, tests will be run against a prepared environment *                            |
| intersmash.skip.undeploy                         | Do not cleanup environment after test (development use)                                                  |
| intersmash.deployments.repository.ref            | Manually set git repository branch of deployments                                                        |
| intersmash.deployments.repository.url            | Manually set git repository url of deployments                                                           |
| intersmash.openshift.script.debug                | Add parameter SCRIPT_DEBUG=true to DeploymentConfig/Pod                                                  |
| intersmash.wildfly.image                         | Wildfly/JBoss EAP 8 Builder image URL                                                                    |
| intersmash.wildfly.runtime.image                 | Wildfly/JBoss EAP 8 Runtime image URL                                                                    |
| intersmash.wildfly.helm.charts.repo              | Wildfly/JBoss EAP 8 Helm Charts repository URL                                                           |
| intersmash.wildfly.helm.charts.branch            | Wildfly/JBoss EAP 8 Helm Charts repository branch                                                        |	
| intersmash.wildfly.helm.charts.name              | Wildfly/JBoss EAP 8 Helm Charts repository namespaces                                                    |
| intersmash.wildfly.operators.catalog_source      | Wildfly/JBoss EAP custom catalog for Operator                                                            |
| intersmash.wildfly.operators.index_image         | Wildfly/JBoss EAP custom index image for Operator                                                        |
| intersmash.wildfly.operators.package_manifest    | Wildfly/JBoss EAP custom package manifest for Operator                                                   |
| intersmash.wildfly.operators.channel             | Wildfly/JBoss EAP desired channel for Operator                                                           |
 | intersmash.bootable.jar.image                    | Open JDK image URL that can be used as the base for an OpenShift Wildfly/JBoss EAP Bootable JAR          |
| intersmash.eap7.image                            | JBoss EAP 7 Builder image URL                                                                            |
| intersmash.eap7.runtime.image                    | JBoss EAP 7 Runtime image URL                                                                            |
| intersmash.eap7.templates.base.url               | JBoss EAP 7 OpenShift Templates base URL                                                                 |
| intersmash.eap7.templates.path                   | JBoss EAP 7 openShift Templates base path                                                                |
| intersmash.infinispan.image                      | Infinispan/Red Hat DataGrid image URL                                                                    |
| intersmash.infinispan.operators.catalog_source   | Infinispan/Red Hat DataGrid custom catalog for Operator                                                  |
| intersmash.infinispan.operators.index_image      | Infinispan/Red Hat DataGrid custom index image for Operator                                              |
| intersmash.infinispan.operators.package_manifest | Infinispan/Red Hat DataGrid custom package manifest for Operator                                         |
| intersmash.infinispan.operators.channel          | Infinispan/Red Hat DataGrid desired channel for Operator                                                 |
| intersmash.keycloak.image                        | Keycloak image URL                                                                                       |
| intersmash.keycloak.operators.catalog_source     | Keycloak custom catalog for Operator                                                                     |
| intersmash.keycloak.operators.index_image        | Keycloak custom index image for Operator                                                                 |
| intersmash.keycloak.operators.package_manifest   | Keycloak custom package manifest for Operator                                                            |
| intersmash.keycloak.operators.channel            | Keycloak desired channel for Operator                                                                    |
| intersmash.rhsso.image                           | Red Hat Single Sign On 7 image URL                                                                       |
| intersmash.rhsso.operators.catalog_source        | Red Hat Single Sign On 7 custom catalog for Operator                                                     |
| intersmash.rhsso.operators.index_image           | Red Hat Single Sign On 7 custom index image for Operator                                                 |
| intersmash.rhsso.operators.package_manifest      | Red Hat Single Sign On 7 custom package manifest for Operator                                            |
| intersmash.rhsso.operators.channel               | Red Hat Single Sign On 7 desired channel for Operator                                                    |
| intersmash.kafka.operators.catalog_source        | Kafka/Red Hat AMQ Streams custom catalog for Operator                                                    |
| intersmash.kafka.operators.index_image           | Kafka/Red Hat AMQ Streams custom index image for Operator                                                |
| intersmash.kafka.operators.package_manifest      | Kafka/Red Hat AMQ Streams custom package manifest for Operator                                           |
| intersmash.kafka.operators.channel               | Kafka/Red Hat AMQ Streams desired channel for Operator                                                   |
| intersmash.activemq.image                        | Apache ActiveMQ Broker/Red Hat AMQ Broker image URL                                                      |
| intersmash.activemq.init.image                   | ActiveMQ Broker/Red Hat AMQ Broker init image URL                                                        |
| intersmash.activemq.operators.catalog_source     | ActiveMQ Broker/Red Hat AMQ Broker custom catalog for Operator                                           |
| intersmash.activemq.operators.index_image        | ActiveMQ Broker/Red Hat AMQ Broker custom index image for Operators                                      |
| intersmash.activemq.operators.package_manifest   | ActiveMQ Broker/Red Hat AMQ Broker custom package manifest for Operators                                 |
| intersmash.activemq.operators.channel            | ActiveMQ Broker/Red Hat AMQ Broker desired channel for Operator                                          |
| intersmash.hyperfoil.operators.catalog_source    | HyperFoil custom catalog for Operator                                                                    |
| intersmash.hyperfoil.operators.index_image       | HyperFoil custom index image for Operators                                                               |
| intersmash.hyperfoil.operators.package_manifest  | HyperFoil custom package manifest for Operators                                                          |
| intersmash.hyperfoil.operators.channel           | HyperFoil desired channel for Operator                                                                   |
| intersmash.mysql.image                           | MySql image URL                                                                                          |
| intersmash.postgresql.image                      | PostgreSql image URL                                                                                     |
| wildfly-maven-plugin.groupId                     | Used by shared configurable deployments: Wildfly/JBoss EAP 8 Maven plugin `groupId`                      |
| wildfly-maven-plugin.artifactId                  | Used by shared configurable deployments: Wildfly/JBoss EAP 8 Maven plugin `artifactId`                   |
| wildfly-maven-plugin.version                     | Used by shared configurable deployments: Wildfly/JBoss EAP 8 Maven plugin `version`                      |
| wildfly.ee-feature-pack.location                 | Used by shared configurable deployments: Wildfly/JBoss EAP 8 EE Galleon feature pack location (G:A:V)    |
| wildfly.feature-pack.location                    | Used by shared configurable deployments: Wildfly/JBoss EAP 8 Galleon feature pack location (G:A:V)       |
| wildfly.cloud-feature-pack.location              | Used by shared configurable deployments: Wildfly/JBoss EAP 8 Cloud Galleon feature pack location (G:A:V) |
| wildfly.ee-channel.groupId                       | Used by shared configurable deployments: JBoss EAP 8 Channel artifact `groupId`                          |
| wildfly.ee-channel.artifactId                    | Used by shared configurable deployments: JBoss EAP 8 Channel artifact `artifactId`                       |
| wildfly.ee-channel.version                       | Used by shared configurable deployments: JBoss EAP 8 Channel artifact `version`                          |
| bom.wildfly-ee.version                           | Used by shared configurable deployments: Wildfly/JBoss BOMs version                                      |

[*] - When `intersmash.skip.deploy` is set, please take into account that the prepared environment should be
configured accordingly to the `Application` descriptors which are defined by the test class `@Service` annotations.
E.g.: if a test class defines an `WildflyOperatorApplication` as one of its services, and such application sets the `name`
to be "wildfly-operator-app", then a Wildfly operator application with such a name should exist in the prepared environment.
This feature is useful to save debugging time during development, where you can deploy a complex scenario and then
enable the property to just execute tests in the following runs.

## Platforms

* Although Intersmash is designed to allow executions on different platforms, at the moment we fully focus on OpenShift
  support.

* Intersmash Framework tooling modules support Java 11 language features, and the compiled code is set to be compliant with
  such version as well.

## Architecture

The Intersmash repository hosts several modules which are meant to provide core APIs (`tools/intersmash-tools-core`),
tooling implementation (`tools/intersmash-tools-provisioners`), shared deployments
(`deployments`), a testsuite (`testsuite`) and examples (`demos`).

### Modules

* **Tools (tools)** - set of core components for platform configuration and application deployment.
  * **intersmash-tools-core** - core interfaces, annotations and extensions.
  * **intersmash-tools-provisioners** - implementations of interfaces from `core` for selected 
    services.
* **Testsuite** - tests that verify the integration with OpenShift, hence a cluster is needed.  
* **Deployments (deployments)** - sources for shared deployments which are used by the testsuite.
  Moreover, the `intersmash-deployments-provider` submodule provides support to get path to compiled deployments. Or
  you can get any deployment by GAV.
* **Demos** - examples that illustrate how to integrate Intersmash.

## Sources and Javadoc artifacts

The Intersmash tooling modules are configured to generate both sources and Javadoc JAR artifacts each time they're 
built.

The `doc` profiles must be activated in order to produce the sources and Javadoc artifacts, e.g.:

```shell
mvn clean install -DskipTests -Pdoc
```

a new directory, namely `apidocs`, is generated inside the `target` directory of both `intersmash-tools-core` and 
`intersmash-tools-provisioners`, and HTML files containing the Javadoc are added to it.

In the `target` directory two additional artifacts are generated, the _sources_ and _Javadoc_ 
artifacts. For instance, for `intersmash-tools-provisioner` the following artifacts are generated:

* intersmash-tools-provisioners-_&lt;version&gt;_.jar - Actual provisioners implementation
* intersmash-tools-provisioners-_&lt;version&gt;_-sources.jar - Unzip to get the sources or use in your IDE
* intersmash-tools-provisioners-_&lt;version&gt;_-javadoc.jar - unzip to get Javadoc

## Publishing Intersmash Library artifacts

The Intersmash Library project is configured to publish some artifacts to the public Maven repository, so that external 
projects can access them, e.g.: the `intersmash-tools-core.jar` artifact which contains the `@Intersmash` annotation 
definition.

Just run:

```shell
mvn clean deploy -DskipTests 
```

## Contributing

See [Intersmash contributing guidelines](CONTRIBUTING.md).

## Future plans

- Kubernetes support
- Quarkus provisioning

## Issue tracking

See [Intersmash contributing guidelines](CONTRIBUTING.md).
